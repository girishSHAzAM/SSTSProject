package com.example.officialproject1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManualRAdapter extends RecyclerView.Adapter<ManualRAdapter.M_Holder> {
    List<String> guidelines;
    Context ctx;
    public ManualRAdapter(Context ctx, List<String> guidelines){
        this.ctx = ctx;
        this.guidelines = guidelines;
    }
    @NonNull
    @Override
    public ManualRAdapter.M_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View rView = inflater.inflate(R.layout.manual_recycler_row,parent,false);
        return new M_Holder(rView);
    }

    @Override
    public void onBindViewHolder(@NonNull ManualRAdapter.M_Holder holder, int position) {
        holder.gT1.setText(guidelines.get(position));
    }

    @Override
    public int getItemCount() {
        return guidelines.size();
    }
    public class M_Holder extends RecyclerView.ViewHolder{
        TextView gT1;
        public M_Holder(@NonNull View itemView) {
            super(itemView);
            gT1 = itemView.findViewById(R.id.app_guideline);
        }
    }
}
