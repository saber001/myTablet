package com.example.mytablet.ui;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.yx.YxDeviceManager;
import android.util.Log;
import android.widget.Toast;
import com.example.mytablet.MyApplication;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.SignInResponse;
import java.io.IOException;
import java.io.InputStream;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SerialReaderThread extends Thread {
    private InputStream inputStream;
    private boolean isRunning = true;
    private Handler mainHandler;

    public SerialReaderThread(InputStream inputStream, Handler mainHandler) {
        this.inputStream = inputStream;
        this.mainHandler = mainHandler; // 直接使用 MainActivity 传入的 Handler
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];  // 缓存接收数据
        int length;
        try {
            while (isRunning) {
                // 检查是否有数据
                if ((length = inputStream.read(buffer)) > 0) {
                    // 读取到数据，输出数据内容
                    String receivedData = bytesToHex(buffer, length);
                    Log.d("SerialReader", "📥 接收到数据：" + receivedData);
                    sendSignInRequest(receivedData);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendSignInRequest(String signInCode) {
        ApiClient.getClient().create(ApiService.class)
                .signIn(signInCode)
                .enqueue(new Callback<Result<SignInResponse>>() {
                    @Override
                    public void onResponse(Call<Result<SignInResponse>> call, Response<Result<SignInResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Result<SignInResponse> result = response.body();
                            if (result.getCode() == 200 && result.getData() != null) {
                                showToast(result.getData().getUserName()+"签到成功");
                                String signinCnt = result.getData().getSigninCnt();  // 获取签到次数
                                // 通过 Handler 发送签到次数到 MainActivity
                                Message msg = Message.obtain();
                                msg.what = 1;
                                msg.obj = signinCnt;
                                mainHandler.sendMessage(msg);
                                MyApplication myApplication = (MyApplication) MyApplication.getContext();
                                YxDeviceManager yxDeviceManager = myApplication.yxDeviceManager;
                                // 开门操作
                                boolean resultOpen = yxDeviceManager.setGpioDirection(113, 1); // 开门
                                if (resultOpen) {
                                    showToast("开门成功");
                                    // 延迟10秒关闭门
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        boolean closeResult = yxDeviceManager.setGpioDirection(113, 0); // 关门
                                        if (closeResult) {
                                            showToast("关门成功");
                                        } else {
                                            showToast("关门失败");
                                        }
                                    }, 10000); // 10000ms = 10秒
                                } else {
                                    showToast("开门失败");
                                }
                            } else {
                                showToast("签到失败：" + result.getMsg());
                            }
                        } else {
                            showToast("请求失败：" + response.message());
                        }
                    }
                    @Override
                    public void onFailure(Call<Result<SignInResponse>> call, Throwable t) {
                        showToast("网络错误：" + t.getMessage());
                    }
                });
    }

    // 显示 Toast
    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(MyApplication.getContext(), message, Toast.LENGTH_SHORT).show());
    }

    // 停止读取线程
    public void stopReading() {
        isRunning = false;
        interrupt();
    }

    // 将字节数组转换为十六进制字符串
    private String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }
}

