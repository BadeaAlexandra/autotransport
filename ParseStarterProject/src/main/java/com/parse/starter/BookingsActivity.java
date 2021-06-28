package com.parse.starter;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class BookingsActivity extends AppCompatActivity {

    String currency = "lei";
    double ronValue = 4.7;
    public class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL (urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result = result + current;
                    data = reader.read();
                }
                return result;
            }
            catch (Exception e) {
                e.printStackTrace();
                return "Fail";
            }
        }

        protected void onPostExecute (String s) {
            super.onPostExecute(s);
            Log.i("JSON",s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                String currencyInfo = jsonObject.getString("rates");
                Log.i("Currency info",currencyInfo);

                //double ronValue = 4.5;
                currencyInfo = currencyInfo.substring(1,currencyInfo.length()-1);
                String[] splitString = currencyInfo.split(",");
                for (int i = 0; i < splitString.length; i++) {
                    //Log.i("Valoarea valutei " + String.valueOf(i),splitString[i]);
                    if (splitString[i].contains("RON"))
                        ronValue = Double.parseDouble(splitString[i].substring(6,splitString[i].length()));
                }
                //Log.i ("Valoare curenta a monedei ron este",String.valueOf(ronValue));

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //region declarari

    private String username;
    private String email;

    private ListView bookingListView = null;
    private ArrayList<Booking> bookingArrayList = new ArrayList<Booking>();
    private BookingAdapter adapter;
    private ConnectionHelper connectionHelper;
    private boolean success = false;

    private ActionBar actionBar;

    //endregion

    //region meniul din dreapta

    public boolean onCreateOptionsMenu (Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.result_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.currency:
                if (item.getTitle().equals("Schimba in euro")) {
                    for (int i = 0; i < bookingArrayList.size(); i++) {
                        double priceInEuro = bookingArrayList.get(i).getPrice()/ronValue;
                        bookingArrayList.get(i).setPrice(priceInEuro);
                        bookingArrayList.get(i).setCurrency("euro");
                        currency = "euro";
                        BookingAdapter.priceTextViewB.setText(Double.toString(priceInEuro) + " €");
                    }
                    adapter.notifyDataSetChanged();
                    item.setTitle("Schimba in lei");
                }
                else {
                    for (int i = 0; i < bookingArrayList.size(); i++) {
                        double priceInLei = bookingArrayList.get(i).getPrice()*ronValue;
                        bookingArrayList.get(i).setPrice(priceInLei);
                        bookingArrayList.get(i).setCurrency("lei");
                        currency = "lei";
                        BookingAdapter.priceTextViewB.setText(Double.toString(priceInLei) + " lei");
                    }
                    adapter.notifyDataSetChanged();
                    item.setTitle("Schimba in euro");
                }
                return true;

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
        setContentView(R.layout.activity_bookings);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3f48cc")));

        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");

        //region lansarea task-ului de cautare a valutei
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://api.exchangeratesapi.io/latest");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        bookingListView = (ListView) findViewById(R.id.bookingsListview);
        connectionHelper = new ConnectionHelper();

        TicketsSync sync = new TicketsSync();
        sync.execute("");

    }

    private class TicketsSync extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BookingsActivity.this, "Se sincronizeaza datele",
                    "Se incarca rezultatele! Te rugam sa astepti...", true);
        }

        @SuppressLint("WrongThread")
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
                    String query ="SELECT b.data_emitere, b.serie, cz.data AS data_cursa, o1.nume_oras AS oras_plecare, o2.nume_oras AS oras_sosire, c.ora_plecare, c.ora_sosire, f.nume_firma, c.pret\n" +
                                  "FROM ((((bilete AS b INNER JOIN curse_zilnice AS cz ON b.cursa_zilnica = cz.id_cursa_zilnica)\n" +
                                  "INNER JOIN curse AS c ON cz.cursa = c.id_cursa)\n" +
                                  "INNER JOIN orase AS o1 ON c.plecare = o1.id_oras)\n" +
                                  "INNER JOIN orase AS o2 ON c.sosire = o2.id_oras)\n" +
                                  "INNER JOIN firme_transport as f ON c.firma=f.id_firma\n" +
                                  "WHERE b.email = '" + email + "';";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if (rs != null) // if resultset not null, I add items to resultArraylist using class created
                    {
                        while (rs.next())
                        {
                            try {
                                String numePoza = rs.getString("nume_firma");
                                numePoza = numePoza.replaceAll("\\s","");
                                numePoza = numePoza.toLowerCase();

                                int resID = getResources().getIdentifier(numePoza, "drawable", getPackageName());

                                //String departure, String arrival, String departureDate, String departureHour,
                                // String arrivalHour, double price, String company, int companyImageID, String series, String issueDate

                                bookingArrayList.add(new Booking(rs.getString("oras_plecare"),rs.getString("oras_sosire"),rs.getString("data_cursa"),
                                        rs.getString("ora_plecare"),rs.getString("ora_sosire"), rs.getDouble("pret"),
                                        rs.getString("nume_firma"), resID, rs.getString("serie"),rs.getString("data_emitere")));

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
            Toast.makeText(BookingsActivity.this, msg + "", Toast.LENGTH_LONG).show();

            if (success == false) {
                ;
            }
            else {
                try {
                    adapter = new BookingAdapter(BookingsActivity.this,bookingArrayList);
                    bookingListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    bookingListView.setAdapter(adapter);

                    int numberOfResults = bookingListView.getAdapter().getCount();
                    Toast.makeText(BookingsActivity.this,"S-au gasit " + numberOfResults + " rezultate",Toast.LENGTH_LONG).show();

                } catch (Exception ex)
                {
                    Toast.makeText(BookingsActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

        }
    }
}
