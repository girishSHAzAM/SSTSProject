package com.example.officialproject1;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class R_Adapter extends RecyclerView.Adapter<R_Adapter.R_Holder> {
    List<String>startDate,endDate,origin,destination,tripStatus,score;
    Context ct;
    public R_Adapter(Context context,List<String> startDate,List<String> endDate,List<String> origin,List<String> destination,List<String> tripStatus,List<String> score){
        ct = context;
        this.startDate = startDate;
        this.endDate = endDate;
        this.origin = origin;
        this.destination = destination;
        this.score = score;
        this.tripStatus = tripStatus;
    }
    @Override
    public R_Holder onCreateViewHolder( ViewGroup viewGroup, int i) {
        LayoutInflater inflator = LayoutInflater.from(ct);
        View rView = inflator.inflate(R.layout.recycler_row,viewGroup,false);
        return new R_Holder(rView);
    }

    @Override
    public void onBindViewHolder(R_Holder r_holder, int i) {
        r_holder.t1.setText(startDate.get(i));
        r_holder.t2.setText(endDate.get(i));
        r_holder.t3.setText(origin.get(i));
        r_holder.t4.setText(destination.get(i));
        r_holder.t5.setText(score.get(i));
        r_holder.t6.setText(tripStatus.get(i));
    }

    @Override
    public int getItemCount() {
        return startDate.size();
    }

    public class R_Holder extends RecyclerView.ViewHolder{
        TextView t1,t2,t3,t4,t5,t6;
        public R_Holder(View view){
            super(view);
            t1 = view.findViewById(R.id.startDate);
            t2 = view.findViewById(R.id.endDate);
            t3 = view.findViewById(R.id.origin);
            t4 = view.findViewById(R.id.dest);
            t5 = view.findViewById(R.id.score);
            t6 = view.findViewById(R.id.tripStatus);
        }
    }
}
