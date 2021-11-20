package de.fhws.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// TODO delete package line in every .java file
// TODO add Timer

public class SubmissionTester {

    public static final String NAME = "DAVE";
    public static final String EMAIL = "david.kupper@student.fhws.de";

    private final File submissions;
    private final File workingDir;
    private final List<File> loadedFiles = new ArrayList<>();
    private final JSONArray json;
    private final File jsonFile;

    private final Window commentWindow = new Window();

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
        File[] allFiles = submissions.listFiles();
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
        updateCommentWindow();
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

    public void printInfo() {
        System.out.println(getInfoString());
    }

    public String getInfoString() {
        StringBuilder builder = new StringBuilder();
        builder.append("current index: ").append(curr).append(" of ").append(json.length() - 1).append("\n");
        if(curr < 0)
            builder.append("index must be at least 0");
        else if(curr >= json.length())
            builder.append("finished - no more submissions available");
        else
            builder.append(JSONHandler.submissionObjToString(json.getJSONObject(curr)));
        return builder.toString();
    }

    public void printAll() {
        for (int i = 0; i < json.length(); i++) {
            System.out.println(JSONHandler.submissionObjToString(json.getJSONObject(i)));
        }
    }

    public void printHelpPage() {
        String s = """
                commands:\s
                info \t\t\t\t\t\t displays info of current submission
                all \t\t\t\t\t\t prints all submission entries
                save \t\t\t\t\t\t saves all submissions
                curr <index> \t\t\t\t jumps to the submission with specified index
                next [-u | -l] \t\t\t\t saves current submission and jumps to the next; if -u next unchecked; if -l with load
                search <matNum | lastname> \t searches for first occurrence of submission belonging to the specified student
                load \t\t\t\t\t\t loads all .java files into working directory for execution
                ls \t\t\t\t\t\t\t shows all loaded .java files
                java [-d] <file index> \t\t\t executes the given .java file; if -d then a no new cmd window will
                \t\t\t\t\t\t\t\t\t be opened (needed for input), instead it's executed directly
                check \t\t\t\t\t\t marks as checked
                uncheck \t\t\t\t\t marks as unchecked
                pass \t\t\t\t\t\t marks as passed
                unpass \t\t\t\t\t\t marks as not passed
                comment
                comment [-a] <commentary> \t\t sets a comment and marks as checked. If -a is set, comment will be appended
                commentary \t\t\t\t\t opens commentary editor
                printcomments \t\t\t\t\t lists all comments (in order to copy paste)
                exit \t\t\t\t\t\t saves and exits
                help \t\t\t\t\t\t displays this page""";
        System.out.println(s);
    }

    public boolean executeCommand(String command) {
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
            if(command.contains("-u")) {
                advanceNextUnchecked();
            }
            else if(command.contains("-l")) {
                if(next())
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
        else if(command.startsWith("java ")) {
            try {
                if(command.replaceFirst("java ", "").startsWith("-d ")) {
                    int fileIndex = Integer.parseInt(command.replaceFirst("java -d ", ""));
                    execJava(fileIndex, false);
                }
                else {
                    int fileIndex = Integer.parseInt(command.replaceFirst("java ", ""));
                    execJava(fileIndex, true);
                }
                return true;
            } catch (NumberFormatException e) {
                System.out.println("number needed!");
                return false;
            }
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
        else if(command.equals("comment")) {
            updateCommentWindow();
        }
        else if(command.startsWith("comment ")) {
            if(command.replaceFirst("comment ", "").startsWith("-a "))
                comment(command.replaceFirst("comment -a ", ""), true);
            else
                comment(command.replaceFirst("comment ", ""), false);

            setChecked(true);
            printInfo();
            return true;
        }
        else if(command.startsWith("commentary")) {
            throw new UnsupportedOperationException("not implemented");
        }
        else if(command.startsWith("printcomments")) {
            printComments();
            return true;
        }
        else if(command.startsWith("exit")) {
            exit();
            return true;
        }
        else if(command.startsWith("help")) {
            printHelpPage();
            return true;
        }
        return false;
    }

    public void save() {
        JSONObject mainObj = new JSONObject();
        mainObj.put("submissions", json);
        if(!JSONHandler.save(jsonFile, mainObj))
            System.out.println("failed to save data");
    }

    public void setCurr(int index) {
        writeToJsonFromWindow();
        if(index >= 0 && index < json.length()) {
            curr = index;
            updateCommentWindow();
        }
        else
            System.out.println("index not in range!");
    }

    /**
     * advances current; equivalent to setCurr(curr+1)
     * @return {@code true} if there is a next element {@code false} otherwise
     */
    public boolean next() {
        save();
        boolean isNext = curr < json.length() - 1;
        setCurr(curr + 1);
        return isNext;
    }

    public void advanceNextUnchecked() {
        while (next()) {
            boolean checked = json.getJSONObject(curr).getBoolean("checked");
            if(checked)
                break;
        }
    }

    public void searchByMatNum(int matNum) {
        for(int i = 0; i < json.length(); i++) {
            if(json.getJSONObject(i).getInt("matNum") == matNum) {
                setCurr(i);
                System.out.println("Found!");
                printInfo();
                return;
            }
        }
        System.out.println("Not found!");
    }

    public void searchByLastName(String name) {
        for(int i = 0; i < json.length(); i++) {
            if(json.getJSONObject(i).getString("lastname").equals(name)) {
                setCurr(i);
                System.out.println("Found!");
                printInfo();
                return;
            }
        }
        System.out.println("Not found!");
    }

    public void load() {
        loadedFiles.clear();

        for (File f : workingDir.listFiles())
            FileHandler.recursiveDelete(f);

        int matNum = json.getJSONObject(curr).getInt("matNum");
        // potentially throws an exception if curr is out of bounds; error handling not supported yet TODO
        File currentDir = submissions.listFiles(path -> path.getName().contains(String.valueOf(matNum)))[0];
        List<File> javaFiles = FileHandler.recursiveSearch(currentDir, path -> path.getName().endsWith(".java"));
        for(File f : javaFiles) {
            File dest = new File(workingDir + "/" + currentDir.toPath().relativize(f.toPath()).toString());
            loadedFiles.add(dest);
            FileHandler.createDirectory(dest.getParentFile());
            try {
                Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        printWorkspace();
    }

    public void printWorkspace() {
        System.out.println(workingDir.getAbsolutePath());
        for(int i = 0; i < loadedFiles.size(); i++) {
            System.out.println((i+1) + ") " + workingDir.toPath().relativize(loadedFiles.get(i).toPath()));
        }
    }

    public void execJava(int fileIndex, boolean extraCmdWindow) {
        if(loadedFiles.isEmpty())
            System.out.println("Nothing to execute");
        else {
            // select file by path name
            File selected;
            try {
                selected = loadedFiles.get(--fileIndex);
            } catch (IndexOutOfBoundsException e) {
                System.out.println("index is not in range!");
                return;
            }

            if(extraCmdWindow) {
                executeCmdCommand(workingDir.getParentFile(), "start run.cmd \"" + selected.toPath().toAbsolutePath() + "\"");
            }
            else {
                if (executeCmdCommand(selected.getParentFile(), "java " + selected.toPath().toAbsolutePath()) != 0)
                    System.out.println("error executing");
            }
        }

    }

    public static int executeCmdCommand(File dir, String command) {
        try {
            Process process = new ProcessBuilder()
                    .directory(dir)
                    .command("cmd.exe", "/c", command)
                    .start();
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.submit(new StreamGobbler(process.getInputStream(), System.out::println));
            int exitCode = process.waitFor();
            service.shutdown();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setChecked(boolean checked) {
        json.put(curr, json.getJSONObject(curr).put("checked", checked));
    }

    public void setPassed(boolean passed) {
        json.put(curr, json.getJSONObject(curr).put("passed", passed));
    }

    public void comment(String commentary, boolean append) {
        if(append)
            commentary = json.getJSONObject(curr).getString("commentary") + " " + commentary;
        json.put(curr, json.getJSONObject(curr).put("commentary", commentary));
    }

    public void printComments() {
        for(int i = 0; i < json.length(); i++) {
            System.out.println(JSONHandler.submissionObjToComment(json.getJSONObject(i)) + "\n");
        }
    }

    public void updateCommentWindow() {
        commentWindow.setInfoText(JSONHandler.submissionObjToStringEssential(json.getJSONObject(curr)));
        commentWindow.setCommentText(json.getJSONObject(curr).getString("commentary"));
    }

    public void writeToJsonFromWindow() {
        String comment = commentWindow.getCommentText();
        json.getJSONObject(curr).put("commentary", comment);
        if(!comment.equals(""))
            setChecked(true);
    }

    public void exit() {
        save();
    }

    public static void main(String[] args) {
        SubmissionTester tester = new SubmissionTester("files/split/" + NAME + "", "files/check");
        //new Window(tester);
        tester.startDialog();
    }


}
