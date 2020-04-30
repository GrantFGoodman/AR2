package com.example.animalrecognition;

import java.io.File;
import java.io.IOException;

public final class ImageList {

    public static File imagesDirectory;

    {
        try {
            imagesDirectory = File.createTempFile("myImages", "jpg");
            imagesDirectory.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
