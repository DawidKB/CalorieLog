package com.example.dbigaj.calorielog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView tvName, tvEmail;
    static private TextView tvCalorie;
    private ImageView photo;
    private String uid, date, token;
    static private Handler handler;
    private static Global global = new Global();

    public MainActivity() {
        handler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);

        tvName = (TextView) headerView.findViewById(R.id.textViewName);
        tvEmail = (TextView) headerView.findViewById(R.id.textViewEmail);
        photo = (ImageView) headerView.findViewById(R.id.imageView);
        tvCalorie = (TextView) findViewById(R.id.tvCalorie);

        String[] user_table = getIntent().getStringArrayExtra("user");

        tvName.setText(user_table[0]);
        tvEmail.setText(user_table[1]);
        if (user_table[2] != null) Picasso.with(this).load(user_table[2]).into(photo);
        uid = user_table[3];
        token = user_table[4];

        Calendar c = Calendar.getInstance();
        date = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
        calculate(date, token);
    }

    public static void calculate(final String date, final String token) {
        new Thread() {
            public void run() {
                final String calorie = getCalorie(date, token);
                if (calorie != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            tvCalorie.setText(tvCalorie.getText().toString().replace("X", calorie));
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_adding) {
            Intent intent = new Intent(this, MealActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("token", token);
            startActivity(intent);
        } else if (id == R.id.nav_meals) {
            Intent intent = new Intent(this, MealsListActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("token", token);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            startActivity(new Intent(this, SignInActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public static String getCalorie(final String date, final String token) {
        try {
            URL url = new URL(String.format(global.getUrl() + "/meals/kcal?date=" + date));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("User-Agent", "my-rest-app");
            connection.addRequestProperty("Authorization", token);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder sb = new StringBuilder(reader.readLine());

            reader.close();

            if (connection.getResponseCode() != 200) {
                connection.disconnect();
            }
            connection.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return e.toString();
        }
    }
}
