package com.example.mytablet.ui.model;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.mytablet.R;

import io.reactivex.annotations.NonNull;

public class SignInDialog extends Dialog {

   public SignInDialog(@NonNull Context context, String message) {
      super(context);
      init(context, message);
   }

   private void init(Context context, String message) {
      View view = LayoutInflater.from(context).inflate(R.layout.dialog_signin, null);
      setContentView(view);
      setContentView(view);

// 设置宽高：让 dialog 显示为 300dp × 150dp
      Window window = getWindow();
      if (window != null) {
         WindowManager.LayoutParams params = window.getAttributes();
         params.width = (int) TypedValue.applyDimension(
                 TypedValue.COMPLEX_UNIT_DIP, 300, context.getResources().getDisplayMetrics());
         params.height = (int) TypedValue.applyDimension(
                 TypedValue.COMPLEX_UNIT_DIP, 150, context.getResources().getDisplayMetrics());
         window.setAttributes(params);
         window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      }

      TextView tvMessage = view.findViewById(R.id.tv_message);
      ImageView ivClose = view.findViewById(R.id.iv_close);
      tvMessage.setText(message);
      // 手动点击关闭
      ivClose.setOnClickListener(v -> dismiss());
      // 自动关闭 3 秒
      new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 3000);
      // 设置 Dialog 属性
      setCancelable(true); // 可取消
      setCanceledOnTouchOutside(true); // 点击外部取消
      getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
   }
}

