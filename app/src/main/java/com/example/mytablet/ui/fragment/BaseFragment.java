package com.example.mytablet.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.mytablet.R;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;

public abstract class BaseFragment extends Fragment {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private MyBroadcastReceiver receiver;
    private InputMethodManager imm;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_TIME = 60000; // 60秒
    private TextView countdownTextView; // 倒计时 TextView

    protected ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    protected void registReceiver(String... actions) {
        if (receiver == null) {
            receiver = new MyBroadcastReceiver();
        }
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        getActivity().registerReceiver(receiver, intentFilter);
    }

    protected void unregistReceiver() {
        if (receiver != null)
            getActivity().unregisterReceiver(receiver);
    }

    protected void showKeyboard(View view) {
        getImm().showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    protected void hideKeyboard(View view) {
        getImm().hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private InputMethodManager getImm() {
        if (imm == null) {
            imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return imm;
    }

    protected void onReceiveBroadcast(Intent intent) {
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCountdown(); // 在 Fragment 切换时停止倒计时
    }

    @Override
    public void onDestroy() {
        unregistReceiver();
        stopCountdown(); // 避免内存泄漏
        super.onDestroy();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBroadcast(intent);
        }
    }

    // **倒计时逻辑**
    protected void startCountdown(TextView countdownTextView) {
        this.countdownTextView = countdownTextView; // 记录倒计时 TextView
        stopCountdown(); // 防止重复启动
        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (countdownTextView != null) {
                    countdownTextView.setText("自动关闭倒计时 " + millisUntilFinished / 1000 + "s");
                }
            }

            @Override
            public void onFinish() {
                Log.d("Countdown", "倒计时结束，返回 HomeFragment");
                navigateToHome();
            }
        }.start();
    }

    protected void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    protected void navigateToHome() {
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            // 使用 replace 来避免 Fragment 堆栈问题
            transaction.replace(R.id.fragment_container, new HomeFragment());
            transaction.addToBackStack(null); // 可以加入栈
            transaction.commitAllowingStateLoss();  // 允许状态丢失，防止崩溃
        }
    }

    // 防止 Fragment 切换时发生状态丢失
    protected void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        String tag = fragment.getClass().getSimpleName();
        Fragment targetFragment = fragmentManager.findFragmentByTag(tag);

        if (targetFragment == null) {
            // Fragment 没有缓存，进行替换
            transaction.replace(R.id.fragment_container, fragment, tag);
        } else {
            // 显示已缓存的 Fragment
            transaction.show(targetFragment);
        }

        // 隐藏当前 Fragment
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        transaction.commitAllowingStateLoss(); // 允许状态丢失，防止崩溃
    }
}

