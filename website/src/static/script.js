let slider;
// HTML Element for Pump Status
const pumpStatusContainer = document.querySelector('.pump-status');
window.onload = function () {
    slider = document.querySelector('.slider input');
    const progressBar = document.querySelector('.slider progress');
    const indicator = document.querySelector('.indicator');

    // Set initial progress and indicator position
    progressBar.value = slider.value;
    indicator.style = `left: calc(${slider.value}% - 17px)`;
    indicator.innerHTML = `<div class='value'>${slider.value}</div>`;

    // Update progress and indicator on slider change
    slider.oninput = function () {
        progressBar.value = slider.value;
        indicator.style = `left: calc(${slider.value}% - 17px)`;
        indicator.innerHTML = `<div class='value'>${slider.value}</div>`;
    };
};


// MQTT Configuration
const brokerURL = "ws://broker.emqx.io:8083/mqtt"; // Public MQTT broker
const client = mqtt.connect(brokerURL);

// MQTT Topics
const topics = {
    soilMoisture: "smart_irrigation/soil_moisture",
    lightIntensity: "smart_irrigation/light_intensity", // Optional, if your ESP32 publishes light data
    threshold: "smart_irrigation/threshold",
    pumpState: "smart_irrigation/pump_state",
    irrigationMode: "smart_irrigation/irrigation_mode",
};

// HTML Elements
const soilMoistureDisplay = document.getElementById("soil-moisture");
const lightIntensityDisplay = document.getElementById("light-intensity");
const pumpStateDisplay = document.getElementById("pump-state");
// const moistureThresholdInput = document.getElementById("moisture-threshold");
const setThresholdButton = document.getElementById("set-threshold");
const irrigationModeSelect = document.getElementById("irrigation-mode");
const setIrrigationModeButton = document.getElementById("set-irrigation-mode");

// Connect to MQTT Broker
client.on("connect", () => {
    console.log("Connected to MQTT broker");

    // Subscribe to topics
    client.subscribe([topics.soilMoisture, topics.pumpState, topics.lightIntensity], (err) => {
        if (err) console.error("Subscription error:", err);
    });
});

// Handle incoming messages
client.on("message", (topic, message) => {
    const data = message.toString();
    if (topic === topics.soilMoisture) {
        soilMoistureDisplay.textContent = `${data}`;
    } else if (topic === topics.lightIntensity) {
        lightIntensityDisplay.textContent = `${data}`;
    }else if (topic === topics.pumpState) {
        pumpStateDisplay.textContent = data;

        // Change background color based on pump state
        if (data === "ON") {
            pumpStatusContainer.style.backgroundColor = "rgba(154, 251, 143, 0.6)"; // Greenish for "ON"
        } else if (data === "OFF") {
            pumpStatusContainer.style.backgroundColor = "rgba(251, 143, 143, 0.6)"; // Reddish for "OFF"
        } else {
            pumpStatusContainer.style.backgroundColor = "rgba(94, 103, 104, 0.3)"; // Neutral for unknown state
        }
    } else {
        console.warn(`Unhandled topic ${topic}: ${data}`);
    }
});

// Set moisture threshold
setThresholdButton.addEventListener("click", () => {
    const threshold = slider.value;
    if (threshold >= 0 && threshold <= 100) {
        client.publish(topics.threshold, threshold, (err) => {
            if (!err) {
                alert(`Threshold set to ${threshold}% successfully!`);
            } else {
                alert("Failed to send threshold. Please try again.");
                console.error("Publish error:", err);
            }
        });
    } else {
        alert("Please enter a valid threshold (0-100)");
    }
});

// Set irrigation mode
setIrrigationModeButton.addEventListener("click", () => {
    const mode = irrigationModeSelect.value;
    client.publish(topics.irrigationMode, mode, (err) => {
        if (!err) {
            alert(`Irrigation mode set to: ${mode}`);
        } else {
            alert("Failed to send irrigation mode. Please try again.");
            console.error("Publish error:", err);
        }
    });
});

// Handle connection issues
client.on("close", () => {
    console.error("Disconnected from MQTT broker");
});
