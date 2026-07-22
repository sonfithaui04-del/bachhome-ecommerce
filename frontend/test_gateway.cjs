const axios = require('axios');

async function run() {
    console.log("Testing Gateway Connectivity to Order Service...");
    
    // Test GET /api/orders (mapped to /orders)
    try {
        const res = await axios.get('http://localhost:8080/api/orders');
        console.log("GET /api/orders: Success", res.status);
    } catch (e) {
        console.log("GET /api/orders: Failed", e.message, e.response?.status);
    }

    // Test PUT /api/orders/9/cancel
    try {
        const res = await axios.put('http://localhost:8080/api/orders/9/cancel');
        console.log("PUT /api/orders/9/cancel: Success", res.status, res.data);
    } catch (e) {
        console.log("PUT /api/orders/9/cancel: Failed", e.message, e.response?.status);
    }
}
run();
