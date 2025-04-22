package db;

import java.sql.*;

public class DBConnection {

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://localhost:3306/dbmsminipro";
        String user = "root";
        String pass = "pushkraj@1";

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(url, user, pass);
        System.out.println("New DB connection created successfully.");
        return conn;
    }
}