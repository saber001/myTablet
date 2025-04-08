package com.example.mytablet.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytablet.MainActivity;
import com.example.mytablet.R;
import com.example.mytablet.ui.adapter.ViewCoursesAdapter;
import com.example.mytablet.ui.model.CourseBean;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.SubjectBean;
import com.example.mytablet.ui.model.Utils;
import com.example.mytablet.ui.model.ViewCourses;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewCoursesFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private ViewCoursesAdapter courseAdapter;
    private List<CourseBean> courseList;
    private LinearLayout llSubject;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_courses, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tv_empty);
        llSubject = view.findViewById(R.id.ll_subject);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        courseList = new ArrayList<>();
        courseAdapter = new ViewCoursesAdapter(courseList,getActivity());
        recyclerView.setAdapter(courseAdapter);

        loadSubjects();
        // 获取倒计时 TextView 并启动倒计时
        TextView countdownTextView = view.findViewById(R.id.tv_countdown);
        startCountdown(countdownTextView);

        // 监听手动关闭
        LinearLayout btnClose = view.findViewById(R.id.ll_home);
        btnClose.setOnClickListener(v -> navigateToHome());
        return view;
    }

    private void loadSubjects() {
        apiService.getSubjects().enqueue(new Callback<Result<List<SubjectBean>>>() {
            @Override
            public void onResponse(Call<Result<List<SubjectBean>>> call, Response<Result<List<SubjectBean>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    List<SubjectBean> subjects = response.body().getData(); // 兼容 data/rows
                    if (subjects != null) {
                        addButtons(subjects);
                    } else {
                        Utils.showToast("数据获取失败");
                    }
                } else {
                    Utils.showToast("数据获取失败");
                }
            }

            @Override
            public void onFailure(Call<Result<List<SubjectBean>>> call, Throwable t) {
                Utils.showToast("网络请求失败");
            }
        });
    }
    private Button selectedButton = null; // 记录当前选中的按钮

    private void addButtons(List<SubjectBean> subjects) {
        llSubject.removeAllViews(); // 清除原有按钮
        for (int i = 0; i < subjects.size(); i++) {
            SubjectBean subject = subjects.get(i);
            Button button = new Button(getActivity());
            button.setText(subject.subjectName);

            // 设置 LayoutParams
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(100),  // 宽度 100dp
                    dpToPx(60)    // 高度 60dp
            );
            params.setMargins(dpToPx(20), 0, 0, 0); // 设置左边距 20dp
            button.setLayoutParams(params);

            // 设置默认样式
            button.setBackgroundColor(Color.WHITE); // 默认白色背景
            button.setTextColor(Color.BLACK);

            // 设置默认高亮第一个按钮
            if (i == 0) {
                selectedButton = button;
                button.setBackgroundColor(Color.parseColor("#f3cda6")); // 选中颜色
                button.setTextColor(Color.BLACK);
                loadCourses(subject.subjectId); // 默认加载第一个分类的课程
            }

            // 添加点击事件
            button.setOnClickListener(v -> {
                if (selectedButton != null) {
                    selectedButton.setBackgroundColor(Color.WHITE); // 取消选中状态
                    selectedButton.setTextColor(Color.BLACK);
                }

                button.setBackgroundColor(Color.parseColor("#f3cda6")); // 选中颜色
                button.setTextColor(Color.BLACK);
                selectedButton = button;

                // 加载选中学科的课程
                loadCourses(subject.subjectId);
            });
            llSubject.addView(button);
        }
    }

    // dp 转 px
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // **加载课程列表**
    private void loadCourses(String subjectId) {
        apiService.getCourses(subjectId, 10, 1).enqueue(new Callback<Result<CourseBean>>() {
            @Override
            public void onResponse(Call<Result<CourseBean>> call, Response<Result<CourseBean>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    List<CourseBean> courses = response.body().getRows();  // 从 `rows` 获取数据
                    if (courses != null && !courses.isEmpty()) {
                        tvEmpty.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        courseList.clear();
                        courseList.addAll(courses);
                        courseAdapter.notifyDataSetChanged();
                    } else {
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    Utils.showToast("获取课程失败");
                }
            }
            @Override
            public void onFailure(Call<Result<CourseBean>> call, Throwable t) {
                Utils.showToast("网络请求失败"+t.getMessage());
            }
        });
    }
}
