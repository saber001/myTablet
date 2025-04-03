package com.example.mytablet.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        apiService = ApiClient.getClient().create(ApiService.class);

        courseList = view.findViewById(R.id.course_list);
        courseList.setLayoutManager(new LinearLayoutManager(getContext()));
        // 添加测试数据
        courseAdapter = new HomeAdapter(homeBeans, this::loadCourseDetailFragment);
        courseList.setAdapter(courseAdapter);
        fetchTodayClasses();
        return view;
    }
    public void fetchTodayClasses() {
        apiService.getTodayClasses().enqueue(new Callback<Result<List<HomeBean>>>() {
            @Override
            public void onResponse(Call<Result<List<HomeBean>>> call, Response<Result<List<HomeBean>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<List<HomeBean>> result = response.body();
                    if (result.getCode() == 200 && result.getData() != null) {
                        homeBeans.clear(); // 先清空列表
                        homeBeans.addAll(result.getData()); // 添加新数据
                        courseAdapter.notifyDataSetChanged(); // 通知 Adapter 更新 UI
                        Log.d("fetchTodayClasses", "课程数据加载成功，共 " + homeBeans.size() + " 条");
                        // 默认加载第一条数据
                        if (!homeBeans.isEmpty()) {
                            loadCourseDetailFragment(homeBeans.get(0));
                        }
                    } else {
                        Utils.showToast("ApiError"+ "请求失败，错误码: " + result.getCode());
                    }
                } else {
                    Utils.showToast("ApiError"+"请求失败，HTTP 状态码: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Result<List<HomeBean>>> call, Throwable t) {
                Utils.showToast("ApiError"+ "网络请求失败: " + t.getMessage());
            }
        });
    }

    private void loadCourseDetailFragment(HomeBean course) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, HomeDetailFragment.newInstance(course.getCourseId())); // 修正括号错误
        transaction.addToBackStack(null);
        transaction.commit();
    }
}



