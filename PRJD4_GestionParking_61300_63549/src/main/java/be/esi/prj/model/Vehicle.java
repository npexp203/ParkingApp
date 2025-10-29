package be.esi.prj.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Représente un véhicule enregistré dans le système de parking.
 *
 * @param id          identifiant unique du véhicule
 * @param plateNumber numéro de plaque d'immatriculation
 * @param entryTime   date et heure d'entrée dans le parking
 * @param exitTime    date et heure de sortie du parking (peut être {@code null} si encore présent)
 */

public record Vehicle(int id, String plateNumber, LocalDateTime entryTime, LocalDateTime exitTime) {

    @Override
    public String toString() {
        return "Véhicule ID: " + id +
                "\nPlaque: " + plateNumber +
                "\nEntrée: " + entryTime +
                "\nSortie: " + exitTime;
    }
}
