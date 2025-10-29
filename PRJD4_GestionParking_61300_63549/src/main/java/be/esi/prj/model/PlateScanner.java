package be.esi.prj.model;

import javafx.application.Platform;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class PlateScanner {
    private final Tesseract tesseract;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public PlateScanner() {
        this.tesseract = new Tesseract();
        tesseract.setLanguage("fra");
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
    }

    /**
     * OCR asynchrone : resultat sur le JavaFX Application Thread
     * @param imageFile   image à traiter
     * @param onSuccess   callback texte brut
     * @param onError     callback exception
     */
    public void ocrAsync(File imageFile,
                         Consumer<String> onSuccess,
                         Consumer<Throwable> onError) {
        executor.submit(() -> {
            try {
                String text = tesseract.doOCR(imageFile);
                Platform.runLater(() -> onSuccess.accept(text));
            } catch (Throwable ex) {
                Platform.runLater(() -> onError.accept(ex));
            }
        });
    }
}
