const axios = require('axios');
async function run() {
    console.log("Testing Gateway Loop...");
    for(let i=0; i<5; i++) {
        try {
            // Use non-existent ID 9999 to avoid changing state, just check endpoint existence (404 vs 500 or 200 with error)
            // But 404 is the error we are debugging. 
            // If endpoint exists, it might return 500 (Order Not Found)?
            // OrderController: orElseThrow(() -> RuntimeException("Không tìm thấy đơn hàng"))
            // If GlobalExceptionHandler absent, 500.
            // So if we get 500 -> Endpoint REACHED.
            // If we get 404 -> Endpoint NOT REACHED.
            await axios.put('http://localhost:8080/api/orders/9999/cancel');
            console.log(`Req ${i}: 200 OK (Unexpected for ID 9999)`);
        } catch(e) {
            console.log(`Req ${i}: ${e.response?.status}`);
        }
    }
}
run();
