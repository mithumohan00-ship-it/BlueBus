package com.busbooking.gui;

public class Bus {
    private int id;
    private boolean ac;
    private int capacity;

    public Bus(int id, boolean ac, int capacity) {
        this.id = id;
        this.ac = ac;
        this.capacity = capacity;
    }

    public int getId() { return id; }
    public boolean isAc() { return ac; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        // How the ComboBox will display each Bus
        return "Bus " + id + (ac ? " (AC)" : " (Non-AC)") + " - seats: " + capacity;
    }
}