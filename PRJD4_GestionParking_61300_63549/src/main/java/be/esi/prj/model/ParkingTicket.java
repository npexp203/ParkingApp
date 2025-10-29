package be.esi.prj.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Représente un ticket de parking associé à un véhicule.
 *
 * @param id          identifiant unique du ticket
 * @param plateNumber numéro de plaque du véhicule
 * @param entryTime   date et heure d'entrée dans le parking
 * @param exitTime    date et heure de sortie du parking (peut être null si encore présent)
 */

public record ParkingTicket(
        int id,
        String plateNumber,
        LocalDateTime entryTime,
        LocalDateTime exitTime
) {
}
