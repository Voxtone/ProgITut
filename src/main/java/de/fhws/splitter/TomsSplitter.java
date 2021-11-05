package de.fhws.splitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TomsSplitter {

    public static final String SUBMISSIONS_DIR = "files/submissions";
    public static final String OUTPUT_DIR = "files";
    public static final int TOM_AMOUNT = 20;
    public static final int DAVE = 20;
    public static final int NICO = 20;
    public static final int NICO2 = 20;


    List<File> filelist = new ArrayList<>();
    Map<String, Integer> map = new HashMap<>();
    Map<String, Integer> diffMap = new HashMap<>();

    TomsSplitter() {
        map.put("TOM", TOM_AMOUNT);
        map.put("DAVE", DAVE);
        map.put("NICO", NICO);
        map.put("NICO2", NICO2);

        for (String name : map.keySet()) {
            diffMap.put(name, 0);
        }
    }

    public static void main(String[] args) throws IOException {

        TomsSplitter sp = new TomsSplitter();
        File dir = new File("abgaben");
        sp.addFiles(dir.listFiles());
        sp.splitFiles();
        System.out.println(sp.map.values().toString());


    }

    public void splitFiles() {
        int sum = map.values().stream().mapToInt(a -> a).sum();
        if (sum < filelist.size()) {

            for (String name : map.keySet()) {
                try {
                    splitInDir(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            int counter = 0;
            String name = "";
            while (filelist.size() > 0) {
                Iterator<String> it = map.keySet().iterator();
                for (int i = 0; i <= counter; i++) {
                    name = it.next();
                }
                map.put(name, map.get(name) + 1);
                diffMap.put(name, diffMap.get(name) + 1);
                moveFile(filelist.get(0).getPath(), "directories\\" + name + "\\" + filelist.get(0).getPath().substring(7, filelist.get(0).getPath().indexOf("_", filelist.get(0).getPath().indexOf("_") + 1)) + ".zip");
                filelist.remove(0);
                counter++;
                counter %= map.keySet().size();
            }

            for (String person : diffMap.keySet()) {
                System.out.println(person + " hat " + diffMap.get(person) + " mehr Abgaben als ausgemacht!");
            }
        } else if (sum == filelist.size()) {
            for (String name : map.keySet()) {
                try {
                    splitInDir(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Aufteilung perfekt nach Plan!");

        } else {
            int counter = 0;
            while (sum > filelist.size()) {
                Iterator<String> it = map.keySet().iterator();
                String name = "";
                for (int i = 0; i <= counter; i++) {
                    name = it.next();
                }
                map.put(name, map.get(name) - 1);
                diffMap.put(name, diffMap.get(name) - 1);
                counter++;
                counter %= map.keySet().size();


                sum = map.values().stream().mapToInt(a -> a).sum();
            }
            for (String name : map.keySet()) {
                try {
                    splitInDir(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (String person : diffMap.keySet()) {
                System.out.println(person + " hat " + diffMap.get(person) + " weniger Abgaben als ausgemacht!");
            }


        }


    }

    public void splitInDir(String name) throws IOException {
        File file = new File(name);
        if (file.exists())
            delete(new File(name));
        new File(name).mkdir();
        if (map.get(name) < 1) return;
        for (int i = 0; i < map.get(name) && filelist.size() > 0; i++) {
            moveFile(filelist.get(0).getPath(), "directories\\" + name + "\\" + filelist.get(0).getPath().substring(7, filelist.get(0).getPath().indexOf("_", filelist.get(0).getPath().indexOf("_") + 1)) + ".zip");
            filelist.remove(0);
        }
    }

    static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    public static boolean moveFile(String sourcePath, String targetPath) {

        boolean fileMoved = true;

        try {

            //Files.move(Paths.get(sourcePath), Paths.get(targetPath), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(sourcePath), Paths.get(targetPath));

        } catch (Exception e) {

            fileMoved = false;
            e.printStackTrace();
        }

        return fileMoved;
    }

    public void addFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {

                addFiles(file.listFiles()); // Calls same method again.
            } else {

                this.filelist.add(file);
            }
        }
    }

}
