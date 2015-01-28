package ru.infonum.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RepeatingAlarmService extends BroadcastReceiver {


    final public static int NOTIFICATION_ID = 1;
    public static int nId = NOTIFICATION_ID;

    final public String TESTTEL = "+79119148047";
    final public String TESTNUM = "a000aa00";

    public String num = TESTNUM;
    public String inText = "Эвакуатор!";

    final String NTF_TITLE = "Инфонум: ";
    final String NTF_TEXT = ": Подойдите к авто!";
    final String NTF_SUBTEXT = "Нажмите сюда, чтобы перейти на страницу номера или смахните, чтобы удалить ";

    final public String INFONUM_SITE = "http://infonum.ru/";
    final public String NOTIF_SITE = "notification/";

    final public String INST_RQ = "Installation";

    public String s = INFONUM_SITE;

    private static final String AUTO = "auto_number";
    private static final String NAME = "name";
    private static final String DATE = "date";
    private static final String TEXT = "text";
    private static final String PATH = "path";

    private static final String DEVID = "DeviceId";
    private static final String TELNUM = "telNum";
    private static final String DATA = "data";

    public static String deviceId = "";
    public static String responseStr = "";
    public static int messQ = 0;

    public String ntfContTitle = "";
    public String ntfContText = "";
    public String ntfSubText = "";
    public String ntfUri = INFONUM_SITE;

    public Context ctx;

    public boolean ntfTop = false;
    public boolean ntfCancel = true;
    public static boolean appActivated = false;

    public static List<String[]> notiArr = new ArrayList<String[]>();
    public static String[] noti;
    public static String appNum = null;


    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;
        Toast.makeText(ctx, "Инфонум: получение новых", Toast.LENGTH_LONG).show();

        new Installation();

        deviceId = appNum;
        // deviceId = getDeviceId(ctx);      // раз не первый запуск приложения, выясняем данные устройства.
        if (appActivated == false) {
            Installation.id(ctx, appNum); // проверяем наличие файла с кодом приложения
            if (appNum == null)
                deviceId = INST_RQ; //если файла нет, то запрашиваем код приложения, передав вместо deviceId строку запроса.
            else
                appActivated = true;
        }
        // запускаем цикл post-запросов к сайту по таймеру.
        // проверяем наличие новых сообщений, разбирая ответ
        // если есть новые - генерируем уведомление.

        //
        responseStr = ""; // в нее придет ответ
        new RequestTask().execute(INFONUM_SITE + NOTIF_SITE); // post-запрос по этому адресу

        Log.v(this.getClass().getName(), "Timed alarm onReceive() started at time: " + new java.sql.Timestamp(System.currentTimeMillis()).toString());

        if (responseStr.length() > 0) {
            jsonToArray(responseStr);
            if (appActivated) {
                for (int i = 0; i < messQ; i++) {
                    setNotiParam(i);
                    sendNotification();
                }
            } else {
                Installation.id(ctx, noti[3]);

            }
        }
    }

    class RequestTask extends AsyncTask<String, String, String> {


        @Override
        protected String doInBackground(String... params) {
            Log.d("inf", "devId= " + deviceId);

            try {
                //создаем запрос на сервер
                DefaultHttpClient hc = new DefaultHttpClient();
                ResponseHandler<String> res = new BasicResponseHandler();
                //он у нас будет посылать post запрос
                HttpPost postMethod = new HttpPost(params[0]);

                //будем передавать два параметра, чтобы не переделывать
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                //передаем две пары имя:значение
                nameValuePairs.add(new BasicNameValuePair(DEVID, deviceId)); //формируется снаружи
                nameValuePairs.add(new BasicNameValuePair(TELNUM, TESTNUM));
                //собираем их вместе и посылаем на сервер
                postMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                //получаем ответ от сервера
                String response = hc.execute(postMethod, res);
                responseStr = response;  // передаем на глоб. уровень

                // if(response.length() > 0) jsonUrl(response);

            } catch (Exception e) {
                System.out.println("Exp=" + e);
            }
            return null;
        }

    }


    /*
    // Разбор ответа сайта и запуск уведомлений
     */
    public void jsonUrl(String result) {

        try {
            //создали читателя json объектов и отдали ему строку - result
            JSONObject json = new JSONObject(result);
            //дальше находим вход в наш json им является ключевое слово "data"
            JSONArray urls = json.getJSONArray(DATA);
            //проходим циклом по всем нашим параметрам
            messQ = urls.length();
            Log.d("inf", "urls.length = " + messQ);

            for (int i = 0; i < messQ; i++) {

                // передаем номер для заголовка извещения через глоб.перем. -- исправить

                num = urls.getJSONObject(i).getString(AUTO);
                inText = urls.getJSONObject(i).getString(TEXT);

                if (appActivated == false) {
                    // это не запрос на код при первой установке приложения
                    // формируем извещения как обычно
                    Log.d("inf", "i = " + i + "    num= " + num);

                    nId = i + 2;

                    ntfContTitle = NTF_TITLE + num;
                    ntfContText = num + ": " + inText + "\n";
                    ntfSubText = ctx.getString(R.string.INFONUM_SITE) + num + NTF_SUBTEXT;
                    ntfUri = INFONUM_SITE + num;
                    ntfTop = false;
                    ntfCancel = true;

                    // Запуск извещения
                    sendNotification();
                } else {
                    // Приложение только что установлено или потеряло свой код.
                    // Других извещений в этом случае обнаружено не будет, так как нет подписок на номера страниц.
                    // Нужно -- правильно обработать первое сообщение.
                    // Можем выдать тестовое оповещение или с вызовом экрана приложения.
                    setAppNum(inText);
                }

            }

        } catch (JSONException e) {
            Log.e("log_tag", "\nError parsing data " + e.toString());
            Toast.makeText(ctx, "Ошибочка разбора json" + s + e.toString(), Toast.LENGTH_LONG).show();
        }

        // отдельно готовим и посылаем извещение, открывающее окно приложения
        nId = 1;
        ntfContTitle = "Infonum Settings";
        ntfContText = "Всего новых: " + messQ;
        ntfSubText = "Tap to change";
        ntfTop = true;
        ntfCancel = false;

        sendNotification();

    }


    public void jsonToArray(String result) {

        try {

            //создали читателя json объектов и отдали ему строку - result
            JSONObject json = new JSONObject(result);
            //дальше находим вход в наш json им является ключевое слово "data"
            JSONArray urls = json.getJSONArray(DATA);
            //проходим циклом по всем нашим параметрам
            messQ = urls.length();
            Log.d("inf", "urls.length = " + messQ);

            for (int i = 0; i < messQ; i++) {

                // передаем номер для заголовка извещения через глоб.перем. -- исправить
                noti[0] = urls.getJSONObject(i).getString(AUTO);
                noti[1] = urls.getJSONObject(i).getString(NAME);
                noti[2] = urls.getJSONObject(i).getString(DATE);
                noti[3] = urls.getJSONObject(i).getString(TEXT);
                noti[4] = urls.getJSONObject(i).getString(PATH);
                notiArr.add(noti);

                num = urls.getJSONObject(i).getString(AUTO);
                //num = noti[0];
                inText = urls.getJSONObject(i).getString(TEXT);
                //inText = noti[3];
            }

        } catch (JSONException e) {
            Log.e("log_tag", "\nError parsing data " + e.toString());
            Toast.makeText(ctx, "Ошибочка разбора json" + s + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    //
    // Отправка тестового извещения через NotificationCompat API.
    //

    public void sendNotification() {
        // BEGIN_INCLUDE(build_action) - действие по клику на уведомлении
        // запуск определен в параметрах кнопки в sample_layout.xml

        // Создает интент, который сработает, когда пользователь кликнет извещение.
        // Интент должен быть упакован в {@link android.app.PendingIntent} так, чтобы
        // сервис извещений смог запустить его по нашему желанию.
        //
        // Мы открываем страницу номера в браузере по клику на уведомлении
        //

        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(ntfUri));
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0);

        // END_INCLUDE(build_action)

        // BEGIN_INCLUDE (build_notification)
        //
        // Use NotificationCompat.Builder to set up our notification.
        //
        Notification.Builder builder = new Notification.Builder(ctx);

        //
        // Устаннавливаем иконку, которая появится в строке уведомлений.
        // Эта иконка также появится в нижнем правом углу самого уведомления.
        // Важное замечание: Иконка должна быть простой и монохромной.
        //
        builder.setSmallIcon(R.drawable.ic_stat_notification);

        // Задаем интент, который сработает по тапу по извещению
        // Set the intent that will fire when the user taps the notification.
        builder.setContentIntent(pendingIntent);

        // Устанавливаем извещение в автоотмену. Это означает, что уведомление будет скрываться
        // после того, как пользователь тапнет по нему,
        builder.setAutoCancel(ntfCancel);

        //
        // Строим представление извещения.
        // Устанавливаем большую иконку, которая должна появиться слева от извещения.
        // Здесь мы взяли в качестве большой иконки иконку приложения. Она подойдет.
        ///
        builder.setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), R.drawable.ic_launcher));

        // Задаем текст уведомления. Этот тестовый набор содержит три чаще всего используемые
        // области текста:
        // 1. Заголовок содержимого, который показывается, когда уведомление развернуто.
        // 2. Текст, который появляется в кратком виде под заголовком.
        // 3. Текст, который появляется еще ниже. Виден только на Андроиде > 4.2.
        ///
        //builder.setContentTitle(NTF_TITLE + num);
        builder.setContentTitle(ntfContTitle);
        builder.setContentText(ntfContText);
        builder.setSubText(ntfSubText);

        //long[] pattern = {500,500,500,500,500,500,500,500,500};
        long[] pattern = {300L, 300L}; // 2 times vibro, not 1
        builder.setVibrate(pattern); //очень раздражает

        //builder.setSound( );
        builder.setTicker(num);
        builder.setNumber(messQ); // not in 4.2
        builder.setContentInfo("InfOnum");
        builder.setOngoing(ntfTop);
        // END_INCLUDE (build_notification)

        // BEGIN_INCLUDE(send_notification)
        //
        // Send the notification. This will immediately display the notification icon in the
        // notification bar.
        //
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(nId, builder.build());
        // END_INCLUDE(send_notification)
        //

    }

    public String getDeviceId(Context context) {
        // Получаем Android_ID устройства
        //final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String androidId, tmDevice, tmSerial;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(ctx.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

        return deviceUuid.toString();  //RFC 4122 UUID - шифрованный
    }

    public boolean checkAppNum() {
        // проверяем наличие файла и, возможно, наличие кода приложения в нем и правильность его формата
        return true;
    }

    public boolean setAppNum(String appNum) {
        // пишем код в файл
        return true;
    }

    public void setNotiParam(int i) {
        if (!appActivated) i = 0;
        for (int j = 0; j < i; j++) {
            switch (i) {
                case 0: //первое сообщение:"всего сообщений"

                    ;
                    break;
                case 1: // новое сообщение с номера
                    ;
                    break;
                case 2:
                    ;
                    break;
                case 3:
                    ;
                    break;
            }
        }
        //добавить 
    }
}

