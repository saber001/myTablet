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
    private int selectedPosition = -1;

    public CalendarAdapter(List<DayInfo> dayList) {
        this.dayList = dayList;
    }

    public interface OnItemClickListener {
        void onItemClick(DayInfo dayInfo);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public void setSelectedDate(int day) {
        for (int i = 0; i < dayList.size(); i++) {
            if (dayList.get(i).getDay() == day) {
                selectedPosition = i;
                notifyDataSetChanged();
                break;
            }
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
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

        if (position == selectedPosition) {
            holder.tvDay.setTextColor(Color.WHITE);
            holder.tvCourseCount.setTextColor(Color.WHITE);
            holder.tvDay.setTypeface(null, Typeface.BOLD);
            holder.tvCourseCount.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundResource(R.drawable.bg_selected_day);
        } else if ("无课".equals(dayInfo.getCourseCount())) {
            holder.tvDay.setTextColor(Color.BLACK);
            holder.tvCourseCount.setTextColor(Color.GRAY);
            holder.tvDay.setTypeface(null, Typeface.NORMAL);
            holder.tvCourseCount.setTypeface(null, Typeface.NORMAL);
            holder.itemView.setBackgroundResource(R.drawable.bg_no_course_day);
        } else {
            holder.tvDay.setTextColor(Color.BLACK);
            holder.tvCourseCount.setTextColor(Color.BLACK);
            holder.tvDay.setTypeface(null, Typeface.BOLD);
            holder.tvCourseCount.setTypeface(null, Typeface.BOLD);
            holder.itemView.setBackgroundResource(R.drawable.bg_has_course_day);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                selectedPosition = position;
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



