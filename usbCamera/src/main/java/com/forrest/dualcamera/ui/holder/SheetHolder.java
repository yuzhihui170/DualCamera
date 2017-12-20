package com.forrest.dualcamera.ui.holder;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.serenegiant.dualcamera.R;
import com.forrest.dualcamera.ui.adapter.CommonAdapter;
import com.forrest.dualcamera.ui.widget.DialogUIDividerItemDecoration;

import java.util.ArrayList;

public class SheetHolder extends SuperHolder {

    private TextView tvTitle;
    private RecyclerView rView;
    private Button btnBottom;
    private boolean isItemType;

    public SheetHolder(Context context) {
        super(context);
    }

    @Override
    protected void findViews() {
        tvTitle = (TextView) rootView.findViewById(R.id.dialogui_tv_title);
        rView = (RecyclerView) rootView.findViewById(R.id.rlv);
        btnBottom = (Button) rootView.findViewById(R.id.btn_bottom);
    }

    @Override
    protected int setLayoutRes() {
        return R.layout.dialogui_holder_sheet;
    }

    public SheetHolder setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    @Override
    public void setDatasAndEvents(final Context context) {
//        if (TextUtils.isEmpty(bean.bottomTxt)) {
//            btnBottom.setVisibility(View.GONE);
//        } else {
//            btnBottom.setVisibility(View.VISIBLE);
//            btnBottom.setText(bean.bottomTxt);
//            btnBottom.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    DialogUIUtils.dismiss(bean.dialog, bean.alertDialog);
//                    bean.itemListener.onBottomBtnClick();
//
//                }
//            });
//        }
//        if (TextUtils.isEmpty(bean.title)) {
//            tvTitle.setVisibility(View.GONE);
//        } else {
//            tvTitle.setVisibility(View.VISIBLE);
//            tvTitle.setText(bean.title);
//        }
            rView.setLayoutManager(new LinearLayoutManager(context));
            rView.addItemDecoration(new DialogUIDividerItemDecoration(context));
//        } else {
//            rView.setLayoutManager(new GridLayoutManager(bean.mContext, bean.gridColumns));// 布局管理器。
//        }
        rView.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        rView.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        ArrayList<String> dataList = new ArrayList<String>();
        dataList.add("1");
        dataList.add("2");
        dataList.add("3");
        rView.setAdapter(new CommonAdapter(context, dataList));
//        if (bean.mAdapter == null) {
//            TieAdapter adapter = new TieAdapter(bean.mContext, bean.mLists, isItemType);
//            bean.mAdapter = adapter;
//        }
//        rView.setAdapter(bean.mAdapter);
//        bean.mAdapter.setOnItemClickListener(new OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                DialogUIUtils.dismiss(bean.dialog, bean.alertDialog);
//                bean.itemListener.onItemClick(bean.mLists.get(position).getTitle(), position);
//            }
//        });
    }


}
