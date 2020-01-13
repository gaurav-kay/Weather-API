package com.example.weatherapi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.location.Location;
import android.media.tv.TvContract;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";

    private double latitude, longitude;
    private Date dataTillDate;

    private ArrayList<UVValue> data = new ArrayList<>();

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Log.d(TAG, "onCreate: OK JIJI");

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
        Date endDate = new Date();

        sendRequestFrom(endDate);
    }

    private void sendRequestFrom(final Date endDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(endDate);
        calendar.add(Calendar.DATE, -5);
        Date startDate = calendar.getTime();

        dataTillDate = startDate;

        String URL = "https://api.openweathermap.org/data/2.5/uvi/history?appid=d1f77cf0e8fa40b4f42c1e9dc0685eb2" +
                "&lat=" + latitude +
                "&lon=" + longitude +
                "&start=" + (startDate.getTime() / 1000L) +
                "&end=" + (endDate.getTime() / 1000L);

        Log.d(TAG, "sendRequestFrom: " + (startDate.getTime() / 1000L) + "end" + (endDate.getTime() / 1000L));

        // TODO: Sort out data retrieval

        StringRequest request = new StringRequest(StringRequest.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);

                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                UVValue[] pageData = gson.fromJson(response, UVValue[].class);

                for(UVValue d : pageData) {
                    Log.d(TAG, "for " + endDate + " w value " + d.getDate() + " : = " + String.valueOf(d.getDateIso()));
                }

                gotData(pageData);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void gotData(UVValue[] pageData) {
        data.addAll(Arrays.asList(pageData));

        Log.d(TAG, "gotData: " + data.size() + data);

        recyclerView.setAdapter(new UVRecyclerViewAdapter(data, this));
    }
}
