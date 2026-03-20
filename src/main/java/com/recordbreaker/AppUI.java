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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class AppUI extends Application {

    // Root containers shared across every screen.
    private StackPane root;
    private BorderPane mainLayout;

    // Lightweight in-app accessibility and display settings.
    private double textScale = 1.0;
    private boolean highContrastMode = false;
    private boolean reducedMotion = false;
    private boolean compactSpacing = false;
    private double preferredCardWidth = 420;

    @Override
    public void start(Stage stage) {
        DatabaseHelper.backupDatabase();
        DatabaseHelper.createTables();

        mainLayout = new BorderPane();
        root = new StackPane();
        root.getChildren().add(mainLayout);

        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/RecordBreakerLogo64.png"))
        );


        Scene scene = new Scene(root, 400, 650);

        showSplashScreen();

        stage.setTitle("Record Breaker");
        stage.setScene(scene);
        stage.show();
    }

    private void showLoginScreen() {
        VBox hero = createBrandHero(
                "Record Breaker",
                "Built like a training app with a cleaner home screen, stronger branding, and faster access to your plan."
        );

        VBox card = createScreenCard(360);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(scaledSpacing(24)));

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

        Label eyebrow = new Label("Welcome Back");
        eyebrow.setStyle(modernTagStyle());

        Label title = new Label("Log in to continue");
        title.setStyle("-fx-font-size: " + px(24) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");

        Label subtitle = new Label("Pick up where you left off and jump straight back into training.");
        subtitle.setWrapText(true);
        subtitle.setStyle(screenSubtitleStyle());

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

        VBox header = new VBox(6, eyebrow, title, subtitle);
        header.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                header,
                usernameField,
                usernameHint,
                passwordField,
                passwordHint,
                loginButton,
                signUpButton,
                messageLabel
        );

        VBox layout = new VBox(scaledSpacing(18), hero, card);
        layout.setAlignment(Pos.CENTER);

        mainLayout.setCenter(wrapScreen(layout));
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

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showSplashScreen() {
        VBox hero = createBrandHero(
                "Record Breaker",
                "Train harder. Track smarter. Keep your whole routine inside one clean training app."
        );
        hero.setMaxWidth(360);

        VBox layout = new VBox(scaledSpacing(18));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle(appBackgroundStyle());

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(240);
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #22c55e;");

        Label loadingLabel = new Label("Loading...");
        loadingLabel.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #94a3b8;"
        );

        VBox statusCard = new VBox(scaledSpacing(10), progressBar, loadingLabel);
        statusCard.setAlignment(Pos.CENTER);
        statusCard.setMaxWidth(320);
        statusCard.setPadding(new Insets(scaledSpacing(18)));
        statusCard.setStyle(
                "-fx-background-color: rgba(15,23,42,0.54);" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: rgba(148,163,184,0.18);" +
                        "-fx-border-radius: 24;"
        );

        layout.getChildren().addAll(hero, statusCard);
        mainLayout.setCenter(wrapScreen(layout));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(progressBar.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(2.2), new KeyValue(progressBar.progressProperty(), 1))
        );

        timeline.setOnFinished(e -> showLoginScreen());
        if (reducedMotion) {
            progressBar.setProgress(1);
            showLoginScreen();
        } else {
            timeline.play();
        }
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
            showInfoScreen(
                    "Rest Day",
                    "Today is a rest day. Recover well and come back stronger tomorrow.",
                    "Back to Dashboard",
                    () -> showDashboard(username)
            );
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
        title.setStyle("-fx-font-size: " + px(30) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label(workoutTitle);
        subtitle.setStyle(screenSubtitleStyle());

        StackPane imageCard = new StackPane();
        imageCard.setPrefSize(260, 260);
        imageCard.setMaxSize(260, 260);
        imageCard.setStyle(
                "-fx-background-color: " + cardFill() + ";" +
                        "-fx-background-radius: 30;" +
                        "-fx-border-color: " + borderColor() + ";" +
                        "-fx-border-radius: 30;" +
                        "-fx-border-width: 2;"
        );

        Label iconLabel = new Label(getExerciseIcon(currentExercise));
        iconLabel.setStyle("-fx-font-size: " + px(90) + "px;");

        imageCard.getChildren().add(iconLabel);

        final int indexToUse = currentIndex;

        Button exitButton = new Button("🚪 Exit");
        exitButton.setPrefWidth(110);
        exitButton.setStyle(modernSecondaryButtonStyle());
        exitButton.setOnAction(e -> confirmExitWorkout(
                username,
                () -> showCustomWorkout(username, workoutTitle, exercises, indexToUse)
        ));
        exitButton.setText("Exit");

        Button backButton = new Button("⬅ Back");
        Button changeButton = new Button("⇄ Change");
        Button nextButton = new Button("Log Sets");
        backButton.setText("Back");
        changeButton.setText("Change");

        backButton.setPrefWidth(110);
        changeButton.setPrefWidth(110);
        nextButton.setPrefWidth(110);

        backButton.setStyle(modernNavButtonStyle());
        changeButton.setStyle(modernNavButtonStyle());
        nextButton.setStyle(modernNavButtonStyle());

        backButton.setOnAction(e -> {
            if (indexToUse > 0) {
                showCustomWorkout(username, workoutTitle, exercises, indexToUse - 1);
            }
        });

        changeButton.setOnAction(e -> showWorkoutExercisePickerScreen(username, workoutTitle, exercises, indexToUse, false));

        nextButton.setOnAction(e -> {
            showSetLoggerScreen(username, workoutTitle, exercises, indexToUse);
        });

        if (currentIndex == 0) {
            backButton.setVisible(false);
            backButton.setManaged(false);
        }

        HBox bottomBar = new HBox(15, backButton, changeButton, nextButton, exitButton);
        bottomBar.setAlignment(Pos.CENTER);

        Region spacerTop = new Region();
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        VBox card = createScreenCard(440);
        card.setAlignment(Pos.CENTER);
        card.getChildren().addAll(
                subtitle,
                spacerTop,
                imageCard,
                title,
                spacerBottom,
                bottomBar
        );

        mainLayout.setCenter(wrapScreen(card));
    }

    /**
     * Themed picker used during an active workout so we never fall back to default JavaFX dialogs.
     */
    private void showWorkoutExercisePickerScreen(String username, String workoutTitle, String[] exercises, int currentIndex, boolean alternativesOnly) {
        String currentExercise = exercises[currentIndex];
        String defaultFilter = alternativesOnly ? DatabaseHelper.getMuscleGroupForExercise(currentExercise) : "All";
        if (defaultFilter == null || defaultFilter.isBlank()) {
            defaultFilter = "All";
        }
        List<String> availableExercises = alternativesOnly
                ? DatabaseHelper.getAlternatives(currentExercise)
                : DatabaseHelper.getExercisesByMuscleGroup(defaultFilter);

        if (availableExercises.isEmpty()) {
            showInfoScreen(
                    "No Alternatives Yet",
                    "There are no saved alternatives for " + currentExercise + " right now.",
                    "Back",
                    () -> showCustomWorkout(username, workoutTitle, exercises, currentIndex)
            );
            return;
        }

        Label title = new Label(alternativesOnly ? "Choose Alternative" : "Change Exercise");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label(alternativesOnly
                ? "Pick a similar movement for " + currentExercise + "."
                : "Swap " + currentExercise + " with any exercise in the catalog.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        Label currentLabel = new Label("Current: " + currentExercise);
        currentLabel.setStyle(modernTagStyle());

        Label filterLabel = new Label("Filter by muscle group");
        filterLabel.setStyle("-fx-font-size: " + px(13) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");

        MenuButton filterButton = new MenuButton("Muscle Group: " + defaultFilter);
        filterButton.setStyle(modernFilterButtonStyle());

        ComboBox<String> exerciseBox = new ComboBox<>();
        exerciseBox.getItems().setAll(availableExercises);
        exerciseBox.setPromptText("Select an exercise");
        exerciseBox.setMaxWidth(Double.MAX_VALUE);
        exerciseBox.setStyle(modernComboBoxStyle());
        exerciseBox.setVisibleRowCount(10);
        styleExerciseComboBox(exerciseBox);

        final String[] selectedFilter = {defaultFilter};

        List<String> muscleGroups = new ArrayList<>();
        muscleGroups.add("All");
        muscleGroups.addAll(DatabaseHelper.getAllMuscleGroups());

        for (String muscleGroup : muscleGroups) {
            MenuItem item = new MenuItem(muscleGroup);
            item.setOnAction(e -> {
                selectedFilter[0] = muscleGroup;
                filterButton.setText("Muscle Group: " + muscleGroup);
                List<String> refreshed = alternativesOnly
                        ? filterExercisesList(DatabaseHelper.getAlternatives(currentExercise), muscleGroup, "")
                        : DatabaseHelper.getExercisesByMuscleGroup(muscleGroup);
                applyExerciseOptions(exerciseBox, refreshed);
            });
            filterButton.getItems().add(item);
        }

        configureAutocompleteComboBox(
                exerciseBox,
                query -> alternativesOnly
                        ? filterExercisesList(DatabaseHelper.getAlternatives(currentExercise), selectedFilter[0], query)
                        : DatabaseHelper.searchExercises(selectedFilter[0], query)
        );

        Button confirmButton = new Button(alternativesOnly ? "Use Alternative" : "Replace Exercise");
        confirmButton.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setStyle(modernPrimaryButtonStyle());
        confirmButton.setOnAction(e -> {
            String selectedExercise = resolveAutocompleteSelection(
                    exerciseBox,
                    query -> alternativesOnly
                            ? filterExercisesList(DatabaseHelper.getAlternatives(currentExercise), selectedFilter[0], query)
                            : DatabaseHelper.searchExercises(selectedFilter[0], query)
            );
            if (selectedExercise != null && !selectedExercise.isBlank()) {
                exercises[currentIndex] = selectedExercise;
                showCustomWorkout(username, workoutTitle, exercises, currentIndex);
            }
        });

        Button altButton = new Button("Alternatives");
        altButton.setMaxWidth(Double.MAX_VALUE);
        altButton.setStyle(modernSecondaryButtonStyle());
        altButton.setDisable(alternativesOnly);
        altButton.setOnAction(e -> showWorkoutExercisePickerScreen(username, workoutTitle, exercises, currentIndex, true));

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showCustomWorkout(username, workoutTitle, exercises, currentIndex));

        VBox card = createScreenCard(430);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle, currentLabel, filterLabel, filterButton, exerciseBox, confirmButton, altButton, backButton);

        mainLayout.setCenter(wrapScreen(card));
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
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Choose an exercise to start logging.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        VBox exerciseBox = new VBox(12);
        exerciseBox.setAlignment(Pos.CENTER);
        exerciseBox.setFillWidth(true);

        for (int i = 0; i < exercises.length; i++) {
            Button exerciseButton = new Button(exercises[i]);
            exerciseButton.setMaxWidth(Double.MAX_VALUE);
            exerciseButton.setStyle(i == currentIndex ? modernPrimaryButtonStyle() : modernSecondaryButtonStyle());

            final int index = i;
            exerciseButton.setOnAction(e -> showSetLoggerScreen(username, "Push Day", exercises, index));

            exerciseBox.getChildren().add(exerciseButton);
        }

        Button finishButton = new Button("Finish Workout");
        Button backButton = new Button("Back");

        finishButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);
        finishButton.setStyle(modernPrimaryButtonStyle());
        backButton.setStyle(modernSecondaryButtonStyle());

        finishButton.setOnAction(e -> showWorkoutComplete(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(430);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle, exerciseBox, finishButton, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showPullWorkout(String username) {
        Label title = new Label("Pull Day Workout");
        title.setStyle(screenTitleStyle());

        Label exercises = new Label(
                "Deadlift\n" +
                        "Lat Pulldown\n" +
                        "Barbell Row\n" +
                        "Face Pull\n" +
                        "Bicep Curl"
        );
        exercises.setStyle(screenSubtitleStyle());

        Button startLoggingButton = new Button("Log Sets");
        Button backButton = new Button("Back");

        startLoggingButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);
        startLoggingButton.setStyle(modernPrimaryButtonStyle());
        backButton.setStyle(modernSecondaryButtonStyle());

        startLoggingButton.setOnAction(e -> showAddWorkoutScreen(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(420);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, exercises, startLoggingButton, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showLegWorkout(String username) {
        Label title = new Label("Leg Day Workout");
        title.setStyle(screenTitleStyle());

        Label exercises = new Label(
                "Squat\n" +
                        "Leg Press\n" +
                        "Romanian Deadlift\n" +
                        "Leg Curl\n" +
                        "Calf Raises"
        );
        exercises.setStyle(screenSubtitleStyle());

        Button startLoggingButton = new Button("Log Sets");
        Button backButton = new Button("Back");

        startLoggingButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);
        startLoggingButton.setStyle(modernPrimaryButtonStyle());
        backButton.setStyle(modernSecondaryButtonStyle());

        startLoggingButton.setOnAction(e -> showAddWorkoutScreen(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(420);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, exercises, startLoggingButton, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showSetLoggerScreen(String username, String workoutType, String[] exercises, int currentIndex) {
        String exercise = exercises[currentIndex];

        List<String> previousSets = DatabaseHelper.getLastLoggedSetsForExercise(username, exercise);
        double estimatedOneRepMax = DatabaseHelper.getEstimatedOneRepMax(username, exercise);

        Label title = new Label(exercise.toUpperCase());
        title.setStyle("-fx-font-size: " + px(26) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("Log your sets");
        subtitle.setStyle(screenSubtitleStyle());

        StackPane imageCard = new StackPane();
        imageCard.setPrefSize(180, 180);

        Button exitButton = new Button("🚪 Exit");
        exitButton.setMaxWidth(Double.MAX_VALUE);
        exitButton.setStyle(modernSecondaryButtonStyle());
        exitButton.setOnAction(e -> confirmExitWorkout(
                username,
                () -> showSetLoggerScreen(username, workoutType, exercises, currentIndex)
        ));
        exitButton.setText("Exit");

        imageCard.setMaxSize(180, 180);
        imageCard.setStyle(
                "-fx-background-color: " + cardFill() + ";" +
                        "-fx-background-radius: 24;" +
                        "-fx-border-color: " + borderColor() + ";" +
                        "-fx-border-radius: 24;" +
                        "-fx-border-width: 2;"
        );

        Label oneRepMaxLabel = new Label(
                estimatedOneRepMax > 0
                        ? "Estimated 1RM: " + estimatedOneRepMax + "kg"
                        : "Estimated 1RM: N/A"
        );
        oneRepMaxLabel.setStyle(
                "-fx-font-size: " + px(16) + "px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + primaryText() + ";"
        );

        VBox previousSetsBox = new VBox(6);
        previousSetsBox.setAlignment(Pos.CENTER_LEFT);
        previousSetsBox.setMaxWidth(320);
        previousSetsBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: " + borderColor() + ";" +
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
            weightField.setStyle(modernFieldStyle());
            setRow.weightField = weightField;

            TextField repsField = new TextField();
            repsField.setPromptText("Reps");
            repsField.setPrefWidth(100);
            repsField.setStyle(modernFieldStyle());
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
            row.setMaxWidth(Double.MAX_VALUE);
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

        HBox buttonRow = new HBox(12, cancelButton, doneButton, exitButton);
        buttonRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        HBox.setHgrow(doneButton, Priority.ALWAYS);

        VBox card = createScreenCard(440);
        card.setAlignment(Pos.CENTER);
        card.getChildren().addAll(
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

        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainLayout.setCenter(wrapScreen(scrollPane));
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
        title.setStyle(screenTitleStyle());

        StackPane pictureBox = new StackPane();
        pictureBox.setPrefSize(120, 120);
        pictureBox.setMaxSize(120, 120);
        pictureBox.setStyle(
                "-fx-background-color: #f8fafc;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;"
        );

        if (profilePicturePath != null && !profilePicturePath.isBlank()) {
            try {
                Image image = new Image("file:" + profilePicturePath);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(110);
                imageView.setFitHeight(110);
                imageView.setPreserveRatio(true);
                pictureBox.getChildren().add(imageView);
            } catch (Exception e) {
                pictureBox.getChildren().add(new Label("No Image"));
            }
        } else {
            Label noImage = new Label("No Image");
            noImage.setStyle("-fx-text-fill: #6b7280;");
            pictureBox.getChildren().add(noImage);
        }

        VBox statsBox = new VBox(12);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setMaxWidth(320);
        statsBox.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 18;" +
                        "-fx-padding: 18;"
        );

        statsBox.getChildren().addAll(
                profileStat("Name", displayName),
                profileStat("Height", height + " cm"),
                profileStat("Weight", weight + " kg"),
                profileStat("Most Logged Exercise", mostLogged),
                profileStat("Heaviest Lift", heaviestLift),
                profileStat("Most Improved", mostImproved),
                profileStat("Logging Streak", streak + " day(s)"),
                profileStat("Date Joined", dateJoined)
        );

        Button editButton = new Button("Edit Profile");
        Button backButton = new Button("Back");

        editButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);

        editButton.setStyle(modernPrimaryButtonStyle());
        backButton.setStyle(modernSecondaryButtonStyle());

        editButton.setOnAction(e -> showEditProfileScreen(username));
        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(420);
        card.getChildren().addAll(title, pictureBox, statsBox, editButton, backButton);

        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainLayout.setCenter(wrapScreen(scrollPane));
    }

    private void showEditProfileScreen(String username) {
        String[] profile = DatabaseHelper.getProfileDetails(username);

        Label title = new Label("Edit Profile");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Update your profile details and picture.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        TextField nameField = new TextField(profile[0]);
        nameField.setPromptText("Display Name");
        nameField.setStyle(modernFieldStyle());

        TextField heightField = new TextField(profile[1].equals("0.0") ? "" : profile[1]);
        heightField.setPromptText("Height (cm)");
        heightField.setStyle(modernFieldStyle());

        TextField weightField = new TextField(profile[2].equals("0.0") ? "" : profile[2]);
        weightField.setPromptText("Weight (kg)");
        weightField.setStyle(modernFieldStyle());

        TextField pictureField = new TextField(profile[3]);
        pictureField.setPromptText("Profile Picture Path");
        pictureField.setStyle(modernFieldStyle());

        Button browseButton = new Button("Browse");
        Button saveButton = new Button("Save Changes");
        Button backButton = new Button("Cancel");
        Label resultLabel = new Label();
        browseButton.setStyle(modernSecondaryButtonStyle());
        saveButton.setStyle(modernPrimaryButtonStyle());
        backButton.setStyle(modernSecondaryButtonStyle());
        resultLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: " + px(13) + "px;");

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

        VBox card = createScreenCard(440);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(
                title,
                subtitle,
                nameField,
                heightField,
                weightField,
                pictureRow,
                saveButton,
                backButton,
                resultLabel
        );

        mainLayout.setCenter(wrapScreen(card));
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

        Button menuButton = new Button("⚙");
        menuButton.setStyle(modernSecondaryButtonStyle());
        menuButton.setOnAction(e -> showQuickSettingsScreen(username));
        menuButton.setPrefWidth(56);
        menuButton.setMinWidth(56);
        HBox titleBox = createCompactBrandBar();
        titleBox.setAlignment(Pos.CENTER);

        Button profileButton = new Button("👤");
        profileButton.setStyle(modernSecondaryButtonStyle());
        profileButton.setOnAction(e -> showProfileScreen(username));
        profileButton.setPrefWidth(56);
        profileButton.setMinWidth(56);

        topBar.setLeft(menuButton);

        topBar.setCenter(titleBox);
        topBar.setRight(profileButton);
        BorderPane.setAlignment(titleBox, Pos.CENTER);

        Label greetingLabel = new Label("Welcome back");
        greetingLabel.setStyle(modernTagStyle());

        Label welcomeLabel = new Label(username);
        welcomeLabel.setStyle("-fx-font-size: " + px(30) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");

        Label planLabel = new Label("Today is " + today + " - " + todayPlan);
        planLabel.setStyle(screenSubtitleStyle());
        planLabel.setWrapText(true);

        Label splitLabel = new Label(selectedSplit);
        splitLabel.setStyle(modernTagStyle());

        Label dayLabel = new Label(today);
        dayLabel.setStyle(modernAccentTagStyle());

        HBox heroTags = new HBox(8, splitLabel, dayLabel);
        heroTags.setAlignment(Pos.CENTER_LEFT);

        Button heroStartButton = new Button("Start Today's Workout");
        heroStartButton.setMaxWidth(Double.MAX_VALUE);
        heroStartButton.setStyle(modernPrimaryButtonStyle());
        heroStartButton.setOnAction(e -> startWorkout(username, planForToday));

        VBox heroCard = new VBox(scaledSpacing(12), greetingLabel, welcomeLabel, planLabel, heroTags, heroStartButton);
        heroCard.setAlignment(Pos.CENTER_LEFT);
        heroCard.setPadding(new Insets(scaledSpacing(22)));
        heroCard.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, rgba(255,255,255,0.98), rgba(241,245,249,0.96));" +
                        "-fx-background-radius: 28;" +
                        "-fx-border-color: " + borderColor() + ";" +
                        "-fx-border-radius: 28;"
        );

        Button splitsButton = new Button("Splits");
        Button modifyButton = new Button("Modify Split");
        Button recordsButton = new Button("History");
        Button logoutButton = new Button("Log Out");

        splitsButton.setMaxWidth(Double.MAX_VALUE);
        modifyButton.setMaxWidth(Double.MAX_VALUE);
        recordsButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        splitsButton.setStyle(modernSecondaryButtonStyle());
        modifyButton.setStyle(modernSecondaryButtonStyle());
        recordsButton.setStyle(modernSecondaryButtonStyle());
        logoutButton.setStyle(modernSecondaryButtonStyle());

        splitsButton.setOnAction(e -> showSplitsScreen(username));
        modifyButton.setOnAction(e -> showModifySplitScreen(username));
        recordsButton.setOnAction(e -> showHistoryScreen(username));
        logoutButton.setOnAction(e -> showLoginScreen());

        GridPane actionGrid = new GridPane();
        actionGrid.setHgap(scaledSpacing(12));
        actionGrid.setVgap(scaledSpacing(12));
        actionGrid.add(createMenuActionCard("Splits", "Choose and manage your training structure.", splitsButton), 0, 0);
        actionGrid.add(createMenuActionCard("Modify", "Swap, replace, and fine tune each workout day.", modifyButton), 1, 0);
        actionGrid.add(createMenuActionCard("History", "Review lifts, records, and previous sessions.", recordsButton), 0, 1);
        actionGrid.add(createMenuActionCard("Account", "Profile details, settings, and sign out.", logoutButton), 1, 1);

        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);
        leftColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);
        rightColumn.setHgrow(Priority.ALWAYS);
        actionGrid.getColumnConstraints().addAll(leftColumn, rightColumn);

        Label sectionLabel = new Label("Quick Actions");
        sectionLabel.setStyle("-fx-font-size: " + px(18) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");

        VBox card = createScreenCard(420);
        card.setAlignment(Pos.TOP_CENTER);
        card.getChildren().addAll(topBar, heroCard, sectionLabel, actionGrid);

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

        Label subtitle = new Label("Choose a day to add, replace, swap, or remove exercises.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

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
        card.getChildren().addAll(title, subtitle, daysBox, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void showModifyDayScreen(String username, String splitName, String day) {
        Label title = new Label(day);
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label(splitName + " • Curate this day with cleaner swaps and filtered exercise picking.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        VBox exerciseList = new VBox(12);
        exerciseList.setFillWidth(true);
        List<String> exercises = DatabaseHelper.getExercisesForDay(username, splitName, day);

        if (exercises.isEmpty()) {
            Label emptyLabel = new Label("No exercises added for this day yet.");
            emptyLabel.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.9);" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-color: #d1d5db;" +
                            "-fx-border-radius: 18;" +
                            "-fx-padding: 18;" +
                            "-fx-text-fill: #6b7280;" +
                            "-fx-font-size: 14px;"
            );
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            exerciseList.getChildren().add(emptyLabel);
        } else {
            for (String exercise : exercises) {
                HBox row = createExerciseEditorRow(username, splitName, day, exercise);
                exerciseList.getChildren().add(row);
            }
        }

        Button addExerciseButton = new Button("Add New Workout");
        addExerciseButton.setMaxWidth(Double.MAX_VALUE);
        addExerciseButton.setStyle(modernPrimaryButtonStyle());
        addExerciseButton.setOnAction(e -> showExercisePickerScreen(username, splitName, day, null));

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showModifySplitScreen(username));

        VBox card = createScreenCard(460);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle, exerciseList, addExerciseButton, backButton);

        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainLayout.setCenter(wrapScreen(scrollPane));
    }

    private void showAddWorkoutScreen(String username) {
        Label title = new Label("Add Workout");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Log a standalone workout entry.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        ComboBox<String> exerciseBox = new ComboBox<>();
        exerciseBox.getItems().addAll("Bench Press", "Squat", "Deadlift", "Shoulder Press", "Barbell Row");
        exerciseBox.setPromptText("Select Exercise");
        exerciseBox.setMaxWidth(Double.MAX_VALUE);
        exerciseBox.setStyle(modernComboBoxStyle());
        styleExerciseComboBox(exerciseBox);

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");
        weightField.setStyle(modernFieldStyle());

        TextField repsField = new TextField();
        repsField.setPromptText("Reps");
        repsField.setStyle(modernFieldStyle());

        Button saveButton = new Button("Save Workout");
        Button backButton = new Button("Back");
        Label resultLabel = new Label();

        saveButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setMaxWidth(Double.MAX_VALUE);
        saveButton.setStyle(modernPrimaryButtonStyle());
        backButton.setStyle(modernSecondaryButtonStyle());
        resultLabel.setStyle("-fx-font-size: " + px(13) + "px; -fx-text-fill: #374151;");

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

        VBox card = createScreenCard(420);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle, exerciseBox, weightField, repsField, saveButton, backButton, resultLabel);

        mainLayout.setCenter(wrapScreen(card));
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
                "-fx-background-color: white;" +
                        "-fx-control-inner-background: white;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-padding: 8;"
        );

        Button backButton = new Button("Back");
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showDashboard(username));
        backButton.setMaxWidth(Double.MAX_VALUE);

        VBox card = createScreenCard(460);
        card.getChildren().addAll(title, subtitle, historyList, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    /**
     * Lightweight settings screen for accessibility and phone-like layout tuning.
     */
    private void showQuickSettingsScreen(String username) {
        Label title = new Label("Quick Settings");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Adjust readability, contrast, motion, and content width.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        Slider textScaleSlider = new Slider(0.9, 1.3, textScale);
        textScaleSlider.setShowTickLabels(true);
        textScaleSlider.setShowTickMarks(true);

        Slider cardWidthSlider = new Slider(360, 440, preferredCardWidth);
        cardWidthSlider.setShowTickLabels(true);
        cardWidthSlider.setShowTickMarks(true);

        CheckBox contrastBox = new CheckBox("High contrast mode");
        contrastBox.setSelected(highContrastMode);
        contrastBox.setStyle(checkboxStyle());

        CheckBox reducedMotionBox = new CheckBox("Reduce motion");
        reducedMotionBox.setSelected(reducedMotion);
        reducedMotionBox.setStyle(checkboxStyle());

        CheckBox compactModeBox = new CheckBox("Compact spacing");
        compactModeBox.setSelected(compactSpacing);
        compactModeBox.setStyle(checkboxStyle());

        VBox controls = new VBox(
                scaledSpacing(10),
                settingsRow("Text size", textScaleSlider),
                settingsRow("Card width", cardWidthSlider),
                contrastBox,
                reducedMotionBox,
                compactModeBox
        );
        controls.setMaxWidth(Double.MAX_VALUE);
        controls.setStyle(surfaceStyle());

        Button applyButton = new Button("Apply Settings");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setStyle(modernPrimaryButtonStyle());
        applyButton.setOnAction(e -> {
            textScale = textScaleSlider.getValue();
            preferredCardWidth = cardWidthSlider.getValue();
            highContrastMode = contrastBox.isSelected();
            reducedMotion = reducedMotionBox.isSelected();
            compactSpacing = compactModeBox.isSelected();
            showDashboard(username);
        });

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showDashboard(username));

        VBox card = createScreenCard(440);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle, controls, applyButton, backButton);

        mainLayout.setCenter(wrapScreen(card));
    }

    private HBox settingsRow(String labelText, Node control) {
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: " + px(14) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(12, label, spacer, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void showInfoScreen(String titleText, String bodyText, String buttonText, Runnable onClose) {
        Label icon = new Label("i");
        icon.setStyle(
                "-fx-font-size: " + px(28) + "px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #2563eb;" +
                        "-fx-background-radius: 18;" +
                        "-fx-padding: 10 16 10 16;"
        );

        Label title = new Label(titleText);
        title.setStyle(screenTitleStyle());

        Label body = new Label(bodyText);
        body.setStyle(screenSubtitleStyle());
        body.setWrapText(true);
        body.setTextAlignment(TextAlignment.CENTER);

        Button closeButton = new Button(buttonText);
        closeButton.setMaxWidth(Double.MAX_VALUE);
        closeButton.setStyle(modernPrimaryButtonStyle());
        closeButton.setOnAction(e -> onClose.run());

        VBox card = createScreenCard(380);
        card.getChildren().addAll(icon, title, body, closeButton);
        mainLayout.setCenter(wrapScreen(card));
    }

    private void showConfirmScreen(String titleText, String bodyText, String confirmText, Runnable onConfirm, Runnable onCancel) {
        Label title = new Label(titleText);
        title.setStyle(screenTitleStyle());

        Label body = new Label(bodyText);
        body.setStyle(screenSubtitleStyle());
        body.setWrapText(true);
        body.setTextAlignment(TextAlignment.CENTER);

        Button confirmButton = new Button(confirmText);
        confirmButton.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setStyle(modernPrimaryButtonStyle());
        confirmButton.setOnAction(e -> onConfirm.run());

        Button cancelButton = new Button("Cancel");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setStyle(modernSecondaryButtonStyle());
        cancelButton.setOnAction(e -> onCancel.run());

        VBox card = createScreenCard(400);
        card.getChildren().addAll(title, body, confirmButton, cancelButton);
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
        Label iconLabel = new Label(getExerciseIcon(exercise));
        iconLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-background-color: #c7d2fe;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 10;"
        );

        Label nameLabel = new Label(exercise);
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(Double.MAX_VALUE);

        Label muscleGroupLabel = new Label(DatabaseHelper.getMuscleGroupForExercise(exercise));
        muscleGroupLabel.setStyle(modernTagStyle());

        Label focusAreaLabel = new Label(DatabaseHelper.getFocusAreaForExercise(exercise));
        focusAreaLabel.setStyle(modernAccentTagStyle());

        HBox tagRow = new HBox(6, muscleGroupLabel);
        if (!focusAreaLabel.getText().isBlank()) {
            tagRow.getChildren().add(focusAreaLabel);
        }

        VBox textBox = new VBox(5, nameLabel, tagRow);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Button replaceButton = new Button("🔄");
        Button removeButton = new Button("🗑");
        Button alternativeButton = new Button("⇄");

        alternativeButton.setText("Alternative");
        replaceButton.setText("Replace");
        removeButton.setText("Remove");

        alternativeButton.setStyle(modernSecondaryButtonStyle());
        replaceButton.setStyle(modernSecondaryButtonStyle());
        removeButton.setStyle(modernDangerButtonStyle());

        replaceButton.setTooltip(new Tooltip("Replace Exercise"));
        removeButton.setTooltip(new Tooltip("Remove Exercise"));
        alternativeButton.setTooltip(new Tooltip("View Alternatives"));

        replaceButton.setOnAction(e -> showExercisePickerScreen(username, splitName, day, exercise));

        removeButton.setOnAction(e -> {
            DatabaseHelper.removeExercise(username, splitName, day, exercise);
            showModifyDayScreen(username, splitName, day);
        });

        alternativeButton.setOnAction(e -> showAlternativesScreen(username, splitName, day, exercise));

        alternativeButton.setText("Alt");
        replaceButton.setText("Replace");
        removeButton.setText("Remove");

        alternativeButton.setStyle(compactSecondaryButtonStyle());
        replaceButton.setStyle(compactSecondaryButtonStyle());
        removeButton.setStyle(compactDangerButtonStyle());

        alternativeButton.setMaxWidth(Double.MAX_VALUE);
        replaceButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(alternativeButton, Priority.ALWAYS);
        HBox.setHgrow(replaceButton, Priority.ALWAYS);
        HBox.setHgrow(removeButton, Priority.ALWAYS);

        HBox topRow = new HBox(12, iconLabel, textBox);
        topRow.setAlignment(Pos.CENTER_LEFT);

        HBox buttonRow = new HBox(8, alternativeButton, replaceButton, removeButton);
        buttonRow.setAlignment(Pos.CENTER);

        VBox cardContent = new VBox(12, topRow, buttonRow);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        cardContent.setPadding(new Insets(16));
        cardContent.setMaxWidth(Double.MAX_VALUE);
        cardContent.setStyle(exerciseEditorRowStyle());

        HBox wrapper = new HBox(cardContent);
        HBox.setHgrow(cardContent, Priority.ALWAYS);
        wrapper.setMaxWidth(Double.MAX_VALUE);
        return wrapper;
    }

    private void showAlternativesScreen(String username, String splitName, String day, String exercise) {
        Label title = new Label("Alternatives for " + exercise);
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label("Pick a similar movement for this slot.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        VBox alternativesBox = new VBox(10);
        alternativesBox.setFillWidth(true);
        List<String> alternatives = DatabaseHelper.getAlternatives(exercise);

        if (alternatives.isEmpty()) {
            Label emptyLabel = new Label("No alternatives found yet.");
            emptyLabel.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.9);" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-color: #d1d5db;" +
                            "-fx-border-radius: 18;" +
                            "-fx-padding: 18;" +
                            "-fx-text-fill: #6b7280;" +
                            "-fx-font-size: 14px;"
            );
            emptyLabel.setMaxWidth(Double.MAX_VALUE);
            alternativesBox.getChildren().add(emptyLabel);
        } else {
            for (String alternative : alternatives) {
                Label nameLabel = new Label(getExerciseIcon(alternative) + "  " + alternative);
                nameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #111827; -fx-font-weight: bold;");
                nameLabel.setWrapText(true);

                Label groupLabel = new Label(DatabaseHelper.getMuscleGroupForExercise(alternative));
                groupLabel.setStyle(modernTagStyle());

                Label focusLabel = new Label(DatabaseHelper.getFocusAreaForExercise(alternative));
                focusLabel.setStyle(modernAccentTagStyle());

                HBox tagRow = new HBox(6, groupLabel);
                if (!focusLabel.getText().isBlank()) {
                    tagRow.getChildren().add(focusLabel);
                }

                VBox buttonContent = new VBox(6, nameLabel, tagRow);
                buttonContent.setAlignment(Pos.CENTER_LEFT);
                buttonContent.setFillWidth(false);

                Button altButton = new Button();
                altButton.setGraphic(buttonContent);
                altButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                altButton.setMaxWidth(Double.MAX_VALUE);
                altButton.setMinHeight(Region.USE_PREF_SIZE);
                altButton.setPrefHeight(Region.USE_COMPUTED_SIZE);
                altButton.setAlignment(Pos.CENTER_LEFT);
                altButton.setStyle(alternativeOptionButtonStyle());
                altButton.setOnAction(e -> {
                    DatabaseHelper.replaceExercise(username, splitName, day, exercise, alternative);
                    showModifyDayScreen(username, splitName, day);
                });
                alternativesBox.getChildren().add(altButton);
            }
        }

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showModifyDayScreen(username, splitName, day));

        VBox card = createScreenCard(460);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle, alternativesBox, backButton);

        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        mainLayout.setCenter(wrapScreen(scrollPane));
    }

    private void showExercisePickerScreen(String username, String splitName, String day, String exerciseToReplace) {
        boolean replacing = exerciseToReplace != null;
        String defaultFilter = replacing ? DatabaseHelper.getMuscleGroupForExercise(exerciseToReplace) : "All";
        if (defaultFilter == null || defaultFilter.isBlank()) {
            defaultFilter = "All";
        }

        Label title = new Label(replacing ? "Replace Workout" : "Add Workout");
        title.setStyle(screenTitleStyle());

        Label subtitle = new Label(replacing
                ? "Choose a new exercise and narrow the list with a muscle-group filter if you want."
                : "Pick a workout from the full catalog, then filter down to the muscle group you want.");
        subtitle.setStyle(screenSubtitleStyle());
        subtitle.setWrapText(true);

        Label currentExerciseLabel = new Label();
        if (replacing) {
            currentExerciseLabel.setText("Current: " + exerciseToReplace);
            currentExerciseLabel.setStyle(modernTagStyle());
        }

        Label filterLabel = new Label("Filter by muscle group");
        filterLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151;");

        MenuButton filterButton = new MenuButton("Muscle Group: " + defaultFilter);
        filterButton.setStyle(modernFilterButtonStyle());

        ComboBox<String> exerciseBox = new ComboBox<>();
        exerciseBox.setPromptText("Select a workout");
        exerciseBox.setMaxWidth(Double.MAX_VALUE);
        exerciseBox.setVisibleRowCount(10);
        exerciseBox.setStyle(modernComboBoxStyle());

        styleExerciseComboBox(exerciseBox);
        refreshExerciseOptions(exerciseBox, defaultFilter);

        final String[] selectedFilter = {defaultFilter};

        List<String> muscleGroups = new ArrayList<>();
        muscleGroups.add("All");
        muscleGroups.addAll(DatabaseHelper.getAllMuscleGroups());

        for (String muscleGroup : muscleGroups) {
            MenuItem item = new MenuItem(muscleGroup);
            item.setOnAction(e -> {
                selectedFilter[0] = muscleGroup;
                filterButton.setText("Muscle Group: " + muscleGroup);
                refreshExerciseOptions(exerciseBox, muscleGroup);
            });
            filterButton.getItems().add(item);
        }

        configureAutocompleteComboBox(
                exerciseBox,
                query -> DatabaseHelper.searchExercises(selectedFilter[0], query)
        );

        Button confirmButton = new Button(replacing ? "Replace Workout" : "Add Workout");
        confirmButton.setMaxWidth(Double.MAX_VALUE);
        confirmButton.setStyle(modernPrimaryButtonStyle());

        Label messageLabel = new Label();
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626;");

        confirmButton.setOnAction(e -> {
            String selectedExercise = resolveAutocompleteSelection(
                    exerciseBox,
                    query -> DatabaseHelper.searchExercises(selectedFilter[0], query)
            );
            if (selectedExercise == null || selectedExercise.isBlank()) {
                messageLabel.setText("Choose a workout from the dropdown first.");
                return;
            }

            if (replacing) {
                DatabaseHelper.replaceExercise(username, splitName, day, exerciseToReplace, selectedExercise);
            } else {
                int nextOrder = DatabaseHelper.getExercisesForDay(username, splitName, day).size();
                DatabaseHelper.addExerciseToDay(username, splitName, day, selectedExercise, nextOrder);
            }

            showModifyDayScreen(username, splitName, day);
        });

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setStyle(modernSecondaryButtonStyle());
        backButton.setOnAction(e -> showModifyDayScreen(username, splitName, day));

        VBox card = createScreenCard(460);
        card.setAlignment(Pos.TOP_LEFT);
        card.getChildren().addAll(title, subtitle);
        if (replacing) {
            card.getChildren().add(currentExerciseLabel);
        }
        card.getChildren().addAll(filterLabel, filterButton, exerciseBox, confirmButton, backButton, messageLabel);

        mainLayout.setCenter(wrapScreen(card));
    }

    private void refreshExerciseOptions(ComboBox<String> exerciseBox, String muscleGroup) {
        List<String> exercises = DatabaseHelper.getExercisesByMuscleGroup(muscleGroup);
        applyExerciseOptions(exerciseBox, exercises);
    }

    private void applyExerciseOptions(ComboBox<String> exerciseBox, List<String> exercises) {
        exerciseBox.getItems().setAll(exercises);
        exerciseBox.getSelectionModel().clearSelection();
        exerciseBox.setPromptText(exercises.isEmpty() ? "No workouts found" : "Select a workout");
    }

    private void configureAutocompleteComboBox(ComboBox<String> exerciseBox, Function<String, List<String>> suggestionProvider) {
        exerciseBox.setEditable(true);
        final boolean[] updating = {false};

        exerciseBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (updating[0]) {
                return;
            }

            List<String> suggestions = suggestionProvider.apply(newValue == null ? "" : newValue.trim());
            updating[0] = true;
            applyExerciseOptions(exerciseBox, suggestions);
            exerciseBox.getEditor().setText(newValue);
            exerciseBox.getEditor().positionCaret(newValue == null ? 0 : newValue.length());
            updating[0] = false;

            if (suggestions.isEmpty()) {
                exerciseBox.hide();
            } else {
                exerciseBox.show();
            }
        });

        exerciseBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || updating[0]) {
                return;
            }

            updating[0] = true;
            exerciseBox.getEditor().setText(newValue);
            exerciseBox.getEditor().positionCaret(newValue.length());
            updating[0] = false;
        });
    }

    private String resolveAutocompleteSelection(ComboBox<String> exerciseBox, Function<String, List<String>> suggestionProvider) {
        String selected = exerciseBox.getValue();
        if (selected != null && !selected.isBlank()) {
            return selected;
        }

        String typedText = exerciseBox.getEditor().getText();
        if (typedText == null || typedText.isBlank()) {
            return null;
        }

        List<String> suggestions = suggestionProvider.apply(typedText.trim());
        if (!suggestions.isEmpty()) {
            return suggestions.get(0);
        }

        return typedText.trim();
    }

    private List<String> filterExercisesList(List<String> exercises, String muscleGroup, String query) {
        List<String> filtered = new ArrayList<>();
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();

        for (String exercise : exercises) {
            String exerciseMuscleGroup = DatabaseHelper.getMuscleGroupForExercise(exercise);
            boolean matchesMuscle = muscleGroup == null || muscleGroup.equals("All") || muscleGroup.equalsIgnoreCase(exerciseMuscleGroup);
            boolean matchesQuery = normalizedQuery.isBlank() || exercise.toLowerCase().contains(normalizedQuery);

            if (matchesMuscle && matchesQuery) {
                filtered.add(exercise);
            }
        }

        filtered.sort((left, right) -> {
            boolean leftStarts = !normalizedQuery.isBlank() && left.toLowerCase().startsWith(normalizedQuery);
            boolean rightStarts = !normalizedQuery.isBlank() && right.toLowerCase().startsWith(normalizedQuery);
            if (leftStarts != rightStarts) {
                return leftStarts ? -1 : 1;
            }
            return left.compareToIgnoreCase(right);
        });

        return filtered;
    }

    private void styleExerciseComboBox(ComboBox<String> exerciseBox) {
        exerciseBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label itemNameLabel = new Label(getExerciseIcon(item) + "  " + item);
                itemNameLabel.setStyle("-fx-text-fill: #111827; -fx-font-size: 14px; -fx-font-weight: bold;");

                Label groupLabel = new Label(DatabaseHelper.getMuscleGroupForExercise(item));
                groupLabel.setStyle(modernTagStyle());

                Label focusLabel = new Label(DatabaseHelper.getFocusAreaForExercise(item));
                focusLabel.setStyle(modernAccentTagStyle());

                HBox tagRow = new HBox(6, groupLabel);
                if (!focusLabel.getText().isBlank()) {
                    tagRow.getChildren().add(focusLabel);
                }

                VBox content = new VBox(4, itemNameLabel, tagRow);
                setGraphic(content);
            }
        });

        exerciseBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label itemNameLabel = new Label(getExerciseIcon(item) + "  " + item);
                itemNameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #111827; -fx-font-weight: bold;");

                Label groupLabel = new Label(DatabaseHelper.getMuscleGroupForExercise(item));
                groupLabel.setStyle(modernTagStyle());

                Label focusLabel = new Label(DatabaseHelper.getFocusAreaForExercise(item));
                focusLabel.setStyle(modernAccentTagStyle());

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox tagRow = new HBox(6, groupLabel);
                if (!focusLabel.getText().isBlank()) {
                    tagRow.getChildren().add(focusLabel);
                }

                VBox textBox = new VBox(4, itemNameLabel, tagRow);
                HBox row = new HBox(10, textBox, spacer);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
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
                "-fx-text-fill: " + primaryText() + ";" +
                "-fx-font-size: " + px(15) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 18;" +
                "-fx-border-radius: 18;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-padding: " + px(12) + " " + px(18) + " " + px(12) + " " + px(18) + ";";
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
                "-fx-font-size: " + px(15) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: " + px(14) + " " + px(18) + " " + px(14) + " " + px(18) + ";";
    }

    private String modernSecondaryButtonStyle() {
        return "-fx-background-color: #ffffff;" +
                "-fx-text-fill: " + primaryText() + ";" +
                "-fx-font-size: " + px(15) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 16;" +
                "-fx-padding: " + px(14) + " " + px(18) + " " + px(14) + " " + px(18) + ";";
    }

    private String modernDangerButtonStyle() {
        return "-fx-background-color: #fff1f2;" +
                "-fx-text-fill: #be123c;" +
                "-fx-font-size: " + px(15) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #fecdd3;" +
                "-fx-border-radius: 16;" +
                "-fx-padding: " + px(14) + " " + px(18) + " " + px(14) + " " + px(18) + ";";
    }

    private String compactSecondaryButtonStyle() {
        return "-fx-background-color: #ffffff;" +
                "-fx-text-fill: " + primaryText() + ";" +
                "-fx-font-size: " + px(13) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 14;" +
                "-fx-padding: " + px(10) + " " + px(12) + " " + px(10) + " " + px(12) + ";";
    }

    private String compactDangerButtonStyle() {
        return "-fx-background-color: #fff1f2;" +
                "-fx-text-fill: #be123c;" +
                "-fx-font-size: " + px(13) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #fecdd3;" +
                "-fx-border-radius: 14;" +
                "-fx-padding: " + px(10) + " " + px(12) + " " + px(10) + " " + px(12) + ";";
    }

    private String modernFieldStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 14;" +
                "-fx-padding: " + px(12) + " " + px(14) + " " + px(12) + " " + px(14) + ";" +
                "-fx-font-size: " + px(14) + "px;" +
                "-fx-text-fill: " + primaryText() + ";";
    }

    private String modernComboBoxStyle() {
        return modernFieldStyle() +
                "-fx-font-size: " + px(14) + "px;" +
                "-fx-font-weight: bold;";
    }

    private String modernFilterButtonStyle() {
        return "-fx-background-color: #eef2ff;" +
                "-fx-text-fill: #3730a3;" +
                "-fx-font-size: " + px(14) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 16;" +
                "-fx-border-color: #c7d2fe;" +
                "-fx-border-radius: 16;" +
                "-fx-padding: " + px(12) + " " + px(16) + " " + px(12) + " " + px(16) + ";";
    }

    private String exerciseEditorRowStyle() {
        return "-fx-background-color: rgba(255,255,255,0.95);" +
                "-fx-background-radius: 22;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 22;";
    }

    private String alternativeOptionButtonStyle() {
        return "-fx-background-color: #ffffff;" +
                "-fx-text-fill: " + primaryText() + ";" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 18;" +
                "-fx-padding: " + px(14) + ";";
    }

    private String modernTagStyle() {
        return "-fx-background-color: #f3f4f6;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: " + px(4) + " " + px(10) + " " + px(4) + " " + px(10) + ";" +
                "-fx-text-fill: #4b5563;" +
                "-fx-font-size: " + px(11) + "px;" +
                "-fx-font-weight: bold;";
    }

    private String modernAccentTagStyle() {
        return "-fx-background-color: #eef2ff;" +
                "-fx-background-radius: 999;" +
                "-fx-padding: " + px(4) + " " + px(10) + " " + px(4) + " " + px(10) + ";" +
                "-fx-text-fill: #4338ca;" +
                "-fx-font-size: " + px(11) + "px;" +
                "-fx-font-weight: bold;";
    }

    private String checkboxStyle() {
        return "-fx-font-size: " + px(14) + "px; -fx-text-fill: " + primaryText() + ";";
    }

    private String surfaceStyle() {
        return "-fx-background-color: white;" +
                "-fx-background-radius: 18;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 18;" +
                "-fx-padding: " + px(16) + ";";
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

    private int px(double base) {
        return (int) Math.round(base * textScale);
    }

    private double scaledSpacing(double base) {
        double spacingScale = compactSpacing ? 0.85 : 1.0;
        return base * textScale * spacingScale;
    }

    private String primaryText() {
        return highContrastMode ? "#030712" : "#111827";
    }

    private String secondaryText() {
        return highContrastMode ? "#1f2937" : "#6b7280";
    }

    private String borderColor() {
        return highContrastMode ? "#94a3b8" : "#e5e7eb";
    }

    private String cardFill() {
        return highContrastMode ? "#ffffff" : "rgba(255,255,255,0.97)";
    }

    private String appBackgroundStyle() {
        return highContrastMode
                ? "-fx-background-color: linear-gradient(to bottom, #020617, #0f172a, #111827);"
                : "-fx-background-color: linear-gradient(to bottom, #0f172a, #111827, #1f2937);";
    }

    private String screenCardStyle() {
        return "-fx-background-color: " + cardFill() + ";" +
                "-fx-background-radius: 24;" +
                "-fx-border-color: " + borderColor() + ";" +
                "-fx-border-radius: 24;";
    }

    // Reusable brand tile so splash, login, and dashboard feel like one app.
    private StackPane createBrandMark(double size) {
        StackPane outer = new StackPane();
        outer.setPrefSize(size, size);
        outer.setMinSize(size, size);
        outer.setMaxSize(size, size);
        outer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0f172a, #172554, #1d4ed8);" +
                        "-fx-background-radius: " + px(26) + ";" +
                        "-fx-border-color: rgba(191,219,254,0.35);" +
                        "-fx-border-radius: " + px(26) + ";"
        );

        Circle glow = new Circle(size * 0.36);
        glow.setFill(Color.rgb(255, 255, 255, 0.12));
        glow.setTranslateY(-size * 0.12);
        glow.setTranslateX(size * 0.1);

        StackPane inner = new StackPane();
        double innerSize = size * 0.72;
        inner.setPrefSize(innerSize, innerSize);
        inner.setMaxSize(innerSize, innerSize);
        inner.setStyle(
                "-fx-background-color: rgba(248,250,252,0.12);" +
                        "-fx-background-radius: " + px(18) + ";" +
                        "-fx-border-color: rgba(255,255,255,0.14);" +
                        "-fx-border-radius: " + px(18) + ";"
        );

        ImageView logo = createLogoView(size * 0.5);
        logo.setOpacity(0.98);
        inner.getChildren().add(logo);

        outer.getChildren().addAll(glow, inner);
        return outer;
    }

    private VBox createBrandHero(String titleText, String subtitleText) {
        StackPane brandMark = createBrandMark(104);

        Label eyebrow = new Label("Strength Companion");
        eyebrow.setStyle(
                "-fx-background-color: rgba(148,163,184,0.18);" +
                        "-fx-background-radius: 999;" +
                        "-fx-padding: " + px(5) + " " + px(12) + ";" +
                        "-fx-text-fill: #dbeafe;" +
                        "-fx-font-size: " + px(11) + "px;" +
                        "-fx-font-weight: bold;"
        );

        Label title = new Label(titleText);
        title.setWrapText(true);
        title.setTextAlignment(TextAlignment.CENTER);
        title.setStyle(
                "-fx-font-size: " + px(31) + "px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );

        Label subtitle = new Label(subtitleText);
        subtitle.setWrapText(true);
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setStyle(
                "-fx-font-size: " + px(14) + "px;" +
                        "-fx-text-fill: #cbd5e1;"
        );

        VBox hero = new VBox(scaledSpacing(12), brandMark, eyebrow, title, subtitle);
        hero.setAlignment(Pos.CENTER);
        return hero;
    }

    private HBox createCompactBrandBar() {
        StackPane mark = createBrandMark(38);

        Label titleLabel = new Label("Record Breaker");
        titleLabel.setStyle("-fx-font-size: " + px(18) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");
        titleLabel.setWrapText(false);

        HBox box = new HBox(8, mark, titleLabel);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private VBox createMenuActionCard(String titleText, String descriptionText, Button actionButton) {
        Label title = new Label(titleText);
        title.setWrapText(true);
        title.setStyle("-fx-font-size: " + px(18) + "px; -fx-font-weight: bold; -fx-text-fill: " + primaryText() + ";");

        Label description = new Label(descriptionText);
        description.setWrapText(true);
        description.setStyle("-fx-font-size: " + px(13) + "px; -fx-text-fill: " + secondaryText() + ";");

        actionButton.setMaxWidth(Double.MAX_VALUE);

        VBox card = new VBox(scaledSpacing(10), title, description, actionButton);
        card.setAlignment(Pos.TOP_LEFT);
        card.setFillWidth(true);
        card.setPadding(new Insets(scaledSpacing(18)));
        card.setStyle(surfaceStyle());
        GridPane.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private ImageView createLogoView(double size) {
        Image image = new Image(getClass().getResourceAsStream("/RecordBreakerLogo128.png"));
        ImageView logo = new ImageView(image);
        logo.setFitWidth(size);
        logo.setFitHeight(size);
        logo.setPreserveRatio(true);
        return logo;
    }

    private void confirmExitWorkout(String username, Runnable onCancel) {
        showConfirmScreen(
                "Exit Workout",
                "Your saved sets will be kept, but unfinished entries on this screen will be lost.",
                "Exit and Save",
                () -> showDashboard(username),
                onCancel
        );
    }

    private String screenTitleStyle() {
        return "-fx-font-size: " + px(28) + "px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: " + primaryText() + ";";
    }

    private String screenSubtitleStyle() {
        return "-fx-font-size: " + px(14) + "px;" +
                "-fx-text-fill: " + secondaryText() + ";";
    }

    private VBox createScreenCard(double maxWidth) {
        VBox card = new VBox(scaledSpacing(14));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(Math.min(maxWidth, preferredCardWidth));
        card.setPadding(new Insets(scaledSpacing(28)));
        card.setStyle(screenCardStyle());
        return card;
    }

    private StackPane wrapScreen(Node content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;"
        );

        if (content instanceof Region) {
            ((Region) content).setMaxWidth(preferredCardWidth + scaledSpacing(80));
        }

        StackPane wrapper = new StackPane(scrollPane);
        wrapper.setStyle(appBackgroundStyle());
        wrapper.setPadding(new Insets(scaledSpacing(30)));
        return wrapper;
    }

    private VBox profileStat(String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: " + px(12) + "px; -fx-text-fill: " + secondaryText() + "; -fx-font-weight: bold;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: " + px(18) + "px; -fx-text-fill: " + primaryText() + ";");
        valueNode.setWrapText(true);

        VBox box = new VBox(2, labelNode, valueNode);
        return box;
    }
}
