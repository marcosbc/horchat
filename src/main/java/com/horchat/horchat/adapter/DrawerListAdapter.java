package com.horchat.horchat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.horchat.horchat.R;
import com.horchat.horchat.model.DrawerEntry;
import com.horchat.horchat.model.DrawerItem;
import com.horchat.horchat.model.DrawerSection;

import java.util.List;

public class DrawerListAdapter extends ArrayAdapter<DrawerItem> {
    public static final int TYPE_SECTION = 0;
    public static final int TYPE_ENTRY = 1;
    public static final int TYPE_ENTRY_WITH_ICON = 2;
    public static final int NUM_TYPES = 3;
    private Context mContext;
    private List<DrawerItem> mItemList;
    public DrawerListAdapter(Context context, List<DrawerItem> itemList) {
        super(context, 0, itemList);
        this.mContext = context;
        this.mItemList = itemList;
    }
    @Override
    public int getItemViewType(int pos) {
        if (mItemList.get(pos).isSection()) {
            return TYPE_SECTION;
        } else {
            DrawerEntry entry = (DrawerEntry) mItemList.get(pos);
            if (entry.hasIcon()) {
                return TYPE_ENTRY_WITH_ICON;
            } else {
                return TYPE_ENTRY;
            }
        }
    }
    @Override
    public int getViewTypeCount() {
        return NUM_TYPES;
    }
    private View getInflatedLayoutForType(int type) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int layoutResource;
        if (type == TYPE_SECTION) {
            layoutResource = R.layout.navigation_drawer_list_section;
        } else if (type == TYPE_ENTRY_WITH_ICON) {
            layoutResource = R.layout.navigation_drawer_list_entry_with_icon;
        } else {
            layoutResource = R.layout.navigation_drawer_list_entry;
        }
        return LayoutInflater.from(mContext).inflate(layoutResource, null);
    }
    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        DrawerItem model = (DrawerItem) mItemList.get(pos);
        // Identify the row type: Section, entry with or without icon
        int type = getItemViewType(pos);
        if (view == null) {
            viewHolder = new ViewHolder();
            view = getInflatedLayoutForType(type);
            if (type == TYPE_SECTION) {
                viewHolder.entry = null;
                viewHolder.name = (TextView) view.findViewById(R.id.title);
                viewHolder.icon = null;
                // Sections cannot be clicked, for now
                view.setOnClickListener(null);
            } else {
                viewHolder.entry = (View) view.findViewById(R.id.list_item_entry);
                viewHolder.name = (TextView) view.findViewById(R.id.list_item_entry_text);
                viewHolder.icon = (ImageView) view.findViewById(R.id.list_item_entry_icon);
            }
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        // Update view contents
        viewHolder.name.setText(model.getItemName());
        if (type == TYPE_ENTRY_WITH_ICON) {
            viewHolder.icon.setImageResource(((DrawerEntry) model).getItemImage());
        }
        if (isSelected(pos) && type != TYPE_SECTION) {
            // TODO: Find a better way of doing this
            viewHolder.entry.setBackgroundColor(Color.parseColor("#DDDDDD"));
        }
        return view;
    }
    private boolean isSelected(int pos) {
        if (!mItemList.get(pos).isSection()) {
            DrawerEntry entry = (DrawerEntry) mItemList.get(pos);
            if (entry.isSelected()) {
                return true;
            }
        }
        return false;
    }
    // View Holder class (check: View Holder pattern)
    private static class ViewHolder {
        View entry;
        TextView name;
        ImageView icon;
    }
}
