const axios = require('axios');

async function run() {
    console.log("Testing Connectivity...");

    // Test 1: Direct to Service (8083)
    try {
        await axios.post('http://localhost:8083/orders/9999/cancel-order');
        console.log("Direct (8083): 200 (Success - Unexpected)");
    } catch(e) {
        // Expect 500 (Order Not Found)
        console.log("Direct (8083):", e.response ? e.response.status : e.message);
    }

    // Test 2: Via Gateway (8080)
    try {
        await axios.post('http://localhost:8080/api/orders/9999/cancel-order');
        console.log("Gateway (8080): 200 (Success - Unexpected)");
    } catch(e) {
        console.log("Gateway (8080):", e.response ? e.response.status : e.message);
    }
}
run();
