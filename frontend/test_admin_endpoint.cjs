const axios = require('axios');
async function run() {
    console.log("Testing Admin Endpoint via Gateway...");
    try {
        // Use Order 11 (User mentioned 11 failed)
        const res = await axios.patch('http://localhost:8080/api/orders/11/status', { status: 'CANCELLED' });
        console.log("Admin Endpoint via Gateway: Success", res.status);
    } catch(e) {
        console.log("Admin Endpoint via Gateway: Failed", e.response?.status, e.response?.data);
    }
}
run();
