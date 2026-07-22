const axios = require('axios');
async function run() {
    console.log("Testing POST /cancel-order...");
    try {
        await axios.post('http://localhost:8080/api/orders/9999/cancel-order');
        console.log("Success (Unexpected 200)");
    } catch(e) {
        // 500 means Service Reached (Order not found exception).
        // 404 means Routing Failed.
        console.log("Result:", e.response?.status);
    }
}
run();
