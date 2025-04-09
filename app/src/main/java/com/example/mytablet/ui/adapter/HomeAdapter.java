package com.example.mytablet.ui.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.model.HomeBean;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.graphics.Typeface;
import android.widget.ImageView;
import java.util.Date;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private ArrayList<HomeBean> homeList;
    private OnHomeClickListener onHomeClickListener;
    private int selectedPosition = -1; // 记录选中的项，-1 表示未选中

    public interface OnHomeClickListener {
        void onCourseClick(HomeBean course);
    }

    public HomeAdapter(ArrayList<HomeBean> homeList, OnHomeClickListener listener) {
        this.homeList = homeList;
        this.onHomeClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HomeBean homeBean = homeList.get(position);
        // 确保数据正常绑定
        Log.d("HomeAdapter", "绑定数据: " + homeBean.getCourseName() + " 位置：" + position);

        formatAndSetTime(homeBean.getBeginTime(), homeBean.getEndTime(), holder.courseTime);
        holder.courseName.setText("《"+homeBean.getCourseName()+"》");
        holder.courseNum.setText(homeBean.getClazzName());

        // 选中状态
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(0xFFE0E0E0); // 浅灰色背景
            holder.courseName.setTypeface(null, Typeface.BOLD);
            holder.courseTime.setTypeface(null, Typeface.BOLD);
            holder.arrowIcon.setImageResource(R.mipmap.ic_orange);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
            holder.courseName.setTypeface(null, Typeface.NORMAL);
            holder.courseTime.setTypeface(null, Typeface.NORMAL);
            holder.arrowIcon.setVisibility(View.GONE);
            holder.arrowIcon.setImageResource(R.mipmap.ic_white_right);
        }

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (onHomeClickListener != null) {
                onHomeClickListener.onCourseClick(homeBean);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("HomeAdapter", "当前 Adapter 数据大小：" + homeList.size());
        return homeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courseName, courseTime,courseNum;
        ImageView arrowIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courseName = itemView.findViewById(R.id.course_name);
            courseTime = itemView.findViewById(R.id.course_time);
            courseNum = itemView.findViewById(R.id.course_num);
            arrowIcon = itemView.findViewById(R.id.arrow_icon); // 右侧箭头
        }
    }

    public static void formatAndSetTime(String startTime, String endTime, TextView textView) {
        // 原始时间格式
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 目标时间格式（只显示时:分）
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            // 解析时间
            Date startDate = inputFormat.parse(startTime);
            Date endDate = inputFormat.parse(endTime);

            // 格式化为 HH:mm
            String startFormatted = outputFormat.format(startDate);
            String endFormatted = outputFormat.format(endDate);

            // 设置到 TextView
            textView.setText(startFormatted + " - " + endFormatted);

        } catch (ParseException e) {
            e.printStackTrace();
            textView.setText("时间格式错误");
        }
    }
}


