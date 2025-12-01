package com.busbooking.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BusBookingApp extends Application {

    public List<Bus> buses = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        // Sample buses
        buses.add(new Bus(1, true, 40));
        buses.add(new Bus(2, false, 30));

        // Title
        Label titleLabel = new Label("🚌 Bus Ticket Booking System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Passenger form
        Label nameLabel = new Label("Passenger Name:");
        TextField nameField = new TextField();

        Label ageLabel = new Label("Age:");
        TextField ageField = new TextField();

        Label genderLabel = new Label("Gender:");
        TextField genderField = new TextField();

        Label busLabel = new Label("Select Bus:");
        ComboBox<Bus> busComboBox = new ComboBox<>();
        busComboBox.getItems().addAll(buses);

        VBox formBox = new VBox(10, nameLabel, nameField, ageLabel, ageField,
                genderLabel, genderField, busLabel, busComboBox);
        formBox.setPadding(new Insets(10));
        formBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-border-width: 1;");

        BorderPane root = new BorderPane();
        root.setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(10));
        root.setCenter(formBox);

        // Seat layout container
        VBox seatContainer = new VBox(10);
        seatContainer.setPadding(new Insets(10));
        root.setBottom(seatContainer);

        // Show seats when bus selected
        busComboBox.setOnAction(e -> {
            seatContainer.getChildren().clear();
            Bus selectedBus = busComboBox.getValue();
            if (selectedBus != null) {
                GridPane seatLayout = new GridPane();
                seatLayout.setHgap(5);
                seatLayout.setVgap(5);

                for (int i = 1; i <= selectedBus.getCapacity(); i++) {
                    Button seatButton = new Button(String.valueOf(i));
                    seatButton.setStyle("-fx-background-color: lightgreen; -fx-min-width: 50px;");

                    int seatNumber = i;
                    seatButton.setOnAction(ev -> {
                        try {
                            String name = nameField.getText();
                            int age = Integer.parseInt(ageField.getText());
                            String gender = genderField.getText();

                            Passenger passenger = new Passenger(name, age, gender);
                            Booking booking = new Booking(passenger, selectedBus, seatNumber);

                            // Popup confirmation
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Ticket Confirmation");
                            alert.setHeaderText("Booking Successful!");
                            alert.setContentText(booking.getDetails());
                            alert.showAndWait();

                            seatButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
                            seatButton.setDisable(true);
                        } catch (Exception ex) {
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("Error");
                            error.setHeaderText("Booking Failed");
                            error.setContentText(ex.getMessage());
                            error.showAndWait();
                        }
                    });

                    seatLayout.add(seatButton, (i - 1) % 5, (i - 1) / 5); // 5 seats per row
                }

                seatContainer.getChildren().add(seatLayout);
            }
        });

        Scene scene = new Scene(root, 700, 600);
        primaryStage.setTitle("Bus Ticket Booking System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}