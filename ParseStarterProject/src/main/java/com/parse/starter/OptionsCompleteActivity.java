package com.parse.starter;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar ;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.parse.starter.Result.hourAscendingComparator;
import static com.parse.starter.Result.hourDescendingComparator;
import static com.parse.starter.Result.priceDescendingComparator;

public class OptionsCompleteActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    //region declarari obiecte

    private RelativeLayout relativeLayout;
    private TextView helloTextView;
    private AutoCompleteTextView departureTextView;
    private AutoCompleteTextView arrivalTextView;
    private TextView dateTextView;
    private EditText numberOfTicketsEditText;
    private Button searchButton;

    int year, month, dayOfMonth;
    Calendar calendar;
    DatePickerDialog datePickerDialog;

    private ConnectionHelper connectionHelper,connectionHelper2;

    private String username = null;
    private boolean success = false;
    private ArrayList<String> Cities = new ArrayList<String>();

    private DrawerLayout drawer;
    private TextView usernameHeader;
    private TextView emailHeader;
    private String emailH = null;

    private String currency = "lei";

    //endregion

    public void goBack(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.relativeLayout || v.getId() == R.id.helloTextView) {  //dismiss the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    public void searchResults (View view) {

        if (departureTextView.getText().toString().trim().length() > 0 && arrivalTextView.getText().toString().trim().length() > 0 && dateTextView.getText().toString().trim().length() > 0 && numberOfTicketsEditText.getText().toString().trim().length() > 0) {
            Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
            intent.putExtra("departure", departureTextView.getText().toString());
            intent.putExtra("arrival", arrivalTextView.getText().toString());
            intent.putExtra("date", dateTextView.getText().toString());
            intent.putExtra("numberOfTickets", numberOfTicketsEditText.getText().toString());        //String.valueOf(numberOfTicketsEditText)
            intent.putExtra("username",username);
            intent.putExtra("email",emailH);
            startActivity(intent);
        }
        else Toast.makeText(this,"Toate campurile sunt obligatorii",Toast.LENGTH_LONG).show();
    }

    //region meniul din stanga

    public void onBackPressed() {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            else {
                super.onBackPressed();
            }
        }

    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.profile) {
            if (username != null) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("username",username);
                startActivity(intent);
            }
            else Toast.makeText(this,"Ne pare rau, dar nu esti autentificat",Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bookings) {
            if (username != null) {
                Intent intent = new Intent(getApplicationContext(), BookingsActivity.class);
                intent.putExtra("username",username);
                intent.putExtra("email",emailH);
                startActivity(intent);
            }
            else {
                Toast.makeText(this,"Ne pare rau, dar nu esti autentificat",Toast.LENGTH_LONG).show();
            }
        } else if (id == R.id.help) {
            Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
            startActivity(intent);
        } else if (id == R.id.termsAndConditions) {
            Intent intent = new Intent(getApplicationContext(), TermsConditionsActivity.class);
            startActivity(intent);
        } else if (id == R.id.contact) {
            Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
            startActivity(intent);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options_complete);

        //region meniul din stanga

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //endregion

        //region cautarea casetelor de text in resurse

        helloTextView = (TextView) findViewById(R.id.helloTextView);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        if (username == null) helloTextView.setText("Bine ai venit!");
            else helloTextView.setText("Buna, " + username + "!");

        connectionHelper = new ConnectionHelper();
        OptionsCompleteActivity.SyncData orderData = new OptionsCompleteActivity.SyncData();
        orderData.execute("");

        departureTextView = (AutoCompleteTextView) findViewById(R.id.departureTextView);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Cities);
        departureTextView.setAdapter(adapter1);

        arrivalTextView = (AutoCompleteTextView) findViewById(R.id.arrivalTextView);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,Cities);
        arrivalTextView.setAdapter(adapter2);

        dateTextView = (TextView) findViewById(R.id.dateTextView);
        numberOfTicketsEditText = (EditText) findViewById(R.id.numberOfTicketsEditText);
        searchButton = (Button) findViewById(R.id.searchButton);
        relativeLayout = findViewById(R.id.relativeLayout);


        View headerView = navigationView.getHeaderView(0);
        usernameHeader = headerView.findViewById(R.id.usernameHeader);
        emailHeader = headerView.findViewById(R.id.emailHeader);

        if (username == null) {
            usernameHeader.setText("Utilizator nou");
            emailHeader.setText("");
        }

        else {
            usernameHeader.setText(username);
            connectionHelper2 = new ConnectionHelper();
            EmailSync sync = new EmailSync();
            sync.execute("");

        }

        //endregion

        //region dismiss keyboard
        helloTextView.setOnClickListener(OptionsCompleteActivity.this);
        departureTextView.setOnClickListener(OptionsCompleteActivity.this);
        dateTextView.setOnClickListener(OptionsCompleteActivity.this);
        arrivalTextView.setOnClickListener(OptionsCompleteActivity.this);
        numberOfTicketsEditText.setOnClickListener(OptionsCompleteActivity.this);
        relativeLayout.setOnClickListener(OptionsCompleteActivity.this);
        //endregion

        //region implementarea calendarului
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar = Calendar.getInstance();
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(OptionsCompleteActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        month = month + 1;
                        String monthTxt = String.valueOf(month);
                        if (month < 10) monthTxt = "0" + monthTxt;
                        String dayOfMonthTxt = String.valueOf(dayOfMonth);
                        if (dayOfMonth < 10) dayOfMonthTxt = "0" + dayOfMonthTxt;
                        dateTextView.setText(dayOfMonthTxt +"/" + monthTxt + "/" + year);
                    }
                },year,month,dayOfMonth);

                datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

                calendar.add(Calendar.DAY_OF_MONTH, 30);
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

                datePickerDialog.show();
            }
        });
        //endregion

        //region numarul maxim de caractere ale campului numberOfTicketsEditText
        int maxLength = 1;
        numberOfTicketsEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        //endregion

    }


    private class SyncData extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";

        @Override
        protected void onPreExecute() {
            msg = "Entered onPreExecute";
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {
            try
            {
                Connection conn = connectionHelper.ConnectionMethod();        // Connect to database
                if (conn == null)
                {
                    msg = "Connection failed";
                    success = false;
                }
                else {
                    String query = "SELECT nume_oras FROM orase;";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if (rs != null) // if resultset not null, I add items to resultArraylist using class created
                    {
                        while (rs.next())
                        {
                            try {
                                Cities.add(rs.getString("nume_oras"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        conn.close();
                        msg = "Found";
                        success = true;

                    } else {
                        msg = "No data found!";
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
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) // disimissing progress dialoge, showing error and setting up my listview
        {
            Toast.makeText(OptionsCompleteActivity.this, msg, Toast.LENGTH_LONG).show();
        }
    }

    private class EmailSync extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";

        @Override
        protected void onPreExecute() {
            msg = "Entered onPreExecute";
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {
            try
            {
                Connection conn = connectionHelper2.ConnectionMethod();        // Connect to database
                if (conn == null)
                {
                    msg = "Connection failed";
                    success = false;
                }
                else {
                    String query =  "SELECT u.email\n" +
                                    "FROM utilizatori AS u\n" +
                                    "WHERE u.nume_utilizator = '" + username + "';";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if (rs != null) // if resultset not null, I add items to resultArraylist using class created
                    {
                        while (rs.next())
                        {
                            try {
                                emailH = rs.getString("email");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        conn.close();
                        msg = "Found";
                        success = true;

                    } else {
                        msg = "No data found!";
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
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) // disimissing progress dialoge, showing error and setting up my listview
        {
            emailHeader.setText(emailH);
            Toast.makeText(OptionsCompleteActivity.this, msg, Toast.LENGTH_LONG).show();
        }
    }

}
