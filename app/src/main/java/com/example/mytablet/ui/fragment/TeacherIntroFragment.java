package com.example.mytablet.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.adapter.TeacherIntroAdapter;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.TeacherIntro;
import com.example.mytablet.ui.model.Utils;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeacherIntroFragment extends BaseFragment implements TeacherIntroAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private TeacherIntroAdapter adapter;
    private List<TeacherIntro> personList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_teacher_intro, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        personList = new ArrayList<>();
        adapter = new TeacherIntroAdapter(personList, this);
        recyclerView.setAdapter(adapter);

        // 发起网络请求
        loadTeachers();
        // 获取倒计时 TextView 并启动倒计时
        TextView countdownTextView = view.findViewById(R.id.tv_countdown);
        startCountdown(countdownTextView);

        // 监听手动关闭
        LinearLayout btnClose = view.findViewById(R.id.ll_home);
        btnClose.setOnClickListener(v -> navigateToHome());
        return view;
    }

    /**
     * 进行网络请求，获取教师介绍数据
     */
    private void loadTeachers() {
        apiService.getTeachers().enqueue(new Callback<Result<TeacherIntro>>() {
            @Override
            public void onResponse(Call<Result<TeacherIntro>> call, Response<Result<TeacherIntro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    personList.clear();
                    personList.addAll(response.body().getRows());
                    adapter.notifyDataSetChanged();
                    // ✅ 默认选中第一条数据
                    if (!personList.isEmpty()) {
                        TeacherIntro firstTeacher = personList.get(0);
                        showDetail(firstTeacher.getUserId()); // 传入 userId
                        adapter.setSelectedPosition(0); // 选中第一条
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<TeacherIntro>> call, Throwable t) {
                Utils.showToast("网络请求失败"+t.getMessage());
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onItemClick(TeacherIntro person) {
        showDetail(person.getUserId()); // 传入 userId
        adapter.setSelectedPosition(personList.indexOf(person)); // 更新选中状态
    }

    private void showDetail(String userId) {
        TeaIntroDetailFragment fragment = TeaIntroDetailFragment.newInstance(userId);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.detail_container, fragment);
        transaction.commit();
    }

}

