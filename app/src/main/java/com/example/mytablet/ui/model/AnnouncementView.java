package com.example.mytablet.ui.model;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mytablet.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class AnnouncementView extends LinearLayout {
    private TextView tvTitle, tvCount,tv_notice_index;
    private TextSwitcher tsNotice;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Notice> notices = new ArrayList<>();
    private int currentIndex = 0;
    private ImageView ivDropdown;

    public AnnouncementView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_announcement, this, true);

        tvTitle = findViewById(R.id.tv_title);   // 固定 "通知公告"
        tvCount = findViewById(R.id.tv_count);   // 显示公告条数
        tsNotice = findViewById(R.id.ts_notice); // 公告轮播
        tv_notice_index = findViewById(R.id.tv_notice_index);

        tvTitle.setText("通知公告");

        // 设置 TextSwitcher 文字切换效果
        tsNotice.setFactory(() -> {
            TextView textView = new TextView(context);
            textView.setTextSize(18);
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return textView;
        });
        ivDropdown = findViewById(R.id.iv_dropdown);
        ivDropdown.setOnClickListener(v -> {
            if (notices != null && notices.size() > 1) {
                handler.removeCallbacks(switchRunnable); // 清除旧轮播
                // 手动切换到下一条
                currentIndex = (currentIndex + 1) % notices.size();
                tv_notice_index.setText(String.valueOf(currentIndex + 1));
                tsNotice.setText(buildDisplayText(notices.get(currentIndex)));
                // 恢复轮播（从点击后的下一条继续）
                handler.postDelayed(switchRunnable, 10000);
            }
        });

        tsNotice.setInAnimation(context, android.R.anim.fade_in);
        tsNotice.setOutAnimation(context, android.R.anim.fade_out);
    }

    public void setNotices(List<Notice> notices) {
        this.notices = notices;
        handler.removeCallbacks(switchRunnable);

        if (notices == null || notices.isEmpty()) {
            tvCount.setText("0");
            tsNotice.setText("暂无公告");
            return;
        }

        tvCount.setText(notices.size()+""); // 设置公告条数
        currentIndex = 0;

        updateNoticeText();

        if (notices.size() > 1) {
            handler.postDelayed(switchRunnable, 10000); // 10秒后切换
        }
    }

    private final DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    private void updateNoticeText() {
        if (notices.isEmpty()) return;
        currentIndex = 0;
        tv_notice_index.setText("1");
        tsNotice.setText(buildDisplayText(notices.get(currentIndex)));
    }

    private final Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            if (notices == null || notices.isEmpty()) return;

            currentIndex = (currentIndex + 1) % notices.size();
            tv_notice_index.setText(String.valueOf(currentIndex + 1));
            final String displayText = buildDisplayText(notices.get(currentIndex));
            tsNotice.post(() -> tsNotice.setText(displayText));

            handler.postDelayed(this, 10000);
        }
    };

    private String buildDisplayText(Notice notice) {
        String formattedTime;
        try {
            LocalDateTime dateTime = LocalDateTime.parse(notice.getCreateTime(), inputFormatter);
            formattedTime = dateTime.format(outputFormatter);
        } catch (Exception e) {
            e.printStackTrace();
            formattedTime = notice.getCreateTime(); // fallback 显示原始
        }
        return "[ " + notice.getSubjectName() + " ]：" + notice.getContent() + "          " + formattedTime;
    }
}



