package com.example.mytablet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.mytablet.ui.activity.SplashActivity;

public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent startIntent = new Intent(context, SplashActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startIntent);
        }
    }
}

