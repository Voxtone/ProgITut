package de.fhws.core;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    public static void createDirectory(File dir) {
        if(!dir.exists())
            createDirectory(dir.getParentFile());
        dir.mkdir();
    }

    public static boolean recursiveDelete(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles())
                recursiveDelete(f);
        }
        return file.delete();
    }

    public static List<File> recursiveSearch(File dir, FileFilter fileFilter) {
        List<File> list = new ArrayList<>();
        recursiveSearch(dir, fileFilter, list);
        return list;
    }

    private static void recursiveSearch(File file, FileFilter fileFilter, List<File> list) {
        if(fileFilter.accept(file))
            list.add(file);

        if(file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveSearch(f, fileFilter, list);
            }
        }
    }
}
