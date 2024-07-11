package com.example.moodcolouring2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StaffModeActivity extends AppCompatActivity {

    private static final String TAG = "COMP3018";
    ResponseRoomDatabase db;
    ResponseDao responseDao;
    TextView tv;
    ActivityResultLauncher<Intent> resultLauncher;
    int totalResponses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_mode);

        db = ResponseRoomDatabase.getDatabase(getApplicationContext());
        responseDao = db.responseDao();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ResponseAdapter adapter = new ResponseAdapter(this);
        recyclerView.setAdapter(adapter);

        tv = findViewById(R.id.ratioTextView);

        int[] responseCounts = new int[5];

        ResponseRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<Response> responses = responseDao.getAllResponses();
            totalResponses = responses.size();
            for (Response response : responses) {
                responseCounts[response.getSelectedOption()-1]++;
            }
            String holder = "Excited: "+ responseCounts[0] +" Happy: "+ responseCounts[1]+
                    " Exhausted: "+ responseCounts[2]+" Surprised: "+ responseCounts[3]+" Downcast: "+ responseCounts[4] + " and Total: "+totalResponses;
            runOnUiThread(() -> {
                adapter.setData(responses);
                tv.setText(holder);
            });

        });


        findViewById(R.id.returnMainButton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent replyIntent = new Intent();
                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });

        findViewById(R.id.viewImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StaffModeActivity.this, ImageActivity.class);
                intent.putExtra("responseCounts", responseCounts);
                intent.putExtra("totalResponses", totalResponses);
                resultLauncher.launch(intent);
            }
        });



        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Log.d("COMP3018", "Returned Main");
                    }
                }
        );

    }
}