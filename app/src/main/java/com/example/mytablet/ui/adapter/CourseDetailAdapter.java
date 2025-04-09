package com.example.mytablet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.model.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailAdapter extends RecyclerView.Adapter<CourseDetailAdapter.ViewHolder> {
    private final List<Course> courseList;

    public CourseDetailAdapter(List<Course> courseList) {
        this.courseList = (courseList != null) ? courseList : new ArrayList<>(); // 防止空指针
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courseList.get(position);
        holder.clazzName.setText("课时名称：" + course.getClazzName());
        holder.clazzDate.setText("上课时间：" + course.getClazzBegin() + " - " + course.getClazzEnd());
        holder.teacherName.setText("上课教师：" + course.getTeacherName());
        holder.roomName.setText("上课教室：" + course.getRoomName());
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView clazzName, clazzDate, teacherName, roomName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clazzName = itemView.findViewById(R.id.clazzName);
            clazzDate = itemView.findViewById(R.id.clazzDate);
            teacherName = itemView.findViewById(R.id.teacherName);
            roomName = itemView.findViewById(R.id.roomName);
        }
    }
}

