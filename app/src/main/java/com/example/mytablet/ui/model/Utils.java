package com.example.mytablet.ui.model;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
   private static Context appContext;

   // 初始化方法，在 Application 类中调用
   public static void init(Context context) {
      appContext = context.getApplicationContext();
   }
   // 显示 Toast
   public static void showToast(String message) {
      if (appContext != null) {
         Toast toast = new Toast(appContext);

         // 自定义 Toast 布局
         TextView textView = new TextView(appContext);
         textView.setText(message);
         textView.setTextSize(24); // 字体大一些
         textView.setTextColor(Color.WHITE); // 字体颜色
         textView.setBackgroundColor(Color.parseColor("#CC333333")); // 背景颜色带点透明
         textView.setPadding(32, 16, 32, 16); // 内边距
         textView.setGravity(Gravity.CENTER);

         // 设置圆角背景（需要 API >= 16）
         GradientDrawable bg = new GradientDrawable();
         bg.setColor(Color.parseColor("#CC333333")); // 背景色
         bg.setCornerRadius(24); // 圆角
         textView.setBackground(bg);

         toast.setView(textView);
         toast.setDuration(Toast.LENGTH_LONG);
         // 计算 20dp 的像素值
         int yOffset = (int) TypedValue.applyDimension(
                 TypedValue.COMPLEX_UNIT_DIP,
                 20,
                 appContext.getResources().getDisplayMetrics()
         );
         toast.setGravity(Gravity.BOTTOM, 0, yOffset); // 居中显示
         toast.show();
      } else {
         throw new IllegalStateException("Utils is not initialized. Call Utils.init(context) in Application class.");
      }
   }

   public static String getLevelText(String level) {
      if (level == null || level.isEmpty()) {
         return "未知";  // 避免 null 引发异常
      }
      switch (level) {
         case "1":
            return "普通";
         case "2":
            return "高级";
         case "3":
            return "特级";
         default:
            return "未知";
      }
   }

   public static String convertToPinyin(String chinese) {
      HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
      format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写
      format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 无声调

      StringBuilder pinyin = new StringBuilder();
      for (char c : chinese.toCharArray()) {
         try {
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (pinyinArray != null) {
               String wordPinyin = pinyinArray[0]; // 获取拼音
               wordPinyin = wordPinyin.substring(0, 1).toUpperCase() + wordPinyin.substring(1); // 首字母大写
               pinyin.append(wordPinyin);
            } else {
               pinyin.append(c); // 非汉字直接添加
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
      return pinyin.toString();
   }

   public static String formatTimeRange(String startTime, String endTime) {
      try {
         SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm-HH:mm");

         Date startDate = inputFormat.parse(startTime);
         Date endDate = inputFormat.parse(endTime);

         String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(startDate);
         String startHourMinute = new SimpleDateFormat("HH:mm").format(startDate);
         String endHourMinute = new SimpleDateFormat("HH:mm").format(endDate);

         return formattedDate + " " + startHourMinute + "-" + endHourMinute;
      } catch (ParseException e) {
         e.printStackTrace();
         return "";
      }
   }
}

