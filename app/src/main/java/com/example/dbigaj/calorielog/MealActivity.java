package com.example.dbigaj.calorielog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MealActivity extends AppCompatActivity {
    private final static int REQUEST_GALLERY = 0;
    private TextView dateTime;
    private EditText name, caloriesAmount;
    private Button bRemove, bEdit;
    private ImageView photo;
    private Spinner type;
    private String mid;
    private int action;
    private final int EDIT = 1;
    private ArrayAdapter<String> adapter;

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
        ArrayList<String> types = new ArrayList<String>();
        types.add("--wybierz--");
        types.add("breakfast");
        types.add("dinner");
        types.add("supper");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        action = getIntent().getIntExtra("action", -1);

        if (action == EDIT) {
            String[] meals_table = getIntent().getStringArrayExtra("meals");

            mid = meals_table[0];
            Picasso.with(this).load(meals_table[1]).into(photo);
            name.setText(meals_table[2]);
            dateTime.setText(meals_table[3]);
            type.setSelection(adapter.getPosition(meals_table[4]));
            caloriesAmount.setText(meals_table[5]);

        } else {
            ViewGroup layout = (ViewGroup) bRemove.getParent();
            layout.removeView(bRemove);
            Calendar c = Calendar.getInstance();
            String date = new SimpleDateFormat("dd-MM-yyyy").format(c.getTime());
            String time = new SimpleDateFormat("hh:mm").format(c.getTime());
            dateTime.setText(date+" "+time);
        }

    }

    public void onClick(View v) {
        if(type.getSelectedItem().toString().equals("--wybierz--") || name.toString().equals("") || caloriesAmount.toString().equals("")){
            showDialog(0);
        } else {
            if (action == EDIT) editMeal();
            else addMeal();
            startActivity(new Intent(getApplicationContext(), MealsListActivity.class));
        }
    }

    void addMeal(){

    };

    void editMeal(){

    };

    //Okno dialogowe informujące i potrzebie wybrania choroby jakiej dotyczy notatka
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        //dialogBuilder.setTitle("Usuwanie notatki");
        dialogBuilder.setTitle("All fields needs to be filled!");
        //dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton("Ok", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        return dialogBuilder.create();
    }

    public void fromGalleryOnClick(View view)
    {
        File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirPath = pictureDir.getPath();
        Uri data = Uri.parse(pictureDirPath);

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(data, "image/*");

        startActivityForResult(intent, REQUEST_GALLERY);
    }
}
