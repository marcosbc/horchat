package com.horchat.horchat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.horchat.horchat.R;
import com.horchat.horchat.model.TargetItem;

import java.util.List;

public class TargetListAdapter extends ArrayAdapter<TargetItem> {
    public static final Integer NUM_TYPES = 2;

    private Context mContext;
    private List<TargetItem> mTargetList;

    public TargetListAdapter(Context context, List<TargetItem> targetList) {
        super(context, 0, targetList);
        this.mContext = context;
        this.mTargetList = targetList;
    }
    @Override
    public int getViewTypeCount() {
        return NUM_TYPES;
    }
    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        TargetItem model = (TargetItem) mTargetList.get(pos);
        // Identify the row type: Section, entry with or without icon
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.target_list_item, null);
            viewHolder.name = (TextView) view.findViewById(R.id.list_item_name);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        // Update view contents
        viewHolder.name.setText(model.getName());
        return view;
    }
    // View Holder class (check: View Holder pattern)
    private static class ViewHolder {
        TextView name;
    }
}
