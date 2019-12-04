package com.example.servicerr;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

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
        broadcastIntent(intent);
        return START_STICKY;
    }

    private void broadcastIntent(Intent intent) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equalsIgnoreCase("getting_data")) {
                    String value=intent.getStringExtra("value");
                    String value2=intent.getStringExtra("value2");
                    Log.e("TAG", "Latitude: "+value+"\n"+" Longitude:"+value2 );
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        // set the custom action
        intentFilter.addAction("getting_data"); //Action is just a string used to identify the receiver as there can be many in your app so it helps deciding which receiver should receive the intent.
        // register the receiver
        registerReceiver(broadcastReceiver, intentFilter);


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
