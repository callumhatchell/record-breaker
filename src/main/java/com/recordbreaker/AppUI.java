package com.recordbreaker;
import javafx.scene.Node;
import javafx.scene.text.TextAlignment;
import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AppUI extends Application {

    private StackPane root;
    private BorderPane mainLayout;
    private HashMap<String, Integer> exerciseRecords = new HashMap<>();

    @Override
    public void start(Stage stage) {
        DatabaseHelper.backupDatabase();
        DatabaseHelper.createTables();

        mainLayout = new BorderPane();
        root = new StackPane();
        root.getChildren().add(mainLayout);

        Scene scene = new Scene(root, 400, 650);

        showSplashScreen();

        stage.setTitle("Record Breaker");
        stage.setScene(scene);
        stage.show();
    }

    private void showLoginScreen() {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(340);
        card.setPadding(new Insets(28));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.96);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 24;"
        );

        Label icon = new Label("🏋");
        icon.setStyle("-fx-font-size: 46px;");

        Label title = new Label("Record Breaker");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitle = new Label("Log in to continue");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setStyle(modernFieldStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setStyle(modernFieldStyle());

        Label usernameHint = new Label("Username: 4-20 characters, letters/numbers/_ only");
        usernameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label passwordHint = new Label("Password: 8+ chars, upper, lower, and number");
        passwordHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Button loginButton = new Button("Log In");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setStyle(modernPrimaryButtonStyle());

        Button signUpButton = new Button("Create Account");
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButton.setStyle(modernSecondaryButtonStyle());

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            if (!isValidUsername(username)) {
                messageLabel.setText("Username must be 4-20 characters and only use letters, numbers, or _");
                return;
            }

            if (!isValidPassword(password)) {
                messageLabel.setText("Password must be at least 8 characters and include uppercase, lowercase, and a number.");
                return;
            }

            boolean validLogin = DatabaseHelper.loginUser(username, password);
            if (validLogin) {
                showDashboard(username);
            } else {
                messageLabel.setText("Invalid username or password.");
            }
        });

        signUpButton.setOnAction(e -> showSignUpScreen());

        card.getChildren().addAll(
                icon,
                title,
                subtitle,
                usernameField,
                usernameHint,
                passwordField,
                passwordHint,
                loginButton,
                signUpButton,
                messageLabel
        );

        StackPane wrapper = new StackPane(card);
        wrapper.setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #111827, #1f2937);");
        wrapper.setPadding(new Insets(30));

        mainLayout.setCenter(wrapper);
    }

    private void showRecordBrokenAnimation() {
        Label recordLabel = new Label("RECORD BROKE!");
        recordLabel.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: red;" +
                        "-fx-background-color: rgba(0,0,0,0.8);" +
                        "-fx-padding: 20;" +
                        "-fx-background-radius: 15;"
        );

        StackPane overlay = new StackPane(recordLabel);
        overlay.setMouseTransparent(true);
        root.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), recordLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition pop = new ScaleTransition(Duration.millis(300), recordLabel);
        pop.setFromX(0.3);
        pop.setFromY(0.3);
        pop.setToX(1);
        pop.setToY(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), recordLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        SequentialTransition animation = new SequentialTransition(
                fadeIn, pop, pause, fadeOut
        );

        animation.setOnFinished(e -> root.getChildren().remove(overlay));
        animation.play();
    }

    private void showRecordBrokenAnimation(String exercise, double weight, int reps) {
        VBox badge = new VBox(10);
        badge.setAlignment(Pos.CENTER);
        badge.setMaxWidth(280);
        badge.setStyle(
                "-fx-background-color: rgba(15,15,15,0.92);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 24;" +
                        "-fx-padding: 25;"
        );

        Label titleLabel = new Label("RECORD BROKE!");
        titleLabel.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #ff4d4d;"
        );

        Label exerciseLabel = new Label(exercise);
        exerciseLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label statsLabel = new Label(weight + "kg x " + reps + " reps");
        statsLabel.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-text-fill: #ffd166;"
        );

        badge.getChildren().addAll(titleLabel, exerciseLabel, statsLabel);
        badge.setScaleX(0.3);
        badge.setScaleY(0.3);
        badge.setOpacity(0);

        Pane shardPane = new Pane();
        shardPane.setMouseTransparent(true);
        shardPane.setPickOnBounds(false);

        StackPane overlay = new StackPane();
        overlay.setMouseTransparent(true);
        overlay.setPickOnBounds(false);
        overlay.getChildren().addAll(shardPane, badge);

        root.getChildren().add(overlay);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), badge);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition popIn = new ScaleTransition(Duration.millis(350), badge);
        popIn.setFromX(0.3);
        popIn.setFromY(0.3);
        popIn.setToX(1.08);
        popIn.setToY(1.08);

        ScaleTransition settle = new ScaleTransition(Duration.millis(140), badge);
        settle.setFromX(1.08);
        settle.setFromY(1.08);
        settle.setToX(1.0);
        settle.setToY(1.0);

        TranslateTransition shake = new TranslateTransition(Duration.millis(70), badge);
        shake.setFromX(-6);
        shake.setToX(6);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);

        PauseTransition pause = new PauseTransition(Duration.millis(900));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(450), badge);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ScaleTransition shrinkOut = new ScaleTransition(Duration.millis(450), badge);
        shrinkOut.setToX(0.85);
        shrinkOut.setToY(0.85);

        for (int i = 0; i < 14; i++) {
            Polygon shard = new Polygon();
            shard.getPoints().addAll(
                    0.0, 0.0,
                    10.0, 3.0,
                    4.0, 13.0
            );

            shard.setFill(Color.LIGHTCYAN);
            shard.setOpacity(0.9);
            shard.setLayoutX(200);
            shard.setLayoutY(325);
            shardPane.getChildren().add(shard);

            double dx = (Math.random() - 0.5) * 360;
            double dy = (Math.random() - 0.5) * 360;

            TranslateTransition move = new TranslateTransition(Duration.millis(900), shard);
            move.setByX(dx);
            move.setByY(dy);

            RotateTransition rotate = new RotateTransition(Duration.millis(900), shard);
            rotate.setByAngle((Math.random() - 0.5) * 900);

            FadeTransition shardFade = new FadeTransition(Duration.millis(900), shard);
            shardFade.setFromValue(0.9);
            shardFade.setToValue(0);

            ParallelTransition shardAnim = new ParallelTransition(move, rotate, shardFade);
            shardAnim.play();
        }

        ParallelTransition intro = new ParallelTransition(fadeIn, popIn);
        ParallelTransition outro = new ParallelTransition(fadeOut, shrinkOut);

        SequentialTransition full = new SequentialTransition(
                intro, settle, shake, pause, outro
        );

        full.setOnFinished(e -> root.getChildren().remove(overlay));
        full.play();
    }

    private void showSignUpScreen() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(380);
        card.setPadding(new Insets(28));
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 24;"
        );

        Label title = new Label("Create Account");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitle = new Label("Build your profile and start tracking progress");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #6b7280;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setStyle(modernFieldStyle());

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setStyle(modernFieldStyle());

        TextField confirmEmailField = new TextField();
        confirmEmailField.setPromptText("Confirm email");
        confirmEmailField.setStyle(modernFieldStyle());

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.setStyle(modernFieldStyle());

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        confirmPasswordField.setStyle(modernFieldStyle());

        Label emailMatchIndicator = createMatchIndicator();
        Label passwordMatchIndicator = createMatchIndicator();

        HBox emailConfirmRow = new HBox(8, confirmEmailField, emailMatchIndicator);
        emailConfirmRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(confirmEmailField, Priority.ALWAYS);

        HBox passwordConfirmRow = new HBox(8, confirmPasswordField, passwordMatchIndicator);
        passwordConfirmRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(confirmPasswordField, Priority.ALWAYS);

        Label usernameHint = new Label("Username: 4-20 characters, letters/numbers/_ only");
        usernameHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label emailHint = new Label("Enter a valid email format");
        emailHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label passwordHint = new Label("Password: 8+ chars, upper, lower, and number");
        passwordHint.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Runnable refreshIndicators = () -> {
            String email = emailField.getText().trim();
            String confirmEmail = confirmEmailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (!confirmEmail.isEmpty()) {
                updateMatchIndicator(emailMatchIndicator, email.equals(confirmEmail));
            } else {
                emailMatchIndicator.setText("○");
                emailMatchIndicator.setStyle("-fx-font-size: 18px; -fx-text-fill: #9ca3af;");
            }

            if (!confirmPassword.isEmpty()) {
                updateMatchIndicator(passwordMatchIndicator, password.equals(confirmPassword));
            } else {
                passwordMatchIndicator.setText("○");
                passwordMatchIndicator.setStyle("-fx-font-size: 18px; -fx-text-fill: #9ca3af;");
            }
        };

        emailField.textProperty().addListener((obs, oldVal, newVal) -> refreshIndicators.run());
        confirmEmailField.textProperty().addListener((obs, oldVal, newVal) -> refreshIndicators.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> refreshIndicators.run());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> refreshIndicators.run());

        Button createButton = new Button("Sign Up");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setStyle(modernPrimaryButtonStyle());

        Button backButton = new Button("Back to Login");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");

        createButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String confirmEmail = confirmEmailField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty() || email.isEmpty() || confirmEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            if (!isValidUsername(username)) {
                messageLabel.setText("Username must be 4-20 characters and only use letters, numbers, or _");
                return;
            }

            if (!isValidEmailFormat(email)) {
                messageLabel.setText("Please enter a valid email address.");
                return;
            }

            if (!email.equals(confirmEmail)) {
                messageLabel.setText("Emails do not match.");
                return;
            }

            if (!isValidPassword(password)) {
                messageLabel.setText("Password must be at least 8 characters and include uppercase, lowercase, and a number.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match.");
                return;
            }

            boolean created = DatabaseHelper.createUser(username, email, password);
            if (created) {
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #16a34a;");
                messageLabel.setText("Account created successfully.");
                // Placeholder for confirmation email hook:
                // EmailHelper.sendConfirmationEmail(email, username);
            } else {
                messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");
                messageLabel.setText("Username already exists.");
            }
        });

        backButton.setOnAction(e -> showLoginScreen());

        card.getChildren().addAll(
                title,
                subtitle,
                usernameField,
                usernameHint,
                emailField,
                emailConfirmRow,
                emailHint,
                passwordField,
                passwordConfirmRow,
                passwordHint,
                createButton,
                backButton,
                messageLabel
        );

        StackPane wrapper = new StackPane(card);
        wrapper.setStyle("-fx-background-color: linear-gradient(to bottom, #0f172a, #111827, #1f2937);");
        wrapper.setPadding(new Insets(30));

        mainLayout.setCenter(wrapper);
    }

    private void showSplashScreen() {
        VBox layout = new VBox(18);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #0f172a, #111827, #1f2937);"
        );

        Label icon = new Label("🏋");
        icon.setStyle("-fx-font-size: 74px;");

        Label title = new Label("RECORD BREAKER");
        title.setStyle(
                "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-letter-spacing: 1.5px;"
        );

        Label subtitle = new Label("Train harder. Track smarter.");
        subtitle.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-text-fill: #cbd5e1;"
        );

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(240);
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #22c55e;");

        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #94a3b8;"
        );

        layout.getChildren().addAll(icon, title, subtitle, progressBar, loadingLabel);
        mainLayout.setCenter(layout);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(2.2), new KeyValue(progressBar.progressProperty(), 1))
        );

        timeline.setOnFinished(e -> showLoginScreen());
        timeline.play();
    }

    private void startWorkout(String username, String todayPlan) {
        String selectedSplit = DatabaseHelper.getSelectedSplit(username);
        String today = LocalDate.now()
                .getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        if (selectedSplit == null || selectedSplit.isBlank()) {
            selectedSplit = "Push Pull Legs";
        }

        List<String> exercises = DatabaseHelper.getExercisesForDay(username, selectedSplit, today);

        if (todayPlan.equals("Rest Day") || exercises.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rest Day");
            alert.setHeaderText(null);
            alert.setContentText("Today is a rest day. Recover well!");
            alert.showAndWait();
            return;
        }

        showCustomWorkout(username, todayPlan, exercises.toArray(new String[0]), -1);
    }

    private void showCustomWorkout(String username, String workoutTitle, String[] exercises, int currentIndex) {
        if (exercises == null || exercises.length == 0) {
            showWorkoutComplete(username);
            return;
        }

        if (currentIndex < 0) {
            currentIndex = 0;
        }

        String currentExercise = exercises[currentIndex];

        Label title = new Label(currentExercise.toUpperCase());
        title.setStyle(
                "-fx-font-size: 30px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        Label subtitle = new Label(workoutTitle);
        subtitle.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #6b7280;"
        );

        StackPane imageCard = new StackPane();
        imageCard.setPrefSize(260, 260);
        imageCard.setMaxSize(260, 260);
        imageCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-width: 2;"
        );

        Label iconLabel = new Label(getExerciseIcon(currentExercise));
        iconLabel.setStyle("-fx-font-size: 90px;");

        imageCard.getChildren().add(iconLabel);

        Button backButton = new Button("⬅ Back");
        Button changeButton = new Button("⇄ Change");
        Button nextButton = new Button("Log Sets");

        backButton.setPrefWidth(110);
        changeButton.setPrefWidth(110);
        nextButton.setPrefWidth(110);

        backButton.setStyle(modernNavButtonStyle());
        changeButton.setStyle(modernNavButtonStyle());
        nextButton.setStyle(modernNavButtonStyle());

        final int indexToUse = currentIndex;

        backButton.setOnAction(e -> {
            if (indexToUse > 0) {
                showCustomWorkout(username, workoutTitle, exercises, indexToUse - 1);
            }
        });

        changeButton.setOnAction(e -> showChangeExerciseDialog(username, workoutTitle, exercises, indexToUse));

        nextButton.setOnAction(e -> {
            showSetLoggerScreen(username, workoutTitle, exercises, indexToUse);
        });

        if (currentIndex == 0) {
            backButton.setVisible(false);
            backButton.setManaged(false);
        }

        HBox bottomBar = new HBox(15, backButton, changeButton, nextButton);
        bottomBar.setAlignment(Pos.CENTER);

        Region spacerTop = new Region();
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        VBox layout = new VBox(18);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8fafc;");
        layout.getChildren().addAll(
                subtitle,
                spacerTop,
                imageCard,
                title,
                spacerBottom,
                bottomBar
        );

        mainLayout.setCenter(layout);
    }

    private void showChangeExerciseDialog(String username, String workoutTitle, String[] exercises, int currentIndex) {
        String currentExercise = exercises[currentIndex];

        List<String> allExercises = new ArrayList<>();
        allExercises.addAll(Arrays.asList(
                "Bench Press",
                "Incline Dumbbell Bench Press",
                "Machine Pec Fly",
                "Tricep Pushdown",
                "Overhead Extension",
                "Dips",
                "Dumbbell Shoulder Press",
                "Cable Lateral Raise",
                "Lat Pulldown",
                "Close Grip Lat Pulldown",
                "Cable Row",
                "Cable Face Pulls",
                "Dumbbell Shrugs",
                "Preacher Curls",
                "Incline Dumbbell Curls",
                "Reverse Wrist Curls",
                "Squats",
                "Leg Extension",
                "Leg Curl",
                "Calf Raise"
        ));

        ChoiceDialog<String> dialog = new ChoiceDialog<>(currentExercise, allExercises);
        dialog.setTitle("Change Exercise");
        dialog.setHeaderText("Choose a replacement for " + currentExercise);
        dialog.setContentText("Exercise:");

        ButtonType alternativesButtonType = new ButtonType("Alternatives");
        dialog.getDialogPane().getButtonTypes().add(alternativesButtonType);

        dialog.setResultConverter(button -> {
            if (button == alternativesButtonType) {
                return "__ALTERNATIVES__";
            }
            if (button == ButtonType.OK) {
                return dialog.getSelectedItem();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (result.get().equals("__ALTERNATIVES__")) {
                showAlternativesPicker(username, workoutTitle, exercises, currentIndex, currentExercise);
            } else {
                exercises[currentIndex] = result.get();
                showCustomWorkout(username, workoutTitle, exercises, currentIndex);
            }
        }
    }

    private void showAlternativesPicker(String username, String workoutTitle, String[] exercises, int currentIndex, String currentExercise) {
        List<String> alternatives = DatabaseHelper.getAlternatives(currentExercise);

        if (alternatives.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Alternatives");
            alert.setHeaderText(null);
            alert.setContentText("No alternatives found for " + currentExercise + ".");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(alternatives.get(0), alternatives);
        dialog.setTitle("Exercise Alternatives");
        dialog.setHeaderText("Alternatives for " + currentExercise);
        dialog.setContentText("Choose one:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(selected -> {
            exercises[currentIndex] = selected;
            showCustomWorkout(username, workoutTitle, exercises, currentIndex);
        });
    }

    private void showPushWorkout(String username) {
        showPushWorkout(username, -1);
    }

    private void showWorkoutComplete(String username) {
        Label icon = new Label("✅");
        icon.setStyle("-fx-font-size: 54px;");

        Label title = new Label("Workout Complete");
        title.setStyle(screenTitleStyle());

        Label summary = new Label("Great work today.\nYour workout has been saved.");
        summary.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");
        summary.setWrapText(true);
        summary.setTextAlignment(TextAlignment.CENTER);

        Button dashboardButton = new Button("Back to Dashboard");
        dashboardButton.setMaxWidth(Double.MAX_VALUE);
        dashboardButton.setStyle(modernPrimaryButtonStyle());
        dashboardButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(380);
        card.getChildren().addAll(icon, title, summary, dashboardButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showPushWorkout(String username, int currentIndex) {
        String[] exercises = getDefaultPushExercises();

        Label title = new Label("Push Day Workout");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox exerciseBox = new VBox(12);
        exerciseBox.setAlignment(Pos.CENTER);

        for (int i = 0; i < exercises.length; i++) {
            Button exerciseButton = new Button(exercises[i]);
            exerciseButton.setMaxWidth(Double.MAX_VALUE);

            final int index = i;
            exerciseButton.setOnAction(e -> showSetLoggerScreen(username, "Push Day", exercises, index));

            if (i == currentIndex) {
                exerciseButton.setStyle("-fx-font-weight: bold; -fx-background-color: #dbeafe;");
            }

            exerciseBox.getChildren().add(exerciseButton);
        }

        Button finishButton = new Button("Finish Workout");
        Button backButton = new Button("Back");

        finishButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        finishButton.setOnAction(e -> showWorkoutComplete(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(title, exerciseBox, finishButton, backButton);

        mainLayout.setCenter(layout);
    }

    private void showPullWorkout(String username) {
        Label title = new Label("Pull Day Workout");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label exercises = new Label(
                "Deadlift\n" +
                        "Lat Pulldown\n" +
                        "Barbell Row\n" +
                        "Face Pull\n" +
                        "Bicep Curl"
        );

        Button startLoggingButton = new Button("Log Sets");
        Button backButton = new Button("Back");

        startLoggingButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        startLoggingButton.setOnAction(e -> showAddWorkoutScreen(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(title, exercises, startLoggingButton, backButton);

        mainLayout.setCenter(layout);
    }

    private void showLegWorkout(String username) {
        Label title = new Label("Leg Day Workout");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label exercises = new Label(
                "Squat\n" +
                        "Leg Press\n" +
                        "Romanian Deadlift\n" +
                        "Leg Curl\n" +
                        "Calf Raises"
        );

        Button startLoggingButton = new Button("Log Sets");
        Button backButton = new Button("Back");

        startLoggingButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        startLoggingButton.setOnAction(e -> showAddWorkoutScreen(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(title, exercises, startLoggingButton, backButton);

        mainLayout.setCenter(layout);
    }

    private void showSetLoggerScreen(String username, String workoutType, String[] exercises, int currentIndex) {
        String exercise = exercises[currentIndex];

        List<String> previousSets = DatabaseHelper.getLastLoggedSetsForExercise(username, exercise);
        double estimatedOneRepMax = DatabaseHelper.getEstimatedOneRepMax(username, exercise);

        Label title = new Label(exercise.toUpperCase());
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitle = new Label("Log your sets");
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");

        StackPane imageCard = new StackPane();
        imageCard.setPrefSize(180, 180);
        imageCard.setMaxSize(180, 180);
        imageCard.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-width: 2;"
        );

        Label oneRepMaxLabel = new Label(
                estimatedOneRepMax > 0
                        ? "Estimated 1RM: " + estimatedOneRepMax + "kg"
                        : "Estimated 1RM: N/A"
        );
        oneRepMaxLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );

        VBox previousSetsBox = new VBox(6);
        previousSetsBox.setAlignment(Pos.CENTER_LEFT);
        previousSetsBox.setMaxWidth(320);
        previousSetsBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 18;" +
                        "-fx-padding: 14;"
        );

        Label previousTitle = new Label("Previous Working Sets");
        previousTitle.setStyle(
                "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );
        previousSetsBox.getChildren().add(previousTitle);

        if (previousSets.isEmpty()) {
            Label emptyLabel = new Label("No previous sets logged yet.");
            emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px;");
            previousSetsBox.getChildren().add(emptyLabel);
        } else {
            for (String setText : previousSets) {
                Label setLabel = new Label(setText);
                setLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");
                previousSetsBox.getChildren().add(setLabel);
            }
        }

        Label iconLabel = new Label(getExerciseIcon(exercise));
        iconLabel.setStyle("-fx-font-size: 70px;");
        imageCard.getChildren().add(iconLabel);

        VBox setsContainer = new VBox(12);
        setsContainer.setFillWidth(true);
        setsContainer.setAlignment(Pos.CENTER);

        class SetRow {
            boolean warmup = false;
            Button badgeButton;
            TextField weightField;
            TextField repsField;
            HBox row;
        }

        List<SetRow> setRows = new ArrayList<>();

        Runnable[] refreshBadges = new Runnable[1];
        refreshBadges[0] = () -> {
            int workingSetNumber = 1;
            for (SetRow setRow : setRows) {
                if (setRow.warmup) {
                    setRow.badgeButton.setText("W");
                    setRow.badgeButton.setStyle(
                            "-fx-background-color: #f59e0b;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-min-width: 42;" +
                                    "-fx-min-height: 42;" +
                                    "-fx-max-width: 42;" +
                                    "-fx-max-height: 42;"
                    );
                } else {
                    setRow.badgeButton.setText(String.valueOf(workingSetNumber));
                    setRow.badgeButton.setStyle(
                            "-fx-background-color: #111827;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-min-width: 42;" +
                                    "-fx-min-height: 42;" +
                                    "-fx-max-width: 42;" +
                                    "-fx-max-height: 42;"
                    );
                    workingSetNumber++;
                }
            }
        };

        java.util.function.Consumer<Boolean> addSetRow = isWarmup -> {
            SetRow setRow = new SetRow();
            setRow.warmup = isWarmup;

            Button badgeButton = new Button();
            setRow.badgeButton = badgeButton;

            TextField weightField = new TextField();
            weightField.setPromptText("Weight (kg)");
            weightField.setPrefWidth(140);
            setRow.weightField = weightField;

            TextField repsField = new TextField();
            repsField.setPromptText("Reps");
            repsField.setPrefWidth(100);
            setRow.repsField = repsField;

            Button removeButton = new Button("✕");
            removeButton.setStyle(
                    "-fx-background-color: #fee2e2;" +
                            "-fx-text-fill: #b91c1c;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 18;"
            );

            badgeButton.setOnAction(e -> {
                setRow.warmup = !setRow.warmup;
                refreshBadges[0].run();
            });

            removeButton.setOnAction(e -> {
                setRows.remove(setRow);
                setsContainer.getChildren().remove(setRow.row);
                refreshBadges[0].run();
            });

            HBox row = new HBox(10, badgeButton, weightField, repsField, removeButton);
            row.setAlignment(Pos.CENTER);
            setRow.row = row;

            setRows.add(setRow);
            setsContainer.getChildren().add(row);

            refreshBadges[0].run();
        };

        addSetRow.accept(false);
        addSetRow.accept(false);
        addSetRow.accept(false);

        Button addSetButton = new Button("+ Add Set");
        addSetButton.setStyle(modernNavButtonStyle());
        addSetButton.setOnAction(e -> addSetRow.accept(false));

        Button doneButton = new Button("Done");
        Button cancelButton = new Button("Cancel");

        doneButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        doneButton.setStyle(modernNavButtonStyle());
        cancelButton.setStyle(modernNavButtonStyle());

        Label resultLabel = new Label();
        resultLabel.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        doneButton.setOnAction(e -> {
            StringBuilder resultText = new StringBuilder();
            int recordsBroken = 0;

            try {
                for (SetRow setRow : setRows) {
                    String weightText = setRow.weightField.getText().trim();
                    String repsText = setRow.repsField.getText().trim();

                    if (weightText.isEmpty() && repsText.isEmpty()) {
                        continue;
                    }

                    if (weightText.isEmpty() || repsText.isEmpty()) {
                        throw new NumberFormatException();
                    }

                    double weight = Double.parseDouble(weightText);
                    int reps = Integer.parseInt(repsText);

                    int previousBest = setRow.warmup ? 0 : DatabaseHelper.getBestReps(username, exercise, weight);

                    DatabaseHelper.saveWorkout(username, exercise, weight, reps, setRow.warmup);

                    resultText.append(setRow.warmup ? "[Warm-up] " : "")
                            .append(exercise)
                            .append(": ")
                            .append(weight)
                            .append("kg x ")
                            .append(reps)
                            .append("\n");

                    if (!setRow.warmup && previousBest > 0 && reps > previousBest) {
                        recordsBroken++;
                    }
                }

                if (resultText.length() == 0) {
                    resultLabel.setText("Please enter at least one full set.");
                    return;
                }

                if (recordsBroken > 0) {
                    showRecordBrokenAnimation();
                }

                int nextIndex = currentIndex + 1;
                if (nextIndex < exercises.length) {
                    showCustomWorkout(username, workoutType, exercises, nextIndex);
                } else {
                    showWorkoutComplete(username);
                }

            } catch (NumberFormatException ex) {
                resultLabel.setText("Please enter valid numbers for weight and reps.");
            }
        });

        cancelButton.setOnAction(e -> showCustomWorkout(username, workoutType, exercises, currentIndex));

        HBox buttonRow = new HBox(12, cancelButton, doneButton);
        buttonRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        HBox.setHgrow(doneButton, Priority.ALWAYS);

        VBox layout = new VBox(14);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f8fafc;");
        layout.getChildren().addAll(
                imageCard,
                title,
                subtitle,
                oneRepMaxLabel,
                previousSetsBox,
                setsContainer,
                addSetButton,
                buttonRow,
                resultLabel
        );

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc;");

        mainLayout.setCenter(scrollPane);
    }

    private void showProfileScreen(String username) {
        String[] profile = DatabaseHelper.getProfileDetails(username);

        String displayName = profile[0].isBlank() ? username : profile[0];
        String height = profile[1];
        String weight = profile[2];
        String profilePicturePath = profile[3];

        String mostLogged = DatabaseHelper.getMostLoggedExercise(username);
        String heaviestLift = DatabaseHelper.getHeaviestLift(username);
        String mostImproved = DatabaseHelper.getMostImprovedExercise(username);
        int streak = DatabaseHelper.getLoggingStreak(username);
        String dateJoined = DatabaseHelper.getDateJoined(username);

        Label title = new Label("Profile");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label pictureLabel = new Label();
        pictureLabel.setPrefSize(100, 100);
        pictureLabel.setStyle(
                "-fx-border-color: black;" +
                        "-fx-border-width: 2;" +
                        "-fx-alignment: center;" +
                        "-fx-background-color: #f4f4f4;"
        );

        if (profilePicturePath != null && !profilePicturePath.isBlank()) {
            try {
                Image image = new Image("file:" + profilePicturePath);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(100);
                imageView.setFitHeight(100);
                imageView.setPreserveRatio(true);
                pictureLabel.setGraphic(imageView);
            } catch (Exception e) {
                pictureLabel.setText("No Image");
            }
        } else {
            pictureLabel.setText("No Image");
        }

        Label nameLabel = new Label("Name: " + displayName);
        Label heightLabel = new Label("Height: " + height + " cm");
        Label weightLabel = new Label("Weight: " + weight + " kg");
        Label mostLoggedLabel = new Label("Most Logged Exercise: " + mostLogged);
        Label heaviestLiftLabel = new Label("Heaviest Lift: " + heaviestLift);
        Label mostImprovedLabel = new Label("Most Improved: " + mostImproved);
        Label streakLabel = new Label("Logging Streak: " + streak + " day(s)");
        Label joinedLabel = new Label("Date Joined: " + dateJoined);

        nameLabel.setStyle("-fx-font-size: 16px;");
        heightLabel.setStyle("-fx-font-size: 16px;");
        weightLabel.setStyle("-fx-font-size: 16px;");
        mostLoggedLabel.setStyle("-fx-font-size: 16px;");
        heaviestLiftLabel.setStyle("-fx-font-size: 16px;");
        mostImprovedLabel.setStyle("-fx-font-size: 16px;");
        streakLabel.setStyle("-fx-font-size: 16px;");
        joinedLabel.setStyle("-fx-font-size: 16px;");

        Button editButton = new Button("Edit Profile");
        Button backButton = new Button("Back");

        editButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        editButton.setOnAction(e -> showEditProfileScreen(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setMaxWidth(420);
        layout.setStyle(screenCardStyle());

        layout.getChildren().addAll(
                title,
                pictureLabel,
                nameLabel,
                heightLabel,
                weightLabel,
                mostLoggedLabel,
                heaviestLiftLabel,
                mostImprovedLabel,
                streakLabel,
                joinedLabel,
                editButton,
                backButton
        );

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        StackPane wrapper = new StackPane(scrollPane);
        wrapper.setStyle(appBackgroundStyle());
        wrapper.setPadding(new Insets(30));
        mainLayout.setCenter(wrapper);
    }

    private void showEditProfileScreen(String username) {
        String[] profile = DatabaseHelper.getProfileDetails(username);

        TextField nameField = new TextField(profile[0]);
        nameField.setPromptText("Display Name");

        TextField heightField = new TextField(profile[1].equals("0.0") ? "" : profile[1]);
        heightField.setPromptText("Height (cm)");

        TextField weightField = new TextField(profile[2].equals("0.0") ? "" : profile[2]);
        weightField.setPromptText("Weight (kg)");

        TextField pictureField = new TextField(profile[3]);
        pictureField.setPromptText("Profile Picture Path");

        Button browseButton = new Button("Browse");
        Button saveButton = new Button("Save Changes");
        Button backButton = new Button("Cancel");
        Label resultLabel = new Label();

        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose Profile Picture");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );

            File selectedFile = fileChooser.showOpenDialog(root.getScene().getWindow());
            if (selectedFile != null) {
                pictureField.setText(selectedFile.getAbsolutePath());
            }
        });

        saveButton.setOnAction(e -> {
            try {
                String displayName = nameField.getText().trim();
                double height = heightField.getText().isBlank() ? 0 : Double.parseDouble(heightField.getText());
                double weight = weightField.getText().isBlank() ? 0 : Double.parseDouble(weightField.getText());
                String profilePicture = pictureField.getText().trim();

                DatabaseHelper.saveProfile(username, displayName, height, weight, profilePicture);
                showProfileScreen(username);

            } catch (NumberFormatException ex) {
                resultLabel.setText("Please enter valid numbers for height and weight.");
            }
        });

        backButton.setOnAction(e -> showProfileScreen(username));

        HBox pictureRow = new HBox(10, pictureField, browseButton);
        pictureRow.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
                new Label("Edit Profile"),
                nameField,
                heightField,
                weightField,
                pictureRow,
                saveButton,
                backButton,
                resultLabel
        );

        mainLayout.setCenter(layout);
    }

    private void showDashboard(String username) {
        String today = LocalDate.now()
                .getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        String selectedSplit = DatabaseHelper.getSelectedSplit(username);
        if (selectedSplit == null || selectedSplit.isBlank()) {
            selectedSplit = "Push Pull Legs";
        }

        String todayPlan = DatabaseHelper.getWorkoutForDay(username, selectedSplit, today);
        if (todayPlan == null || todayPlan.isBlank()) {
            todayPlan = getTodayPlan(username, today);
        }

        final String planForToday = todayPlan;

        BorderPane topBar = new BorderPane();

        Button menuButton = new Button("☰");
        menuButton.setStyle(modernSecondaryButtonStyle());
        menuButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Menu");
            alert.setHeaderText(null);
            alert.setContentText("Quick settings/menu can go here later.");
            alert.showAndWait();
        });

        Label titleLabel = new Label("Record Breaker");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Button profileButton = new Button("👤");
        profileButton.setStyle(modernSecondaryButtonStyle());
        profileButton.setOnAction(e -> showProfileScreen(username));

        topBar.setLeft(menuButton);
        topBar.setCenter(titleLabel);
        topBar.setRight(profileButton);
        BorderPane.setAlignment(titleLabel, Pos.CENTER);

        Label welcomeLabel = new Label("Welcome, " + username);
        welcomeLabel.setStyle(screenTitleStyle());

        Label planLabel = new Label("Today is " + today + " • " + todayPlan);
        planLabel.setStyle(screenSubtitleStyle());
        planLabel.setWrapText(true);

        Button startButton = new Button("Start Workout");
        Button splitsButton = new Button("Splits");
        Button modifyButton = new Button("Modify Split");
        Button recordsButton = new Button("History");
        Button logoutButton = new Button("Log Out");

        startButton.setMaxWidth(Double.MAX_VALUE);
        splitsButton.setMaxWidth(Double.MAX_VALUE);
        modifyButton.setMaxWidth(Double.MAX_VALUE);
        recordsButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        startButton.setStyle(modernPrimaryButtonStyle());
        splitsButton.setStyle(modernSecondaryButtonStyle());
        modifyButton.setStyle(modernSecondaryButtonStyle());
        recordsButton.setStyle(modernSecondaryButtonStyle());
        logoutButton.setStyle(modernSecondaryButtonStyle());

        startButton.setOnAction(e -> startWorkout(username, planForToday));
        splitsButton.setOnAction(e -> showSplitsScreen(username));
        modifyButton.setOnAction(e -> showModifySplitScreen(username));
        recordsButton.setOnAction(e -> showHistoryScreen(username));
        logoutButton.setOnAction(e -> showLoginScreen());

        VBox buttonBox = new VBox(12, startButton, splitsButton, modifyButton, recordsButton, logoutButton);
        buttonBox.setFillWidth(true);

        VBox card = createScreenCard(420);
        card.setAlignment(Pos.TOP_CENTER);
        card.getChildren().addAll(topBar, welcomeLabel, planLabel, buttonBox);

        mainLayout.setCenter(wrapScreen(card));
    }

    private String getWorkoutNameForDay(String splitName, String day) {
        switch (splitName) {
            case "Push Pull Legs":
                switch (day) {
                    case "Monday":
                    case "Friday":
                        return "Push Day";
                    case "Tuesday":
                    case "Saturday":
                        return "Pull Day";
                    case "Wednesday":
                    case "Sunday":
                        return "Leg Day";
                    default:
                        return "Rest Day";
                }
            case "Arnold Split":
                switch (day) {
                    case "Monday":
                    case "Thursday":
                        return "Chest + Back";
                    case "Tuesday":
                    case "Friday":
                        return "Shoulders + Arms";
                    case "Wednesday":
                    case "Saturday":
                        return "Legs";
                    default:
                        return "Rest Day";
                }
            case "Upper Lower":
                switch (day) {
                    case "Monday":
                    case "Thursday":
                        return "Upper Body";
                    case "Tuesday":
                    case "Friday":
                        return "Lower Body";
                    default:
                        return "Rest Day";
                }
            case "Full Body":
                switch (day) {
                    case "Monday":
                    case "Wednesday":
                    case "Friday":
                        return "Full Body";
                    default:
                        return "Rest Day";
                }
            case "Custom Split":
                return "Custom Day";
            default:
                return "Rest Day";
        }
    }

    private String[] getArnoldChestBackExercises() {
        return new String[]{
                "Bench Press",
                "Incline Dumbbell Bench Press",
                "Machine Pec Fly",
                "Lat Pulldown",
                "Cable Row",
                "Chest Supported Row"
        };
    }

    private String[] getArnoldShouldersArmsExercises() {
        return new String[]{
                "Dumbbell Shoulder Press",
                "Cable Lateral Raise",
                "Rear Delt Fly",
                "Tricep Pushdown",
                "Overhead Extension",
                "Preacher Curls",
                "Incline Dumbbell Curls"
        };
    }

    private String[] getArnoldLegExercises() {
        return new String[]{
                "Squats",
                "Leg Extension",
                "Leg Curl",
                "Calf Raise"
        };
    }

    private String[] getUpperBodyExercises() {
        return new String[]{
                "Bench Press",
                "Incline Dumbbell Bench Press",
                "Lat Pulldown",
                "Cable Row",
                "Dumbbell Shoulder Press",
                "Cable Lateral Raise",
                "Tricep Pushdown",
                "Preacher Curls"
        };
    }

    private String[] getLowerBodyExercises() {
        return new String[]{
                "Squats",
                "Leg Extension",
                "Leg Curl",
                "Calf Raise"
        };
    }

    private String[] getFullBodyExercises() {
        return new String[]{
                "Squats",
                "Bench Press",
                "Lat Pulldown",
                "Cable Row",
                "Dumbbell Shoulder Press",
                "Leg Curl",
                "Cable Lateral Raise",
                "Calf Raise"
        };
    }

    private void showSplitsScreen(String username) {
        Label title = new Label("Choose a Split");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Pick a training structure that fits your week");
        subtitle.setStyle(screenSubtitleStyle());

        Button pplButton = new Button("Push Pull Legs");
        Button arnoldButton = new Button("Arnold Split");
        Button upperLowerButton = new Button("Upper Lower");
        Button fullBodyButton = new Button("Full Body");
        Button customButton = new Button("Create Your Own");
        Button backButton = new Button("Back");

        for (Button button : new Button[]{pplButton, arnoldButton, upperLowerButton, fullBodyButton, customButton}) {
            button.setMaxWidth(Double.MAX_VALUE);
            button.setStyle(modernPrimaryButtonStyle());
        }

        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());

        pplButton.setOnAction(e -> {
            DatabaseHelper.saveSelectedSplit(username, "Push Pull Legs");
            seedSelectedSplitForUser(username, "Push Pull Legs");
            showDashboard(username);
        });

        arnoldButton.setOnAction(e -> {
            DatabaseHelper.saveSelectedSplit(username, "Arnold Split");
            seedSelectedSplitForUser(username, "Arnold Split");
            showDashboard(username);
        });

        upperLowerButton.setOnAction(e -> {
            DatabaseHelper.saveSelectedSplit(username, "Upper Lower");
            seedSelectedSplitForUser(username, "Upper Lower");
            showDashboard(username);
        });

        fullBodyButton.setOnAction(e -> {
            DatabaseHelper.saveSelectedSplit(username, "Full Body");
            seedSelectedSplitForUser(username, "Full Body");
            showDashboard(username);
        });

        customButton.setOnAction(e -> {
            DatabaseHelper.saveSelectedSplit(username, "Custom Split");
            showModifySplitScreen(username);
        });

        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(420);
        card.getChildren().addAll(
                title,
                subtitle,
                pplButton,
                arnoldButton,
                upperLowerButton,
                fullBodyButton,
                customButton,
                backButton
        );

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showModifySplitScreen(String username) {
        String currentSplit = DatabaseHelper.getSelectedSplit(username);
        if (currentSplit == null || currentSplit.isBlank()) {
            currentSplit = "Push Pull Legs";
        }

        if (currentSplit.equals("Push Pull Legs")) {
            seedPushPullLegsForUser(username);
        }

        final String splitToUse = currentSplit;

        Label title = new Label("Current Split: " + splitToUse);
        title.setStyle(screenTitleStyle());

        VBox daysBox = new VBox(10);
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (String day : days) {
            Button dayButton = new Button(day);
            dayButton.setMaxWidth(Double.MAX_VALUE);
            dayButton.setStyle(modernPrimaryButtonStyle());

            final String selectedDay = day;
            dayButton.setOnAction(e -> showModifyDayScreen(username, splitToUse, selectedDay));

            daysBox.getChildren().add(dayButton);
        }

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(420);
        card.getChildren().addAll(title, daysBox, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showModifyDayScreen(String username, String splitName, String day) {
        Label title = new Label(day + " - " + splitName);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox exerciseList = new VBox(12);
        List<String> exercises = DatabaseHelper.getExercisesForDay(username, splitName, day);

        if (exercises.isEmpty()) {
            Label emptyLabel = new Label("No exercises added for this day yet.");
            exerciseList.getChildren().add(emptyLabel);
        } else {
            for (String exercise : exercises) {
                HBox row = createExerciseEditorRow(username, splitName, day, exercise);
                exerciseList.getChildren().add(row);
            }
        }

        Button addExerciseButton = new Button("Add New Exercise");
        addExerciseButton.setMaxWidth(Double.MAX_VALUE);
        addExerciseButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add Exercise");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter exercise name:");

            dialog.showAndWait().ifPresent(newExercise -> {
                if (!newExercise.trim().isEmpty()) {
                    int nextOrder = DatabaseHelper.getExercisesForDay(username, splitName, day).size();
                    DatabaseHelper.addExerciseToDay(username, splitName, day, newExercise.trim(), nextOrder);
                    showModifyDayScreen(username, splitName, day);
                }
            });
        });

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> showModifySplitScreen(username));

        VBox layout = new VBox(20, title, exerciseList, addExerciseButton, backButton);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);

        mainLayout.setCenter(scrollPane);
    }

    private void showAddWorkoutScreen(String username) {
        Label title = new Label("Add Workout");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        ComboBox<String> exerciseBox = new ComboBox<>();
        exerciseBox.getItems().addAll("Bench Press", "Squat", "Deadlift", "Shoulder Press", "Barbell Row");
        exerciseBox.setPromptText("Select Exercise");
        exerciseBox.setMaxWidth(Double.MAX_VALUE);

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");

        TextField repsField = new TextField();
        repsField.setPromptText("Reps");

        Button saveButton = new Button("Save Workout");
        Button backButton = new Button("Back");
        Label resultLabel = new Label();

        saveButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        saveButton.setOnAction(e -> {
            String exercise = exerciseBox.getValue();
            String weightText = weightField.getText();
            String repsText = repsField.getText();

            if (exercise == null || weightText.isEmpty() || repsText.isEmpty()) {
                resultLabel.setText("Please complete all workout fields.");
                return;
            }

            try {
                double weight = Double.parseDouble(weightText);
                int reps = Integer.parseInt(repsText);

                int previousBest = DatabaseHelper.getBestReps(username, exercise, weight);
                DatabaseHelper.saveWorkout(username, exercise, weight, reps);

                if (previousBest == 0) {
                    resultLabel.setText("First record saved!");
                    showRecordBrokenAnimation(exercise, weight, reps);
                } else if (reps > previousBest) {
                    resultLabel.setText("🔥 RECORD BROKE! Previous best: " + previousBest + " reps");
                    showRecordBrokenAnimation(exercise, weight, reps);
                } else {
                    resultLabel.setText("Workout saved. Current best: " + previousBest + " reps");
                }
            } catch (NumberFormatException ex) {
                resultLabel.setText("Weight and reps must be numbers.");
            }
        });

        backButton.setOnAction(e -> showDashboard(username));

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(title, exerciseBox, weightField, repsField, saveButton, backButton, resultLabel);

        mainLayout.setCenter(layout);
    }

    private void showHistoryScreen(String username) {
        Label title = new Label("Workout History");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Your recent logged sessions");
        subtitle.setStyle(screenSubtitleStyle());

        ListView<String> historyList = new ListView<>();
        historyList.getItems().addAll(DatabaseHelper.getWorkoutHistory(username));
        historyList.setPrefHeight(420);
        historyList.setStyle(
                "-fx-background-radius: 16;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-color: #d1d5db;"
        );

        Button backButton = new Button("Back");
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showDashboard(username));
        backButton.setMaxWidth(Double.MAX_VALUE);

        VBox card = createScreenCard(460);
        card.getChildren().addAll(title, subtitle, historyList, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    public static void main(String[] args) {
        launch();
    }

    private String getTodayPlan(String username, String day) {
        String selectedSplit = DatabaseHelper.getSelectedSplit(username);
        if (selectedSplit == null || selectedSplit.isBlank()) {
            selectedSplit = "Push Pull Legs";
        }
        return getWorkoutNameForDay(selectedSplit, day);
    }

    private String getTodayMuscles(String plan) {
        switch (plan) {
            case "Push Day":
                return "Chest, Shoulders, Triceps";
            case "Pull Day":
                return "Back, Biceps, Rear Delts";
            case "Leg Day":
                return "Quads, Hamstrings, Glutes, Calves";
            case "Rest Day":
                return "Recovery and mobility";
            default:
                return "";
        }
    }

    private int saveSetIfFilled(String username, String exercise, TextField weightField, TextField repsField, StringBuilder resultText) {
        String weightText = weightField.getText().trim();
        String repsText = repsField.getText().trim();

        if (weightText.isEmpty() && repsText.isEmpty()) {
            return 0;
        }

        if (weightText.isEmpty() || repsText.isEmpty()) {
            throw new NumberFormatException();
        }

        double weight = Double.parseDouble(weightText);
        int reps = Integer.parseInt(repsText);

        int previousBest = DatabaseHelper.getBestReps(username, exercise, weight);
        DatabaseHelper.saveWorkout(username, exercise, weight, reps);

        resultText.append(exercise)
                .append(": ")
                .append(weight)
                .append("kg x ")
                .append(reps)
                .append("\n");

        if (previousBest == 0) {
            return 0;
        } else if (reps > previousBest) {
            return 1;
        }

        return 0;
    }

    private String[] getDefaultPushExercises() {
        return new String[]{
                "Bench Press",
                "Incline Dumbbell Bench Press",
                "Machine Pec Fly",
                "Tricep Pushdown",
                "Overhead Extension",
                "Dips",
                "Dumbbell Shoulder Press",
                "Cable Lateral Raise"
        };
    }

    private String[] getDefaultPullExercises() {
        return new String[]{
                "Lat Pulldown",
                "Close Grip Lat Pulldown",
                "Cable Row",
                "Cable Face Pulls",
                "Dumbbell Shrugs",
                "Preacher Curls",
                "Incline Dumbbell Curls",
                "Reverse Wrist Curls"
        };
    }

    private String[] getDefaultLegExercises() {
        return new String[]{
                "Squats",
                "Leg Extension",
                "Leg Curl",
                "Calf Raise"
        };
    }

    private String getTomorrowMessage(String day) {
        switch (day) {
            case "Monday":
                return "Pull Day tomorrow, be ready!";
            case "Tuesday":
                return "Leg Day tomorrow, be ready!";
            case "Wednesday":
                return "Rest Day tomorrow, recover well!";
            case "Thursday":
                return "Push Day tomorrow, be ready!";
            case "Friday":
                return "Pull Day tomorrow, be ready!";
            case "Saturday":
                return "Leg Day tomorrow, be ready!";
            case "Sunday":
                return "Push Day tomorrow, be ready!";
            default:
                return "";
        }
    }

    private void seedPushPullLegsForUser(String username) {
        String splitName = "Push Pull Legs";

        seedDayIfEmpty(username, splitName, "Monday", getDefaultPushExercises());
        seedDayIfEmpty(username, splitName, "Tuesday", getDefaultPullExercises());
        seedDayIfEmpty(username, splitName, "Wednesday", getDefaultLegExercises());
        seedDayIfEmpty(username, splitName, "Friday", getDefaultPushExercises());
        seedDayIfEmpty(username, splitName, "Saturday", getDefaultPullExercises());
        seedDayIfEmpty(username, splitName, "Sunday", getDefaultLegExercises());

        DatabaseHelper.seedExerciseAlternatives();
    }

    private void seedDayIfEmpty(String username, String splitName, String dayName, String[] exercises) {
        List<String> existingExercises = DatabaseHelper.getExercisesForDay(username, splitName, dayName);
        System.out.println("Checking " + dayName + " for " + username + ": " + existingExercises.size());

        if (existingExercises.isEmpty()) {
            for (int i = 0; i < exercises.length; i++) {
                DatabaseHelper.addExerciseToDay(username, splitName, dayName, exercises[i], i);
                System.out.println("Added " + exercises[i] + " to " + dayName);
            }
        }
    }

    private HBox createExerciseEditorRow(String username, String splitName, String day, String exercise) {
        Label nameLabel = new Label(exercise);
        nameLabel.setPrefWidth(220);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button replaceButton = new Button("🔄");
        Button removeButton = new Button("🗑");
        Button alternativeButton = new Button("⇄");

        replaceButton.setStyle("-fx-font-size: 16px;");
        removeButton.setStyle("-fx-font-size: 16px;");
        alternativeButton.setStyle("-fx-font-size: 16px;");

        replaceButton.setTooltip(new Tooltip("Replace Exercise"));
        removeButton.setTooltip(new Tooltip("Remove Exercise"));
        alternativeButton.setTooltip(new Tooltip("View Alternatives"));

        replaceButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(exercise);
            dialog.setTitle("Replace Exercise");
            dialog.setHeaderText(null);
            dialog.setContentText("New exercise name:");

            dialog.showAndWait().ifPresent(newExercise -> {
                if (!newExercise.trim().isEmpty()) {
                    DatabaseHelper.replaceExercise(username, splitName, day, exercise, newExercise.trim());
                    showModifyDayScreen(username, splitName, day);
                }
            });
        });

        removeButton.setOnAction(e -> {
            DatabaseHelper.removeExercise(username, splitName, day, exercise);
            showModifyDayScreen(username, splitName, day);
        });

        alternativeButton.setOnAction(e -> showAlternativesScreen(username, splitName, day, exercise));

        HBox row = new HBox(10, nameLabel, replaceButton, removeButton, alternativeButton);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void showAlternativesScreen(String username, String splitName, String day, String exercise) {
        Label title = new Label("Alternatives for " + exercise);
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox alternativesBox = new VBox(10);
        List<String> alternatives = DatabaseHelper.getAlternatives(exercise);

        if (alternatives.isEmpty()) {
            alternativesBox.getChildren().add(new Label("No alternatives found yet."));
        } else {
            for (String alternative : alternatives) {
                Button altButton = new Button(alternative);
                altButton.setMaxWidth(Double.MAX_VALUE);
                altButton.setOnAction(e -> {
                    DatabaseHelper.replaceExercise(username, splitName, day, exercise, alternative);
                    showModifyDayScreen(username, splitName, day);
                });
                alternativesBox.getChildren().add(altButton);
            }
        }

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(e -> showModifyDayScreen(username, splitName, day));

        VBox layout = new VBox(20, title, alternativesBox, backButton);
        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);

        mainLayout.setCenter(layout);
    }

    private void seedSelectedSplitForUser(String username, String splitName) {
        switch (splitName) {
            case "Push Pull Legs":
                seedPushPullLegsForUser(username);
                break;
            case "Arnold Split":
                seedArnoldSplitForUser(username);
                break;
            case "Upper Lower":
                seedUpperLowerSplitForUser(username);
                break;
            case "Full Body":
                seedFullBodySplitForUser(username);
                break;
            case "Custom Split":
                break;
            default:
                seedPushPullLegsForUser(username);
                break;
        }

        DatabaseHelper.seedExerciseAlternatives();
    }

    private void seedArnoldSplitForUser(String username) {
        String splitName = "Arnold Split";

        seedDayIfEmpty(username, splitName, "Monday", getArnoldChestBackExercises());
        seedDayIfEmpty(username, splitName, "Tuesday", getArnoldShouldersArmsExercises());
        seedDayIfEmpty(username, splitName, "Wednesday", getArnoldLegExercises());
        seedDayIfEmpty(username, splitName, "Thursday", getArnoldChestBackExercises());
        seedDayIfEmpty(username, splitName, "Friday", getArnoldShouldersArmsExercises());
        seedDayIfEmpty(username, splitName, "Saturday", getArnoldLegExercises());
    }

    private void seedUpperLowerSplitForUser(String username) {
        String splitName = "Upper Lower";

        seedDayIfEmpty(username, splitName, "Monday", getUpperBodyExercises());
        seedDayIfEmpty(username, splitName, "Tuesday", getLowerBodyExercises());
        seedDayIfEmpty(username, splitName, "Thursday", getUpperBodyExercises());
        seedDayIfEmpty(username, splitName, "Friday", getLowerBodyExercises());
    }

    private String modernNavButtonStyle() {
        return "-fx-background-color: white;" +
                "-fx-text-fill: #111827;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-padding: 12 18 12 18;";
    }

    private String getExerciseIcon(String exercise) {
        String name = exercise.toLowerCase();

        if (name.contains("bench")) return "🏋";
        if (name.contains("incline")) return "🏋";
        if (name.contains("pec fly")) return "💪";
        if (name.contains("pushdown")) return "🦾";
        if (name.contains("extension")) return "🦾";
        if (name.contains("dips")) return "🔻";
        if (name.contains("shoulder")) return "🏋";
        if (name.contains("lateral")) return "💪";
        if (name.contains("pulldown")) return "⬇";
        if (name.contains("row")) return "↔";
        if (name.contains("curl")) return "💪";
        if (name.contains("squat")) return "🦵";
        if (name.contains("leg")) return "🦵";
        if (name.contains("calf")) return "🦵";

        return "🏋";
    }

    private void seedFullBodySplitForUser(String username) {
        String splitName = "Full Body";

        seedDayIfEmpty(username, splitName, "Monday", getFullBodyExercises());
        seedDayIfEmpty(username, splitName, "Wednesday", getFullBodyExercises());
        seedDayIfEmpty(username, splitName, "Friday", getFullBodyExercises());
    }

    private String modernPrimaryButtonStyle() {
        return "-fx-background-color: linear-gradient(to right, #111827, #1f2937);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: 14 18 14 18;";
    }

    private String modernSecondaryButtonStyle() {
        return "-fx-background-color: #ffffff;" +
                "-fx-text-fill: #111827;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 16;" +
                "-fx-padding: 14 18 14 18;";
    }

    private String modernFieldStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-radius: 14;" +
                "-fx-padding: 12 14 12 14;" +
                "-fx-font-size: 14px;";
    }

    private boolean isValidUsername(String username) {
        if (username == null) return false;
        return username.matches("^[A-Za-z0-9_]{4,20}$");
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;
        return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$");
    }

    private boolean isValidEmailFormat(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private Label createMatchIndicator() {
        Label indicator = new Label("○");
        indicator.setStyle("-fx-font-size: 18px; -fx-text-fill: #9ca3af;");
        return indicator;
    }

    private void updateMatchIndicator(Label indicator, boolean matches) {
        indicator.setText(matches ? "✓" : "✕");
        indicator.setStyle(matches
                ? "-fx-font-size: 18px; -fx-text-fill: #16a34a; -fx-font-weight: bold;"
                : "-fx-font-size: 18px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
    }

    private String appBackgroundStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #0f172a, #111827, #1f2937);";
    }

    private String screenCardStyle() {
        return "-fx-background-color: rgba(255,255,255,0.97);" +
                "-fx-background-radius: 24;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 24;";
    }

    private String screenTitleStyle() {
        return "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #111827;";
    }

    private String screenSubtitleStyle() {
        return "-fx-font-size: 14px;" +
                "-fx-text-fill: #6b7280;";
    }

    private VBox createScreenCard(double maxWidth) {
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(maxWidth);
        card.setPadding(new Insets(28));
        card.setStyle(screenCardStyle());
        return card;
    }

    private StackPane wrapScreen(Node content) {
        StackPane wrapper = new StackPane(content);
        wrapper.setStyle(appBackgroundStyle());
        wrapper.setPadding(new Insets(30));
        return wrapper;
    }
}