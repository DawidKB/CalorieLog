package com.example.dbigaj.calorielog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
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
    private String uid, p;
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

        myMealsListView = (RecyclerView) findViewById(R.id.listMyMeals);
        emptyView = (TextView) findViewById(R.id.emptyView);
        tvDate = (TextView) findViewById(R.id.textViewDate);
        type = (Spinner) findViewById(R.id.spinnerType);

        ArrayList<String> types = new ArrayList<String>();
        types.add("all");
        types.add("breakfast");
        types.add("dinner");
        types.add("supper");
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, types);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        type.setAdapter(adapter2);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                p = parent.getItemAtPosition(position).toString();
                filter();
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

        searchEvents(getApplicationContext(), uid);
    }

    public static void searchEvents(final Context context, final String id) {
        new Thread() {
            public void run() {
                meals1 = getMeals(context, id);
                meals2 = new ArrayList<>(meals1);
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

    public static ArrayList<Meal> getMeals(final Context context, final String id) {
        try {
            URL url = new URL(String.format("http://156.17.42.122:8000/roles/?account_id=" + id));
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.addRequestProperty("User-Agent", "my-rest-app");

            JsonReader reader = new JsonReader(new InputStreamReader(connection.getInputStream()));

            Meal meal;
            ArrayList<Meal> events = new ArrayList<>();

            reader.beginArray();
            while (reader.hasNext()) {
                meal = new Meal();
                reader.beginObject();
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("mealId")) {
                        meal.setMid(reader.nextString());
                    } else if (name.equals("name")) {
                        meal.setName(reader.nextString());
                    } else if (name.equals("caloriesAmount")) {
                        meal.setCaloriesAmount(reader.nextString());
                    } else if (name.equals("dateTime")) {
                        meal.setDateTime(reader.nextString());
                    } else if (name.equals("Type")) {
                        String type = reader.nextString().toUpperCase();
                        if (type.equals(Type.BREAKFAST.toString())) {
                            meal.setType((Type.BREAKFAST));
                        }
                        if (type.equals(Type.DINNER.toString())) {
                            meal.setType((Type.DINNER));
                        }
                        else {
                            meal.setType((Type.SUPPER));
                        }
                    } else if (name.equals("photo")) {
                        meal.setPhoto(reader.nextString());
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
                return null;
            }
            connection.disconnect();
            return events;
        } catch (Exception e) {
            return null;
        }
    }

    public void filter(){
        meals1.clear();
        for(Meal m : meals2) {
            if (m.getName().contains(et_szukaj.getText().toString()) && (p.equals("all") || m.getType().equals(p))
                    && m.getDateTime().contains(tvDate.getText())) {
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
        String d = timeToString(day) + "/" + timeToString(month) + "/" + timeToString(year);
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
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

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
