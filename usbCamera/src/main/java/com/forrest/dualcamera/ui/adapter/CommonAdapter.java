package com.forrest.dualcamera.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.serenegiant.dualcamera.R;

import java.util.ArrayList;


public class CommonAdapter extends RecyclerView.Adapter <CommonAdapter.CommomAdapterHolder>{

    private Context context;
    private ArrayList<String> dataList;

    public CommonAdapter(Context context, ArrayList<String> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public CommomAdapterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CommomAdapterHolder(LayoutInflater.from(context).inflate(R.layout.dialogui_holder_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CommomAdapterHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    static class CommomAdapterHolder extends RecyclerView.ViewHolder {

        private TextView tv;
        public CommomAdapterHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
