package com.example.horadedormir;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

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
        Log.i(TAG,"Starting timer...");
        sharedPreferences = getSharedPreferences(getPackageName(),MODE_PRIVATE);
        long tempo = sharedPreferences.getLong("time",3000);

        countDownTimer = new CountDownTimer(tempo, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG,"Countdown seconds remaining:" + millisUntilFinished / 1000);
                intent.putExtra("countDown", millisUntilFinished);
            sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                countDownTimer.cancel();
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
