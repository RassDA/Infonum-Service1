package ru.infonum.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class OnBootReceiver extends BroadcastReceiver {
    String sIntrv = "";
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent serviceLauncher = new Intent(context, ServiceExample.class);
            context.startService(serviceLauncher);

            Log.v(this.getClass().getName(), "Service loaded while device boot.");

            SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.F_INTRV), Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            if (sp.contains(context.getString(R.string.F_INTRV))) {
                sIntrv = sp.getString(context.getString(R.string.F_INTRV), "");
                if (sIntrv.length() == 0) {
                    ed.putString(context.getString(R.string.F_INTRV), context.getString(R.string.INIT_INTERVAL)).apply(); // подтверждаем изменения
                }
            }
            ed.putString(context.getString(R.string.INST), "").apply(); // TODO тестовый сброс настроек
            Toast.makeText(context, "Инфонум: Старт сервиса при загрузке! INTRVL= " + sp.getString(context.getString(R.string.F_INTRV), ""), Toast.LENGTH_LONG).show();

        }
    }
}
