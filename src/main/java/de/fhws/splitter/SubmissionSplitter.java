package de.fhws.splitter;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipException;

import de.fhws.excpetions.MultipleFilesContainedException;
import de.fhws.excpetions.NoFileContainedException;
import de.fhws.excpetions.NotADirectoryException;
import de.fhws.excpetions.NotAnArchiveException;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

// TODO check all amount cases


public class SubmissionSplitter {
    public static final String SUBMISSIONS_PATH = "files/submissions";
    public static final String OUTPUT_PATH = "files/split";
    public static final int TOM_AMOUNT = 18;
    public static final int DAVE_AMOUNT = 38;
    public static final int NICOLAS_AMOUNT = 34;
    public static final int JOHANNES_AMOUNT = 21;

    // paths
    private final File submissions;
    private final File outputDir;

    // for logic
    private final Map<String, Integer> amountMap;
    private final Map<String, List<File>> fileMap = new HashMap<>();
    private int summedAmount;

    // Archiver
    private static final Archiver zip = ArchiverFactory.createArchiver("zip");
    private static final Archiver tar = ArchiverFactory.createArchiver("tar");
    private static final Archiver targz = ArchiverFactory.createArchiver("tar", "gz");

    /**
     * Constructs a SubmissionSplitter with the given amounts map
     * @param amountsMap map which describes who has how many submissions per week according to the contract
     * @param submissionsPath path to the submissions
     * @param outputPath path to the output directory
     */
    public SubmissionSplitter(Map<String, Integer> amountsMap, String submissionsPath, String outputPath) {
        this.amountMap = amountsMap;
        summedAmount = 0;
        for (String name : amountsMap.keySet()) {
            summedAmount += amountsMap.get(name);
            fileMap.put(name, new ArrayList<>());
        }
        submissions = new File(submissionsPath);
        outputDir = new File(outputPath);
        for(File f : outputDir.listFiles())
            recursiveDelete(f);

    }

    private boolean recursiveDelete(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles())
                recursiveDelete(f);
        }
        return file.delete();
    }

    /**
     * splits the submissions in the specified output directory into the
     */
    public void split() {
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

                    extractArchiveTo(dir.listFiles()[0], new File(outputDir.getPath() + "/" + name + "/" + extractName(dir)));
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
        } catch (ZipException e) {
            try {
                throw new NotAnArchiveException(source.getPath());
            } catch (NotAnArchiveException notAnArchiveException) {
                notAnArchiveException.printStackTrace();
            }
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
        //amountMap.put("NICOLAS", NICOLAS_AMOUNT);
        //amountMap.put("JOHANNES", JOHANNES_AMOUNT);
        SubmissionSplitter splitter = new SubmissionSplitter(amountMap, SUBMISSIONS_PATH, OUTPUT_PATH);
        splitter.split();
    }

}
