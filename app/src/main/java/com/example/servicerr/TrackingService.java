package com.example.servicerr;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.servicerr.data.Event;
import com.example.servicerr.utils.NotificationHelper;

import org.greenrobot.eventbus.EventBus;

import static com.example.servicerr.utils.NotificationHelper.ACTION_CLOSE;

public class TrackingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_CLOSE)) {
            EventBus.getDefault().post(new Event("event from service"));
            stopSelf();
        } else {
            Notification notification = NotificationHelper.createNotificationBuilder(getApplicationContext(), "asdasd");
            startForeground(1, notification);
        }
    }
}
