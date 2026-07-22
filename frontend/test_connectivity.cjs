const axios = require('axios');

async function run() {
    console.log("Testing Service-Order Direct Connectivity...");
    
    // Test 1: GET /orders (Should exist)
    try {
        const res = await axios.get('http://localhost:8083/orders');
        console.log("GET /orders: Success", res.status);
    } catch (e) {
        console.log("GET /orders: Failed", e.message, e.response?.status);
    }
    
    // Test 2: PUT /orders/8/cancel (My new endpoint)
    try {
        const res = await axios.put('http://localhost:8083/orders/8/cancel');
        console.log("PUT /orders/8/cancel: Success", res.status, res.data);
    } catch (e) {
        console.log("PUT /orders/8/cancel: Failed", e.message, e.response?.status, e.response?.data);
    }
}
run();
