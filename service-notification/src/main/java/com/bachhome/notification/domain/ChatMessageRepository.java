package com.bachhome.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByOrderIdOrderByTimestampAsc(Long orderId);

    /** Toàn bộ tin nhắn, mới nhất trước — dùng để dựng danh sách hội thoại cho admin. */
    List<ChatMessage> findAllByOrderByTimestampDesc();
}
