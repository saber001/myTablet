package com.example.mytablet.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.example.mytablet.R;
import com.example.mytablet.ui.model.AnnouncementView;
import com.example.mytablet.ui.model.HomeDetail;
import com.example.mytablet.ui.model.Notice;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.Utils;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeDetailFragment extends BaseFragment {
    private static final String ARG_COURSE_ID = "course_id";
    private AnnouncementView announcementView;
    private String courseId;
    private TextView course_title,course_time,course_duration,course_description,
            teacher_name,teacher_level,teacher_info,quota,regCnt;
    private CircleImageView teacher_image;

    public static HomeDetailFragment newInstance(String courseId) {
        HomeDetailFragment fragment = new HomeDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COURSE_ID, courseId);
        args.putBoolean("is_static", false); // 默认加载网络数据
        fragment.setArguments(args);
        return fragment;
    }

    public static HomeDetailFragment newStaticInstance() {
        HomeDetailFragment fragment = new HomeDetailFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_static", true); // 静态模式，不加载接口
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_detail, container, false);

        course_title = view.findViewById(R.id.course_title);
        course_time = view.findViewById(R.id.course_time);
        course_duration = view.findViewById(R.id.course_duration);
        course_description = view.findViewById(R.id.course_description);
        teacher_name = view.findViewById(R.id.teacher_name);
        teacher_level = view.findViewById(R.id.teacher_level);
        teacher_info = view.findViewById(R.id.teacher_info);
        teacher_image = view.findViewById(R.id.teacher_image);
        quota = view.findViewById(R.id.quota);
        regCnt = view.findViewById(R.id.reg_cnt);
        announcementView = view.findViewById(R.id.announce);

        if (getArguments() != null) {
            courseId = getArguments().getString(ARG_COURSE_ID);
            boolean isStatic = getArguments().getBoolean("is_static", false);
            if (!isStatic && courseId != null) {
                loadCourseDetail(courseId);
                loadNotice();
            }
        }
        return view;
    }

    private void loadCourseDetail(String courseId) {
        apiService.getCourseDetail(courseId).enqueue(new Callback<Result<HomeDetail>>() {
            @Override
            public void onResponse(Call<Result<HomeDetail>> call, Response<Result<HomeDetail>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Result<HomeDetail> result = response.body();
                    if (result.getCode() == 200) {
                        HomeDetail data = result.getData();
                        updateUI(data);
                    }
                } else {
                    Utils.showToast("CourseDetail"+ "请求失败：" + response.message());
                }
            }

            @Override
            public void onFailure(Call<Result<HomeDetail>> call, Throwable t) {
                Utils.showToast("CourseDetail"+ "网络请求失败"+t.getMessage());
            }
        });
    }

    private void updateUI(HomeDetail homeDetail){
        course_title.setText("《"+homeDetail.getCourse().getName()+"》");
        quota.setText("共"+homeDetail.getCourse().getQuota()+"课");
        regCnt.setText("第"+homeDetail.getCourse().getClassFinishCnt()+"课");
        course_time.setText(Utils.formatTimeRange(homeDetail.getCourse().getRegStartTime(),homeDetail.getCourse().getRegEndTime()));
        course_duration.setText("课程时长："+60+"分钟");
        course_description.setText(homeDetail.getCourse().getInfo());
        teacher_name.setText("上课教师："+homeDetail.getUser().getUserName());
        teacher_level.setText("教师级别："+ Utils.getLevelText(homeDetail.getUser().getLevel()));
        teacher_info.setText(homeDetail.getUser().getInfo());
        Glide.with(getActivity())
                .load(homeDetail.getUser().getPhotoUrl())
                .into(teacher_image);

    }

    /**
     * 设置公告
     */
    private void loadNotice() {
        apiService.getLatestNotices().enqueue(new Callback<Result<List<Notice>>>() {
            @Override
            public void onResponse(Call<Result<List<Notice>>> call, Response<Result<List<Notice>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notice> notices = response.body().getData();
                    if (notices != null) {
                        announcementView.setNotices(notices);
                    }
                }
            }
            @Override
            public void onFailure(Call<Result<List<Notice>>> call, Throwable t) {
                Utils.showToast("公告请求"+ "网络请求失败"+t.getMessage());
            }
        });
    }
}
