package com.example.mytablet;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.model.Result;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HeartbeatService extends Service {
    private Handler handler;
    private Runnable heartbeatRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        startForeground(1, createNotification()); // 启动前台服务
        heartbeatRunnable = new Runnable() {
            @Override
            public void run() {
                sendHeartbeat();
                handler.postDelayed(this, 120 * 1000); // 2分钟一次
            }
        };
        handler.post(heartbeatRunnable);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                "heartbeat_channel", "Heartbeat Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);

        return new Notification.Builder(this, "heartbeat_channel")
                .setContentTitle("服务运行中")
                .setContentText("保持后台连接")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    private void sendHeartbeat() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.sendHeartbeat().enqueue(new Callback<Result<Void>>() {
            @Override
            public void onResponse(Call<Result<Void>> call, Response<Result<Void>> response) {
                Log.d("HeartbeatService", "心跳成功");
            }
            @Override
            public void onFailure(Call<Result<Void>> call, Throwable t) {
                Log.e("HeartbeatService", "心跳失败: " + t.getMessage());
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
