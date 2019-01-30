package fr.epsi.book.dal;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
        import java.sql.DatabaseMetaData;
        import java.sql.DriverManager;
        import java.sql.SQLException;

public class sqlConnection{

    /**
     * Connect to a  sqlLite db sample database
     *
     * @param fileName the database file name
     */
    public static void createNewDatabase(String fileName) {

            String url = "jdbc:sqlite:" +  System.getProperty("user.dir") + "\\" + fileName;
        System.out.println(url);

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        createNewDatabase("db.db");
    }
}