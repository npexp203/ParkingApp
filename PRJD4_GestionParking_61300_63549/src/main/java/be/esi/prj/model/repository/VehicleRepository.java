package be.esi.prj.model.repository;

import be.esi.prj.model.ParkingTicket;
import be.esi.prj.model.dto.VehicleDto;

import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository pour la gestion des véhicules.
 * Cette classe sert de couche intermédiaire entre le VehicleDao et le reste de l'application.
 * Elle utilise un cache mémoire pour optimiser l'accès aux données des véhicules.
 */
public class VehicleRepository {
    private final VehicleDao vehicleDao;
    private final Map<Integer, VehicleDto> vehicleCache;

    public VehicleRepository() {
        Connection connection = ConnectionManager.getConnection();
        this.vehicleDao = new VehicleDao(connection);
        this.vehicleCache = new ConcurrentHashMap<>();
        loadCache();
    }

    VehicleRepository(VehicleDao vehicleDao) {
        this.vehicleDao = Objects.requireNonNull(vehicleDao, "VehicleDao is required");
        this.vehicleCache = new ConcurrentHashMap<>();
        loadCache();
    }

    private void loadCache() {
        vehicleDao.findAll().forEach(
                dto -> vehicleCache.put(dto.id(), dto)
        );
    }

    public Optional<VehicleDto> findById(int id) {
        return Optional.ofNullable(vehicleCache.get(id))
                .or(() -> vehicleDao.findById(id));
    }

    /**
     * Recherche un ticket de parking à partir du numéro de plaque (insensible à la casse).
     */
    public Optional<ParkingTicket> findTicketByPlate(String plateNumber) {
        return vehicleCache.values().stream()
                .filter(dto -> dto.plateNumber().equalsIgnoreCase(plateNumber))
                .findFirst()
                .map(dto -> new ParkingTicket(
                        dto.id(),
                        dto.plateNumber(),
                        dto.entryTime(),
                        dto.exitTime()
                ));
    }

    public List<VehicleDto> findAll() {
        return new ArrayList<>(vehicleCache.values());
    }

    public int save(VehicleDto vehicle) {
        int generatedId = vehicleDao.save(vehicle);
        if (generatedId != -1) {
            vehicleCache.put(
                    generatedId,
                    new VehicleDto(
                            generatedId,
                            vehicle.plateNumber(),
                            vehicle.entryTime(),
                            vehicle.exitTime()
                    )
            );
        }
        return generatedId;
    }

    public int getMaxVehicleId() {
        return vehicleDao.getMaxVehicleId();
    }

    public void delete(int id) {
        vehicleDao.delete(id);
        vehicleCache.remove(id);
    }

    public void close() {
        ConnectionManager.close();
    }
}
