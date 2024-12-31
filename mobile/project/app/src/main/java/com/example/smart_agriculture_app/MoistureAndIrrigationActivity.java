package com.example.smart_agriculture_app;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MoistureAndIrrigationActivity extends AppCompatActivity {
    private MqttHelper mqttHelper;
    private EditText setMoistureLevel;
    private Spinner irrigationSpinner;
    private Button setIrrigationButton;
    private Button backButton;
    private Button setMoistureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_moisture_and_irrigation);

        mqttHelper = new MqttHelper();
        mqttHelper.connectToBroker(new MqttHelper.MqttConnectionListener() {
            @Override
            public void onConnected() {
                Toast.makeText(MoistureAndIrrigationActivity.this, "Connected to MQTT broker!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailed(Throwable throwable) {
                Toast.makeText(MoistureAndIrrigationActivity.this, "Failed to connect to MQTT broker", Toast.LENGTH_SHORT).show();
            }
        });

        irrigationSpinner = findViewById(R.id.irrigationSpinner);
        setMoistureLevel = findViewById(R.id.setMoistureLevel);
        setIrrigationButton = findViewById(R.id.setIrrigationButton);
        backButton = findViewById(R.id.backButton);
        setMoistureButton = findViewById(R.id.setMoistureButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.irrigation_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        irrigationSpinner.setAdapter(adapter);

        backButton.setOnClickListener(v -> finish());

        setIrrigationButton.setOnClickListener(v -> {
            try {
                mqttHelper.publishMessage("smart_irrigation/threshold", setMoistureLevel.getText().toString());
                Toast.makeText(getApplicationContext(), "Threshold set to: " + setMoistureLevel.getText().toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to publish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        setMoistureButton.setOnClickListener(v -> {
            try {
                mqttHelper.publishMessage("smart_irrigation/soil_moisture", setMoistureLevel.getText().toString());
                Toast.makeText(getApplicationContext(), "Soil moisture set to: " + setMoistureLevel.getText().toString(), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to publish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        irrigationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedValue = parent.getItemAtPosition(position).toString();
                try {
                    mqttHelper.publishMessage("smart_irrigation/irrigation_mode", selectedValue);
                    Toast.makeText(getApplicationContext(), "Selected: " + selectedValue, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Failed to publish: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getApplicationContext(), "No irrigation mode selected", Toast.LENGTH_SHORT).show();
            }
        });
    }
}