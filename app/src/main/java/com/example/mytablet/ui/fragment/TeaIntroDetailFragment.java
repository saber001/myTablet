package com.example.mytablet.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mytablet.R;
import com.example.mytablet.ui.api.ApiClient;
import com.example.mytablet.ui.api.ApiService;
import com.example.mytablet.ui.model.Result;
import com.example.mytablet.ui.model.TeacherIntro;
import com.example.mytablet.ui.model.Utils;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;
import static com.google.android.material.internal.ViewUtils.dpToPx;

public class TeaIntroDetailFragment extends BaseFragment {
   private static final String ARG_USER_ID = "userId";
   private String userId;
   private TextView userName, level, info;
   private LinearLayout ll_subject;
   private CircleImageView photoUrl;
   private Call<Result<TeacherIntro>> apiCall;

   public static TeaIntroDetailFragment newInstance(String userId) {
      TeaIntroDetailFragment fragment = new TeaIntroDetailFragment();
      Bundle args = new Bundle();
      args.putString(ARG_USER_ID, userId);
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (getArguments() != null) {
         userId = getArguments().getString(ARG_USER_ID);
      }
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_detail_teaintro, container, false);
      // 需要保证 fragment 添加到视图后再进行数据请求
      fetchTeacherDetail(userId);

      userName = view.findViewById(R.id.userName);
      level = view.findViewById(R.id.level);
      info = view.findViewById(R.id.info);
      ll_subject = view.findViewById(R.id.ll_subject);
      photoUrl = view.findViewById(R.id.photourl);

      return view;
   }

   private void fetchTeacherDetail(String userId) {
      // 确保 Fragment 被添加
      if (!isAdded()) return;

      apiCall = apiService.getTeacherDetail(userId);
      apiCall.enqueue(new Callback<Result<TeacherIntro>>() {
         @Override
         public void onResponse(Call<Result<TeacherIntro>> call, Response<Result<TeacherIntro>> response) {
            if (isAdded() && response.isSuccessful() && response.body() != null) {
               TeacherIntro teacher = response.body().getData();
               // 确保 UI 更新在主线程
               if (getActivity() != null) {
                  getActivity().runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                        updateUI(teacher);
                     }
                  });
               }
            } else {
               Utils.showToast("请求失败，错误码：" + response.code());
            }
         }

         @Override
         public void onFailure(Call<Result<TeacherIntro>> call, Throwable t) {
            if (isAdded() && getActivity() != null) {
               getActivity().runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                     Utils.showToast("网络请求失败: " + t.getMessage());
                  }
               });
            }
         }
      });
   }

   private void updateUI(TeacherIntro teacher) {
      if (getActivity() == null || !isAdded()) return;

      userName.setText(teacher.getUserName());
      level.setText(Utils.getLevelText(teacher.getLevel()));
      info.setText(teacher.getInfo());

      String subjectStr = teacher.getSubject();
      if (subjectStr != null && !subjectStr.isEmpty()) {
         ll_subject.removeAllViews();
         String[] subjects = subjectStr.split(",");
         for (String subject : subjects) {
            Button button = new Button(getActivity());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(110), dpToPx(50));
            params.setMargins(dpToPx(10), 0, 0, 0);
            button.setLayoutParams(params);
            button.setText(subject);
            button.setTextSize(20);
            button.setGravity(Gravity.CENTER);
            button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.parseColor("#EE7802"));
            ll_subject.addView(button);
         }
      }

      // 加载头像
      Glide.with(getActivity())
              .load(teacher.getPhotoUrl())
              .error(R.mipmap.ic_big_header)  // 加载失败时显示默认头像
              .into(photoUrl);
   }

   private int dpToPx(int dp) {
      return (int) (dp * getResources().getDisplayMetrics().density);
   }

   @Override
   public void onStop() {
      super.onStop();
      // 取消请求以避免内存泄漏或空指针异常
      if (apiCall != null && !apiCall.isCanceled()) {
         apiCall.cancel();
      }
   }
}

