package model;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONArray;

public class UserManager {
    private static final String USER_FILE = "users.dat";
    private Map<String, String> users;

    public UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    private void loadUsers() {
        File file = new File(USER_FILE);
        System.out.println("Loading users from: " + file.getAbsolutePath());
        
        if (!file.exists()) {
            System.out.println("No users file found - starting fresh");
            return;
        }

        StringBuilder json = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            
            JSONObject jsonObject = new JSONObject(json.toString());
            JSONArray usersArray = jsonObject.getJSONArray("users");
            
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject user = usersArray.getJSONObject(i);
                String username = user.getString("username");
                String password = user.getString("password");
                users.put(username, password);
            }
            System.out.println("Loaded " + users.size() + " users");
        } catch (Exception e) {
            System.err.println("Error loading users:");
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        File file = new File(USER_FILE);
        System.out.println("Saving users to: " + file.getAbsolutePath());
        
        try {
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            
            JSONObject jsonObject = new JSONObject();
            JSONArray usersArray = new JSONArray();
            
            for (Map.Entry<String, String> entry : users.entrySet()) {
                JSONObject user = new JSONObject();
                user.put("username", entry.getKey());
                user.put("password", entry.getValue());
                user.put("created", System.currentTimeMillis());
                usersArray.put(user);
            }
            
            jsonObject.put("users", usersArray);
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(jsonObject.toString(2)); // Pretty print with 2-space indent
                System.out.println("Successfully saved " + users.size() + " users in JSON format");
                System.out.println("File size: " + file.length() + " bytes");
            }
        } catch (Exception e) {
            System.err.println("Error saving users:");
            e.printStackTrace();
        }
    }

    public boolean validateUser(String username, String password) {
        boolean exists = users.containsKey(username);
        boolean matches = exists && users.get(username).equals(password);
        System.out.println("Validation - User: " + username + 
                         ", Exists: " + exists + 
                         ", Password matches: " + matches);
        return matches;
    }
    
    public Map<String, String> getUsers() {
        return new HashMap<>(users);
    }

    public boolean registerUser(String username, String password) {
        System.out.println("RegisterUser called for: " + username);
        if (users.containsKey(username)) {
            System.out.println("Registration failed - user exists: " + username);
            return false;
        }
        System.out.println("Adding new user to map");
        users.put(username, password);
        System.out.println("Calling saveUsers()");
        saveUsers();
        System.out.println("Registered new user: " + username);
        System.out.println("Current users: " + users);
        
        // Verify file was written
        File file = new File(USER_FILE);
        System.out.println("User file exists: " + file.exists());
        System.out.println("User file size: " + file.length());
        
        return true;
    }
}
