package com.horchat.horchat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.horchat.horchat.R;
import com.horchat.horchat.activity.MainActivity;
import com.horchat.horchat.adapter.MessageListAdapter;
import com.horchat.horchat.model.Conversation;
import com.horchat.horchat.model.Message;

import java.util.Date;

public class ConversationFragment extends Fragment {
    private Conversation mConversation;
    private ListView mMessageList;
    private EditText mInput;
    private MessageListAdapter mAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate view and configure UI elements */
        View view = inflater.inflate(R.layout.fragment_conversation, null);
        /* Configure message list */
        mMessageList = (ListView) view.findViewById(R.id.conversation_list);
        mConversation = (Conversation) getArguments().getSerializable(MainActivity.CONVERSATION);
        /* Configure input bar */
        mInput = (EditText) view.findViewById(R.id.conversation_input);
        mInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });
        /* Disable input for the server conversation */
        if (mConversation.getType() == Conversation.TYPE_SERVER) {
            mInput.setEnabled(false);
        }
        /* Populate message list */
        mAdapter = new MessageListAdapter(getContext(), mConversation.getMessages());
        mMessageList.setAdapter(mAdapter);
        return view;
    }
    public void sendMessage() {
        // Send message
        mConversation.addMessage(new Message(mInput.getText().toString(), "marcos", new Date()));
        // Clear input
        mInput.getText().clear();
        // Notify list adapter
        mAdapter.notifyDataSetChanged();
    }
}
