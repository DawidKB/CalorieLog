package com.example.dbigaj.calorielog;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class MealsListActivity extends AppCompatActivity {

    private static RecyclerView myMealsListView;
    private static TextView emptyView;
    private static Handler handler;
    private EditText et_szukaj;
    private Spinner type;
    static private TextView tvDate;
    private String uid, p, token;
    private static MyMealsListAdapter adapter1;
    private ArrayAdapter<String> adapter2;
    private static ArrayList<Meal> meals1 = new ArrayList<>(), meals2 = new ArrayList<>();

    public MealsListActivity() {
        handler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meals_list);

        meals1.clear();
        meals2.clear();

        uid = getIntent().getStringExtra("uid");
        token = getIntent().getStringExtra("token");

        myMealsListView = (RecyclerView) findViewById(R.id.listMyMeals);
        emptyView = (TextView) findViewById(R.id.emptyView);
        tvDate = (TextView) findViewById(R.id.textViewDate);
        type = (Spinner) findViewById(R.id.spinnerType);

        Calendar c = Calendar.getInstance();
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));

        ArrayList<String> types = new ArrayList<String>();
        types.add("--wybierz--");
        types.add("all");
        types.add("breakfast");
        types.add("brunch");
        types.add("elevenses");
        types.add("lunch");
        types.add("tea");
        types.add("dinner");
        types.add("supper");
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, types);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        type.setAdapter(adapter2);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                p = parent.getItemAtPosition(position).toString();
                if (!p.equals("--wybierz--")) filter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        et_szukaj = (EditText) findViewById(R.id.editTextSearch);

        et_szukaj.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0){
                    filter();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        searchMeals(getApplicationContext(), uid, token);
    }

    public static void searchMeals(final Context context, final String id, final String token) {
        new Thread() {
            public void run() {
                meals1 = getMeals(context, id, token);
                //meals2 = new ArrayList<>(meals1);
                if (meals1 == null) {
                    handler.post(new Runnable() {
                        public void run() {
                            myMealsListView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    handler.post(new Runnable() {
                        public void run() {
                            myMealsListView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            Collections.reverse(meals1);
                            adapter1 = new MyMealsListAdapter(meals1);
                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
                            myMealsListView.setLayoutManager(mLayoutManager);
                            myMealsListView.setAdapter(adapter1);
                        }
                    });
                }
            }
        }.start();
    }

    public static ArrayList<Meal> getMeals(final Context context, final String id, final String token) {
        try {
            URL url = new URL(String.format("https://meal-diary-api.herokuapp.com/meals"));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("User-Agent", "my-rest-app");
            connection.addRequestProperty("Authorization", token);

            JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));

            Meal meal;
            ArrayList<Meal> events = new ArrayList<>();
            reader.beginArray();
            while (reader.hasNext()) {
                meal = new Meal();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("photo")) {
                        meal.setPhoto(reader.nextString());
                    } else if (name.equals("_id")) {
                        meal.setMid(reader.nextString());
                    } else if (name.equals("name")) {
                        meal.setName(reader.nextString());
                    } else if (name.equals("userId")) {
                        meal.setUid(reader.nextString());
                    } else if (name.equals("kcal")) {
                        meal.setCaloriesAmount(reader.nextString());
                    } else if (name.equals("tag")) {
                        meal.setType(tryCompare(reader.nextString().toUpperCase()));
                    } else if (name.equals("date")) {
                        meal.setDateTime(reader.nextString());
                    } else {
                        reader.skipValue();
                    }
                }
                reader.endObject();
                events.add(meal);
            }
            reader.endArray();

            reader.close();

            if (connection.getResponseCode() != 200) {
                connection.disconnect();
            }
            connection.disconnect();
            return events;
        } catch (Exception e) {
            ArrayList<Meal> l1 = new ArrayList<>();
            Meal m = new Meal();
            m.setName(e.getMessage());
            l1.add(m);
            return l1;
        }
    }

    static private Type tryCompare(String s) {
        try {
            return Type.valueOf(s);
        } catch (IllegalArgumentException e) {
            return Type.ELEVENSES;
        }
    }

    public void filter(){
        if (meals1 != null) meals1.clear();
        for(Meal m : meals2) {
            if (p.equals("--wybierz--") || ((et_szukaj.getText().toString().length() == 0 || m.getName().contains(et_szukaj.getText().toString()))
                    && (p.equals("all") ||
                    m.getType().equals(p))
                    && m.getDateTime().contains(tvDate.getText()))) {
                meals1.add(m);
            }

            if (meals1.isEmpty()) {
                myMealsListView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                adapter1.notifyDataSetChanged();
                myMealsListView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    public void setDateOnClick(View v)
    {
        DialogFragment date_pick_fragment = new DatePickerFragment();
        ((MealsListActivity.DatePickerFragment)date_pick_fragment).setActivity(this);
        date_pick_fragment.show(getSupportFragmentManager(), "datePicker");
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
        String d = timeToString(year) + "-" + timeToString(month) + "-" + timeToString(day);
        tvDate.setText(d);
        filter();
    }

    /*Date picker fragment
    * Fragment do wybierania daty wizyty*/
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private MealsListActivity wActivity;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            int year, month, day;
            Calendar c = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            /*Ustawiamy date jako te wczesniej wybrana przez uzytkownika
             *chyba ze jeszcze nic nie wybral, albo format danych jest niepoprawny,
             *wtedy uzywamy aktualnej daty jako podstawowego wyboru*/
            String date = tvDate.getText().toString();

            try {
                c.setTime(format.parse(date));
            }catch (ParseException e) {}

            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void setActivity(MealsListActivity activity)
        {
            wActivity= activity;
        }

        public void onDateSet(DatePicker view, int year, int month, int day)
        {
            wActivity.setDate(year, month, day);
        }
    }
}
