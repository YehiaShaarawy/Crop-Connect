package com.example.smart_agriculture_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class MyPlantActivity extends AppCompatActivity {

    private TextView lightDisplay;
    private TextView pumpDisplay;
    private TextView soilDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_plant);

        lightDisplay = findViewById(R.id.lightDisplay);
        pumpDisplay = findViewById(R.id.pumpDisplay);
        soilDisplay = findViewById(R.id.soilDisplay);

        Intent intent = getIntent();

        lightDisplay.setText(intent.getStringExtra("lightIntensityValue"));
        pumpDisplay.setText(intent.getStringExtra("pumpStateValue"));
        soilDisplay.setText(intent.getStringExtra("MoistureValue"));
    }
}