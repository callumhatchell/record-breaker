package com.recordbreaker;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Collections;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:record_breaker.db";
    private static final String EXERCISE_CATALOG_RESOURCE = "/exercise_catalog.csv";
    private static Map<String, ExerciseProfile> exerciseProfilesCache;

    private record ExerciseProfile(String muscleGroup, String focusArea, String exerciseName) {}

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void createTables() {
        String usersTable = """
    CREATE TABLE IF NOT EXISTS users (
        username TEXT PRIMARY KEY,
        email TEXT NOT NULL,
        password TEXT NOT NULL,
        date_joined TEXT DEFAULT CURRENT_DATE
    );
    """;

        String profilesTable = """
    CREATE TABLE IF NOT EXISTS profiles (
        username TEXT PRIMARY KEY,
        display_name TEXT,
        height REAL DEFAULT 0,
        weight REAL DEFAULT 0,
        profile_picture TEXT,
        FOREIGN KEY (username) REFERENCES users(username)
    );
    """;



        String workoutsTable = """
    CREATE TABLE IF NOT EXISTS workouts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        username TEXT NOT NULL,
        exercise TEXT NOT NULL,
        weight REAL NOT NULL,
        reps INTEGER NOT NULL,
        is_warmup INTEGER NOT NULL DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (username) REFERENCES users(username)
    );
    """;

        String exerciseCatalogTable = """
CREATE TABLE IF NOT EXISTS exercise_catalog (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    exercise_name TEXT NOT NULL UNIQUE,
    muscle_group TEXT NOT NULL
);
""";

        String selectedSplitsTable = """
            CREATE TABLE IF NOT EXISTS selected_splits (
                username TEXT PRIMARY KEY,
                split_name TEXT NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username)
            );
            """;

        String splitExercisesTable = """
            CREATE TABLE IF NOT EXISTS split_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                split_name TEXT NOT NULL,
                day_name TEXT NOT NULL,
                exercise_name TEXT NOT NULL,
                exercise_order INTEGER NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username)
            );
            """;

        String alternativesTable = """
            CREATE TABLE IF NOT EXISTS exercise_alternatives (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                exercise_name TEXT NOT NULL,
                alternative_name TEXT NOT NULL
            );
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(usersTable);
            stmt.execute(profilesTable);
            stmt.execute(workoutsTable);
            stmt.execute(selectedSplitsTable);
            stmt.execute(splitExercisesTable);
            stmt.execute(alternativesTable);
            stmt.execute(exerciseCatalogTable);

            seedExerciseAlternatives();
            seedExerciseCatalog();

            ensureWorkoutWarmupColumn();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean createUser(String username, String email, String password) {
        String checkSql = "SELECT username FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users(username, email, password) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                return false;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, hashedPassword);
            insertStmt.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean loginUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                return BCrypt.checkpw(password, storedHash);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void saveProfile(String username, String displayName, double height, double weight, String profilePicture) {
        String updateSql = """
        INSERT INTO profiles(username, display_name, height, weight, profile_picture)
        VALUES (?, ?, ?, ?, ?)
        ON CONFLICT(username) DO UPDATE SET
            display_name = excluded.display_name,
            height = excluded.height,
            weight = excluded.weight,
            profile_picture = excluded.profile_picture
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {

            stmt.setString(1, username);
            stmt.setString(2, displayName);
            stmt.setDouble(3, height);
            stmt.setDouble(4, weight);
            stmt.setString(5, profilePicture);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getMostLoggedExercise(String username) {
        String sql = """
        SELECT exercise, COUNT(*) as total
        FROM workouts
        WHERE username = ?
        GROUP BY exercise
        ORDER BY total DESC
        LIMIT 1
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("exercise");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "N/A";
    }

    public static String getHeaviestLift(String username) {
        String sql = """
        SELECT exercise, MAX(weight) as max_weight
        FROM workouts
        WHERE username = ? AND is_warmup = 0
        GROUP BY exercise
        ORDER BY max_weight DESC
        LIMIT 1
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("exercise") + " - " + rs.getDouble("max_weight") + "kg";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "N/A";
    }

    public static List<String> getLastLoggedSetsForExercise(String username, String exercise) {
        List<String> sets = new ArrayList<>();

        String sql = """
        SELECT weight, reps
        FROM workouts
        WHERE username = ? AND exercise = ? AND is_warmup = 0
        ORDER BY datetime(created_at) DESC, id DESC
        LIMIT 3
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, exercise);

            ResultSet rs = stmt.executeQuery();

            List<String> temp = new ArrayList<>();
            while (rs.next()) {
                double weight = rs.getDouble("weight");
                int reps = rs.getInt("reps");
                temp.add(weight + "kg x " + reps);
            }

            Collections.reverse(temp);

            for (int i = 0; i < temp.size(); i++) {
                sets.add((i + 1) + ": " + temp.get(i));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sets;
    }

    public static double getEstimatedOneRepMax(String username, String exercise) {
        String sql = """
        SELECT weight, reps
        FROM workouts
        WHERE username = ? AND exercise = ? AND is_warmup = 0
        """;

        double bestEstimate = 0.0;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, exercise);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                double weight = rs.getDouble("weight");
                int reps = rs.getInt("reps");

                if (reps > 0) {
                    double estimate = weight * (1.0 + (reps / 30.0));
                    if (estimate > bestEstimate) {
                        bestEstimate = estimate;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Math.round(bestEstimate * 10.0) / 10.0;
    }

    public static String getMostImprovedExercise(String username) {
        String sql = """
        SELECT exercise, (MAX(weight) - MIN(weight)) as improvement
        FROM workouts
        WHERE username = ? AND is_warmup = 0
        GROUP BY exercise
        ORDER BY improvement DESC
        LIMIT 1
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("exercise") + " (+" + rs.getDouble("improvement") + "kg)";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "N/A";
    }

    public static void seedExerciseCatalog() {
        loadExerciseCatalogFromResource(EXERCISE_CATALOG_RESOURCE);
    }

    private static void loadExerciseCatalogFromResource(String resourcePath) {
        try (InputStream inputStream = DatabaseHelper.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                System.err.println("Exercise catalog resource not found: " + resourcePath);
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    if (isFirstLine) {
                        isFirstLine = false;
                        if (trimmed.equalsIgnoreCase("muscle_group,focus_area,exercise_name")) {
                            continue;
                        }
                    }

                    String[] parts = trimmed.split(",", 3);
                    if (parts.length < 3) {
                        continue;
                    }

                    String muscleGroup = parts[0].trim();
                    String exerciseName = parts[2].trim();

                    if (!muscleGroup.isEmpty() && !exerciseName.isEmpty()) {
                        insertExerciseIfMissing(exerciseName, muscleGroup);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, ExerciseProfile> getExerciseProfiles() {
        if (exerciseProfilesCache != null) {
            return exerciseProfilesCache;
        }

        Map<String, ExerciseProfile> profiles = new LinkedHashMap<>();

        try (InputStream inputStream = DatabaseHelper.class.getResourceAsStream(EXERCISE_CATALOG_RESOURCE)) {
            if (inputStream == null) {
                exerciseProfilesCache = profiles;
                return exerciseProfilesCache;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                boolean isFirstLine = true;

                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }

                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    String[] parts = trimmed.split(",", 3);
                    if (parts.length < 3) {
                        continue;
                    }

                    String muscleGroup = parts[0].trim();
                    String focusArea = parts[1].trim();
                    String exerciseName = parts[2].trim();

                    if (!exerciseName.isEmpty()) {
                        profiles.put(exerciseName, new ExerciseProfile(muscleGroup, focusArea, exerciseName));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        exerciseProfilesCache = profiles;
        return exerciseProfilesCache;
    }

    private static void insertExerciseIfMissing(String exerciseName, String muscleGroup) {
        String checkSql = "SELECT 1 FROM exercise_catalog WHERE exercise_name = ?";
        String insertSql = "INSERT INTO exercise_catalog(exercise_name, muscle_group) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, exerciseName);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                insertStmt.setString(1, exerciseName);
                insertStmt.setString(2, muscleGroup);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getDateJoined(String username) {
        String sql = "SELECT date_joined FROM users WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("date_joined");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "N/A";
    }

    public static int getLoggingStreak(String username) {
        String sql = """
        SELECT DISTINCT DATE(created_at) as workout_date
        FROM workouts
        WHERE username = ?
        ORDER BY workout_date DESC
        """;

        List<String> dates = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dates.add(rs.getString("workout_date"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        if (dates.isEmpty()) {
            return 0;
        }

        int streak = 0;
        java.time.LocalDate current = java.time.LocalDate.now();

        for (String dateStr : dates) {
            java.time.LocalDate workoutDate = java.time.LocalDate.parse(dateStr);

            if (workoutDate.equals(current)) {
                streak++;
                current = current.minusDays(1);
            } else if (workoutDate.equals(current.minusDays(1)) && streak == 0) {
                streak++;
                current = current.minusDays(2);
            } else if (workoutDate.equals(current)) {
                streak++;
                current = current.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    public static String[] getProfileDetails(String username) {
        String sql = """
        SELECT display_name, height, weight, profile_picture
        FROM profiles
        WHERE username = ?
        """;

        String[] profile = new String[]{"", "0", "0", ""};

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                profile[0] = rs.getString("display_name") != null ? rs.getString("display_name") : "";
                profile[1] = String.valueOf(rs.getDouble("height"));
                profile[2] = String.valueOf(rs.getDouble("weight"));
                profile[3] = rs.getString("profile_picture") != null ? rs.getString("profile_picture") : "";
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return profile;
    }

    public static void backupDatabase() {
        try {
            File dbFile = new File("recordbreaker.db");

            if (!dbFile.exists()) {
                return; // no database yet
            }

            File backupDir = new File("backups");
            if (!backupDir.exists()) {
                backupDir.mkdir();
            }

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"));

            File backupFile = new File("backups/recordbreaker_" + timestamp + ".db");

            Files.copy(
                    dbFile.toPath(),
                    backupFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );

            System.out.println("Database backup created: " + backupFile.getName());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double[] getProfile(String username) {
        String sql = "SELECT height, weight FROM profiles WHERE username = ?";
        double[] profile = new double[]{0, 0};

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                profile[0] = rs.getDouble("height");
                profile[1] = rs.getDouble("weight");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return profile;
    }

    public static void saveWorkout(String username, String exercise, double weight, int reps) {
        saveWorkout(username, exercise, weight, reps, false);
    }

    public static void saveWorkout(String username, String exercise, double weight, int reps, boolean isWarmup) {
        String sql = "INSERT INTO workouts(username, exercise, weight, reps, is_warmup) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, exercise);
            stmt.setDouble(3, weight);
            stmt.setInt(4, reps);
            stmt.setInt(5, isWarmup ? 1 : 0);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void ensureWorkoutWarmupColumn() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("PRAGMA table_info(workouts)");
            boolean hasWarmupColumn = false;

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("is_warmup".equalsIgnoreCase(columnName)) {
                    hasWarmupColumn = true;
                    break;
                }
            }

            if (!hasWarmupColumn) {
                stmt.executeUpdate("ALTER TABLE workouts ADD COLUMN is_warmup INTEGER NOT NULL DEFAULT 0");
                System.out.println("Added is_warmup column to workouts table.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getBestReps(String username, String exercise, double weight) {
        String sql = """
        SELECT MAX(reps) AS best_reps
        FROM workouts
        WHERE username = ? AND exercise = ? AND weight = ? AND is_warmup = 0
        """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, exercise);
            stmt.setDouble(3, weight);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("best_reps");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static List<String> getWorkoutHistory(String username) {
        String sql = """
        SELECT exercise, weight, reps, is_warmup, created_at
        FROM workouts
        WHERE username = ?
        ORDER BY created_at DESC
        """;

        List<String> history = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boolean isWarmup = rs.getInt("is_warmup") == 1;
                String prefix = isWarmup ? "[Warm-up] " : "";

                String entry = prefix
                        + rs.getString("exercise") + " - "
                        + rs.getDouble("weight") + "kg x "
                        + rs.getInt("reps") + " reps ("
                        + rs.getString("created_at") + ")";
                history.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return history;
    }

    public static void saveSelectedSplit(String username, String splitName) {
        String sql = """
            INSERT INTO selected_splits(username, split_name)
            VALUES (?, ?)
            ON CONFLICT(username) DO UPDATE SET
                split_name = excluded.split_name
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, splitName);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getSelectedSplit(String username) {
        String sql = "SELECT split_name FROM selected_splits WHERE username = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("split_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<String> getExercisesForDay(String username, String splitName, String day) {
        String sql = """
            SELECT exercise_name
            FROM split_exercises
            WHERE username = ? AND split_name = ? AND day_name = ?
            ORDER BY exercise_order ASC
            """;

        List<String> exercises = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, splitName);
            stmt.setString(3, day);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exercises.add(rs.getString("exercise_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exercises;
    }

    public static String getWorkoutForDay(String username, String splitName, String day) {
        List<String> exercises = getExercisesForDay(username, splitName, day);

        if (exercises.isEmpty()) {
            return "Rest Day";
        }

        return switch (splitName) {
            case "Push Pull Legs" -> switch (day) {
                case "Monday", "Friday" -> "Push Day";
                case "Tuesday", "Saturday" -> "Pull Day";
                case "Wednesday", "Sunday" -> "Leg Day";
                default -> "Rest Day";
            };
            case "Arnold Split" -> switch (day) {
                case "Monday", "Thursday" -> "Chest + Back";
                case "Tuesday", "Friday" -> "Shoulders + Arms";
                case "Wednesday", "Saturday" -> "Legs";
                default -> "Rest Day";
            };
            case "Upper Lower" -> switch (day) {
                case "Monday", "Thursday" -> "Upper Body";
                case "Tuesday", "Friday" -> "Lower Body";
                default -> "Rest Day";
            };
            case "Full Body" -> switch (day) {
                case "Monday", "Wednesday", "Friday" -> "Full Body";
                default -> "Rest Day";
            };
            case "Custom Split" -> "Custom Day";
            default -> "Rest Day";
        };
    }

    public static void addExerciseToDay(String username, String splitName, String day, String exerciseName, int order) {
        String sql = """
            INSERT INTO split_exercises(username, split_name, day_name, exercise_name, exercise_order)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, splitName);
            stmt.setString(3, day);
            stmt.setString(4, exerciseName);
            stmt.setInt(5, order);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void replaceExercise(String username, String splitName, String day, String oldExercise, String newExercise) {
        String sql = """
            UPDATE split_exercises
            SET exercise_name = ?
            WHERE username = ? AND split_name = ? AND day_name = ? AND exercise_name = ?
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newExercise);
            stmt.setString(2, username);
            stmt.setString(3, splitName);
            stmt.setString(4, day);
            stmt.setString(5, oldExercise);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeExercise(String username, String splitName, String day, String exercise) {
        String sql = """
            DELETE FROM split_exercises
            WHERE username = ? AND split_name = ? AND day_name = ? AND exercise_name = ?
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, splitName);
            stmt.setString(3, day);
            stmt.setString(4, exercise);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAlternatives(String exercise) {
        String sql = """
            SELECT alternative_name
            FROM exercise_alternatives
            WHERE exercise_name = ?
            ORDER BY alternative_name ASC
            """;

        LinkedHashSet<String> alternatives = new LinkedHashSet<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, exercise);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                alternatives.add(rs.getString("alternative_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String focusArea = getFocusAreaForExercise(exercise);
        String muscleGroup = getMuscleGroupForExercise(exercise);

        if (focusArea != null && !focusArea.isBlank()) {
            for (ExerciseProfile profile : getExerciseProfiles().values()) {
                if (!profile.exerciseName().equalsIgnoreCase(exercise)
                        && profile.muscleGroup().equalsIgnoreCase(muscleGroup)
                        && profile.focusArea().equalsIgnoreCase(focusArea)) {
                    alternatives.add(profile.exerciseName());
                }
            }
        } else if (muscleGroup != null && !muscleGroup.isBlank()) {
            for (String relatedExercise : getExercisesByMuscleGroup(muscleGroup)) {
                if (!relatedExercise.equalsIgnoreCase(exercise)) {
                    alternatives.add(relatedExercise);
                }
            }
        }

        return new ArrayList<>(alternatives);
    }

    public static void seedExerciseAlternatives() {
        insertAlternativeIfMissing("Bench Press", "Dumbbell Bench Press");
        insertAlternativeIfMissing("Bench Press", "Machine Chest Press");
        insertAlternativeIfMissing("Incline Dumbbell Bench Press", "Incline Barbell Bench Press");
        insertAlternativeIfMissing("Machine Pec Fly", "Cable Fly");
        insertAlternativeIfMissing("Lat Pulldown", "Pull Ups");
        insertAlternativeIfMissing("Cable Row", "Chest Supported Row");
        insertAlternativeIfMissing("Dumbbell Shoulder Press", "Machine Shoulder Press");
        insertAlternativeIfMissing("Cable Lateral Raise", "Dumbbell Lateral Raise");
        insertAlternativeIfMissing("Tricep Pushdown", "Overhead Tricep Extension");
        insertAlternativeIfMissing("Preacher Curls", "Barbell Curl");
        insertAlternativeIfMissing("Squats", "Leg Press");
        insertAlternativeIfMissing("Leg Curl", "Romanian Deadlift");
        insertAlternativeIfMissing("Calf Raise", "Seated Calf Raise");
    }

    public static List<String> getAllMuscleGroups() {
        List<String> groups = new ArrayList<>();
        String sql = """
        SELECT DISTINCT muscle_group
        FROM exercise_catalog
        ORDER BY muscle_group ASC
    """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                groups.add(rs.getString("muscle_group"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return groups;
    }

    public static List<String> getExercisesByMuscleGroup(String muscleGroup) {
        List<String> exercises = new ArrayList<>();
        String sql;

        if (muscleGroup == null || muscleGroup.equals("All")) {
            sql = """
            SELECT exercise_name
            FROM exercise_catalog
            ORDER BY exercise_name ASC
        """;
        } else {
            sql = """
            SELECT exercise_name
            FROM exercise_catalog
            WHERE muscle_group = ?
            ORDER BY exercise_name ASC
        """;
        }

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (sql.contains("WHERE muscle_group = ?")) {
                stmt.setString(1, muscleGroup);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                exercises.add(rs.getString("exercise_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return exercises;
    }

    public static List<String> searchExercises(String muscleGroup, String query) {
        List<String> exercises = getExercisesByMuscleGroup(muscleGroup);
        if (query == null || query.isBlank()) {
            return exercises;
        }

        String normalizedQuery = query.trim().toLowerCase();
        List<String> startsWithMatches = new ArrayList<>();
        List<String> containsMatches = new ArrayList<>();

        for (String exercise : exercises) {
            String normalizedExercise = exercise.toLowerCase();
            if (normalizedExercise.startsWith(normalizedQuery)) {
                startsWithMatches.add(exercise);
            } else if (normalizedExercise.contains(normalizedQuery)) {
                containsMatches.add(exercise);
            }
        }

        List<String> results = new ArrayList<>(startsWithMatches);
        results.addAll(containsMatches);
        return results;
    }

    public static String getMuscleGroupForExercise(String exerciseName) {
        ExerciseProfile profile = getExerciseProfiles().get(exerciseName);
        if (profile != null) {
            return profile.muscleGroup();
        }

        String sql = """
            SELECT muscle_group
            FROM exercise_catalog
            WHERE exercise_name = ?
            LIMIT 1
            """;

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, exerciseName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("muscle_group");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Custom";
    }

    public static String getFocusAreaForExercise(String exerciseName) {
        ExerciseProfile profile = getExerciseProfiles().get(exerciseName);
        if (profile != null) {
            return profile.focusArea();
        }
        return "";
    }

    private static void insertAlternativeIfMissing(String exerciseName, String alternativeName) {
        String checkSql = """
            SELECT 1
            FROM exercise_alternatives
            WHERE exercise_name = ? AND alternative_name = ?
            """;

        String insertSql = """
            INSERT INTO exercise_alternatives(exercise_name, alternative_name)
            VALUES (?, ?)
            """;

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, exerciseName);
            checkStmt.setString(2, alternativeName);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                insertStmt.setString(1, exerciseName);
                insertStmt.setString(2, alternativeName);
                insertStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
