package com.horchat.horchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class UserCredentialsFragment extends Fragment {
    private String host;
    private String port;
    private String username;
    private TextView nickname;
    private TextView password;
    private TextView realName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Recover data from server connection fragment */
        host = getArguments().getString(MainActivity.SERVER_HOST);
        port = getArguments().getString(MainActivity.SERVER_HOST);
        username = getArguments().getString(MainActivity.SERVER_HOST);
        /* Inflate view and configure UI elements */
        View view = inflater.inflate(R.layout.fragment_user_credentials, container, false);
        nickname = (TextView) view.findViewById(R.id.nickname);
        password = (TextView) view.findViewById(R.id.password);
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
        Log.d("DAM", "pressed click 2");
        /* Start main activity */
        Intent activityIntent = new Intent(getContext(), MainActivity.class);
        activityIntent.putExtra(MainActivity.SERVER_HOST, host);
        activityIntent.putExtra(MainActivity.SERVER_PORT, port);
        activityIntent.putExtra(MainActivity.SERVER_USERNAME, username);
        activityIntent.putExtra(MainActivity.USER_NICKNAME, nickname.getText().toString());
        activityIntent.putExtra(MainActivity.USER_PASSWORD, password.getText().toString());
        activityIntent.putExtra(MainActivity.USER_REALNAME, realName.getText().toString());
        activityIntent.putExtra(MainActivity.TODO_IS_LOGGED_IN, true);
        startActivity(activityIntent);
    }
}
