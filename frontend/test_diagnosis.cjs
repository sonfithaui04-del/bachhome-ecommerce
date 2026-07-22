const axios = require('axios');

async function run() {
    console.log("--- TEST DIRECT SERVICE:8083 ---");
    try {
        const res = await axios.put('http://localhost:8083/orders/9/cancel');
        console.log("Direct PUT /orders/9/cancel: Success", res.status);
    } catch (e) {
        console.log("Direct PUT /orders/9/cancel: Failed", e.message, e.response?.status);
    }

    console.log("\n--- TEST GATEWAY ROUTES ---");
    try {
        const res = await axios.get('http://localhost:8080/actuator/gateway/routes');
        // Filter for service-order (route_id might be distinct from id in yaml?)
        // Actuator returns list of routes.
        const orderRoute = res.data.find(r => r.route_id === 'service-order');
        console.log(JSON.stringify(orderRoute || res.data, null, 2));
    } catch(e) { console.log("Failed to get routes", e.message); }
}
run();
