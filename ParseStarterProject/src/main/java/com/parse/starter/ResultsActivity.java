package com.parse.starter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import static com.parse.starter.Result.hourAscendingComparator;
import static com.parse.starter.Result.hourDescendingComparator;
import static com.parse.starter.Result.priceDescendingComparator;

public class ResultsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String username = null, email = null;

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
    }  //aceasta clasa face conversia valutara

    //region declarari
    private ListView resultListView = null;
    private ArrayList<Result> resultArrayList = new ArrayList<Result>();
    private ResultAdapter adapter;
    private Button sortButton;
    private ConnectionHelper connectionHelper;
    private boolean success = false;

    private int numberOfTickets;
    private String departure, arrival, date;

    private DrawerLayout drawer;
    private TextView usernameHeader;
    private TextView emailHeader;

    private Button filterButton;
    String[] listItems;
    boolean[] checkedItems;
    ArrayList<Integer> selectedItems = new ArrayList<>();
    String mItemSelected = null;
    String[] splitString;
    //endregion

    public void goBack(View view) {     //intent-ul de trecere de la OptionsCompleteActivity la activitatea curenta
        finish();
    }

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
                    for (int i = 0; i < resultArrayList.size(); i++) {
                        double priceInEuro = resultArrayList.get(i).getPrice()/ronValue;
                        resultArrayList.get(i).setPrice(priceInEuro);
                        resultArrayList.get(i).setCurrency("euro");
                        currency = "euro";
                        ResultAdapter.priceTextView.setText(Double.toString(priceInEuro) + " €");
                    }
                    adapter.notifyDataSetChanged();
                    item.setTitle("Schimba in lei");
                }
                else {
                    for (int i = 0; i < resultArrayList.size(); i++) {
                        double priceInLei = resultArrayList.get(i).getPrice()*ronValue;
                        resultArrayList.get(i).setPrice(priceInLei);
                        resultArrayList.get(i).setCurrency("lei");
                        currency = "lei";
                        ResultAdapter.priceTextView.setText(Double.toString(priceInLei) + " lei");
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
                intent.putExtra("email",email);
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
        setContentView(R.layout.activity_results);

        //region meniul din stanga

        Toolbar toolbar = findViewById(R.id.resultToolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.resultDrawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_result);
        navigationView.setNavigationItemSelectedListener(this);

        //endregion

        //region preluarea tuturor datelor completate de utilizator
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        email = intent.getStringExtra("email");
        departure = intent.getStringExtra("departure");
        arrival = intent.getStringExtra("arrival");
        numberOfTickets = Integer.parseInt(intent.getStringExtra("numberOfTickets"));
        date = intent.getStringExtra("date");
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateToShow = "";
        try {
            int month,year,dayOfMonth,dayOfWeek;
            Date date1 = format.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date1);
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            TextView selectedDateTextView = (TextView) findViewById(R.id.selectedDateTextView);
            dateToShow = dayName(dayOfWeek) + ", " + dayOfMonth + " " + monthName(month + 1) + " " + year;
            selectedDateTextView.setText(dateToShow);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // endregion

        //region header stanga
        View headerView = navigationView.getHeaderView(0);
        usernameHeader = headerView.findViewById(R.id.usernameHeader);
        emailHeader = headerView.findViewById(R.id.emailHeader);

        if (username == null) {
            usernameHeader.setText("Utilizator nou");
            emailHeader.setText("");
        }

        else {
            usernameHeader.setText(username);
            emailHeader.setText(email);
        }
        //endregion


        //region lansarea task-ului de cautare a valutei
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://api.exchangeratesapi.io/latest");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //endregion

        resultListView = (ListView) findViewById(R.id.resultListView);

        connectionHelper = new ConnectionHelper();
        SyncData orderData = new SyncData();
        orderData.execute("");

        //region intent spre purchase

        final String finalDateToShow = dateToShow;
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ResultsActivity.this,"Item " + Integer.toString((position + 1)) +" clicked", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), PurchaseActivity.class);
                intent.putExtra("username",username);
                intent.putExtra("departureFormatDate", date);
                intent.putExtra("departureDate", finalDateToShow);
                intent.putExtra("departure", resultArrayList.get(position).getDeparture());
                intent.putExtra("arrival", resultArrayList.get(position).getArrival());
                intent.putExtra("departureHour", resultArrayList.get(position).getDepartureHour());
                intent.putExtra("arrivalHour", resultArrayList.get(position).getArrivalHour());
                intent.putExtra("price", resultArrayList.get(position).getPrice());
                intent.putExtra("company", resultArrayList.get(position).getCompany());
                intent.putExtra("rideId",resultArrayList.get(position).getIdRide());
                intent.putExtra("currency",currency);
                intent.putExtra("numberOfTickets", numberOfTickets);
                startActivity(intent);
            }
        });

        //endregion

        // region butonul de sortare
        sortButton = (Button) findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(ResultsActivity.this,sortButton);
                popupMenu.getMenuInflater().inflate(R.menu.sort_popup_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(ResultsActivity.this,item.getTitle().toString(),Toast.LENGTH_LONG).show();
                        switch (item.getItemId()) {
                            case R.id.sortAscendingPrice:
                                //populateListView(numberOfTickets);

                                Collections.sort(resultArrayList,Result.priceAscendingComparator);
                                adapter = new ResultAdapter(ResultsActivity.this, resultArrayList);
                                resultListView.setAdapter(adapter);
                                return true;

                            case R.id.sortDescendingPrice:
                                Collections.sort(resultArrayList, priceDescendingComparator);
                                adapter = new ResultAdapter(ResultsActivity.this, resultArrayList);
                                resultListView.setAdapter(adapter);
                                return true;

                            case R.id.sortAscendingHour:
                                Collections.sort(resultArrayList, hourAscendingComparator);
                                adapter = new ResultAdapter(ResultsActivity.this, resultArrayList);
                                resultListView.setAdapter(adapter);
                                return true;

                            case R.id.sortDescendingHour:
                                Collections.sort(resultArrayList, hourDescendingComparator);
                                adapter = new ResultAdapter(ResultsActivity.this, resultArrayList);
                                resultListView.setAdapter(adapter);
                                return true;

                            default: return false;
                        }
                    }
                });
                popupMenu.show();
            }
        });
        //endregion

        //region butonul de filtrare

        filterButton = findViewById(R.id.filterButton);
        listItems = getResources().getStringArray(R.array.facilities_item);
        checkedItems = new boolean[listItems.length];

        filterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(ResultsActivity.this);
                mBuilder.setTitle("Facilitati");
                mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                        if (isChecked) {
                            if (!selectedItems.contains(position)) {
                                selectedItems.add(position);
                            }
                        }
                        else if (selectedItems.contains(position)) {
                            selectedItems.remove(position);
                        }
                    }

                });

                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        String item = "";
                        for (int i = 0; i < selectedItems.size(); i++) {
                            item = item + listItems[selectedItems.get(i)];
                            if (i != selectedItems.size() - 1) {
                                item = item + ",";
                            }
                        }
                        mItemSelected = item;

//                        splitString = mItemSelected.split(",");
//                        for (int i = splitString.length; i < listItems.length; i++)
//                            splitString[i] = splitString[0];

//                        resultArrayList.clear();
//                        FilterSyncData filterData = new FilterSyncData();
//                        filterData.execute("");

                    }
                });

                mBuilder.setNegativeButton("Renunta", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                });

                mBuilder.setNeutralButton("Reseteaza", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            checkedItems[i] = false;
                            selectedItems.clear();
                            mItemSelected = "";
                        }
                        resultArrayList.clear();
                        SyncData orderData = new SyncData();
                        orderData.execute("");
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();
            }
        });

        //endregion
    }

    static String monthName (int monthIndex) {
        switch (monthIndex) {
            case 1:  return "ianuarie";
            case 2:  return "februarie";
            case 3:  return "martie";
            case 4:  return "aprilie";
            case 5:  return "mai";
            case 6:  return "iunie";
            case 7:  return "iulie";
            case 8:  return "august";
            case 9:  return "septembrie";
            case 10: return "octombrie";
            case 11: return "noiembrie";
            case 12: return "decembrie";
            default: return null;
        }
    }
    static String dayName (int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:     return "Luni";  //=2
            case Calendar.TUESDAY:    return "Marți"; //=3
            case Calendar.WEDNESDAY:  return "Miercuri";
            case Calendar.THURSDAY:   return "Joi";
            case Calendar.FRIDAY:     return "Vineri";
            case Calendar.SATURDAY:   return "Sâmbătă";
            case Calendar.SUNDAY:     return "Duminică";  //=1
            default:                  return null;
        }
    }

    private class SyncData extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ResultsActivity.this, "Synchronising",
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
                    String query = "SELECT o1.nume_oras AS plecare, o2.nume_oras as sosire, c.ora_plecare, c.ora_sosire, c.pret, f.nume_firma, ta.nr_locuri, cz.id_cursa_zilnica\n" +
                                   "FROM ((((CURSE AS c INNER JOIN ORASE AS o1 ON c.plecare = o1.id_oras)\n" +
                                   "        INNER JOIN ORASE AS o2 ON c.sosire = o2.id_oras)\n" +
                                   "        INNER JOIN FIRME_TRANSPORT AS f ON c.firma = f.id_firma)\n" +
                                   "        INNER JOIN CURSE_ZILNICE AS cz ON cz.cursa = c.id_cursa)\n" +
                                   "        INNER JOIN TIP_AUTOCARE AS ta ON cz.tip_autocar = ta.id_tip\n" +
                                   "WHERE cz.data = '" + date + "' AND o1.nume_oras = '" + departure + "' \n" +
                                   "AND o2.nume_oras = '" + arrival + "' AND ta.nr_locuri >= '" + Integer.toString(numberOfTickets) + "';";
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
                                resultArrayList.add(new Result(rs.getString("plecare"),rs.getString("sosire"),date,
                                                               rs.getString("ora_plecare"),rs.getString("ora_sosire"),
                                                               numberOfTickets * Double.parseDouble(rs.getString("pret")) ,
                                                               rs.getString("nume_firma"), resID, rs.getInt("id_cursa_zilnica")));

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
            Toast.makeText(ResultsActivity.this, msg + "", Toast.LENGTH_LONG).show();

            if (success == false) {
                ;
            }
            else {
                try {
                    adapter = new ResultAdapter(ResultsActivity.this,resultArrayList);
                    resultListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    resultListView.setAdapter(adapter);

                    int numberOfResults = resultListView.getAdapter().getCount();
                    Toast.makeText(ResultsActivity.this,"S-au gasit " + numberOfResults + " rezultate",Toast.LENGTH_LONG).show();

                } catch (Exception ex)
                {
                    Toast.makeText(ResultsActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private class FilterSyncData extends AsyncTask<String, String, String> {

        String msg = "Internet/DB_Credentials/Windows_FireWall_TurnOn Error, See Android Monitor in the bottom For details!";

        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ResultsActivity.this, "Se sincronizeaza datele",
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
                    String query = "SELECT DISTINCT cz.data,o1.nume_oras AS plecare, o2.nume_oras as sosire, c.ora_plecare, c.ora_sosire, c.pret, ft.nume_firma, ta.nr_locuri, cz.id_cursa_zilnica \n" +
                                   "FROM ((((((CURSE AS c INNER JOIN ORASE AS o1 ON c.plecare = o1.id_oras)\n" +
                                   "INNER JOIN ORASE AS o2 ON c.sosire = o2.id_oras)\n" +
                                   "INNER JOIN FIRME_TRANSPORT AS ft ON c.firma = ft.id_firma)\n" +
                                   "INNER JOIN CURSE_ZILNICE AS cz ON cz.cursa = c.id_cursa)\n" +
                                   "INNER JOIN TIP_AUTOCARE AS ta ON cz.tip_autocar = ta.id_tip)\n" +
                                   "INNER JOIN FACILITATI/AUTOCARE AS fa ON fa.id_model_autocar = ta.id_tip)\n" +
                                   "INNER JOIN FACILITATI AS f ON fa.id_facilitate=f.id_facilitate\n" +
                                   "WHERE cz.data = '" + date + "' AND o1.nume_oras = '" + departure + "' \n" +
                                   "AND o2.nume_oras = '" + arrival + "' AND ta.nr_locuri >= '" + Integer.toString(numberOfTickets) + "'\n" +
                                   "AND f.nume_facilitate = 'masuta'";
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
                                resultArrayList.add(new Result(rs.getString("plecare"),rs.getString("sosire"),date,
                                        rs.getString("ora_plecare"),rs.getString("ora_sosire"),
                                        numberOfTickets * Double.parseDouble(rs.getString("pret")) ,
                                        rs.getString("nume_firma"), resID, rs.getInt("id_cursa_zilnica")));

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
            Toast.makeText(ResultsActivity.this, msg + "", Toast.LENGTH_LONG).show();

            if (success == false) {
                ;
            }
            else {
                try {
                    adapter = new ResultAdapter(ResultsActivity.this,resultArrayList);
                    resultListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    resultListView.setAdapter(adapter);

                    int numberOfResults = resultListView.getAdapter().getCount();
                    Toast.makeText(ResultsActivity.this,"S-au gasit " + numberOfResults + " rezultate",Toast.LENGTH_LONG).show();

                } catch (Exception ex)
                {
                    Toast.makeText(ResultsActivity.this,ex.getMessage(),Toast.LENGTH_LONG).show();
                }
            }

        }
    }


    //popularea listei de rezultate
    public void populateListView (int numberOfTickets) {                //popularea listei
        resultListView = (ListView) findViewById(R.id.resultListView);
        resultArrayList.clear();

        resultArrayList.add(new Result("Constanta", "Bucuresti", "12.12.2019", "18:00", "21:00", 50.23*numberOfTickets, "alttransport", R.drawable.alttransport,1));
        resultArrayList.add(new Result("Constanta", "Brasov", "12.12.2019", "12:00", "18:00", 100.23*numberOfTickets, "alttransport", R.drawable.alttransport,2));
        resultArrayList.add(new Result("Constanta","Otopeni","14.12.2019","12:00","13:00",30.00*numberOfTickets,"directaeroport",R.drawable.directaeroport,3));

        adapter = new ResultAdapter(this,resultArrayList);
        resultListView.setAdapter(adapter);

        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ResultsActivity.this,"Item " + Integer.toString((position + 1)) +" clicked", Toast.LENGTH_LONG).show();
            }
        });
    }

}
