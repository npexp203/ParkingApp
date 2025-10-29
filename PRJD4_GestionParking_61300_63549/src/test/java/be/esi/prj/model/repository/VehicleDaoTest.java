package be.esi.prj.model.repository;

import be.esi.prj.model.dto.VehicleDto;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VehicleDaoTest {

    private static Connection connection;
    private VehicleDao instance;

    private final Date testDate = java.sql.Date.valueOf(LocalDate.of(2023, 5, 15));
    private final Time testTimeArrival = Time.valueOf(LocalTime.of(8, 30));
    private final Time testTimeDeparture = Time.valueOf(LocalTime.of(17, 45));

    private final VehicleDto testVehicle = new VehicleDto(
            1,
            "1-ABC-123",
            testDate,
            testTimeArrival,
            testDate,
            testTimeDeparture
    );

    @BeforeAll
    static void setupDatabase() throws SQLException {
        // Utilisation d'une base en mémoire pour les tests
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE Vehicle (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plateNumber TEXT NOT NULL,
                    dateArrived DATE NOT NULL,
                    hourArrived TIME NOT NULL,
                    dateLeaving DATE,
                    hourLeaving TIME
                )
                """);
        }
    }

    @BeforeEach
    void setup() {
        instance = new VehicleDao(connection);
    }

    @AfterEach
    void cleanDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM Vehicle");
        }
    }

    @AfterAll
    static void closeDatabase() throws SQLException {
        connection.close();
    }

    @Test
    void findById_shouldReturnVehicle_whenExists() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Vehicle (plateNumber, dateArrived, hourArrived) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "1-ABC-123");
            stmt.setDate(2, new java.sql.Date(testVehicle.dateArrived().getTime()));
            stmt.setTime(3, new java.sql.Time(testVehicle.hourArrived().getTime()));
            stmt.executeUpdate();
        }
        int vehicleId = insertTestVehicle();

        Optional<VehicleDto> result = instance.findById(vehicleId);

        assertTrue(result.isPresent());
        assertEquals("1-ABC-123", result.get().plateNumber());
    }

    @Test
    void save_shouldInsertNewVehicle() {
        int newId = instance.save(testVehicle);

        assertTrue(newId > 0);
        Optional<VehicleDto> savedVehicle = instance.findById(newId);
        assertTrue(savedVehicle.isPresent());
    }

    @Test
    void save_shouldUpdateExistingVehicle() throws SQLException {
        int id = insertTestVehicle();

        VehicleDto updated = new VehicleDto(
                id,
                "1-XYZ-987",
                testVehicle.dateArrived(),
                testVehicle.hourArrived(),
                testVehicle.dateLeaving(),
                testVehicle.hourLeaving()
        );

        int result = instance.save(updated);

        assertEquals(id, result);
        Optional<VehicleDto> fromDb = instance.findById(id);
        assertEquals("1-XYZ-987", fromDb.get().plateNumber());
    }

    @Test
    void delete_shouldRemoveVehicle() throws SQLException {
        int id = insertTestVehicle();

        instance.delete(id);

        Optional<VehicleDto> deleted = instance.findById(id);
        assertFalse(deleted.isPresent());
    }

    private int insertTestVehicle() throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO Vehicle (plateNumber, dateArrived, hourArrived) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "1-ABC-123");
            stmt.setDate(2, new java.sql.Date(testVehicle.dateArrived().getTime()));
            stmt.setTime(3, new java.sql.Time(testVehicle.hourArrived().getTime()));
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        }
    }

    private void insertTestVehicles() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                INSERT INTO Vehicle (plateNumber, dateArrived, hourArrived) VALUES
                ('1-ABC-123', '2023-05-15', '08:30:00'),
                ('1-DEF-456', '2023-05-16', '09:15:00'),
                ('1-GHI-789', '2023-05-17', '10:00:00')
                """);
        }
    }
}