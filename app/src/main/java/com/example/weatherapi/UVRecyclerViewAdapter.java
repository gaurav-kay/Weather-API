package com.example.weatherapi;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.toolbox.StringRequest;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        if (uvValue.getValue() >= 10D) {
            uvViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#efc4ff"));
        } else if (uvValue.getValue() >= 9D) {
            uvViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#ff9696"));
        } else if (uvValue.getValue() >= 8D) {
            uvViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#ffc489"));
        } else if (uvValue.getValue() >= 7D) {
            uvViewHolder.cardView.setCardBackgroundColor(Color.parseColor("#fdff89"));
        }

        Timestamp timestamp = new Timestamp(uvValue.getDate() * 1000L);
        Date date = new Date(timestamp.getTime());

        uvViewHolder.uvIndex.setText(new StringBuilder("UV Index: " + uvValue.getValue()));
        uvViewHolder.date.setText(new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH).format(date));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class UVViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private CardView cardView;
        private TextView uvIndex, date;

        public UVViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardView);
            imageView = itemView.findViewById(R.id.imageView);
            uvIndex = itemView.findViewById(R.id.uvIndex);
            date = itemView.findViewById(R.id.date);
        }
    }
}
