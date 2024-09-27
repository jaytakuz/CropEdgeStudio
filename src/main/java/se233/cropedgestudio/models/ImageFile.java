package se233.cropedgestudio.models;

import javafx.scene.image.Image;
import java.io.File;

public class ImageFile {
    private File file;
    private Image image;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ImageFile(File file) {
        this.file = file;
        this.image = new Image(file.toURI().toString());
    }
}