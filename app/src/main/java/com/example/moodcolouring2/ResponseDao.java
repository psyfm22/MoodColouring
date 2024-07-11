package com.example.moodcolouring2;

import android.database.Cursor;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ResponseDao {

    @Insert
    void insert(Response response);

    @Query("SELECT * FROM response_table")
    List<Response> getAllResponses();
}
