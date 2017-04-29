package com.horchat.horchat.activity;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.horchat.horchat.R;
import com.horchat.horchat.db.DatabaseHelper;
import com.horchat.horchat.model.Account;
import com.horchat.horchat.model.Session;

public class MainActivity extends AppCompatActivity {

    private static final String ID = "horchat";

    public static final String ID_SESSION = "session";
    public static final String SERVER_HOST = "serverHost";
    public static final String SERVER_PORT = "serverPort";
    public static final String SERVER_USERNAME = "username";
    public static final String USER_NICKNAME = "nickname";
    public static final String USER_PASSWORD = "password";
    public static final String USER_REALNAME = "realName";
    /* TODO: Remove */
    public static final String TODO_IS_LOGGED_IN = "isLoggedIn";

    private Account account = null;
    private Session session = null;
    private DatabaseHelper db = null;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up layouts
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        // Configure toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Configure navigation drawer
        /*
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                return true;
            }
        });
        */
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Setup the database handler
        db = new DatabaseHelper(getApplicationContext());
        // Setup the session if it wasn't defined yet
        if (savedInstanceState != null) {
            /* TODO: Verify */
            Log.i(ID, "Activity has been re-loaded");
            session = (Session) savedInstanceState.getSerializable(ID_SESSION);
            if (session != null) {
                account = session.getAccount();
            }
        } else {
            // Check if the user was already logged in
            account = db.getCurrentAccount();
            if (account == null) {
                Log.d(ID, "Looking for account");
                // Check if credentials were sent from login activity
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    Log.d(ID, "Creating account with credentials from login activity");
                    account = db.createAccount(extras);
                }
                if (account != null) {
                    Log.d(ID, "Account was successfully created");
                    // The session is now valid
                    session = new Session(account);
                    // Save in settings
                    db.setSetting(DatabaseHelper.ID_CURRENT_ACCOUNT, String.valueOf(account.getId()));
                }
                // If we got nothing, logout
                else {
                    // The user has not logged in yet, go to server connection activity
                    logout();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        /* TODO: Make use of savedInstance (e.g. orientation change) */
        savedInstanceState.putSerializable(ID_SESSION, session);
    }

    /* Toolbar configurations */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        /* TODO: Add search functionality */
        /*
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        */
        /* Will always return true */
        return true;
    }

    /* Toolbar clicks */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                /* Open navigation drawer */
                Log.d(ID, "Toolbar: Opening navigation drawer");
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.action_options:
                /* Dropdown button will be handled via Views */
                Log.d(ID, "Toolbar: Opening options menu");
                break;
            case R.id.action_settings:
                /* Open a new activity for settings */
                Log.d(ID, "Toolbar: Opening settings activity");
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_logout:
                /* Perform logout of the user */
                Log.d(ID, "Toolbar: Logging out");
                Toast.makeText(getApplicationContext(), R.string.toast_logout, Toast.LENGTH_SHORT).show();
                logout();
                break;
            default:
                Log.d(ID, "Toolbar: Other button clicked, *ignoring*");
        }
        return true;
    }

    /* Logs a user out, and starts the login-related activity */
    public void logout() {
        Intent activityIntent = new Intent(this, ConnectToServerActivity.class);
        /* Clear navigation history */
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        /* Remove login mark in settings */
        if (db != null) {
            db.logout();
        }
        /* Start the activity */
        startActivity(activityIntent);
        finish();
    }
}