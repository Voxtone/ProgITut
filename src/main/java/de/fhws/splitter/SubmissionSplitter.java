package de.fhws.splitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import de.fhws.excpetions.MultipleFilesContainedException;
import de.fhws.excpetions.NoFileContainedException;
import de.fhws.excpetions.NotADirectoryException;
import de.fhws.excpetions.NotAnArchiveException;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

public class SubmissionSplitter {
    public static final String SUBMISSIONS_DIR = "files/submissions/";
    public static final String OUTPUT_DIR = "files/split/";
    public static final int TOM_AMOUNT = 10;
    public static final int DAVE_AMOUNT = 30;
    public static final int NICO_AMOUNT = 20;
    public static final int NICO2_AMOUNT = 20;

    // for logic
    private Map<String, Integer> amountMap;
    private Map<String, List<File>> fileMap = new HashMap<>();
    private int summedAmount;

    // Archiver
    private static final Archiver zip = ArchiverFactory.createArchiver("zip");
    private static final Archiver tar = ArchiverFactory.createArchiver("tar");
    private static final Archiver targz = ArchiverFactory.createArchiver("tar", "gz");

    public SubmissionSplitter(Map<String, Integer> amountsMap) {
        this.amountMap = amountsMap;
        summedAmount = 0;
        for (String name : amountsMap.keySet()) {
            summedAmount += amountsMap.get(name);
            fileMap.put(name, new ArrayList<>());
        }

    }

    public void split() {
        File submissions = new File(SUBMISSIONS_DIR);
        List<File> allFiles = Arrays.stream(submissions.listFiles()).collect(Collectors.toList());
        int fileCount = allFiles.size();
        int overhead = (fileCount - summedAmount) / amountMap.size();
        int remainder = (fileCount - summedAmount) % amountMap.size();
        for (String name : amountMap.keySet()) {
            for (int i = 0; i < amountMap.get(name) + overhead; i++) {
                giveFileTo(name, allFiles.remove(0));
            }
            if (remainder-- > 0)
                giveFileTo(name, allFiles.remove(0));
        }
        printSplit(fileCount);
        executeSplit();
    }

    private void giveFileTo(String name, File file) {
        fileMap.get(name).add(file);
    }

    private void printSplit(int total) {
        System.out.println("Total submissions: " + total);
        for (String name : fileMap.keySet()) {
            int assigned = fileMap.get(name).size();
            int diff = amountMap.get(name) - assigned;
            System.out.println(name + (name.length() < 4 ? "\t" : "") + "\t\t assigned submissions: " + assigned
                    + "; Difference to determined amount: " + (diff > 0 ? "+" : "") + diff);
        }
    }

    private void executeSplit() {
        for (String name : fileMap.keySet()) {
            for (File dir : fileMap.get(name)) {
                try {
                    if (!dir.isDirectory())
                        throw new NotADirectoryException(dir.getName());
                    if (dir.listFiles().length == 0)
                        throw new NoFileContainedException(dir.getName());
                    if (dir.listFiles().length != 1)
                        throw new MultipleFilesContainedException(dir.getName());
                    System.out.println(extractName(dir));
                    extractArchiveTo(dir.listFiles()[0], new File(OUTPUT_DIR + name + "/" + extractName(dir)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void extractArchiveTo(File source, File dest) {
        try {
            Archiver archiver;
            if (source.getName().endsWith(".zip"))
                archiver = zip;
            else if(source.getName().endsWith(".tar"))
                archiver = tar;
            else if(source.getName().endsWith(".tar.gz"))
                archiver = targz;
            else if(source.getName().endsWith(".tgz")) {
                archiver = targz;
                String path = source.getPath();
                path.substring(0, path.length()-4).concat(".tar.gz");
                source.renameTo(new File(path));
            }
            else
                throw new NotAnArchiveException(source.getName());

            archiver.extract(source, dest);
        } catch (NotAnArchiveException | IOException e) {
            e.printStackTrace();
        }
    }

    private String extractName(File f) {
        return f.getName().replaceFirst("_assignsubmission_file_", "").replaceFirst(" ", "_");
    }

    public static void main(String args[]) {
        Map<String, Integer> amountMap = new HashMap<>();
        amountMap.put("TOM", TOM_AMOUNT);
        amountMap.put("DAVE", DAVE_AMOUNT);
        //  amountsMap.put("NICO", NICO_AMOUNT);
        //  amountsMap.put("NICO2", NICO2_AMOUNT);
        SubmissionSplitter splitter = new SubmissionSplitter(amountMap);
        splitter.split();
    }

}
