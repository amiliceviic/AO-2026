/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package baza;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 *
 * @author Aleksandar Milicevic
 */
public class Konekcija {
    
    private static Konekcija instance;
    private Connection connection;
    
    private Konekcija() {
        try {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                System.out.println("Ucitan je driver baze podataka!");
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            
            Properties properties = new Properties();
            properties.load(new FileInputStream("config/dbproperties.properties"));

            String url = properties.getProperty("url");
            String user = properties.getProperty("user");
            String password = properties.getProperty("password");
            
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Konekcija sa bazom uspesno uspostavljena");

            connection.setAutoCommit(false);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static Konekcija getInstance() {
        if(instance == null) {
            instance = new Konekcija();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
    
}
