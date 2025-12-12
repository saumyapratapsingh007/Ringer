package com.ringer.app;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int DND_REQUEST = 101;
    private static final String PREFS_NAME = "config";
    private static final String KEY_INTERVAL = "interval";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NumberPicker picker = findViewById(R.id.intervalPicker);
        Button saveBtn = findViewById(R.id.saveBtn);


        picker.setMinValue(1);
        picker.setMaxValue(120);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int saved = prefs.getInt(KEY_INTERVAL, 15);
        picker.setValue(saved);

        saveBtn.setOnClickListener(v -> {
            int newInterval = picker.getValue();
            prefs.edit().putInt(KEY_INTERVAL, newInterval).apply();


            Toast.makeText(MainActivity.this,
                    "Interval saved: " + newInterval + " minute" + (newInterval == 1 ? "" : "s"),
                    Toast.LENGTH_SHORT).show();


            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Interval Updated")
                    .setMessage("The ringer interval is now set to " + newInterval + " minute" + (newInterval == 1 ? "" : "s") + ".")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .setCancelable(true)
                    .show();


            Intent svc = new Intent(MainActivity.this, com.ringer.app.RingerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
        });

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        if (!nm.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivityForResult(intent, DND_REQUEST);
        } else {

            Intent svc = new Intent(this, com.ringer.app.RingerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent i = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(i);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DND_REQUEST) {
            Intent svc = new Intent(this, com.ringer.app.RingerService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
        }
    }
}
