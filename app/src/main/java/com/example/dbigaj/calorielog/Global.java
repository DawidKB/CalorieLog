package com.example.dbigaj.calorielog;

/**
 * Created by dbigaj on 2019-01-10.
 */

public class Global {
    private String url = "https://meal-diary-api.herokuapp.com";

    public Global() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
