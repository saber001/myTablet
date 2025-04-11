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
import com.example.mytablet.ui.model.DayInfo;
import java.util.List;

public class CourseDetailFragment extends BaseFragment {
    private List<Course> courseList;
    private CourseDetailAdapter mAdapter;
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private static final String ARG_DAY_INFO = "dayInfo";
    private DayInfo dayInfo;

    public static CourseDetailFragment newInstance(DayInfo dayInfo) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DAY_INFO, dayInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            dayInfo = (DayInfo) getArguments().getSerializable(ARG_DAY_INFO);
            courseList = dayInfo.getCourses(); // 原逻辑保持
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_detail, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView); // 获取 TextView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (courseList == null || courseList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            mAdapter = new CourseDetailAdapter(courseList);
            recyclerView.setAdapter(mAdapter);
        }
        return view;
    }
}

