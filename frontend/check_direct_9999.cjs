const axios = require('axios');
async function run() {
    try {
        await axios.put('http://localhost:8083/orders/9999/cancel');
        console.log("Direct: 200");
    } catch(e) {
        console.log("Direct:", e.response?.status);
    }
}
run();
