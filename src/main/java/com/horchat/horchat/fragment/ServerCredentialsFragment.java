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
import android.widget.Toast;

import com.horchat.horchat.R;
import com.horchat.horchat.activity.MainActivity;
import com.horchat.horchat.exception.ServerValidationException;
import com.horchat.horchat.model.Server;

public class ServerCredentialsFragment extends Fragment {
    private TextView serverHost;
    private TextView serverPort;
    private TextView serverPassword;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /* Inflate view and configure UI elements */
        View view = inflater.inflate(R.layout.fragment_server_credentials, container, false);
        serverHost = (TextView) view.findViewById(R.id.serverHost);
        serverPort = (TextView) view.findViewById(R.id.serverPort);
        serverPassword = (TextView) view.findViewById(R.id.serverPassword);
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
        UserCredentialsFragment userCredentialsFragment = new UserCredentialsFragment();
        Bundle args = new Bundle();
        Server server = null;
        try {
            server = new Server(serverHost.getText(), serverPort.getText(),
                    serverPassword.getText());
        } catch (ServerValidationException e) {
            Toast.makeText(getContext(), getResources().getString(e.toResourceString()),
                    Toast.LENGTH_SHORT).show();
        }
        // TODO: Connect to server
        /* Continue if any error happened */
        if (server == null) {
            return;
        }
        /* Move to the next fragment */
        args.putSerializable(MainActivity.SERVER, server);
        userCredentialsFragment.setArguments(args);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        /* Replace server credentials fragment with user credentials fragment */
        transaction.replace(R.id.connect_frame, userCredentialsFragment);
        /* Add the transaction to the back stack so the user can navigate back */
        transaction.addToBackStack(null);
        /* Commit transaction */
        transaction.commit();
    }
}
