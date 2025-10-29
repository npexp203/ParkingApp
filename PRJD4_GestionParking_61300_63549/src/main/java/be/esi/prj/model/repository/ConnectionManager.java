package be.esi.prj.model.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 * Gère une connexion unique à la base de données à l'aide des propriétés définies
 * dans le fichier database.properties
 *
 */

public class ConnectionManager {
    private static Properties properties = null;
    private static Connection connection;

    /**
     * Charge les propriétés de configuration de la base de données.
     *
     * @return les propriétés chargées
     * @throws RepositoryException si le fichier est introuvable ou illisible
     */
    private static Properties loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = ConnectionManager.class.getClassLoader().getResourceAsStream("database.properties")) {
                if (input == null) {
                    throw new RepositoryException("Insertion impossible");
                }
                properties.load(input);
                System.out.println("Properties loaded successfully.");
            } catch (IOException e) {
                System.err.println("Failed to load properties: " + e.getMessage());
                throw new RepositoryException("Properties illisible");
            }
        }
        return properties;
    }

    /**
     * Retourne une connexion JDBC
     *
     * @return connexion à la base de données
     * @throws RepositoryException si la connexion échoue
     */
    public static synchronized Connection getConnection() {
        if (connection == null) {
            try {
                loadProperties();
                String url = properties.getProperty("db.url");
                String user = properties.getProperty("db.user");
                String password = properties.getProperty("db.password");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Connection established.");
            } catch (SQLException ex) {
                throw new RepositoryException("Connection failed");
            }
        }
        return connection;
    }

    /**
     * Ferme la connexion à la base de données si elle est ouverte.
     *
     * @throws RepositoryException en cas d'échec
     */

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException ex) {
            throw new RepositoryException("Failed to close connection");
        }
    }
}
