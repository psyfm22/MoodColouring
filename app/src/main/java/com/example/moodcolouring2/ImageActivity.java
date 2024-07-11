package com.example.moodcolouring2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import android.content.ServiceConnection;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "COMP3018";
    private ImageProcessingService imageProcessingService;
    private boolean isBound = false;
    private ImageView imageView;
    private int inputImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        imageView = findViewById(R.id.bitmapImageView);
        Button button = findViewById(R.id.startProcessingServiceButton);
        Spinner spinner = findViewById(R.id.imageSpinner);
        spinner.setOnItemSelectedListener(this);
        String[] imageChoices = getResources().getStringArray(R.array.ImageChoices);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,imageChoices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        int[] responseCounts = getIntent().getIntArrayExtra("responseCounts");
        int totalResponses = getIntent().getIntExtra("totalResponses",0);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setImageResource(R.drawable.loading);
                Intent intent = new Intent(ImageActivity.this, ImageProcessingService.class);
                intent.putExtra("responseCounts", responseCounts);
                intent.putExtra("totalResponses", totalResponses);
                intent.putExtra("inputImage", inputImage);
                startService(intent);
                bindService(intent, connection, Context.BIND_AUTO_CREATE);

            }
        });

    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(TAG, "onServiceConnected");
            ImageProcessingService.LocalBinder binder = (ImageProcessingService.LocalBinder) iBinder;
            imageProcessingService = binder.getService();
            Log.d(TAG, "isBound set to true");
            isBound = true;

            imageProcessingService.setCallback(new ImageProcessingService.DownloadCallback() {

                @Override
                public void onImageProcessed(Bitmap bitmap) {
                    runOnUiThread(() -> {
                        imageView.setImageBitmap(bitmap);
                    });
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    protected void onStop(){
        Log.d(TAG,"OnStop called");
        super.onStop();
        if(isBound){
            unbindService(connection);
            isBound=false;
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

        if (adapterView.getId() == R.id.imageSpinner) {
            String valueFromSpinner = adapterView.getItemAtPosition(position).toString();
            switch (valueFromSpinner) {
                case "Nottingham Logo":
                    inputImage = R.drawable.nottslogo;
                    break;
                case "Coca Cola":
                    inputImage = R.drawable.cocacola;
                    break;
                case "Apple":
                    inputImage = R.drawable.apple;
                    break;
                default:
                    inputImage = R.drawable.nottslogo; // Default image
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        inputImage = R.drawable.nottslogo;
    }
}

