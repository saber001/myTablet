package com.example.mytablet.ui.adapter;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.fragment.CalendarFragment;
import com.example.mytablet.ui.model.CourseBean;
import java.util.List;

public class ViewCoursesAdapter extends RecyclerView.Adapter<ViewCoursesAdapter.ViewHolder> {
    private final List<CourseBean> courseList;
    private final FragmentActivity fragmentActivity;

    public ViewCoursesAdapter(List<CourseBean> courseList, FragmentActivity activity) {
        this.fragmentActivity = activity;
        this.courseList = courseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_course, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CourseBean course = courseList.get(position);
        holder.tvName.setText("《"+course.getName()+"》");
        holder.tvName.setTextColor(Color.parseColor("#ee7802"));

        int quota = course.getClassFinishCnt();
        int classCnt = course.getClassCnt();
        String text = "课时：" + quota + " /" + classCnt;
        SpannableStringBuilder spannable = new SpannableStringBuilder(text);
        int start = text.indexOf(String.valueOf(quota));
        int end = start + String.valueOf(quota).length();
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#ee7802")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvHours.setText(spannable);

        holder.tvTeacher.setText("授课老师："+course.getTeacherName());
        holder.tvDescription.setText(course.getInfo());

        // 监听按钮点击事件，传递课程 ID 切换 Fragment
        holder.btnSchedule.setOnClickListener(v -> {
            FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            // 传递 courseId
            CalendarFragment calendarFragment = new CalendarFragment();
            Bundle args = new Bundle();
            args.putString("courseId", course.getId()); // 传递课程 ID
            args.putString("courseName",course.getName());
            calendarFragment.setArguments(args);

            transaction.replace(R.id.fragment_container, calendarFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvHours, tvTeacher, tvDescription;
        LinearLayout btnSchedule;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvHours = itemView.findViewById(R.id.tv_hours);
            tvTeacher = itemView.findViewById(R.id.tv_teacher);
            tvDescription = itemView.findViewById(R.id.tv_description);
            btnSchedule = itemView.findViewById(R.id.ll_schedule);
        }
    }
}

