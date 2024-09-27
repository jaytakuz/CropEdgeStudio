package se233.cropedgestudio.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import se233.cropedgestudio.models.ProcessingJob;
import se233.cropedgestudio.utils.ImageProcessor;
import se233.cropedgestudio.utils.ImageProcessingException;

import javax.imageio.ImageIO;
import javafx.scene.image.Image;
import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BatchProcessorService extends Service<Void> {
    private List<ProcessingJob> jobs;
    private int numThreads;

    public BatchProcessorService(List<ProcessingJob> jobs, int numThreads) {
        this.jobs = jobs;
        this.numThreads = numThreads;
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                ExecutorService executor = Executors.newFixedThreadPool(numThreads);
                int total = jobs.size();
                AtomicInteger completed = new AtomicInteger(0);

                for (ProcessingJob job : jobs) {
                    executor.submit(() -> {
                        try {
                            processJob(job);
                            int completedCount = completed.incrementAndGet();
                            updateProgress(completedCount, total);
                            updateMessage("Processed " + completedCount + " of " + total + " images");
                        } catch (ImageProcessingException e) {
                            updateMessage("Error processing " + job.getInputFile().getName() + ": " + e.getMessage());
                        }
                    });
                }

                executor.shutdown();
                while (!executor.isTerminated()) {
                    Thread.sleep(100);
                }
                return null;
            }
        };
    }

    private void processJob(ProcessingJob job) throws ImageProcessingException {
        try {
            Image image = new Image(job.getInputFile().toURI().toString());
            Image processedImage;

            switch (job.getAlgorithm()) {
                case "Roberts Cross":
                    processedImage = ImageProcessor.applyRobertsCross(image, job.getStrength());
                    break;
                case "Sobel":
                    processedImage = ImageProcessor.applySobel(image);
                    break;
                case "Laplacian":
                    processedImage = ImageProcessor.applyLaplacian(image, job.getMaskSize());
                    break;
                default:
                    throw new ImageProcessingException("Unsupported algorithm: " + job.getAlgorithm());
            }

            ImageIO.write(ImageProcessor.fromFXImage(processedImage), "png", job.getOutputFile());
        } catch (Exception e) {
            throw new ImageProcessingException("Error processing " + job.getInputFile().getName(), e);
        }
    }
}