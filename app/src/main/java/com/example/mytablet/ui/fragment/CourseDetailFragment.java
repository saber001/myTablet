package com.example.mytablet.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.adapter.CourseDetailAdapter;
import com.example.mytablet.ui.model.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailFragment extends BaseFragment {
    private static final String ARG_COURSES = "courses";
    private List<Course> courseList;
    private CourseDetailAdapter mAdapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;

    public static CourseDetailFragment newInstance(List<Course> courses) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_COURSES, new ArrayList<>(courses));  // 确保 List 可序列化
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            courseList = (List<Course>) getArguments().getSerializable(ARG_COURSES);
        }
        if (courseList == null) {
            courseList = new ArrayList<>(); // 防止空指针异常
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView); // 获取 TextView

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (courseList == null || courseList.isEmpty()) {
            courseList = new ArrayList<>(); // 防止 Adapter 为空导致崩溃
            emptyTextView.setVisibility(View.VISIBLE);  // 显示“暂无课程”提示
            recyclerView.setVisibility(View.GONE);      // 隐藏 RecyclerView
        } else {
            emptyTextView.setVisibility(View.GONE);     // 隐藏提示
            recyclerView.setVisibility(View.VISIBLE);   // 显示 RecyclerView
        }

        mAdapter = new CourseDetailAdapter(courseList);
        recyclerView.setAdapter(mAdapter);

        return view;
    }
}

