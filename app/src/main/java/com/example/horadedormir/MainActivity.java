package com.example.horadedormir;

import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //variaveis
    private TextView tvContador;
    private Button btnHorario;
    private Intent intent;
    String TAG = "Main";

    TimePickerDialog.OnTimeSetListener timeSetListener;
    Calendar calendar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvContador = findViewById(R.id.tvContador);
        btnHorario = findViewById(R.id.btnHorario);


        TimePicker timePicker = new TimePicker(getApplicationContext());

        Toast.makeText(getApplicationContext(), "clique", Toast.LENGTH_SHORT).show();

        //verifica permissões
        if (!Settings.System.canWrite(getApplicationContext())) {
            Intent mIntent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            mIntent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(mIntent);
        }
        try {
            btnHorario.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View v) {
                    timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            calendar = Calendar.getInstance();
                            int hora = calendar.get(Calendar.HOUR_OF_DAY);
                            int minuto = calendar.get(Calendar.MINUTE);

                            int minutoTotal = ((hourOfDay * 60) + minute) - ((hora * 60) + minuto);

                            //minuto maior que zero impede que usuario escolha uma hora anterior a atual
                            if (minutoTotal >= 0) {
                                stopService(new Intent(MainActivity.this, BroadcastService.class)); //para o serviço em background se tiver executando

                                long mili = minutoTotal * 60000;
                                btnHorario.setText("" + hourOfDay + ":" + minute + " Minutos para desligamento: " + minutoTotal);

                                SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                                sharedPreferences.edit().putLong("time", mili).apply();

                                intent = new Intent(MainActivity.this, BroadcastService.class); //cria o broadcastService com o tempo selecionado atribuido no sharedPreferences

                                startService(intent); //inicia o broadcast service
                                Log.i(TAG, "Started Service");
                            } else {
                                btnHorario.setText("Hora errada: " + minutoTotal);
                            }
                        }
                    };
                    calendar = Calendar.getInstance();
                    new TimePickerDialog(MainActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE), true).show();

                }
            });

        } catch (Exception e) {
            Log.d("Erro", e.toString());
        } //TimePicker
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //update GUi
            updateGUI(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(BroadcastService.COUNTDOWN_BR));
        Log.i(TAG, "Registered broadcast receiver");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.i(TAG, "Unregistered broadcast receiver");
    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            // Receiver was probly already

        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, BroadcastService.class));
        Log.i(TAG, "Stopped service");
        super.onDestroy();
    }

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {

            long milisegundos = intent.getLongExtra("countDown", 30000);

            Log.i(TAG, "Countdown seconds remaining:" + milisegundos / 1000);
            tvContador.setText(Long.toString(milisegundos / 1000));

            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

            sharedPreferences.edit().putLong("time", milisegundos).apply();

            if (milisegundos <= 0) {
                Toast.makeText(this, "Acabou", Toast.LENGTH_SHORT).show();
                stopService(new Intent(this, BroadcastService.class));
            }
        }
    }
}