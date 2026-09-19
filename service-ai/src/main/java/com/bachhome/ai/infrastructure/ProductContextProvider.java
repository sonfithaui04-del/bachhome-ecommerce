package com.bachhome.ai.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Cung cấp dữ liệu sản phẩm cho chatbot.
 *
 * <p>Cửa hàng có vài trăm mặt hàng nên không thể nhồi hết vào mỗi lượt hỏi: vừa tốn,
 * vừa chậm, vừa làm loãng câu trả lời. Lớp này làm ba việc:</p>
 * <ol>
 *   <li>Giữ sẵn danh sách sản phẩm trong bộ nhớ, chỉ gọi lại service-product sau mỗi
 *       khoảng thời gian cấu hình được (mặc định 5 phút) thay vì gọi mỗi tin nhắn.</li>
 *   <li>Chọn ra những sản phẩm liên quan tới câu khách vừa hỏi (theo từ khoá và mức giá
 *       khách nêu) để đưa vào ngữ cảnh.</li>
 *   <li>Dựng một đoạn tóm tắt toàn cửa hàng (mỗi danh mục có bao nhiêu món, giá từ đâu
 *       tới đâu) để trợ lý vẫn biết cửa hàng bán những gì dù không nhìn thấy đủ danh sách.</li>
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductContextProvider {

    /** Từ quá phổ biến, có đếm cũng không phân biệt được sản phẩm nào với sản phẩm nào. */
    private static final Set<String> TU_BO_QUA = Set.of(
            "cho", "toi", "minh", "ban", "co", "khong", "la", "gi", "nao", "the", "voi", "va",
            "mua", "can", "muon", "tim", "gioi", "thieu", "tu", "van", "xin", "chao", "a", "ah",
            "duoc", "nhe", "nha", "hang", "san", "pham", "shop", "o", "day", "tren", "duoi",
            "khoang", "tam", "gia", "tien", "loai", "cai", "chiec", "bo", "hay", "nen", "nhat",
            "hon", "rat", "qua", "thi", "ma", "de", "dung", "nhu", "sao", "bao", "nhieu");

    private static final Pattern MUC_GIA = Pattern.compile(
            "(duoi|tren|khoang|tam|gan|khoang chung)?\\s*(\\d+([.,]\\d+)?)\\s*(trieu|tr|k|nghin|ngan|dong|d)\\b");

    private final RestTemplate loadBalancedRestTemplate;

    @Value("${ai.max-products:40}")
    private int maxProducts;

    @Value("${ai.cache-seconds:300}")
    private int cacheSeconds;

    /** Bản sao danh sách sản phẩm và thời điểm lấy về, dùng chung cho mọi lượt chat. */
    private volatile List<Map<String, Object>> boNho = Collections.emptyList();
    private volatile long layLuc = 0L;

    /* ===================== Lấy dữ liệu ===================== */

    /**
     * Toàn bộ sản phẩm đang bán. Gọi service-product tối đa một lần mỗi {@code ai.cache-seconds};
     * nếu gọi hỏng thì vẫn trả bản cũ còn trong bộ nhớ để chatbot không bị "mù" dữ liệu.
     */
    public List<Map<String, Object>> fetchProducts() {
        long bayGio = System.currentTimeMillis();
        if (!boNho.isEmpty() && bayGio - layLuc < cacheSeconds * 1000L) {
            return boNho;
        }
        try {
            ResponseEntity<List<Map<String, Object>>> resp = loadBalancedRestTemplate.exchange(
                    "http://SERVICE-PRODUCT/products?availableOnly=true",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            List<Map<String, Object>> items = resp.getBody();
            if (items != null && !items.isEmpty()) {
                boNho = items;
                layLuc = bayGio;
                log.debug("[AI] Đã nạp {} sản phẩm vào bộ nhớ đệm.", items.size());
            }
        } catch (Exception e) {
            log.warn("[AI] Không lấy được danh sách sản phẩm từ service-product: {}", e.getMessage());
        }
        return boNho;
    }

    /* ===================== Chọn sản phẩm liên quan ===================== */

    /**
     * Chọn những sản phẩm sát với câu hỏi nhất, tối đa {@code ai.max-products} món.
     * Khách chưa nói gì cụ thể (VD "xin chào") thì trả về một rổ hàng trải đều các danh mục.
     */
    public List<Map<String, Object>> selectRelevant(String cauHoi, List<Map<String, Object>> tatCa) {
        if (tatCa == null || tatCa.isEmpty()) {
            return Collections.emptyList();
        }
        if (tatCa.size() <= maxProducts) {
            return tatCa;
        }

        Set<String> tuKhoa = tachTuKhoa(cauHoi);
        long[] khoangGia = docKhoangGia(cauHoi);

        List<Map.Entry<Map<String, Object>, Integer>> chamDiem = new ArrayList<>();
        for (Map<String, Object> p : tatCa) {
            int diem = chamDiem(p, tuKhoa, khoangGia);
            if (diem > 0) {
                chamDiem.add(Map.entry(p, diem));
            }
        }
        if (chamDiem.isEmpty()) {
            return traiDeuTheoDanhMuc(tatCa);
        }
        chamDiem.sort(Comparator
                .comparingInt((Map.Entry<Map<String, Object>, Integer> e) -> e.getValue()).reversed()
                .thenComparing(e -> soThuc(e.getKey().get("averageRating")), Comparator.reverseOrder()));

        List<Map<String, Object>> ketQua = chamDiem.stream()
                .limit(maxProducts)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        log.debug("[AI] Câu hỏi khớp {} sản phẩm, đưa {} món vào ngữ cảnh.", chamDiem.size(), ketQua.size());
        return ketQua;
    }

    /** Điểm càng cao càng sát câu hỏi: khớp ở tên ăn 3 điểm, ở nơi khác 1 điểm, đúng tầm giá cộng 2. */
    private int chamDiem(Map<String, Object> p, Set<String> tuKhoa, long[] khoangGia) {
        String ten = boDau(chuoi(p.get("name")));
        String phanConLai = boDau(String.join(" ",
                chuoi(p.get("categoryName")), chuoi(p.get("brand")),
                chuoi(p.get("specification")), chuoi(p.get("material")), chuoi(p.get("description"))));

        int diem = 0;
        for (String t : tuKhoa) {
            if (ten.contains(t)) {
                diem += 3;
            } else if (phanConLai.contains(t)) {
                diem += 1;
            }
        }
        if (khoangGia != null) {
            BigDecimal gia = p.get("price") != null ? new BigDecimal(p.get("price").toString()) : null;
            if (gia != null) {
                long g = gia.longValue();
                if (g >= khoangGia[0] && g <= khoangGia[1]) {
                    diem += 2;
                } else if (diem > 0) {
                    diem -= 1; // đúng loại hàng nhưng lệch tầm giá thì xếp sau
                }
            }
        }
        return diem;
    }

    /** Khách chưa nêu nhu cầu cụ thể: lấy vài món tiêu biểu của từng danh mục. */
    private List<Map<String, Object>> traiDeuTheoDanhMuc(List<Map<String, Object>> tatCa) {
        Map<String, List<Map<String, Object>>> theoDanhMuc = new LinkedHashMap<>();
        for (Map<String, Object> p : tatCa) {
            theoDanhMuc.computeIfAbsent(chuoi(p.get("categoryName")), k -> new ArrayList<>()).add(p);
        }
        int moiDanhMuc = Math.max(1, maxProducts / Math.max(1, theoDanhMuc.size()));
        List<Map<String, Object>> ketQua = new ArrayList<>();
        for (List<Map<String, Object>> nhom : theoDanhMuc.values()) {
            nhom.stream()
                    .sorted(Comparator.comparing((Map<String, Object> p) -> soThuc(p.get("averageRating"))).reversed())
                    .limit(moiDanhMuc)
                    .forEach(ketQua::add);
        }
        return ketQua.size() > maxProducts ? ketQua.subList(0, maxProducts) : ketQua;
    }

    /* ===================== Dựng chuỗi cho prompt ===================== */

    /**
     * Tóm tắt toàn cửa hàng: mỗi danh mục có bao nhiêu món và giá trải từ đâu tới đâu.
     * Nhờ đoạn này trợ lý biết cửa hàng bán những gì kể cả khi danh sách chi tiết bị cắt bớt.
     */
    public String buildCatalogSummary(List<Map<String, Object>> tatCa) {
        if (tatCa == null || tatCa.isEmpty()) {
            return "";
        }
        Map<String, List<Map<String, Object>>> theoDanhMuc = new LinkedHashMap<>();
        for (Map<String, Object> p : tatCa) {
            theoDanhMuc.computeIfAbsent(chuoi(p.get("categoryName")), k -> new ArrayList<>()).add(p);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Cửa hàng đang bán ").append(tatCa.size()).append(" sản phẩm thuộc ")
                .append(theoDanhMuc.size()).append(" danh mục:\n");
        theoDanhMuc.forEach((ten, nhom) -> {
            long thap = nhom.stream().mapToLong(p -> soNguyen(p.get("price"))).min().orElse(0);
            long cao = nhom.stream().mapToLong(p -> soNguyen(p.get("price"))).max().orElse(0);
            sb.append("- ").append(ten).append(": ").append(nhom.size()).append(" sản phẩm, giá từ ")
                    .append(dinhDangTien(thap)).append(" đến ").append(dinhDangTien(cao)).append(".\n");
        });
        return sb.toString();
    }

    /** Danh sách chi tiết các sản phẩm được chọn, mỗi dòng một món. */
    public String buildContext(List<Map<String, Object>> items) {
        if (items == null || items.isEmpty()) {
            return "";
        }
        return items.stream()
                .map(this::formatItem)
                .collect(Collectors.joining("\n"));
    }

    private String formatItem(Map<String, Object> m) {
        Object name = m.get("name");
        Object price = m.get("price");
        Object category = m.get("categoryName");
        Object spec = m.get("specification");
        Object desc = m.get("description");
        StringBuilder sb = new StringBuilder("- ").append(name);
        if (category != null) {
            sb.append(" (danh mục: ").append(category);
            if (price != null) {
                sb.append(", giá: ").append(dinhDangTien(soNguyen(price)));
            }
            sb.append(")");
        } else if (price != null) {
            sb.append(" (giá: ").append(dinhDangTien(soNguyen(price))).append(")");
        }
        Object chiTiet = spec != null && !spec.toString().isBlank() ? spec : desc;
        if (chiTiet != null && !chiTiet.toString().isBlank()) {
            sb.append(": ").append(chiTiet);
        }
        return sb.toString();
    }

    /* ===================== Tiện ích ===================== */

    /** Tách câu hỏi thành các từ khoá đáng kể (bỏ dấu, bỏ từ quá phổ biến). */
    private Set<String> tachTuKhoa(String cauHoi) {
        if (cauHoi == null || cauHoi.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(boDau(cauHoi).split("\\s+"))
                .filter(t -> t.length() >= 2 && !TU_BO_QUA.contains(t))
                .collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Đọc mức giá khách nêu trong câu hỏi, trả về [thấp, cao] tính bằng đồng.
     * VD "dưới 2 triệu" -> [0, 2.000.000]; "khoảng 500k" -> [400.000, 600.000].
     */
    private long[] docKhoangGia(String cauHoi) {
        if (cauHoi == null || cauHoi.isBlank()) {
            return null;
        }
        String s = boDau(cauHoi);
        Matcher m = MUC_GIA.matcher(s);
        if (!m.find()) {
            return null;
        }
        String huong = m.group(1) == null ? "" : m.group(1);
        double so = Double.parseDouble(m.group(2).replace(",", "."));
        String donVi = m.group(4);
        long tien = switch (donVi) {
            case "trieu", "tr" -> Math.round(so * 1_000_000);
            case "k", "nghin", "ngan" -> Math.round(so * 1_000);
            default -> Math.round(so);
        };
        if (huong.startsWith("duoi")) {
            return new long[]{0, tien};
        }
        if (huong.startsWith("tren")) {
            return new long[]{tien, Long.MAX_VALUE};
        }
        return new long[]{Math.round(tien * 0.8), Math.round(tien * 1.2)};
    }

    /** Bỏ dấu tiếng Việt và ký tự lạ để so khớp không phụ thuộc cách gõ dấu. */
    private String boDau(String s) {
        if (s == null) {
            return "";
        }
        String tam = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase();
        return tam.replaceAll("[^a-z0-9]+", " ").replaceAll("\\s+", " ").trim();
    }

    private String chuoi(Object o) {
        return o == null ? "" : o.toString();
    }

    private double soThuc(Object o) {
        try {
            return o == null ? 0d : Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return 0d;
        }
    }

    private long soNguyen(Object o) {
        try {
            return o == null ? 0L : new BigDecimal(o.toString()).longValue();
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private String dinhDangTien(long d) {
        return String.format("%,d", d).replace(',', '.') + "đ";
    }
}
