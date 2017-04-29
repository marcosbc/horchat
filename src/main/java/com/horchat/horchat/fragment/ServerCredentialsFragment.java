package com.horchat.horchat.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.horchat.horchat.R;
import com.horchat.horchat.activity.MainActivity;

public class ServerCredentialsFragment extends Fragment {
    private TextView serverHost;
    private TextView serverPort;
    private TextView username;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate view and configure UI elements */
        View view = inflater.inflate(R.layout.fragment_server_credentials, container, false);
        serverHost = (TextView) view.findViewById(R.id.serverHost);
        serverPort = (TextView) view.findViewById(R.id.serverPort);
        username = (TextView) view.findViewById(R.id.username);
        Button connectToServerButton = (Button) view.findViewById(R.id.connectToServer_button);
        connectToServerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextButtonClick((Button) view);
            }
        });
        return view;
    }
    public void nextButtonClick(Button view) {
        Log.d("DAM", "pressed click 1");
        UserCredentialsFragment userCredentialsFragment = new UserCredentialsFragment();
        Bundle args = new Bundle();
        args.putString(MainActivity.SERVER_HOST, serverHost.getText().toString());
        args.putString(MainActivity.SERVER_PORT, serverPort.getText().toString());
        args.putString(MainActivity.SERVER_USERNAME, username.getText().toString());
        userCredentialsFragment.setArguments(args);
        /* Move to the next fragment */
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        /* Replace server credentials fragment with user credentials fragment */
        transaction.replace(R.id.connect_frame, userCredentialsFragment);
        /* Add the transaction to the back stack so the user can navigate back */
        transaction.addToBackStack(null);
        /* Commit transaction */
        transaction.commit();
    }
}
