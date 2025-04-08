package com.example.mytablet.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mytablet.MyApplication;
import com.example.mytablet.R;

public class InstructionFragment extends BaseFragment {

    private String userGuide,getSerialno;
    private ImageView imageView;
    private TextView tv_serial;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_instruction, container, false);

        // 获取传递的数据
        if (getArguments() != null) {
            userGuide = getArguments().getString("userGuide", ""); // 取值，避免 null
            getSerialno = getArguments().getString("serialno","");
        }
        imageView = view.findViewById(R.id.image);
        tv_serial = view.findViewById(R.id.tv_serial);
        tv_serial.setText(getSerialno);
        // 加载图片
        if (userGuide != null && !userGuide.isEmpty()) {
            Glide.with(requireActivity())
                    .load(userGuide)
                    .into(imageView);
        } else {
            Log.e("InstructionFragment", "userGuide 为空，无法加载图片");
        }
        // 获取倒计时 TextView 并启动倒计时
        TextView countdownTextView = view.findViewById(R.id.tv_countdown);
        startCountdown(countdownTextView);
        // 监听手动关闭
        LinearLayout btnClose = view.findViewById(R.id.ll_home);
        btnClose.setOnClickListener(v -> navigateToHome());
        return view;
    }
}

