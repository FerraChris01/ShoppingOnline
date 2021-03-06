/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chris
 */
public class DatabaseConnector {
    private static DatabaseConnector instance = null;
    
    private Connection conn;
    private boolean connected = false;
    private final String url = "jdbc:mysql://localhost:3306/homeshopping";
    private final String username = "root";
    private final String password = "";
    
    private DatabaseConnector() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url,username,password);
            connected = true;
        } catch (SQLException ex) {
            throw new IllegalStateException(ex.toString(), ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseConnector.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public static synchronized  DatabaseConnector getIstance() {
        if(instance == null)
            instance = new DatabaseConnector();
        return instance;
    }
    public Connection getConnection() {
        return conn;
    }
    public boolean isConnected() {
        return connected;
    }
}
