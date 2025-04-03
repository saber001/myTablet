package com.example.mytablet.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytablet.R;
import com.example.mytablet.ui.model.ClassRoomBean;

import java.util.List;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.CourseViewHolder> {

   private List<ClassRoomBean> classList;

   public ClassAdapter(List<ClassRoomBean> classList) {
      this.classList = classList;
   }

   @NonNull
   @Override
   public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class, parent, false);
      return new CourseViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
      ClassRoomBean course = classList.get(position);
      holder.roomName.setText(course.getRoomName());
      holder.area.setText("面积："+course.getArea()+"㎡");
      holder.address.setText(course.getAddress());
   }

   @Override
   public int getItemCount() {
      return classList.size();
   }

   static class CourseViewHolder extends RecyclerView.ViewHolder {
      TextView roomName, area, address, text5;
      public CourseViewHolder(@NonNull View itemView) {
         super(itemView);
         roomName = itemView.findViewById(R.id.room_name);
         area = itemView.findViewById(R.id.area);
         address = itemView.findViewById(R.id.address);
         text5 = itemView.findViewById(R.id.text5);
      }
   }
}

