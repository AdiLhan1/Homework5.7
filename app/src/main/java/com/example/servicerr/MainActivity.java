package com.example.servicerr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.servicerr.data.Event;
import com.example.servicerr.utils.NotificationHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etDesc;
    private TextView tvtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etDesc = findViewById(R.id.etDescription);
        tvtitle = findViewById(R.id.tvTitle);
        findViewById(R.id.btnPush).setOnClickListener(this);
        findViewById(R.id.btnStartService).setOnClickListener(this);
        findViewById(R.id.btnStopService).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showMessage(Event event) {
        Toast.makeText(this, event.getTitle(), Toast.LENGTH_LONG).show();
        tvtitle.setText(event.getTitle());
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPush:
                NotificationHelper.createNotification(this, getTextFromEt());
                break;
            case R.id.btnStartService:
                startForeground();
                break;
            case R.id.btnStopService:
                stopForeground();
                break;
        }
    }

    private void stopForeground() {
        Intent intent = new Intent(this, TrackingService.class);
        stopService(intent);
    }

    private void startForeground() {
        Intent intent = new Intent(this, TrackingService.class);
        startService(intent);
    }

    private String getTextFromEt() {
        if (etDesc.getText() == null) return "No typed description";
        return etDesc.getText().toString();
    }
}
