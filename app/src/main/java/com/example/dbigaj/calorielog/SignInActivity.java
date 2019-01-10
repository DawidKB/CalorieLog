package com.example.dbigaj.calorielog;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static SignInButton buttonSignIn;
    private static final int REQ_CODE = 9001;
    String photo;
    private static Handler handler;
    GoogleApiClient googleApiClient;
    private static Global global = new Global();

    public SignInActivity() {
        handler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        buttonSignIn = (SignInButton) findViewById(R.id.buttonSignIn);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId().requestEmail().requestIdToken("105799362512-o4nlroec61d01dbchauq48odd13uhi9h.apps.googleusercontent.com").build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions).build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void handleResult(GoogleSignInResult result) {

        if(result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            if (account.getPhotoUrl() != null) photo = account.getPhotoUrl().toString();
            else photo = null;

            logUser(this, account.getId(), account.getDisplayName(), account.getEmail(), account.getIdToken(), photo);
        }
    }

    private void signIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    public static void logUser(final Context context, final String id, final String name, final String email, final String token, final String photo) {
        new Thread() {
            public void run() {
                String s = isCorrectData(context, email, id, name, token);

                handler.post(new Runnable() {
                    public void run() {
                        Intent intent = new Intent(context, MainActivity.class);
                        String[] user_table = {name, email, photo, id, token};
                        intent.putExtra("user", user_table);
                        context.startActivity(intent);
                    }
                });
            }
        }.start();
    }

    public static String isCorrectData(final Context context, final String email, final String id, final String name, final String token) {
        try {
            URL url = new URL(String.format(global.getUrl() + "/login/google"));
            HttpURLConnection connection =
            (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            JSONObject postData = new JSONObject();
            JSONObject profile = new JSONObject();
            profile.put("email", email);
            profile.put("id", id);
            profile.put("name", name);
            postData.put("profile", profile);
            postData.put("token", token);

            connection.getOutputStream().write(postData.toString().getBytes());

            int con = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(
            new InputStreamReader(connection.getInputStream()));

            StringBuilder sb = new StringBuilder(reader.readLine());

            reader.close();

            if (connection.getResponseCode() != 200) {
                connection.disconnect();
                return "Connection error";
            }
            connection.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return "All fields are required";
        }
    }
}
