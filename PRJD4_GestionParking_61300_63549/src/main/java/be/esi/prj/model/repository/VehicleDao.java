package be.esi.prj.model.repository;

import be.esi.prj.model.dto.VehicleDto;
import be.esi.prj.model.*;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Cette classe gère toutes les opérations SQL bas niveau (CRUD) liées aux véhicules.
 */

public class VehicleDao {
    private final Connection connection;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public VehicleDao(Connection connection) {
        this.connection = Objects.requireNonNull(connection, "Connexion requise");
    }

    public Optional<VehicleDto> findById(int id) {
        String sql = """
            SELECT 
                id, plateNumber, entryTime, exitTime
            FROM 
                Vehicle 
            WHERE 
                id = ?
            """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int vehicleId = rs.getInt("id");
                    String plateNumber = rs.getString("plateNumber");

                    String entryTimeStr = rs.getString("entryTime");
                    String exitTimeStr = rs.getString("exitTime");

                    LocalDateTime entryTime = LocalDateTime.parse(entryTimeStr, DATE_TIME_FORMATTER);
                    LocalDateTime exitTime = LocalDateTime.parse(exitTimeStr, DATE_TIME_FORMATTER);

                    VehicleDto vehicle = new VehicleDto(vehicleId, plateNumber, entryTime, exitTime);
                    return Optional.of(vehicle);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Sélection impossible");
        }
        return Optional.empty();
    }

    public List<VehicleDto> findAll() {
        List<VehicleDto> vehicles = new ArrayList<>();
        String sql = "SELECT id, plateNumber, entryTime, exitTime FROM Vehicle";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String plateNumber = rs.getString("plateNumber");
                String entryTimeStr = rs.getString("entryTime");
                String exitTimeStr = rs.getString("exitTime");

                LocalDateTime entryTime = null;
                LocalDateTime exitTime = null;

                if (entryTimeStr != null && !entryTimeStr.isEmpty()) {
                    entryTime = LocalDateTime.parse(entryTimeStr, DATE_TIME_FORMATTER);
                }

                if (exitTimeStr != null && !exitTimeStr.isEmpty()) {
                    exitTime = LocalDateTime.parse(exitTimeStr, DATE_TIME_FORMATTER);
                }

                VehicleDto vehicle = new VehicleDto(id, plateNumber, entryTime, exitTime);
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Sélection impossible");
        }
        return vehicles;
    }


    private int insert(VehicleDto vehicle) {
        String sql = """
        INSERT INTO 
            Vehicle (plateNumber, entryTime, exitTime) 
        VALUES 
            (?, ?, ?)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, vehicle.plateNumber());


            String entryTimeStr = vehicle.entryTime().format(DATE_TIME_FORMATTER);
            stmt.setString(2, entryTimeStr);


            if (vehicle.exitTime() != null) {
                String exitTimeStr = vehicle.exitTime().format(DATE_TIME_FORMATTER);
                stmt.setString(3, exitTimeStr); // Si exitTime n'est pas null, on le formate et l'ajoute
            } else {
                stmt.setNull(3, Types.TIMESTAMP); // Si exitTime est null, on insère un NULL en base
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException("Insertion impossible");
        }
        return -1;
    }

    private int update(VehicleDto vehicle) {
        String sql = """
            UPDATE Vehicle
            SET
                plateNumber = ?, 
                entryTime = ?, 
                exitTime = ?
            WHERE id = ? 
            """;
        int updatedRows = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vehicle.plateNumber());

            String entryTimeStr = vehicle.entryTime().format(DATE_TIME_FORMATTER);
            String exitTimeStr = vehicle.exitTime().format(DATE_TIME_FORMATTER);

            stmt.setString(2, entryTimeStr);
            stmt.setString(3, exitTimeStr);
            stmt.setInt(4, vehicle.id());
            updatedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Sauvegarde impossible");
        }
        return updatedRows;
    }

    public int save(VehicleDto vehicle) {
        int result = -1;
        String sql = "SELECT COUNT(*) FROM Vehicle WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehicle.id());
            ResultSet rs = stmt.executeQuery();
            boolean found = rs.next() && rs.getInt(1) > 0;
            if (found) {
                if (this.update(vehicle) > 0) {
                    result = vehicle.id();
                }
            } else {
                result = this.insert(vehicle);
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Recherche impossible");
        }
    }

    public void delete(int id) {
        System.out.println("Tentative de suppression du véhicule avec ID : " + id);
        String sql = "DELETE FROM Vehicle WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Nombre de lignes affectées : " + rowsAffected);
            if (rowsAffected == 0) {
                System.out.println("Aucun véhicule trouvé avec l'ID " + id);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du véhicule avec ID " + id);
            e.printStackTrace();
        }
    }

    public int getMaxVehicleId() {
        String sql = "SELECT MAX(id) FROM Vehicle";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erreur lors de la récupération de l'ID maximal");
        }
        return 0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Erreur de fermeture de connexion");
        }
    }
}