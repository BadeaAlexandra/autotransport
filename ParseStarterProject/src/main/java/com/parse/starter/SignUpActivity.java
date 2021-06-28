package com.parse.starter;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    //region declarari

    private ActionBar actionBar;

    RelativeLayout relativeLayout;
    EditText firstNameEditText;
    EditText lastNameEditText;
    TextView birthdateTextView;
    EditText usernameEditText;
    EditText passwordEditText;
    EditText emailEditText;
    ImageView logoImageView;
    Button signUpButton;
    private ConnectionHelper connectionHelper;
    private String firstName,lastName,birthdate, username, password, email;
    private ArrayList<String> Users = new ArrayList<String>();
    private ArrayList<String> Emails = new ArrayList<String>();

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^" +
            "(?=.*[0-9])" +         //cel putin o cifra
            "(?=.*[a-z])" +         //cel putin o litera mica
            "(?=.*[A-Z])" +         //cel putin o litera mare
            "(?=\\S+$)" +           //niciun spatiu
            ".{4,}" +               //cel putin 4 caractere
            "$");

    DatePickerDialog.OnDateSetListener dateSetListener;

    //endregion

    public void goBack(View view) {
        finish();
    }

    //dismiss keyboard method
    public void onClick(View v) {
        if (v.getId() == R.id.relativeLayout || v.getId() == R.id.logoImageView) {  //dismiss the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    private boolean validateAge() {

        try {
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");

            Date selectedDate = format.parse(birthdate);
            SimpleDateFormat selectedDay = new SimpleDateFormat("dd");
            SimpleDateFormat selectedMonth = new SimpleDateFormat("MM");
            SimpleDateFormat selectedYear = new SimpleDateFormat("yyyy");

            int dayOfBirth = Integer.parseInt(selectedDay.format(selectedDate));
            int monthOfBirth = Integer.parseInt(selectedMonth.format(selectedDate));
            int yearOfBirth = Integer.parseInt(selectedYear.format(selectedDate));

            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            int currentMonth = calendar.get(Calendar.MONTH) + 1;
            int currentYear = calendar.get(Calendar.YEAR);
            int currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


            if (currentYear - yearOfBirth > 18) return true;
            else if (currentYear - yearOfBirth < 18) return false;
            else {
                if (currentMonth > monthOfBirth) return true;
                else if (currentMonth < monthOfBirth) return false;
                else {
                    if (currentDayOfMonth >= dayOfBirth) return true;
                    return false;
                }
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean validateUsername() {
        if (username.length() > 15) {
            return false;
        }
        else return true;
    }

    private boolean validatePassword() {
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return false;
        }
        else return true;
    }

    private boolean validateEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        else return true;
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
        setContentView(R.layout.activity_sign_up);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3f48cc")));

        //region initializari obiecte
        relativeLayout = findViewById(R.id.relativeLayout);
        logoImageView = findViewById(R.id.logoImageView);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        birthdateTextView = findViewById(R.id.birthdateTextView);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailEditText = findViewById(R.id.emailEditText);
        signUpButton = findViewById(R.id.signUpButton);

        connectionHelper = new ConnectionHelper();
        //endregion

        //region dismiss keyboard
        relativeLayout.setOnClickListener(SignUpActivity.this);
        logoImageView.setOnClickListener(SignUpActivity.this);
        firstNameEditText.setOnClickListener(SignUpActivity.this);
        lastNameEditText.setOnClickListener(SignUpActivity.this);
        birthdateTextView.setOnClickListener(SignUpActivity.this);
        usernameEditText.setOnClickListener(SignUpActivity.this);
        passwordEditText.setOnClickListener(SignUpActivity.this);
        emailEditText.setOnClickListener(SignUpActivity.this);
        //endregion


        //region functionalitatea campului de data nasterii
        birthdateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(SignUpActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener, year, month, day);

                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String monthTxt = String.valueOf(month);
                if (month < 10) monthTxt = "0" + monthTxt;
                String dayOfMonthTxt = String.valueOf(dayOfMonth);
                if (dayOfMonth < 10) dayOfMonthTxt = "0" + dayOfMonthTxt;
                birthdateTextView.setText(dayOfMonthTxt + "/" + monthTxt + "/" + year);
            }
        };

        //endregion

        //region butonul de SIGN UP
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = firstNameEditText.getText().toString();
                lastName = lastNameEditText.getText().toString();
                birthdate= birthdateTextView.getText().toString();
                username = usernameEditText.getText().toString();
                password = passwordEditText.getText().toString();
                email = emailEditText.getText().toString();

                CheckSignUp checkSignUp = new CheckSignUp();  //the AsyncTask
                checkSignUp.execute();
            }
        });
        //endregion
    }

    private class CheckSignUp extends AsyncTask<String, String, String> {

        Boolean success = false;
        String msg = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {

            if (firstName.trim().equals("") || lastName.trim().equals("") || birthdate.trim().equals("") ||
                    username.trim().equals("") || password.trim().equals("") || email.trim().equals("")) {
                msg = "Toate campurile sunt obligatorii";

            }
            else if (validateAge() == false) {  //validarea fiecarui camp pe rand cu else if
                msg = "Trebuie sa ai macar 18 ani";
            }
            else if (validateUsername() == false ) {
                msg = "Username invalid";
            }
            else if (validatePassword() == false) {
                msg = "Parola prea slaba";
            }
            else if (validateEmail() == false) {
                msg = "E-mail invalid";
            }
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
                        success = true;
                        Statement stm = conn.createStatement();
                        String insertQuery = "INSERT INTO utilizatori(nume,prenume,data_nasterii,nume_utilizator,parola,email) VALUES (?,?,?,?,?,?)";
                        PreparedStatement pst = conn.prepareStatement(insertQuery);


                        //zona username
                        String usernameQuery = "SELECT nume_utilizator FROM utilizatori";
                        Statement stm1 = conn.createStatement();
                        ResultSet rs1 = stm1.executeQuery(usernameQuery);

                        if (rs1 != null) // if resultset not null, I add items to resultArraylist using class created
                        {
                            while (rs1.next()) {
                                try {
                                    Users.add(rs1.getString("nume_utilizator"));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        if (Users.contains(username)) {
                            success = false;
                            msg = "Username deja existent";
                        }

                        //zona e-mail
                        String emailQuery = "SELECT email FROM utilizatori";
                        Statement stm2 = conn.createStatement();
                        ResultSet rs2 = stm2.executeQuery(emailQuery);

                        if (rs2 != null) // if resultset not null, I add items to resultArraylist using class created
                        {
                            while (rs2.next()) {
                                try {
                                    Emails.add(rs2.getString("email"));
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        if (Emails.contains(email)) {
                            success = false;
                            msg = "E-mail a mai fost folosit";
                        }


                        if (success == true) {
                            pst.setString(1, firstName);
                            pst.setString(2, lastName);
                            pst.setString(3, birthdate);
                            pst.setString(4, username);
                            pst.setString(5, password);
                            pst.setString(6, email);
                            pst.executeUpdate();

                            msg = "Inregistrare finalizata cu succes";

                            Intent intent = new Intent(getApplicationContext(), OptionsCompleteActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
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
            Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_LONG).show();
            if (success)
            {
                Toast.makeText(SignUpActivity.this, "Inregistrare reusita", Toast.LENGTH_LONG).show();
            }
        }
    }

}
