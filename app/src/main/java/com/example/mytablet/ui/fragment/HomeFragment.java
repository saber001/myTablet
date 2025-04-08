package com.example.mytablet.ui.fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;

import com.example.mytablet.MainActivity;
import com.example.mytablet.R;
import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.ui.adapter.HomeAdapter;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.model.HomeBean;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.Utils;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView courseList;
    private HomeAdapter courseAdapter;
    private ArrayList<HomeBean> homeBeans = new ArrayList<>();
    private ApiService apiService;
    private TextView emptyView;
    // 刷新定时器
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            fetchTodayClasses();
            handler.postDelayed(this, 3 * 60 * 1000); // 3分钟刷新一次
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        apiService = ApiClient.getClient().create(ApiService.class);
        emptyView = view.findViewById(R.id.empty_view);
        courseList = view.findViewById(R.id.course_list);
        courseList.setLayoutManager(new LinearLayoutManager(getContext()));
        courseAdapter = new HomeAdapter(homeBeans, this::loadCourseDetailFragment);
        courseList.setAdapter(courseAdapter);

        fetchTodayClasses(); // 初次加载数据
        return view;
    }

    // 启动定时刷新
    private void startAutoRefresh() {
        Log.d("HomeFragment", "启动自动刷新");
        handler.removeCallbacks(refreshRunnable);
        handler.post(refreshRunnable); // 立即执行一次
    }

    // 停止定时刷新
    private void stopAutoRefresh() {
        Log.d("HomeFragment", "停止自动刷新");
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoRefresh();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoRefresh();
    }

    public void fetchTodayClasses() {
        apiService.getTodayClasses().enqueue(new Callback<Result<List<HomeBean>>>() {
            @Override
            public void onResponse(Call<Result<List<HomeBean>>> call, Response<Result<List<HomeBean>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<HomeBean>> result = response.body();
                    result.setData(new ArrayList<>());
                    if (result.getCode() == 200 && result.getData() != null) {
                        homeBeans.clear();
                        homeBeans.addAll(result.getData());
                        courseAdapter.notifyDataSetChanged();

                        boolean hasData = !homeBeans.isEmpty();
                        courseList.setVisibility(hasData ? View.VISIBLE : View.GONE);
                        emptyView.setVisibility(hasData ? View.GONE : View.VISIBLE);

                        Log.d("fetchTodayClasses", "课程数据加载成功，共 " + homeBeans.size() + " 条");

                        if (!homeBeans.isEmpty()) {
                            loadCourseDetailFragment(homeBeans.get(0));
                        } else {
                            loadStaticCourseDetailFragment();
                        }
                    } else {
                        Utils.showToast("ApiError 请求失败，错误码: " + result.getCode());
                    }
                } else {
                    Utils.showToast("ApiError 请求失败，HTTP 状态码: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Result<List<HomeBean>>> call, Throwable t) {
                Utils.showToast("ApiError 网络请求失败: " + t.getMessage());
            }
        });
    }

    private void loadCourseDetailFragment(HomeBean course) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, HomeDetailFragment.newInstance(course.getCourseId()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void loadStaticCourseDetailFragment() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, HomeDetailFragment.newStaticInstance());
        transaction.commit();
    }
}



