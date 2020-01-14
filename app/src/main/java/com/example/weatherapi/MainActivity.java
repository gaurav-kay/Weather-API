package com.example.weatherapi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";

    private double latitude, longitude;
    private Date nextCallEndDate;

    private ArrayList<UVValue> data = new ArrayList<>();

    private RecyclerView recyclerView;
    private UVRecyclerViewAdapter uvRecyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        progressBar.setVisibility(View.VISIBLE);
        requestQueue = Volley.newRequestQueue(this);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getLocation();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Log.d(TAG, "onPermissionDenied: TOO BAD");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (linearLayoutManager.findLastVisibleItemPosition() == data.size() - 1) {
                    Date startDate = getStartDate(nextCallEndDate);
                    sendRequestFrom(startDate, nextCallEndDate);
                    updateNextCallEndDate(startDate);
                }
            }
        });
    }

    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();

                            Log.d(TAG, "onSuccess: got location" + location);

                            gotLocation();
                        }
                    }
                });
    }

    private void gotLocation() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                nextCallEndDate = new Date();

                data.clear();

                Date endDate = new Date();  // 14th
                Date startDate = getStartDate(endDate);  // 9th

                sendRequestFrom(startDate, endDate);

                updateNextCallEndDate(startDate);
            }
        });

        Date endDate = new Date();  // 14th
        Date startDate = getStartDate(endDate);  // 9th

        sendRequestFrom(startDate, endDate);

        updateNextCallEndDate(startDate);

        uvRecyclerViewAdapter = new UVRecyclerViewAdapter(data, this);
        recyclerView.setAdapter(uvRecyclerViewAdapter);
    }

    private void updateNextCallEndDate(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.DATE, -1);
        nextCallEndDate = calendar.getTime();
    }

    private Date getStartDate(Date endDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DATE, -5);
        return calendar.getTime();
    }

    private void sendRequestFrom(Date startDate, Date endDate) {
        progressBar.setVisibility(View.VISIBLE);

        String URL = "https://api.openweathermap.org/data/2.5/uvi/history?appid=d1f77cf0e8fa40b4f42c1e9dc0685eb2" +
                "&lat=" + latitude +
                "&lon=" + longitude +
                "&start=" + (startDate.getTime() / 1000L) +
                "&end=" + (endDate.getTime() / 1000L);

        StringRequest request = new StringRequest(StringRequest.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                List<UVValue> pageData = Arrays.asList(gson.fromJson(response, UVValue[].class));
                gotData(pageData);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
            }
        });

        requestQueue.add(request);
    }

    private void gotData(List<UVValue> pageData) {
        Collections.reverse(pageData);
        data.addAll(pageData);

        Log.d(TAG, "gotData: " + data.size() + data);

        uvRecyclerViewAdapter.notifyDataSetChanged();
        // uvRecyclerViewAdapter.notifyItemRangeInserted(data.size() - pageData.size(), pageData.size());
    }
}
