package com.example.mytablet.ui.fragment;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytablet.R;
import com.example.mytablet.ui.adapter.CalendarAdapter;

import com.example.mytablet.ui.model.Course;
import com.example.mytablet.ui.model.DayInfo;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.Utils;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class CalendarFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private CalendarAdapter adapter;
    private List<DayInfo> dayList = new ArrayList<>();
    private String courseId,courseName,currentMonth;
    private TextView tv_name,tv_date;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        // 获取传递的课程 ID
        if (getArguments() != null) {
            courseId = getArguments().getString("courseId");
            courseName = getArguments().getString("courseName");
        }
        tv_name = view.findViewById(R.id.tv_name);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        currentMonth = sdf.format(new Date());  // 获取当前月份
        tv_date = view.findViewById(R.id.tv_date);
        tv_date.setText(currentMonth);

        StringBuilder verticalText = new StringBuilder();
        for (char c : courseName.toCharArray()) {
            verticalText.append(c).append("\n"); // 每个字后面加换行
        }
        tv_name.setText(verticalText.toString().trim());

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));

        adapter = new CalendarAdapter(dayList);
        recyclerView.setAdapter(adapter);

        // 监听日历点击事件
        adapter.setOnItemClickListener(dayInfo -> {
            if ("无课".equals(dayInfo.getCourseCount())) {
                dayInfo.setToday(true);        // 当前日期标记（可选）
                dayInfo.setEmptyDay(true);     // 标记为无课
            } else {
                dayInfo.setEmptyDay(false);    // 清除无课标记
            }
            loadCourseDetailFragment(dayInfo);
        });
        // 获取倒计时 TextView 并启动倒计时
        TextView countdownTextView = view.findViewById(R.id.tv_countdown);
        startCountdown(countdownTextView);
        // 监听手动关闭
        LinearLayout btnClose = view.findViewById(R.id.ll_home);
        btnClose.setOnClickListener(v -> navigateToHome());
        loadCalendarData();
        return view;
    }

    private void loadCalendarData() {
        //1900375243529134082
        apiService.getCourseCalendar(courseId, currentMonth).enqueue(new Callback<Result<Map<String, List<Course>>>>() {
            @Override
            public void onResponse(Call<Result<Map<String, List<Course>>>> call, Response<Result<Map<String, List<Course>>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<Map<String, List<Course>>> result = response.body();
                    if (result.getCode() == 200 && result.getData() != null) {
                        updateCalendar(result.getData());
                    } else {
                        Utils.showToast("错误代码: " + result.getCode() + ", 错误信息: " + result.getMsg());
                    }
                } else {
                    Utils.showToast("请求失败: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Result<Map<String, List<Course>>>> call, Throwable t) {
                Utils.showToast("网络请求失败: " + t.getMessage());
            }
        });
    }
    private void updateCalendar(Map<String, List<Course>> courseData) {
        dayList.clear();
        LocalDate today = null;
        String todayStr = null;
        int year, month, daysInMonth;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            today = LocalDate.now();
            todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            year = today.getYear();
            month = today.getMonthValue();
            daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        } else {
            return;
        }

        int todayIndex = -1;
        DayInfo todayInfo = null;
        for (int i = 1; i <= daysInMonth; i++) {
            String date = String.format("%04d-%02d-%02d", year, month, i);
            List<Course> courses = courseData.getOrDefault(date, new ArrayList<>());

            String courseCount = courses.isEmpty() ? "无课" : (courses.size() + "节");
            DayInfo dayInfo = new DayInfo(i, courseCount, courses);
            dayList.add(dayInfo);
            if (date.equals(todayStr)) {
                todayIndex = i - 1;
                todayInfo = dayInfo;
            }
        }
        adapter.notifyDataSetChanged();
        if (todayIndex != -1) {
            adapter.setSelectedPosition(todayIndex);
            recyclerView.scrollToPosition(todayIndex);
        }
        if (todayInfo == null) {
            todayInfo = new DayInfo(today.getDayOfMonth(), "无课", new ArrayList<>());
            todayInfo.setToday(true);
            todayInfo.setEmptyDay(true); // 标记为“默认无课”
        }
        loadCourseDetailFragment(todayInfo);
    }

    private void loadCourseDetailFragment(DayInfo dayInfo) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, CourseDetailFragment.newInstance(dayInfo))
                    .commit();
        }
    }
}


