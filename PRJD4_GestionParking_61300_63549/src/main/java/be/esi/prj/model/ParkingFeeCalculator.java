package be.esi.prj.model;

import javafx.application.Platform;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Utilitaire pour calculer les frais de stationnement et générer un résumé associé.
 * Fournit également des méthodes asynchrones avec callbacks pour une intégration fluide avec JavaFX.
 */
public class ParkingFeeCalculator {

    private static final double RATE_PER_HOUR = 2.0;
    private static final double RATE_PER_DAY = 15.0;
    public static final double LATE_SURCHARGE_PER12H = 10;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final DateTimeFormatter SUMMARY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static long calculateDurationInMinutes(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (entryTime == null || exitTime == null) {
            throw new IllegalArgumentException("Les horaires d'entrée et de sortie ne peuvent pas être nuls.");
        }
        if (exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("L'heure de sortie ne peut pas être avant l'heure d'entrée.");
        }
        return Duration.between(entryTime, exitTime).toMinutes();
    }

    /**
     * Calcule le tarif de stationnement en fonction de la durée et du retard éventuel.
     *
     * @param entryTime     heure d'entrée
     * @param exitTime      heure de sortie prévue
     * @param departureTime heure de départ réelle
     * @return tarif total à payer
     */

    public static double calculateFee(LocalDateTime entryTime, LocalDateTime exitTime, LocalDateTime departureTime) {
        if (exitTime == null) exitTime = LocalDateTime.now();
        long minutes = calculateDurationInMinutes(entryTime, exitTime);
        double hours = minutes / 60.0;

        double fee;
        if (hours >= 24) {
            fee = Math.ceil(hours / 24) * RATE_PER_DAY;
        } else {
            fee = Math.ceil(hours) * RATE_PER_HOUR;
        }

        if (departureTime != null && departureTime.isAfter(exitTime)) {
            long lateHours = Duration.between(exitTime, departureTime).toHours();
            long latePeriods = lateHours / 12;
            if (latePeriods > 0) {
                fee += latePeriods * LATE_SURCHARGE_PER12H;
            }
        }
        return fee;
    }
    /**
     * Génère un résumé formaté du stationnement.
     *
     * @param entryTime     heure d'entrée
     * @param exitTime      heure de sortie prévue
     * @param departureTime heure de sortie réelle
     * @return résumé lisible du stationnement
     */

    public static String getExitSummary(LocalDateTime entryTime, LocalDateTime exitTime, LocalDateTime departureTime) {
        long minutes = calculateDurationInMinutes(entryTime, exitTime);
        double fee = calculateFee(entryTime, exitTime, departureTime);
        return "Entrée : " + SUMMARY_FMT.format(entryTime)
                + "\nSortie prévue : " + SUMMARY_FMT.format(exitTime)
                + "\nSortie réelle : " + SUMMARY_FMT.format(departureTime)
                + "\nDurée : " + minutes + " minutes"
                + "\nPrix à payer : " + String.format("%.2f€", fee);
    }

    /**
     * Calcule le tarif de stationnement de manière asynchrone et renvoie le résultat via un callback.
     *
     * @param entryTime     heure d'entrée
     * @param exitTime      heure de sortie prévue
     * @param departureTime heure réelle de sortie
     * @param onResult      callback avec le tarif
     * @param onError       callback en cas d'erreur
     */
    public static void calculateFeeAsync(
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            LocalDateTime departureTime,
            Consumer<Double> onResult,
            Consumer<Exception> onError
    ) {
        executorService.submit(() -> {
            try {
                double fee = calculateFee(entryTime, exitTime, departureTime);
                Platform.runLater(() -> onResult.accept(fee));
            } catch (Exception e) {
                Platform.runLater(() -> onError.accept(e));
            }
        });
    }


    /**
     * Génère un résumé de stationnement de manière asynchrone et le transmet via un callback.
     *
     * @param entryTime     heure d'entrée
     * @param exitTime      heure de sortie prévue
     * @param departureTime heure réelle de sortie
     * @param onResult      callback avec le résumé formaté
     * @param onError       callback en cas d'erreur
     */
    public static void getExitSummaryAsync(
            LocalDateTime entryTime,
            LocalDateTime exitTime,
            LocalDateTime departureTime,
            Consumer<String> onResult,
            Consumer<Exception> onError
    ) {
        executorService.submit(() -> {
            try {
                String summary = getExitSummary(entryTime, exitTime, departureTime);
                Platform.runLater(() -> onResult.accept(summary));
            } catch (Exception e) {
                Platform.runLater(() -> onError.accept(e));
            }
        });
    }


    }

