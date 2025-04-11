package com.example.mytablet.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import com.example.mytablet.MainActivity;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;

public abstract class BaseFragment extends Fragment {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private MyBroadcastReceiver receiver;
    private InputMethodManager imm;

    private CountDownTimer countDownTimer;
    private TextView countdownTextView;
    private static final long COUNTDOWN_TIME = 60 * 1000L;

    private boolean isFirstResume = true;

    protected ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFirstResume) {
            restartCountdownIfNeeded(); // ✅ 返回时重启倒计时
        } else {
            isFirstResume = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCountdown(); // 离开时停止
    }

    @Override
    public void onDestroy() {
        unregistReceiver();
        stopCountdown(); // 避免泄漏
        super.onDestroy();
    }

    protected void unregistReceiver() {
        if (receiver != null) {
            getActivity().unregisterReceiver(receiver);
        }
    }


    // ✅ 子类重写，重新绑定 TextView 启动倒计时
    protected void restartCountdownIfNeeded() {
        // 默认空实现，子类重写
    }

    private InputMethodManager getImm() {
        if (imm == null) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return imm;
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBroadcast(intent);
        }
    }

    protected void onReceiveBroadcast(Intent intent) {}
    // 显示倒计时
    protected void startCountdown(TextView view) {
        stopCountdown(); // 确保旧的 timer 被取消
        this.countdownTextView = view;
        // 每次重新开始都从 60s
        final long totalTime = COUNTDOWN_TIME;
        countDownTimer = new CountDownTimer(totalTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (countdownTextView != null) {
                    String text = "自动关闭倒计时 " + (millisUntilFinished / 1000) + "s";
                    countdownTextView.setText(text);
                }
            }

            @Override
            public void onFinish() {
                if (countdownTextView != null) {
                    countdownTextView.setText("自动关闭倒计时 0s");
                }
                navigateToHome();
            }
        };
        countDownTimer.start(); // 启动新的 timer
    }

    // ✅ 倒计时停止
    protected void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }


    // ✅ 跳转到主页方法
    protected void navigateToHome() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).loadFragment(new HomeFragment());
        }
    }
}


