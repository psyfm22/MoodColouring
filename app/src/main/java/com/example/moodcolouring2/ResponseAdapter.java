package com.example.moodcolouring2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder>{

    private List<Response> data;
    private Context context;
    private LayoutInflater layoutInflater;

    public ResponseAdapter(Context context) {
        this.data = new ArrayList<>();
        this.context = context;
        this.layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public ResponseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.response_layout, parent, false);
        return new ResponseViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(ResponseViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<Response> newData) {
        if (data != null) {
            data.clear();
            data.addAll(newData);
            notifyDataSetChanged();
        } else {
            data = newData;
        }
    }

    class ResponseViewHolder extends RecyclerView.ViewHolder {

        TextView idView;
        TextView responseView;

        ResponseViewHolder(View itemView) {
            super(itemView);

            idView = itemView.findViewById(R.id.idTextView);
            responseView = itemView.findViewById(R.id.responseTextView);
        }
        void bind(final Response response) {
            if (response != null) {
                idView.setText(String.valueOf(response.getId()));
                responseView.setText(String.valueOf(response.getSelectedOption()));
            }
        }
    }

}
