package com.busbooking.gui;

public class Booking {
    private Passenger passenger;
    private Bus bus;
    private int seatNumber;

    public Booking(Passenger passenger, Bus bus, int seatNumber) {
        this.passenger = passenger;
        this.bus = bus;
        this.seatNumber = seatNumber;
    }

    public String getDetails() {
        return "Passenger: " + passenger.getName() +
                " | Age: " + passenger.getAge() +
                " | Gender: " + passenger.getGender() +
                "\nBus: " + bus.toString() +
                "\nSeat Number: " + seatNumber;
    }
}