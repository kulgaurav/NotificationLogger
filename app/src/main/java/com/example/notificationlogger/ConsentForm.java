package com.example.notificationlogger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class ConsentForm extends AppCompatActivity {

    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent_form);

        checkBox = findViewById(R.id.consent_form_CB);

        Button consentBtn = findViewById(R.id.btn_consent_form);
        consentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionOnConsent();
            }
        });
    }

    private void actionOnConsent() {
        boolean checked = checkBox.isChecked();
        if(!checked){
            Toast.makeText(getApplicationContext(), "Please check the box above.", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(ConsentForm.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
