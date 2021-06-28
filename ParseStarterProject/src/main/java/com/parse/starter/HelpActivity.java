package com.parse.starter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class HelpActivity extends AppCompatActivity {

    private ActionBar actionBar;

    public void goBack(View view) {     //intent-ul de trecere de la OptionsCompleteActivity la activitatea curenta
        finish();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        Intent intent = getIntent();

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3f48cc")));

    }
}
