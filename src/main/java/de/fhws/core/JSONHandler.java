package de.fhws.core;

import org.json.JSONObject;

import java.io.*;

public class JSONHandler {

    public static JSONObject createSubmissionObj(int matNum, String firstname, String lastname) {
        JSONObject student = new JSONObject();
        student.put("matNum", matNum);
        student.put("firstname", firstname);
        student.put("lastname", lastname);
        student.put("checked", false);
        student.put("commentary", "");
        student.put("passed", false);
        return student;
    }

    public static String submissionObjToString(JSONObject sub) {
        return "{\nmatriculate number: " + sub.getInt("matNum") + "\n" +
                "name: \t\t\t\t" + sub.getString("firstname") + " " + sub.getString("lastname") + "\n" +
                "checked: \t\t\t" + sub.getBoolean("checked") + "\n" +
                "commentary: \t\t" + sub.getString("commentary") + "\n" +
                "passed: \t\t\t" + sub.getBoolean("passed") + "\n}";
    }

    public static String submissionObjToComment(JSONObject sub) {
        String comment = sub.getString("commentary");
        return sub.getString("firstname") + " " + sub.getString("lastname") + " " + sub.getInt("matNum") + " " +
                (sub.getBoolean("passed") ? "passed" : "!NOT PASSED!") +
                "\n" + (comment.equals("") ? "-/-" : comment);
    }

    public static JSONObject load(File path) {
        if(!path.isFile())
            throw new IllegalArgumentException("file is a directory");
        if(!path.getName().endsWith(".json"))
            throw new IllegalArgumentException("file not a .json");
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sourceString = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sourceString.append(line);
            }
            return new JSONObject(sourceString.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean save(File path, JSONObject json) {
        try {
            json.write(new BufferedWriter(new FileWriter(path))).close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
