package de.fhws.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

// TODO implement execute command (only if loaded successfully)

public class SubmissionTester {

    public static final String NAME = "DAVE";

    private final File submissions;
    private final File workingDir;
    private final JSONArray json;
    private final File jsonFile;

    private int curr = 0;

    /**
     * constructs a SubmissionTester;
     * IMPORTANT: the project directory needs an already existing working directory
     * @param submissionsPath path to the assigned submissions, must neither be null nor an empty directory
     * @param workingPath the working space path which must already exist
     */
    public SubmissionTester(String submissionsPath, String workingPath) {
        submissions = new File(submissionsPath);
        if(!submissions.isDirectory())
            throw new IllegalArgumentException("given path is not a directory");

        workingDir = new File(workingPath);
        if(!workingDir.exists())
            throw new IllegalArgumentException("given working space path does not exist");

        jsonFile = new File(submissions.getPath() + "/log.json");
        if(jsonFile.exists())
            json = JSONHandler.load(jsonFile).getJSONArray("submissions");
        else {
            json = new JSONArray();
            initJson();
        }
    }

    private void initJson() {
        List<File> allFiles = Arrays.asList(submissions.listFiles());
        for(File f : allFiles) {
            String[] parts = f.getName().split("_");
            JSONObject student = JSONHandler.createSubmissionObj(Integer.parseInt(parts[2]), parts[0], parts[1]);
            json.put(student);
        }
    }

    /**
     * starts a command based dialog between user and console
     */
    public void startDialog() {
        Scanner sc = new Scanner(System.in);
        printHelpPage();
        String in;
        do {
            System.out.print("$: ");
            in = sc.nextLine();
            boolean success = executeCommand(in);
            if(!success)
                System.out.println("Not a command! Type 'help' to display help page");
        } while (!in.equals("exit"));
        sc.close();
    }

    private void printInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append("current index: ").append(curr).append(" of ").append(json.length()).append("\n");
        if(curr < 0)
            builder.append("index must be at least 0");
        else if(curr >= json.length())
            builder.append("finished - no more submissions available");
        else
            builder.append(JSONHandler.submissionObjToString(json.getJSONObject(curr)));
        System.out.println(builder.toString());
    }

    private void printAll() {
        for (int i = 0; i < json.length(); i++) {
            System.out.println(JSONHandler.submissionObjToString(json.getJSONObject(i)));
        }
    }

    private void printHelpPage() {
        String s = "commands: \n" +
                "info \t\t\t\t\t displays info of current submission\n" +
                "all \t\t\t\t\t prints all submission entries\n" +
                "save \t\t\t\t\t saves all submissions\n" +
                "curr <index> \t\t\t jumps to the submission with specified index\n" +
                "next [-u | -l] \t\t\t\t saves current submission and jumps to the next; if -u next unchecked; if -l with load\n" +
                "search <matNum | lastname> \t\t\t\t searches for first occurrence of submission belonging to the specified student\n" +
                "load \t\t\t\t\t loads all .java files into working directory for execution\n" +
                "ls \t\t\t\t shows all loaded .java files" +
                "check \t\t\t\t\t marks as checked\n" +
                "uncheck \t\t\t\t marks as unchecked\n" +
                "pass \t\t\t\t\t marks as passed\n" +
                "unpass \t\t\t\t\t marks as not passed\n" +
                "comment <commentary> \t adds a comment and marks as checked\n" +
                "exit \t\t\t\t\t saves and exits\n" +
                "help \t\t\t\t\t displays this page";
        System.out.println(s);
    }

    private boolean executeCommand(String command) {
        if(command.startsWith("info")) {
            printInfo();
            return true;
        }
        else if(command.startsWith("all")) {
            printAll();
            return true;
        }
        else if(command.startsWith("save")) {
            save();
            return true;
        }
        else if(command.startsWith("curr ")) {
            save();
            setCurr(Integer.parseInt(command.replaceFirst("curr ", "")));
            printInfo();
            return true;
        }
        else if(command.startsWith("next")) {
            save();
            if(command.contains("-n")) {
                advanceNextUnchecked();
            }
            else if(command.contains("-l")) {
                if(next());
                    load();
            }
            else {
                next();
            }
            printInfo();
            return true;
        }
        else if(command.startsWith("search ")) {
            save();
            try {
                searchByMatNum(Integer.parseInt(command.replaceFirst("search ", "")));
            } catch (NumberFormatException e) {
                searchByLastName(command.replaceFirst("search ", ""));
            }
            return true;
        }
        else if(command.startsWith("load")) {
            load();
            return true;
        }
        else if(command.startsWith("ls")) {
            printWorkspace();
            return true;
        }
        else if(command.startsWith("check")) {
            setChecked(true);
            printInfo();
            return true;
        }
        else if(command.startsWith("uncheck")) {
            setChecked(false);
            printInfo();
            return true;
        }
        else if(command.startsWith("pass")) {
            setPassed(true);
            printInfo();
            return true;
        }
        else if(command.startsWith("unpass")) {
            setPassed(false);
            printInfo();
            return true;
        }
        else if(command.startsWith("comment ")) {
            comment(command.replaceFirst("comment ", ""));
            setChecked(true);
            printInfo();
            return true;
        }
        else if(command.startsWith("exit")) {
            save();
            return true;
        }
        else if(command.startsWith("help")) {
            printHelpPage();
            return true;
        }
        return false;
    }

    private void save() {
        JSONObject mainObj = new JSONObject();
        mainObj.put("submissions", json);
        JSONHandler.save(jsonFile, mainObj);
    }

    private void setCurr(int index) {
        curr = index;
    }

    /**
     * advances current; equivalent to setCurr(curr+1)
     * @return {@code true} if there is a next element {@code false} otherwise
     */
    private boolean next() {
        curr++;
        return curr >= json.length();
    }

    private void advanceNextUnchecked() {
        boolean checked = true;
        while (next() && checked) {
            checked = json.getBoolean(curr);
        }
    }

    private void searchByMatNum(int matNum) {
        for(int i = 0; i < json.length(); i++) {
            if(json.getJSONObject(i).getInt("matNum") == matNum) {
                curr = i;
                System.out.println("Found!");
                printInfo();
                return;
            }
        }
        System.out.println("Not found!");
    }

    private void searchByLastName(String name) {
        for(int i = 0; i < json.length(); i++) {
            if(json.getJSONObject(i).getString("lastname").equals(name)) {
                curr = i;
                System.out.println("Found!");
                printInfo();
                return;
            }
        }
        System.out.println("Not found!");
    }

    private void load() {
        for (File f : workingDir.listFiles())
            FileHandler.recursiveDelete(f);

        int matNum = json.getJSONObject(curr).getInt("matNum");
        // potentially throws an exception if curr is out of bounds; error handling not supported yet TODO
        File currentDir = submissions.listFiles(path -> path.getName().contains(String.valueOf(matNum)))[0];
        List<File> javaFiles = FileHandler.recursiveSearch(currentDir, path -> path.getName().endsWith(".java"));

        for(File f : javaFiles) {
            File dest = new File(workingDir + "/" + currentDir.toPath().relativize(f.toPath()).toString());
            FileHandler.createDirectory(dest.getParentFile());
            try {
                Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        printWorkspace();
    }

    private void printWorkspace() {
        System.out.println(workingDir.getAbsolutePath());
        for(File f : FileHandler.recursiveSearch(workingDir, path -> path.getName().endsWith(".java"))) {
            System.out.println(workingDir.toPath().relativize(f.toPath()));
        }
    }

    private void setChecked(boolean checked) {
        json.put(curr, json.getJSONObject(curr).put("checked", checked));
    }

    private void setPassed(boolean passed) {
        json.put(curr, json.getJSONObject(curr).put("passed", passed));
    }

    private void comment(String commentary) {
        json.put(curr, json.getJSONObject(curr).put("commentary", commentary));
    }

    public static void main(String[] args) {
        SubmissionTester tester = new SubmissionTester("files/split/" + NAME + "", "files/check");
        tester.startDialog();
    }


}
