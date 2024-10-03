package jdbc;

import ifaces.InterfaceConnectionManager;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager implements InterfaceConnectionManager {

    private Connection c = null;

    public ConnectionManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dbDirectory = new File("./db");

            // Verify if the directory exists or if it can be created
            if (!dbDirectory.exists()) {
                if (!dbDirectory.mkdirs()) {
                    throw new IOException("No se pudo crear el directorio ./db");
                }
            }

            // Establish the connection to the database
            c = DriverManager.getConnection("jdbc:sqlite:./db/EssentialTremor.db");
            c.createStatement().execute("PRAGMA foreign_keys=ON");
            System.out.println("Conexión a la base de datos abierta.");
            this.createTables();
        } catch (ClassNotFoundException e) {
            System.out.println("Librerías no cargadas.");
        } catch (SQLException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //------CREATE TABLES-------------
    @Override
    public void createTables() {
        try {
            Statement stmt = c.createStatement();
            String table = "";
            stmt.executeUpdate(table);
            String table1 = "";
            stmt.executeUpdate(table1);
        } catch (SQLException e) {
            // Check if the exception is because the tables already exist
            if (e.getMessage().contains("already exist")) {
                System.out.println("Tables already created.");
                return;
            }
            System.out.println("Database error.");
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return c;
    }

    @Override
    public void disconnect() {
        try {
            c.close();
        } catch (SQLException e) {
            System.out.println("Database error.");
            e.printStackTrace();
        }
    }
}
