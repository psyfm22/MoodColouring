package com.example.moodcolouring2;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "response_table")
public class Response {



    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;


    @ColumnInfo(name = "option")
    private int selectedOption;
    //1=excited , 2=happy , 3=exhausted , 4=surprised , 5=downcast


    public Response(int selectedOption) {
        this.selectedOption = selectedOption;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(int selectedOption) {
        this.selectedOption = selectedOption;
    }

}
