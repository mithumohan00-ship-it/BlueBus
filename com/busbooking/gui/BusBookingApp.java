package com.busbooking.gui;

import com.google.zxing.BarcodeFormat;
import java.awt.Desktop;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BusBookingApp extends Application {

    private List<Bus> buses = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // Sample buses (2+2 layout)
        buses.add(new Bus(1, true, 40)); // id, ac, capacity
        buses.add(new Bus(2, false, 30));

        // Header
        Label titleLabel = new Label("🚌 BlueBus Ticket Booking App");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox topBox = new HBox(titleLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(12));
        topBox.setStyle("-fx-background-color: #0b63ce;");

        // Form controls
        Label nameLabel = new Label("Passenger Name:");
        TextField nameField = new TextField();

        Label ageLabel = new Label("Age:");
        TextField ageField = new TextField();

        Label genderLabel = new Label("Gender:");
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRadio = new RadioButton("Male"); maleRadio.setToggleGroup(genderGroup);
        RadioButton femaleRadio = new RadioButton("Female"); femaleRadio.setToggleGroup(genderGroup);
        RadioButton otherRadio = new RadioButton("Other"); otherRadio.setToggleGroup(genderGroup);
        HBox genderBox = new HBox(8, maleRadio, femaleRadio, otherRadio);
        genderBox.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("Travel Date:");
        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        Label busLabel = new Label("Select Bus:");
        ComboBox<Bus> busComboBox = new ComboBox<>();
        busComboBox.getItems().addAll(buses);

        Label departureLabel = new Label("Departure City:");
        ComboBox<String> departureCombo = new ComboBox<>();
        departureCombo.getItems().addAll("Bengaluru", "Chennai", "Hyderabad", "Mumbai", "Delhi");

        Label boardingLabel = new Label("Boarding City:");
        ComboBox<String> boardingCombo = new ComboBox<>();
        boardingCombo.getItems().addAll("Bengaluru", "Chennai", "Hyderabad", "Mumbai", "Delhi");

        VBox formBox = new VBox(8,
                nameLabel, nameField,
                ageLabel, ageField,
                genderLabel, genderBox,
                dateLabel, datePicker,
                busLabel, busComboBox,
                departureLabel, departureCombo,
                boardingLabel, boardingCombo
        );
        formBox.setPadding(new Insets(15));
        formBox.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-border-color: #bdc3c7; -fx-border-radius: 6; -fx-border-width: 1;");
        formBox.setMaxWidth(320);

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setLeft(formBox);
        root.setStyle("-fx-background-color: linear-gradient(#e6f2ff, #cfe9ff);");

        VBox seatContainer = new VBox(10);
        seatContainer.setPadding(new Insets(15));
        ScrollPane seatScroll = new ScrollPane(seatContainer);
        seatScroll.setFitToWidth(true);
        seatScroll.setPrefViewportHeight(500);
        root.setCenter(seatScroll);

        // Button to show booked tickets (optional)
        Button showBookingsBtn = new Button("View Bookings (CSV)");
        showBookingsBtn.setOnAction(ev -> {
            // open bookings.csv with system default or show path
            try {
                Path p = Paths.get("bookings.csv");
                if (Files.exists(p)) {
                    Desktop.getDesktop().open(p.toFile());
                } else {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "No bookings saved yet.");
                    a.showAndWait();
                }
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Cannot open bookings.csv: " + ex.getMessage()).showAndWait();
            }
        });
        root.setBottom(showBookingsBtn);
        BorderPane.setAlignment(showBookingsBtn, Pos.CENTER);
        BorderPane.setMargin(showBookingsBtn, new Insets(8));

        // When a bus is selected, build realistic seat layout (2+2)
        busComboBox.setOnAction(e -> {
            seatContainer.getChildren().clear();
            Bus selectedBus = busComboBox.getValue();
            if (selectedBus == null) return;

            GridPane seatLayout = new GridPane();
            seatLayout.setHgap(10);
            seatLayout.setVgap(8);
            seatLayout.setPadding(new Insets(10));

            Label seatCounter = new Label("Seats left: " + selectedBus.getCapacity() + "/" + selectedBus.getCapacity());
            int[] seatsLeft = { selectedBus.getCapacity() };

            int seatsPerRow = 4;
            int rows = (selectedBus.getCapacity() + seatsPerRow - 1) / seatsPerRow;
            int seatNumber = 1;
            for (int r = 0; r < rows; r++) {
                // left two seats
                for (int c = 0; c < 2; c++) {
                    if (seatNumber <= selectedBus.getCapacity()) {
                        Button seatButton = createSeatButton(seatNumber);
                        final int sNum = seatNumber;
                        seatButton.setOnAction(ev -> handleSeatBookingWithQR(seatButton, sNum, nameField, ageField, genderGroup, datePicker,
                                selectedBus, departureCombo, boardingCombo, seatCounter, seatsLeft));
                        seatLayout.add(seatButton, c, r);
                        seatNumber++;
                    }
                }
                // aisle spacer
                Region aisle = new Region();
                aisle.setPrefWidth(20);
                seatLayout.add(aisle, 2, r);

                // right two
                for (int c = 0; c < 2; c++) {
                    if (seatNumber <= selectedBus.getCapacity()) {
                        Button seatButton = createSeatButton(seatNumber);
                        final int sNum = seatNumber;
                        seatButton.setOnAction(ev -> handleSeatBookingWithQR(seatButton, sNum, nameField, ageField, genderGroup, datePicker,
                                selectedBus, departureCombo, boardingCombo, seatCounter, seatsLeft));
                        seatLayout.add(seatButton, 3 + c, r);
                        seatNumber++;
                    }
                }
            }

            seatContainer.getChildren().addAll(seatCounter, seatLayout);
        });

        Scene scene = new Scene(root, 980, 650);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bus Ticket Booking System");
        primaryStage.show();
    }

    /* ----------------------
       Booking handler with QR generation + CSV persistence
       ---------------------- */
    private void handleSeatBookingWithQR(Button seatButton, int seatNumber,
                                         TextField nameField, TextField ageField, ToggleGroup genderGroup, DatePicker datePicker,
                                         Bus selectedBus, ComboBox<String> departureCombo, ComboBox<String> boardingCombo,
                                         Label seatCounter, int[] seatsLeft) {
        try {
            String name = nameField.getText();
            String ageText = ageField.getText();
            Toggle selected = genderGroup.getSelectedToggle();
            LocalDate travelDate = datePicker.getValue();

            // Validation
            if (name == null || name.isBlank() || ageText == null || ageText.isBlank() ||
                    selected == null || travelDate == null ||
                    departureCombo.getValue() == null || boardingCombo.getValue() == null) {
                new Alert(Alert.AlertType.ERROR, "Please fill all required fields (name, age, gender, date, departure, boarding).").showAndWait();
                return;
            }

            int age = Integer.parseInt(ageText);
            String gender = ((RadioButton) selected).getText();
            String departure = departureCombo.getValue();
            String boarding = boardingCombo.getValue();
            double price = calculatePrice(departure, boarding);

            // Create unique ticketId (privacy: QR only contains ticketId)
            String ticketId = "TICKET-" + System.currentTimeMillis() + "-" + (new Random().nextInt(9000) + 1000);

            // Persist booking details to CSV (maps ticketId -> details)
            saveBookingToCSV(ticketId, name, age, gender, selectedBus.getBusNumber(), seatNumber, travelDate, departure, boarding, price);

            // Create booking object (existing class usage)
            Passenger passenger = new Passenger(name, age, gender);
            Booking booking = new Booking(passenger, selectedBus, seatNumber); // reuse existing Booking

            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ticket Confirmation");
            alert.setHeaderText("Booking Successful!");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
            alert.setContentText(booking.getDetails() +
                    "\nTravel Date: " + travelDate.format(fmt) +
                    "\nDeparture: " + departure +
                    "\nBoarding: " + boarding +
                    "\nPrice: ₹" + price +
                    "\nTicket ID: " + ticketId);
            alert.showAndWait();

            // Generate QR image containing only ticketId
            Image qrImage = generateQRCodeImage(ticketId, 300, 300);
            if (qrImage != null) {
                showQRWindow(qrImage, ticketId);
            }

            // Animations & disable seat
            FadeTransition fade = new FadeTransition(Duration.seconds(0.7), seatButton);
            fade.setFromValue(1.0);
            fade.setToValue(0.5);
            fade.setOnFinished(ae -> {
                seatButton.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-border-radius: 6; -fx-background-radius: 6;");
                seatButton.setDisable(true);
            });
            fade.play();

            ScaleTransition scale = new ScaleTransition(Duration.seconds(0.25), seatButton);
            scale.setFromX(1.0);
            scale.setFromY(1.0);
            scale.setToX(1.12);
            scale.setToY(1.12);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();

            seatsLeft[0]--;
            seatCounter.setText("Seats left: " + seatsLeft[0] + "/" + selectedBus.getCapacity());

        } catch (NumberFormatException nfe) {
            new Alert(Alert.AlertType.ERROR, "Age must be a number.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Booking failed: " + ex.getMessage()).showAndWait();
            ex.printStackTrace();
        }
    }

    // Save booking details to bookings.csv
    private void saveBookingToCSV(String ticketId, String name, int age, String gender,
                                  int busNumber, int seatNumber, LocalDate travelDate,
                                  String departure, String boarding, double price) {
        try {
            String line = String.join(",",
                    ticketId,
                    name.replace(",", " "),
                    String.valueOf(age),
                    gender,
                    String.valueOf(busNumber),
                    String.valueOf(seatNumber),
                    travelDate.toString(),
                    departure,
                    boarding,
                    String.valueOf(price)
            ) + System.lineSeparator();

            Path path = Paths.get("bookings.csv");
            Files.write(path, line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Generate QR code image using ZXing and return as JavaFX Image
    private Image generateQRCodeImage(String data, int width, int height) {
        try {
            QRCodeWriter qrWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            return SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Show QR in a small popup and allow saving as PNG
    private void showQRWindow(Image qrImage, String ticketId) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Your Ticket QR — " + ticketId);

        ImageView view = new ImageView(qrImage);
        view.setFitWidth(300);
        view.setFitHeight(300);
        view.setPreserveRatio(true);

        Button saveBtn = new Button("Save QR as PNG");
        saveBtn.setOnAction(ev -> {
            FileChooser fc = new FileChooser();
            fc.setInitialFileName(ticketId + ".png");
            File f = fc.showSaveDialog(stage);
            if (f != null) {
                try {
                    WritableImage wi = view.snapshot(new SnapshotParameters(), null);
                    BufferedImage b = SwingFXUtils.fromFXImage(wi, null);
                    ImageIO.write(b, "png", f);
                } catch (IOException ex) {
                    new Alert(Alert.AlertType.ERROR, "Error saving file: " + ex.getMessage()).showAndWait();
                }
            }
        });

        VBox box = new VBox(12, view, saveBtn);
        box.setPadding(new Insets(12));
        box.setAlignment(Pos.CENTER);

        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // Helper to create seat buttons
    private Button createSeatButton(int seatNumber) {
        Button seatButton = new Button(String.valueOf(seatNumber));
        seatButton.setPrefWidth(60);
        seatButton.setPrefHeight(40);
        seatButton.setStyle("-fx-background-color: #8dd36f; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold;");
        seatButton.setOnMouseEntered(ev -> seatButton.setStyle("-fx-background-color: #ffd54f; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold;"));
        seatButton.setOnMouseExited(ev -> {
            if (!seatButton.isDisabled()) {
                seatButton.setStyle("-fx-background-color: #8dd36f; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-weight: bold;");
            }
        });
        return seatButton;
    }

    // Price calculation method (same as before)
    private double calculatePrice(String departure, String boarding) {
        if (departure == null || boarding == null) return 0.0;
        if (departure.equals("Bengaluru") && boarding.equals("Chennai")) return 500;
        if (departure.equals("Bengaluru") && boarding.equals("Hyderabad")) return 450;
        if (departure.equals("Mumbai") && boarding.equals("Delhi")) return 800;
        return 600; // default
    }

    public static void main(String[] args) {
        launch(args);
    }
}
