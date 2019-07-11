package com.yufs.wechatlocation.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.yufs.wechatlocation.R;
import com.yufs.wechatlocation.bean.PoiBean;

import java.util.List;

/**
 * Created by yufs on 2017/3/1.
 */

public class PoiAdapter extends BaseAdapter {
    List<PoiBean> data;
    Context context;
    public PoiAdapter(Context context, List<PoiBean> data) {
        this.context=context;
        this.data=data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView= View.inflate(context, R.layout.item_select_address,null);
        TextView tv_title=ViewHolder.get(convertView,R.id.tv_title);
        TextView tv_address=ViewHolder.get(convertView,R.id.tv_address);
        ImageView iv_checked=ViewHolder.get(convertView,R.id.iv_checked);
        PoiBean item= data.get(position);
        if(item.isSelected()){
            iv_checked.setVisibility(View.VISIBLE);
        }else{
            iv_checked.setVisibility(View.INVISIBLE);
        }
        if(item.isLoc()){
            tv_address.setVisibility(View.GONE);
        }else{
            tv_address.setVisibility(View.VISIBLE);
        }
        tv_title.setText(item.getTitleName());
        tv_address.setText(item.getCityName()+item.getAd()+item.getSnippet());
        return convertView;
    }
}
