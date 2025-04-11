package com.example.mytablet.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.mytablet.R;
import com.example.mytablet.ui.model.TeacherIntro;
import com.example.mytablet.ui.model.Utils;
import java.util.List;
import android.graphics.Typeface;
import android.widget.ImageView;
import de.hdodenhof.circleimageview.CircleImageView;

public class TeacherIntroAdapter extends RecyclerView.Adapter<TeacherIntroAdapter.PersonViewHolder> {

    private List<TeacherIntro> personList;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(TeacherIntro person);
    }

    public TeacherIntroAdapter(List<TeacherIntro> personList, OnItemClickListener listener) {
        this.personList = personList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_teacher_intro, parent, false);
        return new PersonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder holder, int position) {
        TeacherIntro person = personList.get(position);
        holder.tvName.setText(person.getUserName());
        holder.tvSubject.setText(Utils.getLevelText(person.getLevel()));
        holder.tvDetail.setText("详情");

        Glide.with(holder.itemView.getContext())
                .load(person.getPhotoUrl())
                .into(holder.circleImageView);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(0xFFE0E0E0);
            holder.tvName.setTypeface(null, Typeface.BOLD);
            holder.tvSubject.setTypeface(null, Typeface.BOLD);
            holder.tvDetail.setTypeface(null, Typeface.BOLD);
            holder.arrowIcon.setImageResource(R.mipmap.ic_orange);
        } else {
            holder.itemView.setBackgroundColor(0xFFFFFFFF);
            holder.tvName.setTypeface(null, Typeface.NORMAL);
            holder.tvSubject.setTypeface(null, Typeface.NORMAL);
            holder.tvDetail.setTypeface(null, Typeface.NORMAL);
            holder.arrowIcon.setImageResource(R.mipmap.ic_white_right);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int previous = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(previous);
                notifyItemChanged(position);
                listener.onItemClick(person);
            }
        });
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    static class PersonViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSubject, tvDetail;
        ImageView arrowIcon;
        CircleImageView circleImageView;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvSubject = itemView.findViewById(R.id.tv_subject);
            tvDetail = itemView.findViewById(R.id.tv_detail);
            circleImageView = itemView.findViewById(R.id.img_header);
            arrowIcon = itemView.findViewById(R.id.iv_arrow);
        }
    }

    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previous);
        notifyItemChanged(position);
    }
}

