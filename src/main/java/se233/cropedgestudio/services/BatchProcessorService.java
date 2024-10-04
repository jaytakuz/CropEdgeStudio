package se233.cropedgestudio.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import se233.cropedgestudio.models.ProcessingJob;
import se233.cropedgestudio.utils.EdgeDetectionAlgorithm;
import se233.cropedgestudio.utils.ImageProcessor;
import se233.cropedgestudio.utils.ImageProcessingException;

import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchProcessorService extends Service<Void> {
    private List<ProcessingJob> jobs;
    private int numThreads;
    private Map<String, EdgeDetectionAlgorithm> algorithms;

    public BatchProcessorService(List<ProcessingJob> jobs, int numThreads, Map<String, EdgeDetectionAlgorithm> algorithms) {
        this.jobs = jobs;
        this.numThreads = numThreads;
        this.algorithms = algorithms;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                int total = jobs.size();
                AtomicInteger completed = new AtomicInteger(0);

                jobs.parallelStream().forEach(job -> {
                    try {
                        processJob(job);
                        int completedCount = completed.incrementAndGet();
                        updateProgress(completedCount, total);
                        updateMessage("Processed " + completedCount + " of " + total + " images");
                    } catch (ImageProcessingException e) {
                        updateMessage("Error processing " + job.getInputFile().getName() + ": " + e.getMessage());
                    }
                });

                return null;
            }
        };
    }

    private void processJob(ProcessingJob job) throws ImageProcessingException {
        try {
            Image image = new Image(job.getInputFile().toURI().toString());
            EdgeDetectionAlgorithm edgeAlgorithm = algorithms.get(job.getAlgorithm());
            if (edgeAlgorithm == null) {
                throw new ImageProcessingException("Unsupported algorithm: " + job.getAlgorithm());
            }

            Image processedImage = edgeAlgorithm.apply(image, job.getAlgorithm().equals("Laplacian") ? job.getMaskSize() : job.getStrength());

            ImageIO.write(ImageProcessor.fromFXImage(processedImage), "png", job.getOutputFile());
        } catch (Exception e) {
            throw new ImageProcessingException("Error processing " + job.getInputFile().getName(), e);
        }
    }
}