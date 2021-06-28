package com.parse.starter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PurchaseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //region declarari variabile

    private ActionBar actionBar;

    private String emailContent = null;
    private int currentDayOfMonth, currentMonth, currentYear;
    private String currentDate;
    private String departureFormatDate;
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    String username;
    private ConnectionHelper connectionHelper1,connectionHelper2;
    private boolean success = false;
    TextView departureArrivalTextView, departureDateTextView, departureArrivalHourTextView, companyTextView, priceTextView;
    EditText firstNameEditText, lastNameEditText, emailEditText;
    TextView birthdateTextView;
    EditText cardOwnerEditText, cardNumberEditText, cvvEditText;
    Button purchaseButton;
    Spinner cardTypeSpinner, monthSpinner, yearSpinner;

    private String departureDate, departure, arrival, departureHour, arrivalHour, company;
    private int numberOfTickets;
    private double price;
    private String currency;
    private String firstName,lastName, birthdate, email;
    private String cardOwner, cardNumber, cvv;
    private int rideId;

    DatePickerDialog.OnDateSetListener dateSetListener;

    private TextView termsAndConditionsTextView;
    private CheckBox checkBox;

    private ArrayList<String> seriesArrayList = new ArrayList<String>();

    //endregion

    private int monthIndex (String month) {
        switch (month) {
            case "Ianuarie": return 1;
            case "Februarie": return 2;
            case "Martie": return 3;
            case "Aprilie": return 4;
            case "Mai": return 5;
            case "Iunie": return 6;
            case "Iulie": return 7;
            case "August": return 8;
            case "Septembrie": return 9;
            case "Octombrie": return 10;
            case "Noiembrie": return 11;
            case "Decembrie": return 12;
            default: return -1;
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
            Date date = calendar.getTime();
            int currentDate = calendar.get(Calendar.DATE);
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

    private boolean validateEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        else return true;
    }

    private boolean validateCardNumber () {
        if (cardNumber.length() == 19) return true;
        return false;
    }

    private boolean validateCVV () {
        if (cvv.length() == 3) return true;
        return false;
    }

    private boolean validateExpirationDate () {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        Date date = calendar.getTime();
        int currentDate = calendar.get(Calendar.DATE);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentYear = calendar.get(Calendar.YEAR);
        int month = monthIndex(monthSpinner.getSelectedItem().toString());
        int year = Integer.parseInt(yearSpinner.getSelectedItem().toString());
        if (year > currentYear) return true;
        else if (year == currentYear) {
            if (month > currentMonth) return true;
            else return false;
        }
        return false;
    }

    private boolean validateCheckbox () {
        if (checkBox.isChecked()) return true;
        return false;
    }

    private void sendMessage() {
        final ProgressDialog dialog = new ProgressDialog(PurchaseActivity.this);
        dialog.setTitle("Sending Email");
        dialog.setMessage("Please wait");
        dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    GMailSender sender = new GMailSender("autotransportapp@gmail.com", "Transport_service@@19");
                    sender.sendMail("Confirmation e-mail", emailContent, "autotransportapp@gmail.com", email);
                    dialog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        sender.start();
    }

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

    //region 2 metode pentru spinnere

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);

        //region declararea si initializarea casetelor de text

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3f48cc")));

        departureArrivalTextView = findViewById(R.id.departureArrivalTextView);
        departureDateTextView = findViewById(R.id.departureDateTextView);
        departureArrivalHourTextView = findViewById(R.id.departureArrivalHourTextView);
        companyTextView = findViewById(R.id.companyTextView);
        priceTextView = findViewById(R.id.priceTextView);

        emailEditText = findViewById(R.id.emailEditText);
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        birthdateTextView = findViewById(R.id.birthdateTextView);

        cardOwnerEditText = findViewById(R.id.cardOwnerEditText);
        cardNumberEditText = findViewById(R.id.cardNumberEditText);
        cvvEditText = findViewById(R.id.cvvEditText);

        termsAndConditionsTextView = findViewById(R.id.termsAndConditionsTextView);
        checkBox = findViewById(R.id.checkBox);
        purchaseButton = findViewById(R.id.purchaseButton);

        //endregion

        //region preluarea datelor prin intent

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        departureFormatDate = intent.getStringExtra("departureFormatDate");
        departureDate = intent.getStringExtra("departureDate");
        departure = intent.getStringExtra("departure");
        arrival = intent.getStringExtra("arrival");
        departureHour = intent.getStringExtra("departureHour");
        arrivalHour = intent.getStringExtra("arrivalHour");
        price = intent.getDoubleExtra("price",0);;
        company = intent.getStringExtra("company");
        rideId = intent.getIntExtra("rideId",0);
        currency = intent.getStringExtra("currency");
        numberOfTickets = intent.getIntExtra("numberOfTickets",0);

        //endregion

        //region detaliile de calatorie

        departureArrivalTextView.setText(departure + "-" + arrival);
        departureDateTextView.setText(departureDate);
        departureArrivalHourTextView.setText("Interval orar: " + departureHour + "-" + arrivalHour);
        companyTextView.setText("Companie: " + company);
        priceTextView.setText("Pret: " + df2.format(price) + " " + currency);

        //endregion

        //region completare nume, prenume, email si data nasterii din BD

        connectionHelper1 = new ConnectionHelper();
        SyncData syncData = new SyncData();
        syncData.execute("");

        firstNameEditText.setText(firstName);
        lastNameEditText.setText(lastName);
        emailEditText.setText(email);
        birthdateTextView.setText(birthdate);

        //endregion

        //region data curenta

        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String currentMonthTxt = String.valueOf(currentMonth);
        if (currentMonth < 10) currentMonthTxt = "0" + currentMonthTxt;
        String currentDayOfMonthTxt = String.valueOf(currentDayOfMonth);
        if (currentDayOfMonth < 10) currentDayOfMonthTxt = "0" + currentDayOfMonthTxt;
        currentDate = currentDayOfMonthTxt + "/" + currentDayOfMonthTxt + "/" + currentYear;

        //endregion

        //region functionalitatea campului de data nasterii

        birthdateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(PurchaseActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener, currentYear, currentMonth, currentDayOfMonth);

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

        //formatul campului cu 4 seturi de cate 4 numere

        cardNumberEditText.addTextChangedListener(new TextWatcher() {
            private static final char space = ' ';

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Remove all spacing char
                int pos = 0;
                while (true) {
                    if (pos >= s.length()) break;
                    if (space == s.charAt(pos) && (((pos + 1) % 5) != 0 || pos + 1 == s.length())) {
                        s.delete(pos, pos + 1);
                    } else {
                        pos++;
                    }
                }

                // Insert char where needed.
                pos = 4;
                while (true) {
                    if (pos >= s.length()) break;
                    final char c = s.charAt(pos);
                    // Only if its a digit where there should be a space we insert a space
                    if ("0123456789".indexOf(c) >= 0) {
                        s.insert(pos, "" + space);
                    }
                    pos += 5;
                }
            }
        });

        //region numarul maxim de caractere ale campului CVV
        int maxLength = 3;
        cvvEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        //endregion

        //region spinner card
        cardTypeSpinner = findViewById(R.id.cardTypeSpinner);
        cardTypeSpinner.setPrompt("Tipul cardului");
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,R.array.cardTypes,android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cardTypeSpinner.setAdapter(adapter1);
        cardTypeSpinner.setOnItemSelectedListener(this);
        //endregion

        //region spinner luna
        monthSpinner = findViewById(R.id.monthSpinner);
        monthSpinner.setPrompt("Luna");
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.months,android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(adapter2);
        monthSpinner.setOnItemSelectedListener(this);
        //endregion

        //region spinner an
        yearSpinner = findViewById(R.id.yearSpinner);
        yearSpinner.setPrompt("Anul");
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,R.array.years,android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(adapter3);
        yearSpinner.setOnItemSelectedListener(this);
        //endregion

        //region termeni si conditii

        termsAndConditionsTextView.setPaintFlags(termsAndConditionsTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        termsAndConditionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TermsConditionsActivity.class);
                startActivity(intent);
            }
        });

        //endregion

        //region butonul de purchase

        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstName = firstNameEditText.getText().toString();
                lastName = lastNameEditText.getText().toString();
                birthdate= birthdateTextView.getText().toString();
                email = emailEditText.getText().toString();

                cardOwner = cardOwnerEditText.getText().toString();
                cardNumber = cardNumberEditText.getText().toString();
                cvv = cvvEditText.getText().toString();

                connectionHelper2 = new ConnectionHelper();
                CheckPurchaseTask checkPurchaseTask = new CheckPurchaseTask();  //the AsyncTask
                checkPurchaseTask.execute();
            }
        });

        //endregion
    }

    private class SyncData extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(PurchaseActivity.this, "Synchronising",
                    "Se incarca rezultatele! Te rugam sa astepti...", true);
        }

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {
            try
            {
                Connection conn = connectionHelper1.ConnectionMethod();        // Connect to database
                if (conn == null)
                {
                    msg = "Connection failed";
                    success = false;
                }
                else {
                    String query = "SELECT u.nume, u.prenume, u.data_nasterii, u.email \n" +
                                   "FROM utilizatori AS u \n" +
                                   "WHERE u.nume_utilizator =  '" + username + "';";
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
            progress.dismiss();
            firstNameEditText.setText(firstName);
            lastNameEditText.setText(lastName);
            emailEditText.setText(email);
            birthdateTextView.setText(birthdate);
        }
    }

    private class CheckPurchaseTask extends AsyncTask<String, String, String> {

        Boolean success = false;
        String msg = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... strings)  // Connect to the database, write query and add items to array list
        {

            if (firstName.trim().equals("") || lastName.trim().equals("") || birthdate.trim().equals("") || email.trim().equals("") ||
                cardOwner.trim().equals("") || cardNumber.trim().equals("") || cvv.trim().equals(""))
            {
                msg = "Toate campurile sunt obligatorii";
            }
            else if (validateAge() == false) {  //validarea fiecarui camp pe rand cu else if
                msg = "Trebuie sa ai macar 18 ani";
            }
            else if (validateEmail() == false) {
                msg = "E-mail invalid";
            }
            else if (validateCardNumber() == false) {
                msg = "Numar de card invalid";
            }
            else if (validateCVV() == false) {
                msg = "CVV invalid";
            }
            else if (validateExpirationDate() == false) {
                msg = "Data de expirare invalida";
            }
            else if (validateCheckbox() == false) {
                msg = "Trebuie sa fii de acord cu termenii si conditiile";
            }
            else {
                try
                {
                    Connection conn = connectionHelper2.ConnectionMethod();        // Connect to database
                    if (conn == null)
                    {
                        msg = "Connection failed";
                        success = false;
                    }
                    else {
                        success = true;

                        int nr = 0;
                        Statement st = conn.createStatement();
                        String countQuery = "SELECT count (b.id_bilet) AS nrBilete\n" +
                                            "FROM bilete as b\n" +
                                            "WHERE b.cursa_zilnica = "+ rideId + "\n" +
                                            "GROUP BY b.cursa_zilnica;";

                        ResultSet rs = st.executeQuery(countQuery);

                        if (rs != null) // if resultset not null, I add items to resultArraylist using class created
                        {
                            while (rs.next())
                            {
                                try {
                                    nr = rs.getInt("nrBilete");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        String series = rideId + departureFormatDate;
                        series = series.replace("/","");

                        for (int i = nr + 1; i<= nr + numberOfTickets; i++) {
                            Statement stm = conn.createStatement();
                            String insertQuery = "INSERT INTO bilete(cursa_zilnica, serie, data_emitere, email) VALUES (?,?,?,?)";
                            PreparedStatement pst = conn.prepareStatement(insertQuery);

                            pst.setString(1, String.valueOf(rideId));
                            pst.setString(2, series + i);
                            seriesArrayList.add(series + i);
                            pst.setString(3, currentDate);
                            pst.setString(4, email);
                            pst.executeUpdate();

                            msg = "Achizitie finalizata cu succes";
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
            Toast.makeText(PurchaseActivity.this, msg, Toast.LENGTH_LONG).show();
            if (success)
            {
                Toast.makeText(PurchaseActivity.this, "Achizitie finalizata", Toast.LENGTH_LONG).show();
                if (numberOfTickets ==1){
                    emailContent = "Felicitari! \n" +
                            "Ai achizitionat un bilet de autocar cu urmatoarele detalii: \n" +
                            "Data plecarii: " + departureDate + "\n" +
                            departure + " - " + arrival + "\n" +
                            "Interval orar: " + departureHour + " - " + arrivalHour + "\n" +
                            "Companie: " + company + "\n" +
                            "Pret: " + df2.format(price) + " " + currency + "\n" +
                            "Seria biletului: \n" + seriesArrayList.get(0) + "\n";
                }
                else {
                    emailContent = "Felicitari! \n" +
                            "Ai achizitionat " + numberOfTickets + " bilete de autocar cu urmatoarele detalii: \n" +
                            "Data plecarii: " + departureDate + "\n" +
                            departure + " - " + arrival + "\n" +
                            "Interval orar: " + departureHour + " - " + arrivalHour + "\n" +
                            "Companie: " + company + "\n" +
                            "Pret: " + df2.format(price) + " " + currency + "\n" +
                            "Seriile biletelor: \n";
                    for (int i = 0; i < numberOfTickets; i++)
                    {
                        emailContent += seriesArrayList.get(i) + "\n";
                    }
                }

                sendMessage();
                Intent intent = new Intent(getApplicationContext(), EmailActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        }
    }

}