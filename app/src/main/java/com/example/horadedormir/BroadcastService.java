package com.example.horadedormir;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BroadcastService extends Service {

    //variaveis
    public static final String COUNTDOWN_BR = "com.example.horadedormir";
    private String TAG = "BroadcastService";
    CountDownTimer countDownTimer = null;
    Intent intent = new Intent(COUNTDOWN_BR);

    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        long tempo = sharedPreferences.getLong("time",3000);

        Log.i(TAG,"Starting timer...");

        countDownTimer = new CountDownTimer(tempo, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG,"Countdown seconds remaining:" + millisUntilFinished / 1000);
                intent.putExtra("countDown", millisUntilFinished);
            sendBroadcast(intent);
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFinish() {
                countDownTimer.cancel();
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 0); //altera o tempo de suspensão, não é exatamente o que quero
            }
        };
        countDownTimer.start();
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
