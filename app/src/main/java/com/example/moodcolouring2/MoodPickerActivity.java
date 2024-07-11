package com.example.moodcolouring2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import taimoor.sultani.sweetalert2.Sweetalert;


public class MoodPickerActivity extends AppCompatActivity {

    ResponseDao responseDao;
    private static final String TAG = "COMP3018";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_picker);

        Button excitedButton = findViewById(R.id.excitedButton);
        Button happyButton = findViewById(R.id.happyButton);
        Button exhaustedButton = findViewById(R.id.exhaustedButton);
        Button surprisedButton = findViewById(R.id.surprisedButton);
        Button downcastButton = findViewById(R.id.downcastButton);

        ResponseRoomDatabase db = ResponseRoomDatabase.getDatabase(getApplicationContext());
        responseDao = db.responseDao();

        excitedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Excited pressed");
                enterEmotion(1);
            }
        });
        happyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Happy pressed");
                enterEmotion(2);
            }
        });
        exhaustedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Exhausted pressed");
                enterEmotion(3);
            }
        });
        surprisedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Surprised pressed");
                enterEmotion(4);
            }
        });
        downcastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Downcast pressed");
                enterEmotion(5);
            }
        });


        findViewById(R.id.returnMainButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });
    }


    private void enterEmotion(int emotion) {
        Log.d(TAG, "enterEmotion: " + emotion);
        Response response = new Response(emotion);
        ResponseRoomDatabase.databaseWriteExecutor.execute(() -> {
            responseDao.insert(response);
        });
        new Sweetalert(this, Sweetalert.SUCCESS_TYPE)
                .setTitleText("Your Emotion has been Added")
                .setNeutralButton("Okay",new Sweetalert.OnSweetClickListener() {
                    @Override
                    public void onClick(Sweetalert sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation(); // Dismiss the dialog with animation
                    }
                }).show();

    }


}