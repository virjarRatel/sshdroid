package com.virjar.sshdroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = findViewById(R.id.id_newProcess);
        textView.setText(Configs.newProcess ? " 是" : " 否");


        TextView textViewTargetApp = findViewById(R.id.id_targetApp);
        textViewTargetApp.setText(Configs.targetPackage);


        TextView textViewTargetPort = findViewById(R.id.id_targetPort);
        textViewTargetPort.setText(String.valueOf(Configs.ssdServerPort));

    }
}
