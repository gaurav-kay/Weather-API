package com.example.weatherapi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class UVRecyclerViewAdapter extends RecyclerView.Adapter<UVRecyclerViewAdapter.UVViewHolder> {

    private ArrayList<UVValue> data;
    private Context context;

    public UVRecyclerViewAdapter(ArrayList<UVValue> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public UVViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_layout, parent, false);

        return new UVViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UVViewHolder uvViewHolder, int position) {
        UVValue uvValue = data.get(position);

        uvViewHolder.uvIndex.setText(String.valueOf(uvValue.getValue()));
        uvViewHolder.date.setText(uvValue.getDateIso());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class UVViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView uvIndex, date;

        public UVViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            uvIndex = itemView.findViewById(R.id.uvIndex);
            date = itemView.findViewById(R.id.date);
        }
    }
}
