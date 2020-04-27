package com.example.stockmarketapplicationproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

/*
* The La
*/
public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        Button enterGame = findViewById(R.id.enterGame);
        enterGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText dollarAmountText = findViewById(R.id.enterValue);
                String test = dollarAmountText.getText().toString();
                if (test.matches("")) {
                    System.out.println("Invalid Entry");
                    return;
                }
                double dollarAmountValue = Double.parseDouble(dollarAmountText.getText().toString());
                DecimalFormat format = new DecimalFormat("##.##");
                dollarAmountValue = Double.parseDouble(format.format(dollarAmountValue));
                System.out.println(dollarAmountValue);
                Intent toSend = new Intent(getBaseContext(), MainActivity.class);
                toSend.putExtra("startingValue", dollarAmountValue);
                startActivity(toSend);
                finish();
            }
        });
    }
}
