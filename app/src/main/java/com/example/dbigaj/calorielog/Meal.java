package com.example.dbigaj.calorielog;

/**
 * Created by dbigaj on 2018-12-13.
 */

public class Meal {
    private String mid;
    private String name;
    private String caloriesAmount;
    private String dateTime;
    private Type type;
    public String photo;

    public Meal() {
    }

    public Meal(String mid, String name, String caloriesAmount, String dateTime, Type type, String photo) {
        this.mid = mid;
        this.name = name;
        this.caloriesAmount = caloriesAmount;
        this.dateTime = dateTime;
        this.type = type;
        this.photo = photo;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCaloriesAmount() {
        return caloriesAmount;
    }

    public void setCaloriesAmount(String caloriesAmount) {
        this.caloriesAmount = caloriesAmount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
