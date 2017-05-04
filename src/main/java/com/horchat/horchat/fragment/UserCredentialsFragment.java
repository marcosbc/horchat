package com.horchat.horchat.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.horchat.horchat.R;
import com.horchat.horchat.activity.MainActivity;
import com.horchat.horchat.exception.AccountValidationException;
import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Server;

public class UserCredentialsFragment extends Fragment {
    private Server mServer;
    private TextView username;
    private TextView nickname;
    private TextView realName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Recover data from server connection fragment */
        mServer = (Server) getArguments().getSerializable(MainActivity.SERVER);
        /* Inflate view and configure UI elements */
        View view = inflater.inflate(R.layout.fragment_user_credentials, container, false);
        username = (TextView) view.findViewById(R.id.username);
        nickname = (TextView) view.findViewById(R.id.nickname);
        realName = (TextView) view.findViewById(R.id.realName);
        Button connectToServerButton = (Button) view.findViewById(R.id.connectToServer_button);
        connectToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectButtonClick((Button) view);
            }
        });
        return view;
    }
    public void connectButtonClick(Button view) {
        /* Validate user */
        Account account = null;
        try {
            account = new Account(username.getText(), realName.getText(), nickname.getText());
        } catch (AccountValidationException e) {
            Toast.makeText(getContext(), getResources().getString(e.toResourceString()),
                    Toast.LENGTH_SHORT).show();
        }
        // TODO: Connect to server
        /* Continue if any error happened */
        if (account == null) {
            return;
        }
        /* Start main activity */
        Intent activityIntent = new Intent(getContext(), MainActivity.class);
        activityIntent.putExtra(MainActivity.SERVER, mServer);
        activityIntent.putExtra(MainActivity.ACCOUNT, account);
        // Clear the navigation stack and start the new activity
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activityIntent);
        getActivity().finish();
    }
}
