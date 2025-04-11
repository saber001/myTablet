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
    private long lastClickTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teacher_intro, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        personList = new ArrayList<>();
        adapter = new TeacherIntroAdapter(personList, this);
        recyclerView.setAdapter(adapter);

        loadTeachers();

        TextView countdownTextView = view.findViewById(R.id.tv_countdown);
        startCountdown(countdownTextView);

        LinearLayout btnClose = view.findViewById(R.id.ll_home);
        btnClose.setOnClickListener(v -> navigateToHome());

        return view;
    }

    private void loadTeachers() {
        apiService.getTeachers().enqueue(new Callback<Result<TeacherIntro>>() {
            @Override
            public void onResponse(Call<Result<TeacherIntro>> call, Response<Result<TeacherIntro>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    personList.clear();
                    personList.addAll(response.body().getRows());
                    adapter.notifyDataSetChanged();
                    if (!personList.isEmpty()) {
                        TeacherIntro firstTeacher = personList.get(0);
                        showDetail(firstTeacher.getUserId());
                        adapter.setSelectedPosition(0);
                    }
                }
            }

            @Override
            public void onFailure(Call<Result<TeacherIntro>> call, Throwable t) {
                Utils.showToast("网络请求失败" + t.getMessage());
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onItemClick(TeacherIntro person) {
        if (isFastClick()) return;
        showDetail(person.getUserId());
        adapter.setSelectedPosition(personList.indexOf(person));
    }

    private boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < 500) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }

    private void showDetail(String userId) {
        TeaIntroDetailFragment fragment = TeaIntroDetailFragment.newInstance(userId);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.detail_container, fragment);
        transaction.commitAllowingStateLoss();
    }
}

