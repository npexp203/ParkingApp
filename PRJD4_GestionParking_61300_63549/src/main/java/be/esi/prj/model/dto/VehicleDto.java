package be.esi.prj.model.dto;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Dto, transport des données suivants :
 *
 * @param plateNumber La plaque du véhicule.
 * @param entryTime    La date et l'heure d'entrée.
 * @param exitTime     La date et heure de sortie
 */
public record   VehicleDto(int id, String plateNumber, LocalDateTime entryTime,LocalDateTime exitTime ) {
}
