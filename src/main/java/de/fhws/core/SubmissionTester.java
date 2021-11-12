package de.fhws.core;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// TODO fix java command, add quick selection with number
// TODO delete package line in every .java file
// TODO fix next -u (one to far)
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
        builder.append("current index: ").append(curr).append(" of ").append(json.length() - 1).append("\n");
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
                java [-n] <filename> \t\t\t executes the given .java file; if -n then a new cmd window will be opened (needed for input)
                check \t\t\t\t\t\t marks as checked
                uncheck \t\t\t\t\t marks as unchecked
                pass \t\t\t\t\t\t marks as passed
                unpass \t\t\t\t\t\t marks as not passed
                comment [-a] <commentary> \t\t sets a comment and marks as checked. If -a is set, comment will be appended
                commentary \t\t\t\t\t lists all comments (in order to copy paste)
                exit \t\t\t\t\t\t saves and exits
                help \t\t\t\t\t\t displays this page""";
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
            if(command.replaceFirst("java ", "").startsWith("-n "))
                execJava(command.replaceFirst("java -n ", ""), true);
            else
                execJava(command.replaceFirst("java ", ""), false);
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
            if(command.replaceFirst("comment ", "").startsWith("-a "))
                comment(command.replaceFirst("comment -a ", ""), true);
            else
                comment(command.replaceFirst("comment ", ""), false);

            setChecked(true);
            printInfo();
            return true;
        }
        else if(command.startsWith("commentary")) {
            commentary();
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

    private void save() {
        JSONObject mainObj = new JSONObject();
        mainObj.put("submissions", json);
        if(!JSONHandler.save(jsonFile, mainObj))
            System.out.println("failed to save data");
    }

    private void setCurr(int index) {
        if(index >= 0 && index < json.length())
            curr = index;
        else
            System.out.println("index not in range!");
    }

    /**
     * advances current; equivalent to setCurr(curr+1)
     * @return {@code true} if there is a next element {@code false} otherwise
     */
    private boolean next() {
        boolean isNext = curr < json.length() - 1;
        setCurr(curr + 1);
        return isNext;
    }

    private void advanceNextUnchecked() {
        boolean checked = true;
        while (next() && checked) {
            checked = json.getJSONObject(curr).getBoolean("checked");
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

    private void execJava(String filename, boolean extraCmdWindow) {
        List<File> javaFiles = FileHandler.recursiveSearch(workingDir, path -> path.getName().endsWith(".java"));
        if(javaFiles.isEmpty())
            System.out.println("Nothing to execute");
        else {
            // select file by path name
            if(!filename.endsWith(".java"))
                filename += ".java";
            File selected = null;
            for (File f : javaFiles) {
                if(f.getName().equals(filename)) {
                    selected = f;
                    break;
                }
            }
            // if a file could be selected
            if(selected != null) {
                if(extraCmdWindow) {
                    executeCmdCommand(workingDir.getParentFile(), "start run.cmd " + selected.getName().replaceFirst(".java", ""));
                }
                else {
                    if (executeCmdCommand(selected.getParentFile(), "javac " + selected.getName()) != 0) {
                        System.out.println("error compiling");
                    } else {
                        if (executeCmdCommand(selected.getParentFile(), "java " + selected.getName().replaceFirst(".java", "")) != 0)
                            System.out.println("error executing");
                    }
                }
            }
            else
                System.out.println("file not found!");
        }

    }

    private static int executeCmdCommand(File dir, String command) {
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

    private void setChecked(boolean checked) {
        json.put(curr, json.getJSONObject(curr).put("checked", checked));
    }

    private void setPassed(boolean passed) {
        json.put(curr, json.getJSONObject(curr).put("passed", passed));
    }

    private void comment(String commentary, boolean append) {
        if(append)
            commentary = json.getJSONObject(curr).getString("commentary") + " " + commentary;
        json.put(curr, json.getJSONObject(curr).put("commentary", commentary));
    }

    private void commentary() {
        for(int i = 0; i < json.length(); i++) {
            System.out.println(JSONHandler.submissionObjToComment(json.getJSONObject(i)) + "\n");
        }
    }

    private void exit() {
        save();
    }

    public static void main(String[] args) {
        SubmissionTester tester = new SubmissionTester("files/split/" + NAME + "", "files/check");
        tester.startDialog();
    }


}
