package ru.infonum.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ServiceActivity extends Activity implements View.OnClickListener {

    Button buttonStart, buttonStop, button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        button = (Button) findViewById(R.id.button);

        buttonStart.setOnClickListener(this);
        buttonStop.setOnClickListener(this);
        button.setOnClickListener(this);


    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonStart:
                Log.v(this.getClass().getName(), "onClick: Сервис стартовал.");
                startService(new Intent(this, ServiceExample.class));
                break;
            case R.id.buttonStop:
                Log.v(this.getClass().getName(), "onClick: Сервис остановлен.");
                stopService(new Intent(this, ServiceExample.class));
                break;
            case R.id.button:

                SharedPreferences sp = this.getSharedPreferences(this.getString(R.string.F_INTRV), Context.MODE_PRIVATE);
                EditText editText = (EditText) findViewById(R.id.editText);
                String s2 = editText.getText().toString();
                SharedPreferences.Editor e = sp.edit();
                e.putString(this.getString(R.string.F_INTRV), s2).apply();

                stopService(new Intent(this, ServiceExample.class));
                Log.v(this.getClass().getName(), "onClick: Сервис остановлен.");
                startService(new Intent(this, ServiceExample.class));
                Log.v(this.getClass().getName(), "onClick: Сервис стартовал.");

                Log.v(this.getClass().getName(), "024--onClick: Новый интервал= " + s2);

                break;
        }
    }
}
