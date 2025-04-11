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
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import com.example.mytablet.R;

import java.util.ArrayList;
import java.util.List;
public class AnnouncementView extends LinearLayout {
    private TextView tvTitle, tvCount,tv_notice_index;
    private TextSwitcher tsNotice;
    private Handler handler = new Handler(Looper.getMainLooper());
    private List<Notice> notices = new ArrayList<>();
    private int currentIndex = 0;

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
            textView.setTextSize(16);
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            return textView;
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

    private void updateNoticeText() {
        if (notices.isEmpty()) return;
        Notice notice = notices.get(currentIndex);
        String displayText = notice.getSubjectName() + "：" + notice.getContent();
        tsNotice.setText(displayText);
    }

    private Runnable switchRunnable = new Runnable() {
        @Override
        public void run() {
            currentIndex = (currentIndex + 1) % notices.size();
            // 更新公告序号
            int displayIndex = currentIndex + 1;
            tv_notice_index.setText(String.valueOf(displayIndex));
            // 切换公告内容
            tsNotice.post(() -> tsNotice.setText(notices.get(currentIndex).getContent()));
            handler.postDelayed(this, 10000); // 10秒后切换
        }
    };

}



