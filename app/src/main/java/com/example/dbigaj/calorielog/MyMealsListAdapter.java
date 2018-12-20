package com.example.dbigaj.calorielog;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by DBIGAJ on 2018-03-20.
 */

public class MyMealsListAdapter extends RecyclerView.Adapter<MyMealsListAdapter.MyViewHolder> {
    private Context context;
    private final List<Meal> myMeals;

    @Override
    public MyMealsListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.meals_list_row, parent, false);
        return new MyMealsListAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyMealsListAdapter.MyViewHolder holder, int position) {
        holder.m = myMeals.get(position);

        holder.mid = holder.m.getMid();
        holder.uid = holder.m.getUid();
        holder.photoS = holder.m.getPhoto();
        //Picasso.with(context).load(holder.m.getPhoto()).into(holder.photo);
        holder.name.setText(holder.m.getName());
        holder.dateTime.setText(holder.m.getDateTime());
        if (holder.m.getType() == null) holder.type.setText("Supper");
        else holder.type.setText(holder.m.getType().toString().toLowerCase());
        holder.caloriesAmount.setText(holder.m.getCaloriesAmount());
    }

    @Override
    public int getItemCount() {
        return myMeals.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView photo;
        private String photoS;
        private TextView name, dateTime, type, caloriesAmount;
        private String mid, uid;
        private Meal m;

        MyViewHolder(View view) {
            super(view);
            photo = (ImageView) view.findViewById(R.id.photo);
            name = (TextView) view.findViewById(R.id.name);
            dateTime = (TextView) view.findViewById(R.id.dateTime);
            type = (TextView) view.findViewById(R.id.type);
            caloriesAmount = (TextView) view.findViewById(R.id.caloriesAmount);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MealActivity.class);
                    String[] meals_table = {mid, photoS, name.getText().toString(), dateTime.getText().toString(), type.getText().toString(),
                            caloriesAmount.getText().toString()};
                    intent.putExtra("uid", uid);
                    intent.putExtra("meals", meals_table);
                    intent.putExtra("action", 1);
                    context.startActivity(intent);
                }
            });

        }
    }

    public MyMealsListAdapter(List<Meal> myMeals) {
        this.myMeals = myMeals;
    }
}
