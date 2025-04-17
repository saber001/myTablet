package com.example.mytablet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.yx.YxDeviceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.mytablet.ui.SerialHelper;
import com.example.mytablet.ui.SerialReaderThread;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.fragment.ClassroomFragment;
import com.example.mytablet.ui.fragment.HomeFragment;
import com.example.mytablet.ui.fragment.ViewCoursesFragment;
import com.example.mytablet.ui.fragment.TeacherIntroFragment;
import com.example.mytablet.ui.fragment.InstructionFragment;
import com.example.mytablet.ui.model.BoardInfo;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.Utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class MainActivity extends AppCompatActivity {

    private ImageView logo, img_scan;
    private TextView tvName, tvTime, tvWeek, tvDate, unit_name, name_eng, tv_signinCnt;
    private LinearLayout ll_top_home, ll_scan;
    private Handler handler = new Handler();
    private Handler mainHandler;
    private Handler heartbeatHandler = new Handler(Looper.getMainLooper());
    private YxDeviceManager yxDeviceManager;
    private SerialReaderThread serialReaderThread;
    private SerialHelper serialHelper;
    private ApiService apiService;
    private String userGuide;
    private Fragment currentFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDeviceManager();
    }

    private void initDeviceManager() {
        yxDeviceManager = YxDeviceManager.getInstance(this);
        String serialNo = yxDeviceManager.getSerialno();
//        serialNo = "bfcc2b9ab3bc770a";
        if (TextUtils.isEmpty(serialNo)) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("设备异常")
                    .setMessage("无法获取设备序列号，设备可能存在故障，5秒后将自动退出应用。")
                    .setCancelable(false)
                    .setPositiveButton("确认", (dialog1, which) -> exitApp())
                    .create();
            dialog.show();
            // 自动延迟退出
            new Handler(Looper.getMainLooper()).postDelayed(this::exitApp, 5000);
            return;
        } else {
            ApiClient.setDeviceSerialNumber(serialNo);
            yxDeviceManager.selfStart("com.example.mytablet");
            initApiService();
            initViews();
            initHandlers();
            initSerialPort();
            initClickListeners();
            loadFragment(new HomeFragment());
            updateTime();
            fetchBoardInfo();
            startService(new Intent(this, HeartbeatService.class));
        }
    }

    private void exitApp() {
        finishAffinity(); // 结束所有 Activity
        System.exit(0);   // 强制退出进程
    }

    private void initApiService() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void initViews() {
        tv_signinCnt = findViewById(R.id.tv_signinCnt);
        tvName = findViewById(R.id.tv_home);
        ll_scan = findViewById(R.id.ll_scan);
        ll_top_home = findViewById(R.id.ll_top_home);
        logo = findViewById(R.id.logo);
        unit_name = findViewById(R.id.unit_name);
        name_eng = findViewById(R.id.name_eng);
        tvTime = findViewById(R.id.tv_time);
        tvWeek = findViewById(R.id.tv_week);
        tvDate = findViewById(R.id.tv_date);
        img_scan = findViewById(R.id.img_scan);
    }

    private void initHandlers() {
        mainHandler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.what == 1) {
                tv_signinCnt.setText((String) msg.obj);
            }
            return true;
        });
    }

    private void initSerialPort() {
        serialHelper = new SerialHelper();
        if (serialHelper.openSerialPort("/dev/ttyS4", 9600)) {
            serialReaderThread = new SerialReaderThread(serialHelper.getInputStream(), mainHandler, this);
            serialReaderThread.start();
        } else {
            Utils.showToast("串口打开失败");
        }
    }

    private void initClickListeners() {
        findViewById(R.id.ll_home).setOnClickListener(v -> loadFragment(new ClassroomFragment()));
        findViewById(R.id.ll_location).setOnClickListener(v -> loadFragment(new TeacherIntroFragment()));
        findViewById(R.id.ll_teacher).setOnClickListener(v -> loadFragment(new ViewCoursesFragment()));
        findViewById(R.id.ll_course).setOnClickListener(v -> loadInstruction());
    }

    private void loadInstruction() {
        InstructionFragment instructionFragment = new InstructionFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userGuide", userGuide);
        bundle.putString("serialno", yxDeviceManager.getSerialno());
        instructionFragment.setArguments(bundle);
        loadFragment(instructionFragment);
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            transaction.remove(currentFragment); // ⚠️ 直接 remove 掉当前 fragment，释放资源
        }
        transaction.add(R.id.fragment_container, fragment, fragment.getClass().getSimpleName());
        currentFragment = fragment;
        transaction.commitAllowingStateLoss();
        updateUI(currentFragment);
    }

    private void updateUI(Fragment fragment) {
        boolean isHome = fragment instanceof HomeFragment;
        ll_top_home.setVisibility(isHome ? View.VISIBLE : View.GONE);
        ll_scan.setVisibility(isHome ? View.VISIBLE : View.GONE);
    }


    private void updateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        String[] weeks = {"星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

        // 实时获取系统当前时间
        String nowTime = timeFormat.format(calendar.getTime());
        tvTime.setText(nowTime);
        tvDate.setText(dateFormat.format(calendar.getTime()));
        tvWeek.setText(weeks[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        // 重新设定1分钟或更短的更新间隔，确保不偏差
        handler.postDelayed(this::updateTime, 60 * 1000); // 每分钟刷新一次，避免累计误差
    }

    public void fetchBoardInfo() {
        apiService.getBoardInfo().enqueue(new Callback<Result<BoardInfo>>() {
            @Override
            public void onResponse(Call<Result<BoardInfo>> call, Response<Result<BoardInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<BoardInfo> result = response.body();
                    if (result.getCode() == 200) {
                        BoardInfo boardInfo = result.getData();
                        tvName.setText(boardInfo.room.roomName);
                        unit_name.setText(boardInfo.siteInfo.unit);
                        name_eng.setText(Utils.convertToPinyin(boardInfo.siteInfo.unit));
                        Glide.with(MainActivity.this).load(boardInfo.siteInfo.logoUrl).into(logo);
                        Glide.with(MainActivity.this).load(boardInfo.siteInfo.qrCodeUrl).into(img_scan);
                        userGuide = boardInfo.siteInfo.userGuideUrl;
                        checkTimeSyncWithServer(boardInfo.now);
                    } else {
                        Utils.showToast("网络请求失败" + result.getMsg());
                    }
                } else {
                    Utils.showToast("服务器错误" + response.code());
                }
            }

            @Override
            public void onFailure(Call<Result<BoardInfo>> call, Throwable t) {
                Utils.showToast("网络请求失败" + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void checkTimeSyncWithServer(String serverTimeStr) {
        if (TextUtils.isEmpty(serverTimeStr)) {
            Log.e("TimeCheck", "服务器时间为空，无法比较！");
            return;
        }
        try {
            // 1. 解析服务器时间字符串 -> 毫秒值
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai")); // 设置时区，避免误差
            long serverTimeMillis = sdf.parse(serverTimeStr).getTime();
            // 2. 获取本地时间
            long localTimeMillis = System.currentTimeMillis();
            // 3. 比较差值
            long diff = Math.abs(serverTimeMillis - localTimeMillis);
            if (diff > 60 * 1000) { // 超过1分钟
                yxDeviceManager.setSystemTime(serverTimeMillis);
            } else {
                Log.d("TimeCheck", "时间同步正常，差值：" + diff + "ms");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("TimeCheck", "解析服务器时间失败：" + serverTimeStr);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        heartbeatHandler.removeCallbacksAndMessages(null);
        if (serialReaderThread != null) {
            serialReaderThread.stopReading();
        }
        serialHelper.closeSerialPort();
    }
}
