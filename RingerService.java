package com.ringer.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class RingerService extends Service {

    private Handler handler;
    private Runnable task;

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ringer_channel")
                .setContentTitle("HabitBreaker Active")
                .setContentText("Ensuring phone is not on silent")
                .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
                .setOngoing(true);

        startForeground(1, builder.build());

        handler = new Handler();
        task = () -> {
            turnOnRinger();
            int interval = getSharedPreferences("config", MODE_PRIVATE)
                    .getInt("interval", 15);

            handler.postDelayed(task, interval * 60 * 1000);
        };

        handler.post(task);
    }

    private void turnOnRinger() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        AudioManager audio = (AudioManager)getSystemService(AUDIO_SERVICE);

        if (nm != null && nm.isNotificationPolicyAccessGranted() && audio != null) {
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audio.setStreamVolume(AudioManager.STREAM_RING,
                    audio.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ringer_channel",
                    "Ringer Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager mgr = getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
