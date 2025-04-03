package com.example.mytablet;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.yx.YxDeviceManager;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ImageView logo,img_scan;
    private TextView tvName;
    private LinearLayout ll_top_home,ll_scan;
    private TextView tvTime, tvWeek, tvDate,unit_name,name_eng,tv_signinCnt;
    private Handler handler = new Handler();
    private YxDeviceManager yxDeviceManager;
    private SerialReaderThread serialReaderThread;
    private SerialHelper serialHelper;
    private String userGuide;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        yxDeviceManager = YxDeviceManager.getInstance(this);

        ApiClient.setDeviceSerialNumber(yxDeviceManager.getSerialno());
//        ApiClient.setDeviceSerialNumber("bfcc2b9ab3bc770a");  //这个是测试用的数据。
        tv_signinCnt = findViewById(R.id.tv_signinCnt);

        // 创建 Handler 处理签到次数更新
        mainHandler = new Handler(Looper.getMainLooper(), msg -> {
            if (msg.what == 1) { // 识别签到次数的消息
                String signinCnt = (String) msg.obj;
                tv_signinCnt.setText(signinCnt);
                Log.i("sssssssss",tv_signinCnt.toString());
            }
            return true;
        });

        // 初始化 SerialHelper
        serialHelper = new SerialHelper();
        // 打开串口
        if (serialHelper.openSerialPort("/dev/ttyS4", 9600)) {
            // 启动串口数据读取线程
            serialReaderThread = new SerialReaderThread(serialHelper.getInputStream(),mainHandler);
            serialReaderThread.start();
        } else {
            Utils.showToast("串口打开失败");
        }

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

        updateTime();
        // 初始加载 HomeFragment
        loadFragment(new HomeFragment());
        // 监听 Fragment 切换
        getSupportFragmentManager().addOnBackStackChangedListener(() -> updateUI());
        findViewById(R.id.ll_home).setOnClickListener(v -> loadFragment(new ClassroomFragment()));
        findViewById(R.id.ll_location).setOnClickListener(v -> loadFragment(new TeacherIntroFragment()));
        findViewById(R.id.ll_teacher).setOnClickListener(v -> loadFragment(new ViewCoursesFragment()));
        findViewById(R.id.ll_course).setOnClickListener(v -> loadInstruction());

        fetchBoardInfo();
    }

    private void loadInstruction(){
        InstructionFragment instructionFragment = new InstructionFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userGuide", userGuide);
        instructionFragment.setArguments(bundle);
        loadFragment(instructionFragment);
    }

    public void fetchBoardInfo() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        // 发起请求
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
                        Glide.with(MainActivity.this)
                                .load(boardInfo.siteInfo.logoUrl)
                                .into(logo);
                        Glide.with(MainActivity.this)
                                .load(boardInfo.siteInfo.qrCodeUrl)
                                .into(img_scan);
                        userGuide = boardInfo.siteInfo.userGuideUrl;
                        System.out.println("✅ 单位名称：" + boardInfo.siteInfo.unit);
                        if (boardInfo.room != null) {
                            System.out.println("✅ 教室名称：" + boardInfo.room.roomName);
                        }
                    } else {
                        Utils.showToast("网络请求失败"+result.getMsg());
                    }
                } else {
                    Utils.showToast("服务器错误"+response.code());
                }
            }

            @Override
            public void onFailure(Call<Result<BoardInfo>> call, Throwable t) {
                Utils.showToast("网络请求失败"+t.getMessage());
                t.printStackTrace();
            }
        });
    }

    private void updateTime() {
        Calendar calendar = Calendar.getInstance();
        // 获取当前时间
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        String time = timeFormat.format(calendar.getTime());
        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        String date = dateFormat.format(calendar.getTime());
        // 获取星期几
        String[] weeks = {"星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        int weekIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String week = weeks[weekIndex];
        // 设置文本
        tvTime.setText(time);
        tvDate.setText(date);
        tvWeek.setText(week);
        // 每小时更新一次时间
        handler.postDelayed(this::updateTime, 3600 * 1000);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void updateUI() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
            ll_top_home.setVisibility(View.VISIBLE);
            ll_scan.setVisibility(View.VISIBLE);
        } else {
            ll_top_home.setVisibility(View.GONE);
            ll_scan.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        // 停止串口数据读取线程
        if (serialReaderThread != null) {
            serialReaderThread.stopReading();
        }
        // 关闭串口
        serialHelper.closeSerialPort();
    }
}
