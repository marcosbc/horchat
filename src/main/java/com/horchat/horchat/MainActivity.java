package com.horchat.horchat;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String ID = "horchat";

    private static final String ACCOUNT_ID = "account_id";

    private Account account;
    private DatabaseHelper db;
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
        /* navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override public boolean onNavigationItemSelected(MenuItem menuItem) {
                return true;
            }
        }); */
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Set up the database
        db = new DatabaseHelper(getApplicationContext());
        // Extract data from server connection intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            /* TODO: Get data from server connection intent */
            // db.setSetting(db.KEY_CURRENT_ACCOUNT, userId)
        }
        // Configure the user account
        account = db.getCurrentAccount();
        if (account == null) {
            // The user has not logged in yet, go to server connection activity
            Intent activityIntent = new Intent(this, ConnectToServerActivity.class);
            startActivity(activityIntent);
        }
        /* TODO: Restore data from savedInstance */
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        /* TODO: Make use of savedInstance (e.g. orientation change) */
        // savedInstanceState.putInt(KEY_ID, KEY_VALUE);
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
        // Always return true
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
                /* Will be handled via Views */
                Log.d(ID, "Toolbar: Opening options menu");
                break;
            case R.id.action_settings:
                Log.d(ID, "Toolbar: Opening settings activity");
                /* TODO: Implement settings */
                break;
            case R.id.action_logout:
                Log.d(ID, "Toolbar: Logging out");
                /* TODO: Implement logout */
                break;
            default:
                Log.d(ID, "Toolbar: Other button clicked, *ignoring*");
        }
        return true;
    }
}
