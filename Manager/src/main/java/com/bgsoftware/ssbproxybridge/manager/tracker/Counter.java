package com.bgsoftware.ssbproxybridge.manager.tracker;

public class Counter {

    private int counter = 0;

    public int get() {
        return this.counter;
    }

    public void increase() {
        ++this.counter;
    }

}
