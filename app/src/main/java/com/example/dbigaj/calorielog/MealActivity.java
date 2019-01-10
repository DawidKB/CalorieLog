package com.example.dbigaj.calorielog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static java.lang.Thread.sleep;

public class MealActivity extends AppCompatActivity {
    private final static int REQUEST_GALLERY = 0;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "SelectImageActivity";
    private static TextView dateTime, tvError;
    static private String info;
    private EditText name, caloriesAmount;
    private Button bRemove, bEdit;
    private ImageView photo;
    private Spinner type;
    private String mid, uid, p, token;
    private int action;
    private final int EDIT = 1;
    private ArrayAdapter<String> adapter;
    private static Handler handler;
    private static Global global = new Global();


    public MealActivity() {
        handler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_edit);

        bRemove = (Button)findViewById(R.id.buttonRemove);
        bEdit = (Button)findViewById(R.id.buttonEdit);
        dateTime = (TextView)findViewById(R.id.DateTime);
        name = (EditText)findViewById(R.id.editTextName);
        caloriesAmount = (EditText)findViewById(R.id.editTextCaloriesAmount);
        photo = (ImageView)findViewById(R.id.photo);
        type = (Spinner)findViewById(R.id.spinnerType);
        tvError = (TextView) findViewById(R.id.textViewError);
        ArrayList<String> types = new ArrayList<String>();
        types.add("--wybierz--");
        types.add("breakfast");
        types.add("brunch");
        types.add("elevenses");
        types.add("lunch");
        types.add("tea");
        types.add("dinner");
        types.add("supper");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                p = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        action = getIntent().getIntExtra("action", -1);
        uid = getIntent().getStringExtra("uid");
        token = getIntent().getStringExtra("token");

        if (action == EDIT) {
            String[] meals_table = getIntent().getStringArrayExtra("meals");

            mid = meals_table[0];
            Picasso.with(this).load(meals_table[1]).into(photo);
            name.setText(meals_table[2]);
            dateTime.setText(meals_table[3]);
            type.setSelection(adapter.getPosition(meals_table[4].toLowerCase()));
            caloriesAmount.setText(meals_table[5]);

        } else {
            ViewGroup layout = (ViewGroup) bRemove.getParent();
            layout.removeView(bRemove);
            bEdit.setText("Add");
            Calendar c = Calendar.getInstance();
            String date = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
            String time = new SimpleDateFormat("HH:mm:ss").format(c.getTime());
            dateTime.setText(date+" "+time);
        }

    }

    public void onClick(View v) throws InterruptedException {
        if(type.getSelectedItem().toString().equals("--wybierz--") || name.toString().equals("") || caloriesAmount.toString().equals("")){
            info = "All fields needs to be filled!";
            showDialog(0);
        } else {
            if (action == EDIT) {
                editMeal(name.getText().toString(), uid, Integer.parseInt(caloriesAmount.getText().toString()), p, dateTime.getText().toString(), token, mid);
                Intent intent = new Intent(getApplicationContext(), MealsListActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("token", token);
                sleep(1000);
                startActivity(intent);
            }
            else {
                addMeal(name.getText().toString(), uid, Integer.parseInt(caloriesAmount.getText().toString()), p, dateTime.getText().toString(), token);
                Intent intent = new Intent(getApplicationContext(), MealsListActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("token", token);
                sleep(1000);
                startActivity(intent);
            }
        }
    }

    public void removeOnClick(View v) {
        final Context context = this;
        new Thread() {
            public void run() {
                deleteFromDatabase(mid, token);
                Intent intent = new Intent(getApplicationContext(), MealsListActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("token", token);
                startActivity(intent);
            }
        }.start();
    }

    void editMeal(final String name, final String userId, final int kcal, final String type, final String dateTime, final String token, final String mid) {
        new Thread() {
            public void run() {
                validateData(name, userId, kcal, type, dateTime, token, mid);
            }
        }.start();
    };

    //Okno dialogowe informujące i potrzebie wybrania choroby jakiej dotyczy notatka
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(info);
        dialogBuilder.setNegativeButton("Ok", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        return dialogBuilder.create();
    }

    public void fromGalleryOnClick(View view)
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            Log.i(TAG, "Image Path : " + path);
                            // Set the image in ImageView
                            photo.post(new Runnable() {
                                @Override
                                public void run() {
                                    photo.setImageURI(selectedImageUri);
                                }
                            });
                        }
                    }
                }
            }
        }).start();
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    //Okno dialogowe informujące i potrzebie wybrania choroby jakiej dotyczy notatka
    protected Dialog onCreateDialog1(int id) {
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        dialogBuilder.setMessage("Do you want to delete this meal?");
        dialogBuilder.setNegativeButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        dialogBuilder.setPositiveButton("No", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        return dialogBuilder.create();
    }

    public void setDateOnClick(View v)
    {
        DialogFragment date_pick_fragment = new MealActivity.DatePickerFragment();
        ((MealActivity.DatePickerFragment)date_pick_fragment).setActivity(this);
        date_pick_fragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void setTimeOnClick()
    {
        DialogFragment time_pick_fragment = new TimePickerFragment();
        ((TimePickerFragment)time_pick_fragment).setActivity(this);
        time_pick_fragment.show(getSupportFragmentManager(), "timePicker");
    }

    private String timeToString(int val)
    {
        Integer i = Integer.valueOf(val);
        String res;

        if(val < 10)
            res = "0" + i.toString();
        else
            res = i.toString();

        return res;
    }

    public void setDate(int year, int month, int day)
    {
        month += 1;
        String d = timeToString(year) + "/" + timeToString(month) + "/" + timeToString(day);
        dateTime.setText(d);
    }

    public void setTime(int hour, int minute)
    {
        String t = timeToString(hour) + ":" + timeToString(minute);

        dateTime.setText(dateTime.getText().toString() + " " + t);
    }

    /*Date picker fragment
    * Fragment do wybierania daty wizyty*/
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private MealActivity wActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int year, month, day;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            /*Ustawiamy date jako te wczesniej wybrana przez uzytkownika
             *chyba ze jeszcze nic nie wybral, albo format danych jest niepoprawny,
             *wtedy uzywamy aktualnej daty jako podstawowego wyboru*/
            String date = dateTime.getText().toString();

            try {
                c.setTime(format.parse(date));
            }catch (ParseException e) {}

            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void setActivity(MealActivity activity)
        {
            wActivity= activity;
        }

        public void onDateSet(DatePicker view, int year, int month, int day)
        {
            wActivity.setDate(year, month, day);
            wActivity.setTimeOnClick();
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private MealActivity mActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int hour, minute;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void setActivity(MealActivity activity)
        {
            mActivity = activity;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute)
        {
            mActivity.setTime(hourOfDay, minute);
        }
    }

    public static void addMeal(final String name, final String userId, final int kcal, final String type, final String dateTime, final String token) {
        new Thread() {
            public void run() {
                isCorrectData(name, userId, kcal, type, dateTime, token);
            }
        }.start();
    }

    public static String isCorrectData(final String name, final String userId, final int kcal, final String type, final String dateTime, final String token) {
        try {
            URL url = new URL(String.format(global.getUrl() + "/meals"));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Authorization", token);
            JSONObject postData = new JSONObject();
            postData.put("photo", "photo.png");
            postData.put("name", name);
            postData.put("userId", userId);
            postData.put("kcal", kcal);
            postData.put("tag", type);
            dateTime.replace(" ", "T");
            postData.put("date", dateTime + ":00.000000");
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
            return "All fields are required";
        }
    }

    public static String validateData(final String name, final String userId, final int kcal, final String type, final String dateTime,final String token, final String mid) {
        try {
            URL url = new URL(String.format(global.getUrl() + "/meals/" + mid));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PATCH");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Authorization", token);
            JSONObject postData = new JSONObject();
//            postData.put("photo", "photo.png");
            postData.put("name", name);
//            postData.put("kcal", kcal);
//            postData.put("tag", type);
//            dateTime.replace(" ", "T");
//            postData.put("date", dateTime + ":00.000000");
            connection.getOutputStream().write(postData.toString().getBytes());

            int con = connection.getResponseCode();

//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(connection.getInputStream()));
//
//            StringBuilder sb = new StringBuilder(reader.readLine());
//
//            reader.close();

            if (connection.getResponseCode() != 200) {
                connection.disconnect();
                return "Connection error";
            }
            connection.disconnect();
            return null;
        } catch (Exception e) {
            return "All fields are required";
        }
    }

    public static String deleteFromDatabase(final String mealId, final String token) {
        try {
            URL url = new URL(String.format(global.getUrl() + "/meals/" + mealId));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Authorization", token);
            JSONObject postData = new JSONObject();

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
            return e.getMessage();
        }
    }
}