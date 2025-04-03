package com.example.mytablet.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mytablet.MainActivity;
import com.example.mytablet.R;

public class SplashActivity extends AppCompatActivity {

   private TextView tvNoNetwork;
   private Button btnRetry;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_splash);

      tvNoNetwork = findViewById(R.id.tv_no_network);
      btnRetry = findViewById(R.id.btn_retry);

      checkNetworkAndProceed();

      // 重试按钮
      btnRetry.setOnClickListener(v -> checkNetworkAndProceed());
   }

   private void checkNetworkAndProceed() {
      if (isNetworkAvailable()) {
         tvNoNetwork.setVisibility(View.GONE);
         btnRetry.setVisibility(View.GONE);

         // 延迟 2 秒进入 MainActivity（可以改为 0 立即跳转）
         new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
         }, 0);
      } else {
         tvNoNetwork.setVisibility(View.VISIBLE);
         btnRetry.setVisibility(View.VISIBLE);
      }
   }

   // 判断是否有网络连接
   private boolean isNetworkAvailable() {
      ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      if (cm != null) {
         NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
         return activeNetwork != null && activeNetwork.isConnected();
      }
      return false;
   }
}
