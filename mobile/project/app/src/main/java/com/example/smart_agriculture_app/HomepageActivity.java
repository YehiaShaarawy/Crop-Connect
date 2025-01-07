package com.example.smart_agriculture_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomepageActivity extends AppCompatActivity {
    private TextView lightIntensityDisplay;
    private TextView pumpStateDisplay;
    private TextView MoistureDisplay;
    private Button plantButton;
    private Button configureButton;

    private static final String TAG = "HomepageActivity";
    private MqttHelper mqttHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);

        plantButton = findViewById(R.id.plantButton);
        configureButton = findViewById(R.id.configureButton);
        lightIntensityDisplay = findViewById(R.id.lightIntensityDisplay);
        pumpStateDisplay = findViewById(R.id.pumpStateDisplay);
        MoistureDisplay = findViewById(R.id.MoistureDisplay);

        mqttHelper = new MqttHelper();
        mqttHelper.connectToBroker(new MqttHelper.MqttConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "Connected to MQTT broker!");
                mqttHelper.subscribeToTopic("smart_irrigation/soil_moisture", (topic, message) -> handleIncomingMessage(topic, message));
                mqttHelper.subscribeToTopic("smart_irrigation/light_intensity", (topic, message) -> handleIncomingMessage(topic, message));
                mqttHelper.subscribeToTopic("smart_irrigation/pump_state", (topic, message) -> handleIncomingMessage(topic, message));
            }
            @Override
            public void onConnectionFailed(Throwable throwable) {
                Log.e(TAG, "Failed to connect to MQTT broker", throwable);
            }
        });

        plantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, MyPlantActivity.class);
                intent.putExtra("lightIntensityValue",lightIntensityDisplay.getText().toString());
                intent.putExtra("pumpStateValue",pumpStateDisplay.getText().toString());
                intent.putExtra("MoistureValue",MoistureDisplay.getText().toString());
                startActivity(intent);
            }
        });

        configureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, MoistureAndIrrigationActivity.class);
                startActivity(intent);
            }
        });
    }
    // Handle incoming messages
    private void handleIncomingMessage(String topic, String message) {
        switch (topic) {
            case "smart_irrigation/soil_moisture":
                Log.d(TAG, "Soil moisture received: " + message);
                int soilMoisture = Integer.parseInt(message);
                runOnUiThread(() -> {
                    MoistureDisplay.setText(String.valueOf(soilMoisture));
                });
                if (soilMoisture < 20) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Soil moisture is low. Consider irrigating.", Toast.LENGTH_SHORT).show();
                    });
                    Log.d(TAG, "Soil moisture is low. Consider irrigating.");
                }
                break;

            case "smart_irrigation/light_intensity":
                Log.d(TAG, "Light intensity received: " + message);
                int lightIntensity = Integer.parseInt(message);
                runOnUiThread(() -> {
                    lightIntensityDisplay.setText(String.valueOf(lightIntensity));
                });
                if (lightIntensity > 1000) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Light intensity is high. Adjust conditions if needed.", Toast.LENGTH_SHORT).show();
                    });
                    Log.d(TAG, "Light intensity is high. Adjust conditions if needed.");
                }
                break;

            case "smart_irrigation/pump_state":
                Log.d(TAG, "Pump state received: " + message);
                if ("ON".equalsIgnoreCase(message)) {
                    runOnUiThread(() -> {
                        pumpStateDisplay.setText("ON");
                    });
                    Log.d(TAG, "Pump is currently ON.");
                } else if ("OFF".equalsIgnoreCase(message)) {
                    runOnUiThread(() -> {
                        pumpStateDisplay.setText("OFF");
                    });
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
        mqttHelper.disconnectFromBroker();
    }
}