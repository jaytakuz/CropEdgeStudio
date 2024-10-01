package se233.cropedgestudio.models;

import java.io.File;

public class ProcessingJob {
    private File inputFile;
    private File outputFile;
    private String algorithm;
    private int strength;
    private int maskSize;

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getMaskSize() {
        return maskSize;
    }

    public void setMaskSize(int maskSize) {
        this.maskSize = maskSize;
    }

    public ProcessingJob(File inputFile, File outputFile, String algorithm, int strength, int maskSize) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.algorithm = algorithm;
        this.strength = strength;
        this.maskSize = maskSize;
    }
}