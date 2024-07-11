package com.example.moodcolouring2;

import static androidx.activity.OnBackPressedDispatcherKt.addCallback;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;



@Database(entities = {Response.class}, version = 1, exportSchema = false)
public abstract class ResponseRoomDatabase extends RoomDatabase {

    public abstract ResponseDao responseDao();

    private static final int threadCount = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(threadCount);

    private static volatile ResponseRoomDatabase instance;



    static ResponseRoomDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (ResponseRoomDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    ResponseRoomDatabase.class, "response_databse")
                            .fallbackToDestructiveMigration().build();

                    //allowMainThreadQueries()
                }
            }
        }
        return instance;
    }

}

