package com.busbooking.gui;

public class Bus {
    private int busNumber;
    private boolean ac;
    private int capacity;

    public Bus(int busNumber, boolean ac, int capacity) {
        this.busNumber = busNumber;
        this.ac = ac;
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Bus " + busNumber + (ac ? " (AC)" : " (Non-AC)") + " | Capacity: " + capacity;
    }

    public int getBusNumber() { return busNumber; }
    public boolean isAc() { return ac; }
    public int getCapacity() { return capacity; }
}
