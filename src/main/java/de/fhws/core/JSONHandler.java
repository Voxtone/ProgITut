package de.fhws.core;

import org.json.JSONObject;

import java.io.*;

public class JSONHandler {

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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            bw.write(json.toString());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
