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
   private TextView userName,level,info;
   private LinearLayout ll_subject;
   private CircleImageView photoUrl;
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
      fetchTeacherDetail(userId);
      userName = view.findViewById(R.id.userName);
      level = view.findViewById(R.id.level);
      info = view.findViewById(R.id.info);
      ll_subject = view.findViewById(R.id.ll_subject);
      photoUrl = view.findViewById(R.id.photourl);
      return view;
   }

   private void fetchTeacherDetail(String userId) {
      apiService.getTeacherDetail(userId).enqueue(new Callback<Result<TeacherIntro>>() {
         @Override
         public void onResponse(Call<Result<TeacherIntro>> call, Response<Result<TeacherIntro>> response) {
            if (response.isSuccessful() && response.body() != null) {
               TeacherIntro teacher = response.body().getData();
               userName.setText(teacher.getUserName());
               level.setText(Utils.getLevelText(teacher.getLevel()));
               info.setText(teacher.getInfo());

               String subjectStr = teacher.getSubject(); // 从数据源获取
               if (subjectStr == null || subjectStr.isEmpty()) return;
               ll_subject.removeAllViews(); // 清除旧的 View
               String[] subjects = subjectStr.split(","); // 按逗号拆分
               for (String subject : subjects) {
                  Button button = new Button(getActivity());
                  LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                          dpToPx(100), dpToPx(40)); // 设置宽高
                  params.setMargins(dpToPx(10), 0, 0, 0); // 设置间距
                  button.setLayoutParams(params);
                  button.setText(subject);
                  button.setTextSize(16);
                  button.setGravity(Gravity.CENTER);
                  button.setTextColor(Color.WHITE);
                  button.setBackgroundColor(Color.parseColor("#EE7802")); // 设置背景色
                  ll_subject.addView(button); // 添加到 LinearLayout
               }
               // 加载头像
               Glide.with(getActivity())
                       .load(teacher.getPhotoUrl())
                       .into(photoUrl);
            }else {
               Utils.showToast("请求失败，错误码：" + response.code());
            }
         }

         @Override
         public void onFailure(Call<Result<TeacherIntro>> call, Throwable t) {
            Utils.showToast("网络请求失败: " + t.getMessage());
         }
      });
   }

   // 工具方法：dp 转 px
   private int dpToPx(int dp) {
      return (int) (dp * getResources().getDisplayMetrics().density);
   }
}

