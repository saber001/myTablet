package com.example.mytablet.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.adapter.ClassAdapter;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.model.ClassRoomBean;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.Utils;

import java.util.ArrayList;
import java.util.List;
import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassroomFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private ClassAdapter classAdapter;
    private List<ClassRoomBean> courseList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_classroom, container, false);

        recyclerView = view.findViewById(R.id.course_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        classAdapter = new ClassAdapter(courseList);
        recyclerView.setAdapter(classAdapter);

        // 获取倒计时 TextView 并启动倒计时
        TextView countdownTextView = view.findViewById(R.id.tv_countdown);
        startCountdown(countdownTextView);

        // 监听手动关闭
        LinearLayout btnClose = view.findViewById(R.id.ll_home);
        btnClose.setOnClickListener(v -> navigateToHome());
        loadData();
        return view;
    }

    private void loadData(){
        apiService.getRooms().enqueue(new Callback<Result<ClassRoomBean>>() {
            @Override
            public void onResponse(Call<Result<ClassRoomBean>> call, Response<Result<ClassRoomBean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    courseList.clear();
                    courseList.addAll(response.body().getRows());
                    classAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<Result<ClassRoomBean>> call, Throwable t) {
                Utils.showToast("API Error"+"请求失败: " + t.getMessage());
            }
        });
    }
}

