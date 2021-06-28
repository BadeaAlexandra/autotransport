/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse.starter;

import android.annotation.SuppressLint;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;

//import static com.google.android.gms.analytics.internal.zzy.i;

//Parse Password from EC2 -> Actions -> Instance Settings -> Get System Log
//X5xVLMtl2GlQ

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    //region declarari

    RelativeLayout registrationLayout;
    ImageView logoImageView;
    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    TextView signUpTextView;
    TextView skipTextView;
    private ConnectionHelper connectionHelper;
    String username, password;

    //endregion

    ActionBar actionBar;

    @SuppressLint("NewApi")
    @Override
    public void onClick (View view) {
        if (view.getId() == R.id.signUpTextView) {
            Log.i("Log in","Sign up option selected");
            Intent intent = new Intent(getApplicationContext(),SignUpActivity.class);
            startActivity(intent);
        }
        else if (view.getId() == R.id.skipTextView) {
            Log.i("Log in","Skip this step selected");
          //  Intent intent = new Intent(getApplicationContext(),OptionsCompleteActivity.class);

            Intent intent = new Intent(getApplicationContext(),OptionsCompleteActivity.class);
            startActivity(intent);
        }

        else if (view.getId() == R.id.logoImageView || view.getId() == R.id.registrationLayout) {  //dismiss the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent event) {  //la mine in loc de i aveam keyCode si in loc de view, v      in loc de keyEvent am event

        if (i == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
          //  loginButtonClicked(view);
        }
        return false;
    }

    //region meniul din dreapta

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.language:
                Toast.makeText(this, "Change language", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.romanian:
                Toast.makeText(this, "Select romanian", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.english:
                Toast.makeText(this, "Select english", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.help:
                Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
                return true;

            case R.id.contactUs:
                intent = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3f48cc")));


        //region initializari

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpTextView);
        skipTextView = findViewById(R.id.skipTextView);
        logoImageView = findViewById(R.id.logoImageView);
        registrationLayout = findViewById(R.id.registrationLayout);

        //endregion

        //region dezactivare keyboard
        signUpTextView.setOnClickListener(this);
        skipTextView.setOnClickListener(this);
        passwordEditText.setOnKeyListener(this);
        logoImageView.setOnClickListener(this);
        registrationLayout.setOnClickListener(this);
        //endregion

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionHelper = new ConnectionHelper();
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                CheckLogin checkLogin = new CheckLogin();  //the AsyncTask
                checkLogin.execute("");
            }
        });

    }

    private class CheckLogin extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";
        Boolean success = false;

        @Override
        protected void onPreExecute() {
            msg = "Entered onPreExecute";
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {

            if (username.trim().equals("") || password.trim().equals(""))
                msg = "Please enter username and password";
            else {
                try
                {
                    Connection conn = connectionHelper.ConnectionMethod();        // Connect to database
                    if (conn == null)
                    {
                        msg = "Connection failed";
                        success = false;
                    }
                    else {
                        String query = "SELECT *\n" +
                                "FROM utilizatori\n" +
                                "WHERE nume_utilizator = '" + username + "' AND parola = '" + password + "';";
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(query);

                        if (rs.next()) // if resultset not null, I add items to resultArraylist using class created
                        {
                            msg = "Login successful";
                            success = true;
                            Intent intent = new Intent(getApplicationContext(),OptionsCompleteActivity.class);
                            intent.putExtra("username",username);
                            startActivity(intent);
                            conn.close();

                        } else {
                            msg = "Invalid username or password";
                            success = false;
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    Writer writer = new StringWriter();
                    e.printStackTrace(new PrintWriter(writer));
                    msg = writer.toString();
                    success = false;
                }
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) // disimissing progress dialogue, showing error and setting up my listview
        {
            Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            if (success)
            {
                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_LONG).show();
            }
        }
    }
}