package ru.infonum.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


public class RepeatingAlarmService extends BroadcastReceiver {

    final public static int NOTIFICATION_ID = 1;
    public static int nId = NOTIFICATION_ID;

    public static final String TESTTEL = "+79119148047";
    public static final String TESTNUM = "a000aa00";
    public static String inText = "Эвакуатор!";

    public static String num = TESTNUM;

    public static final String NTF_TITLE = "Инфонум: ";
    public static final String NTF_TEXT = ": Подойдите к авто!";
    public static final String NTF_SUBTEXT = "Нажмите сюда, чтобы перейти на страницу номера или смахните, чтобы удалить ";

    public static final String INFONUM_SITE = "http://checkin.infonum.ru/";
    public static final String NOTIF_SITE = "notification/";

    public static final String INST_RQ = "Installation";

    public static String s = INFONUM_SITE;

    private static final String AUTO = "auto_number";
    private static final String NAME = "name";
    private static final String DATE = "date";
    private static final String TEXT = "text";
    private static final String PATH = "path";

    private static final int F_AUTO = 0;
    private static final int F_NAME = 1;
    private static final int F_DATE = 2;
    private static final int F_TEXT = 3;
    private static final int F_PATH = 4;


    private static final String DEVID = "DeviceId";
    private static final String TELNUM = "telNum";
    private static final String DATA = "data";

    //public static String deviceId = "";
    public static String responseStr = "";
    public static String postParam1 = "";
    public static String postParam2 = TESTTEL;
    public static int messQ = 0;

    public String ntfContentTitle = "InfonumDef1";
    public String ntfContentText = "InfonumDef2";
    public String ntfContentInfo = "InfOnum";
    public String ntfSubText = "InfonumDef3";
    public String ntfTicker = "";

    public static Intent ntfIntent;

    //long[] pattern = {500,500,500,500,500,500,500,500,500};
    private static final long[] NTF_VIBRO_PATTERN = {300L, 300L};
    public long[] ntfVibroPattern = NTF_VIBRO_PATTERN;

    public String ntfUri = INFONUM_SITE;

    public Context ctx;

    public static boolean ntfTop = false;
    public static boolean ntfCancel = true;
    public static boolean ntfVibro = false;
    public static boolean ntfSettings = false;
    public static boolean ntfNoClear = true;

    public static String appNum = "";
    public static final String INSTALLATION = "Installation";
    public static final String TAG = "inf";

    public static int jsonFields = 5; // количество полей в одном извещении

    private static final int NTF_SMALL_ICON_1 = R.drawable.ic_stat_notification;
    private static final int NTF_SMALL_ICON_2 = R.drawable.ic_stat_notification;
    private static final int NTF_SMALL_ICON_3 = R.drawable.ic_stat_notification;
    private static final int NTF_SMALL_ICON_4 = R.drawable.ic_stat_notification;
    private static final int NTF_SMALL_ICON_5 = R.drawable.ic_stat_notification;
    public static int ntfSmallIcon = NTF_SMALL_ICON_1;

    private static final int NTF_LARGE_ICON_1 = R.drawable.ic_launcher;
    private static final int NTF_LARGE_ICON_2 = R.drawable.ic_launcher;
    private static final int NTF_LARGE_ICON_3 = R.drawable.ic_launcher;
    private static final int NTF_LARGE_ICON_4 = R.drawable.ic_launcher;
    private static final int NTF_LARGE_ICON_5 = R.drawable.ic_launcher;
    public static int ntfLargeIcon = NTF_LARGE_ICON_1;

    //public static List<String> notiArr = new ArrayList<>();
    //public static String[] n = new String[jsonFields];



    @Override
    public void onReceive(Context context, Intent intent) {

        ctx = context;
        Toast.makeText(ctx, "Инфонум: Получение новых", Toast.LENGTH_LONG).show();

        //postParam1 = getDeviceId(ctx);      // раз не первый запуск приложения, выясняем данные устройства.
        SharedPreferences sp = ctx.getSharedPreferences(ctx.getString(R.string.INST), Context.MODE_PRIVATE);
        appNum = sp.getString(ctx.getString(R.string.INST), "");
        Log.d(TAG, "001--appNum=" + appNum.length() + ": " + appNum);

        if (appNum.length() == 0 || appNum == null) {
            Log.d(TAG, "015--New app code -------------------------------------------------------------------------------");

            postParam1 = ctx.getString(R.string.INST); //если файла нет, то запрашиваем код приложения, передав вместо deviceId строку запроса.
            // делаем только один запрос на получение кода, код не проверяем
            // TODO нужен протокол обмена для получения кода

            String response = "";
            PostTask postTask = new PostTask();
            postTask.execute(INFONUM_SITE + NOTIF_SITE); // post-запрос по этому адресу
            try {
                response = postTask.get();

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            responseStr = response;

            if (responseStr.length() > 0) {
                Log.d(TAG, "010--responseStr " + responseStr.length() + ": " + responseStr);
                String[] nA = jsonToArray(0, responseStr); // ключ должен вернуться в первом элементе массива
                if (num.equals(ctx.getString(R.string.INST))) {
                    //appNum = notiArr.get(1)[F_TEXT]; // записываем, не проверяя соотв. спец. UUID. UUID.fromString(String)
                    Log.d(TAG, "002--appNum: " + appNum);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putString(ctx.getString(R.string.INST), appNum).apply();
                }
            } else {
                // следующий запрос на получение кода - по таймеру
                Log.d(TAG, "003--appNum: пустая строка, код не получен, следующий запрос по таймеру");
            }
        } else { // Нормальное получение извещений

            // код приложения получен и сохранен ранее. Обычное получение новых.
            //postParam1 = getDeviceId(ctx);      // раз не первый запуск приложения, выясняем данные устройства.

            // post-запрос к сайту по таймеру.
            // проверяем наличие новых сообщений, разбирая ответ
            // если есть новые - генерируем уведомление.
            postParam1 = appNum;

            String response = "";
            PostTask postTask = new PostTask();
            postTask.execute(INFONUM_SITE + NOTIF_SITE); // post-запрос по этому адресу
            Log.d(TAG, "033--postTask.execute: " + INFONUM_SITE + NOTIF_SITE);

            try {
                response = postTask.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            responseStr = response;

            Log.v(this.getClass().getName(), "004--Timed alarm onReceive() started at time: " + new java.sql.Timestamp(System.currentTimeMillis()).toString());
            Log.d(TAG, "034--response=" + response);

            String[] nA = null;

            if (responseStr.length() > 0) { // main loop ****************************

                nA = jsonToArray(0, responseStr); // только чтобы получить длину массива messQ

                for (int i = 0; i < messQ; i++) {
                    Log.d(TAG, "017--Запрос на получение новых");
                    nA = jsonToArray(i, responseStr);
                    if (nA != null) {
                        setNotiParam(i + 1, nA);
                        sendNotification();
                        Log.d(TAG, "005--i= " + i + ": " + nA[F_AUTO] + " # " + nA[F_TEXT]);
                    }
                }
                if (nA != null) { // Настройки в извещениях - всегда id=0
                    setNotiParam(0, nA);
                    sendNotification();
                }
            }
        }
    }

    class PostTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            return postData(params[0]);
        }

        @Override
        protected void onPostExecute(String str) {
            // Выполнение какого-то действия, когда поток выполнится
        }

        public String postData(String url) {
            // Подключаемся и указываем принимающий URL
            DefaultHttpClient hc = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);

            try {
                // Создаём коллекцию, которая используется для передачи данных
                List<NameValuePair> sendData = new ArrayList<>(2);

                // Добавляем в неё данные
                sendData.add(new BasicNameValuePair(DEVID, postParam1));
                sendData.add(new BasicNameValuePair(TELNUM, postParam2));
                Log.d(TAG, "037--sendData: " + sendData.toString());

                // Упаковываем данные
                httppost.setEntity(new UrlEncodedFormEntity(sendData));

                // Выполняем POST-запрос
                HttpResponse response = hc.execute(httppost);
                // Возвращаем преобразованный в строку ответ - вероятно, неправильно
                return EntityUtils.toString(response.getEntity());

            } catch (ClientProtocolException e) {
                // исключение
            } catch (IOException e) {
                // исключение
            }
            return null;
        }

    }


    public String[] jsonToArray(int notiIndex, String result) {

        try {
            Log.d(TAG, "011--result=" + result.length() + ": " + result);

            //создали читателя json объектов и отдали ему строку - result
            JSONObject json = new JSONObject(result);
            //дальше находим вход в наш json им является ключевое слово "data"
            JSONArray jArr = json.getJSONArray(DATA);

            Log.d(TAG, "012--urls=" + jArr.length() + ": " + jArr.toString() + " кол.полей=" + jArr.getJSONObject(0).length());

            messQ = jArr.length();
            jsonFields = jArr.getJSONObject(0).length();


            String[] n = new String[jsonFields];
            for (int i = 0; i < jsonFields; i++) n[i] = "";

            n[F_AUTO] = jArr.getJSONObject(notiIndex).getString(AUTO);
            n[F_NAME] = jArr.getJSONObject(notiIndex).getString(NAME);
            n[F_DATE] = jArr.getJSONObject(notiIndex).getString(DATE);
            n[F_TEXT] = jArr.getJSONObject(notiIndex).getString(TEXT);
            n[F_PATH] = jArr.getJSONObject(notiIndex).getString(PATH);

            num = n[F_AUTO];
            inText = n[F_TEXT];
            appNum = n[F_TEXT];

            Log.d(TAG, "008--i= " + notiIndex + ": " + n[F_AUTO] + ", TEXT= " + n[F_TEXT]);
            return n;

        } catch (JSONException e) {
            Log.e(TAG, "009--Error parsing data " + e.toString());
            Toast.makeText(ctx, "Ошибочка разбора json" + s + e.toString(), Toast.LENGTH_LONG).show();
        }
        return null;
    }

    //
    // Отправка тестового извещения через NotificationCompat API.
    //
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

        //Intent intent;
        //if(ntfSettings)
        //    intent = new Intent(ctx, ServiceActivity.class);
        //else
        //    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ntfUri));

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, ntfIntent, 0);

        Notification.Builder builder = new Notification.Builder(ctx);

        builder.setContentIntent(pendingIntent)
                //дальше методы к этому
                .setSmallIcon(ntfSmallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(ctx.getResources(), ntfLargeIcon))

                .setAutoCancel(ntfCancel)

                .setContentTitle(ntfContentTitle)
                .setContentText(ntfContentText)
                .setSubText(ntfSubText)
                .setVibrate(ntfVibroPattern) //очень раздражает
                .setTicker(ntfTicker)
                .setNumber(messQ) // not in 4.1
                .setContentInfo(ntfContentInfo)
                .setOngoing(ntfTop);

        if (ntfNoClear) builder.setDefaults(Notification.FLAG_NO_CLEAR);
        //builder.setSound( );

        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(nId, builder.build());
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


    public void setNotiParam(int iNotif, String[] n) {


        // Значения по-умолчанию
        nId = iNotif;
        ntfIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ntfUri));

        ntfSmallIcon = NTF_SMALL_ICON_1;
        ntfLargeIcon = NTF_LARGE_ICON_1;

        ntfContentTitle = n[F_AUTO];
        ntfContentText = n[F_AUTO] + ": " + n[F_TEXT];
        ntfSubText = ctx.getString(R.string.INFONUM_SITE) + n[F_AUTO];
        ntfTicker = n[F_AUTO];
        ntfUri = INFONUM_SITE + n[F_AUTO] + "?appid=" + appNum;
        ntfTop = false;
        ntfCancel = true;
        ntfContentInfo = "InfOnum";
        ntfVibroPattern = null; // 2 times vibro
        ntfSettings = false;
        ntfNoClear = true;
        ntfVibro = true;

        switch (iNotif) {

            case 0: //первое сообщение:НАСТРОЙКИ Инфонум. Запускает экран настроек.
                ntfIntent = new Intent(ctx, ServiceActivity.class);
                ntfSmallIcon = NTF_SMALL_ICON_1;
                ntfLargeIcon = NTF_LARGE_ICON_1;
                ntfSettings = true;
                ntfNoClear = false;
                ntfTicker = "Новых " + messQ;
                ntfContentTitle = "Настройки";
                ntfContentText = "Осталось незагруженных: " + messQ;
                ntfSubText = "Войти в настройки";
                ntfContentInfo = "ИнфОнум";
                ntfUri = INFONUM_SITE; // не используется при вызове
                ntfTop = true;
                ntfCancel = false;
                if (ntfVibro) ntfVibroPattern = NTF_VIBRO_PATTERN; // 2 times vibro
                break;

            case 4: // сводное сообщение -- Всего новых сообщений 8

                ntfContentTitle = n[F_TEXT];
                ntfContentText = ctx.getString(R.string.INFONUM_SITE);
                ntfSubText = "Читать все на сайте";
                ntfSettings = false;
                ntfNoClear = true;
                ntfVibroPattern = null;
                break;

            case 2: // новое сообщение с номера
                ntfSettings = false;
                ntfNoClear = true;
                ntfVibroPattern = null;
                break;

            case 3:
                ntfSettings = false;
                ntfNoClear = true;
                ntfVibroPattern = null;
                break;

            case 1:
                ntfSettings = false;
                ntfNoClear = true;
                ntfVibroPattern = null;
                break;
        }
    }


}

