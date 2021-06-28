package com.parse.starter;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHelper {

    String ip = "";              //adresa ip + portul implicit pentru serverul SQL
    String db = "";                    //numele bazei de date
    String DBUserNameStr = "";             //numele de utilizator pentru serverul SQL
    String DBPasswordStr = "";            //parola pentru serverul SQL

    @SuppressLint("NewApi")
    public Connection ConnectionMethod () {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Connection connection = null;
        String ConnectionURL = null;

        try
        {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + ip + ";databaseName=" + db +
                            ";user=" + DBUserNameStr + ";password=" + DBPasswordStr + ";";
            connection = DriverManager.getConnection(ConnectionURL);
        }
        catch (SQLException se)
        {
            Log.e("Error 1: ", se.getMessage());
        }
        catch (ClassNotFoundException cne)
        {
            Log.e("Error 2: ", cne.getMessage());
        }
        catch (Exception ex)
        {
            Log.e("Error 3: ", ex.getMessage());
        }
        return connection;
    }
}
