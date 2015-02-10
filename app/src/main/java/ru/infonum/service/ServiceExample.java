package ru.infonum.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class ServiceExample extends Service {

    public static final int INTERVAL = 60000; // 60 sec (n * 1/1000 sec)
    public static final int FIRST_RUN = 5000; // 5 seconds
    //int REQUEST_CODE = 11223344;
    int REQUEST_CODE = 4278684;


    public static final String TAG = "inf";
    AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();

        startService();
        Log.v(this.getClass().getName() + " " + TAG, "026--onCreate(..)");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(this.getClass().getName() + " " + TAG, "027--onBind(..)");
        return null;
    }

    @Override
    public void onDestroy() {
        if (alarmManager != null) {
            Intent intent = new Intent(this, RepeatingAlarmService.class);
            alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0));
        }
        Toast.makeText(this, "Инфонум: проверка новых на сайте остановлена!", Toast.LENGTH_LONG).show();
        Log.v(this.getClass().getName() + " " + TAG, "025--onDestroy(). Сервис остановлен в " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
    }

    private void startService() {

        int n = INTERVAL; //TODO сохранение интервала не работает

        Intent intent = new Intent(this, RepeatingAlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE, intent, 0);

        SharedPreferences sp = this.getSharedPreferences(this.getString(R.string.F_INTRV), Context.MODE_PRIVATE);
        String s = sp.getString(this.getString(R.string.F_INTRV), "");
        if (s.length() != 0) {
            n = Integer.valueOf(s); // без проверок
            Log.v(TAG, "020--Прочитан интервал: " + s);
        } else {
            SharedPreferences.Editor ed = sp.edit();
            ed.putString(this.getString(R.string.F_INTRV), this.getString(R.string.INIT_INTERVAL)).apply();
            Log.v(TAG, "021--Не удалось прочитать сохраненный интервал. Установлен заново.");
            n = INTERVAL;

        }

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + FIRST_RUN,
                n,
                pendingIntent);

        Toast.makeText(this, "Инфонум: периодическая проверка новых на сайте стартовала! Интервал =" + s, Toast.LENGTH_LONG).show();
        Log.v(this.getClass().getName(), "022--AlarmManger started at " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
    }
}
