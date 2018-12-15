package com.example.dbigaj.calorielog;

/**
 * Created by Dawid on 2017-12-04.
 */

interface LoginView {

    String getEmail();

    String getPassword();

    void showError(int s);

    void signIn();

    boolean checkUser();

    void startMenuActivity();
}
