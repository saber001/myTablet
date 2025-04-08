package com.example.mytablet.ui.adapter;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mytablet.R;
import com.example.mytablet.ui.model.DayInfo;
import java.util.List;
import io.reactivex.annotations.NonNull;


public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<DayInfo> dayList;
    private OnItemClickListener onItemClickListener;
    private int selectedPosition = -1; // 记录选中的位置
    public CalendarAdapter(List<DayInfo> dayList) {
        this.dayList = dayList;
    }
    public interface OnItemClickListener {
        void onItemClick(DayInfo dayInfo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DayInfo dayInfo = dayList.get(position);
        holder.tvDay.setText(String.valueOf(dayInfo.getDay()));
        holder.tvCourseCount.setText(dayInfo.getCourseCount());

        // 设置不同的背景颜色和字体样式
        if (position == selectedPosition) {
            // 选中的日期，字体变成白色，加粗
            holder.tvDay.setTextColor(Color.WHITE);
            holder.tvCourseCount.setTextColor(Color.WHITE);
            holder.tvDay.setTypeface(null, Typeface.BOLD);
            holder.tvCourseCount.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_day);
        } else if ("无课".equals(dayInfo.getCourseCount())) {
            // 无课，白色背景，默认字体
            holder.tvDay.setTextColor(Color.BLACK);
            holder.tvCourseCount.setTextColor(Color.GRAY);
            holder.tvDay.setTypeface(null, Typeface.NORMAL);
            holder.tvCourseCount.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setBackgroundResource(R.drawable.bg_no_course_day);
        } else {
            // 有课，淡橙色背景，字体加粗
            holder.tvDay.setTextColor(Color.BLACK);
            holder.tvCourseCount.setTextColor(Color.BLACK);
            holder.tvDay.setTypeface(null, Typeface.BOLD);
            holder.tvCourseCount.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundResource(R.drawable.bg_has_course_day);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                selectedPosition = position;  // 更新选中状态
                notifyDataSetChanged();
                onItemClickListener.onItemClick(dayInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvCourseCount;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            tvCourseCount = itemView.findViewById(R.id.tv_course_count);
        }
    }
}


