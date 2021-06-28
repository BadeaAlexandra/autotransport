package com.parse.starter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProfileActivity extends AppCompatActivity {

    //region declarari

    private TextView profileFirstNameTextView;
    private TextView profileLastNameTextView;
    private TextView profileBirthdateTextView;
    private TextView profileUsernameTextView;
    private TextView profileEmailTextView;
    private Button editButton;

    private String username;

    private ConnectionHelper connectionHelper;
    private boolean success = false;

    private String firstName, lastName, birthdate,email;

    private ActionBar actionBar;

    //endregion

    //region meniul din dreapta

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);
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
                Toast.makeText(this, "Ajutor", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
                return true;

            default:
                return false;
        }
    }

    //endregion

    public void goBack(View view) {     //intent-ul de trecere de la OptionsCompleteActivity la activitatea curenta
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3f48cc")));

        //region initializari

        profileFirstNameTextView = findViewById(R.id.profileFirstNameTextView);
        profileLastNameTextView = findViewById(R.id.profileLastNameTextView);
        profileBirthdateTextView = findViewById(R.id.profileBirthdateTextView);
        profileUsernameTextView = findViewById(R.id.profileUsernameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        editButton = findViewById(R.id.editButton);
        connectionHelper = new ConnectionHelper();

        //endregion

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        profileUsernameTextView.setText("Nume utilizator: " + username);

        DataSync sync = new DataSync();
        sync.execute("");

    }

    private class DataSync extends AsyncTask<String, String, String> {

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
                    String query =  "SELECT u.nume, u.prenume, u.data_nasterii, u.email\n" +
                            "FROM utilizatori AS u\n" +
                            "WHERE u.nume_utilizator = '" + username + "';";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if (rs != null) // if resultset not null, I add items to resultArraylist using class created
                    {
                        while (rs.next())
                        {
                            try {
                                firstName = rs.getString("nume");
                                lastName = rs.getString("prenume");
                                birthdate = rs.getString("data_nasterii");
                                email = rs.getString("email");
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
            profileFirstNameTextView.setText("Nume: " + firstName);
            profileLastNameTextView.setText("Prenume: " + lastName);
            profileBirthdateTextView.setText("Data nasterii: " + birthdate);
            profileEmailTextView.setText("E-mail: " + email);
        }
    }

}
