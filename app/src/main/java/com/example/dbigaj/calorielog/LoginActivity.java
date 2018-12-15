package com.example.dbigaj.calorielog;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView emailView;
    private static EditText passwordView;
    private Button signInButton;
    private static Handler handler;

    public LoginActivity() {
        handler = new Handler();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailView = (AutoCompleteTextView) findViewById(R.id.email);

        passwordView = (EditText) findViewById(R.id.password);

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }

    private void attemptLogin() {

        emailView.setError(null);
        passwordView.setError(null);

        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
        }

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
        } else {
            check(this, email, password);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    public static void check(final Context context, final String email, final String password) {
        new Thread() {
            public void run() {
                final String token = isCorrectData(context, email, password);
                if (token.length() >= 25) {
                    handler.post(new Runnable() {
                        public void run() {

                            Intent intent = new Intent(context, MealsListActivity.class);
                            String code = token.substring(token.indexOf('.') + 1);
                            byte[] decodedBytes = Base64.decode(code.substring(0, code.indexOf('.')).getBytes(), Base64.DEFAULT);
                            intent.putExtra("uid", new String(decodedBytes).substring(11, 12));
                            context.startActivity(intent);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            passwordView.setError(token);
                        }
                    });
                }
            }
        }.start();
    }

    public static String isCorrectData(final Context context, final String email, final String password) {
        try {
            URL url = new URL(String.format("http://156.17.42.122:8000/api-token-auth/"));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            JSONObject postData = new JSONObject();
            postData.put("username", email);
            postData.put("password", password);
            connection.getOutputStream().write(postData.toString().getBytes());

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
            return "Wrong email or password";
        }
    }
}

