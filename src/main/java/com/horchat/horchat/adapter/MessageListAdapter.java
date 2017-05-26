package com.horchat.horchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.horchat.horchat.R;
import com.horchat.horchat.model.Message;

import java.util.LinkedList;
import java.util.List;

public class MessageListAdapter extends ArrayAdapter<Message> {
    private Context mContext;
    private List<Message> mMessages;
    public MessageListAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
        mContext = context;
        mMessages = messages;
    }
    @Override
    public int getItemViewType(int pos) {
        return mMessages.get(pos).getType();
    }
    @Override
    public int getViewTypeCount() {
        return Message.NUM_TYPES;
    }
    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        ViewHolder viewHolder;
        int type = getItemViewType(pos);
        if (view == null) {
            view = getInflatedLayoutForType(type);
            viewHolder = new ViewHolder();
            viewHolder.sender = (TextView) view.findViewById(R.id.conversation_message_sender);
            viewHolder.date = (TextView) view.findViewById(R.id.conversation_message_date);
            viewHolder.text = (TextView) view.findViewById(R.id.conversation_message_text);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        // Update views
        Message message = mMessages.get(pos);
        if (message.getSender() != null) {
            viewHolder.sender.setText(message.getSender());
        }
        viewHolder.date.setText(message.getFormattedDate());
        viewHolder.text.setText(message.getText());
        return view;
    }
    private View getInflatedLayoutForType(int type) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        int layoutResource;
        if (type == Message.TYPE_SERVER) {
            layoutResource = R.layout.conversation_status;
        } else {
            layoutResource = R.layout.conversation_message;
        }
        return LayoutInflater.from(mContext).inflate(layoutResource, null);
    }
    // View Holder class (check: View Holder pattern)
    private static class ViewHolder {
        TextView sender;
        TextView date;
        TextView text;
    }
}
