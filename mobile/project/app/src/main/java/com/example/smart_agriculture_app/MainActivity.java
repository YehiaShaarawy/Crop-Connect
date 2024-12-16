package com.example.smart_agriculture_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MqttHelper mqttHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        },1500);

        // Initialize the MQTT Helper
        mqttHelper = new MqttHelper();
        // Connect to the MQTT broker
        mqttHelper.connectToBroker(new MqttHelper.MqttConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Connected to MQTT broker!");

                // Subscribe to soil moisture, light intensity, and pump state topics
                mqttHelper.subscribeToTopic("smart_irrigation/soil_moisture", (topic, message) -> handleIncomingMessage(topic, message));
                mqttHelper.subscribeToTopic("smart_irrigation/light_intensity", (topic, message) -> handleIncomingMessage(topic, message));
                mqttHelper.subscribeToTopic("smart_irrigation/pump_state", (topic, message) -> handleIncomingMessage(topic, message));

                // Publish initial settings to threshold and irrigation mode topics
//                mqttHelper.publishMessage("smart_irrigation/threshold", "30");
//                mqttHelper.publishMessage("smart_irrigation/irrigation_mode", "always");
//                mqttHelper.publishMessage("smart_irrigation/soil_moisture", "40");
            }
            @Override
            public void onConnectionFailed(Throwable throwable) {
                Log.e(TAG, "Failed to connect to MQTT broker", throwable);
            }
        });
    }
    // Handle incoming messages
    private void handleIncomingMessage(String topic, String message) {
        switch (topic) {
            case "smart_irrigation/soil_moisture":
                Log.d(TAG, "Soil moisture received: " + message);
                int soilMoisture = Integer.parseInt(message);
                if (soilMoisture < 20) {
                    Log.d(TAG, "Soil moisture is low. Consider irrigating.");
                }
                break;

            case "smart_irrigation/light_intensity":
                Log.d(TAG, "Light intensity received: " + message);
                int lightIntensity = Integer.parseInt(message);
                if (lightIntensity > 1000) {
                    Log.d(TAG, "Light intensity is high. Adjust conditions if needed.");
                }
                break;

            case "smart_irrigation/pump_state":
                Log.d(TAG, "Pump state received: " + message);
                if ("ON".equalsIgnoreCase(message)) {
                    Log.d(TAG, "Pump is currently ON.");
                } else if ("OFF".equalsIgnoreCase(message)) {
                    Log.d(TAG, "Pump is currently OFF.");
                }
                break;

            default:
                Log.d(TAG, "Unknown topic received: " + topic);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect from the MQTT broker
        mqttHelper.disconnectFromBroker();
    }
}