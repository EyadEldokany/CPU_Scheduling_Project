package com.example.demo;

import javafx.beans.property.*;

public class Process {
    private StringProperty processId;
    private IntegerProperty cpuTime;
    private IntegerProperty waitingTime;
    private IntegerProperty turnaroundTime;
    /* private IntegerProperty  priority;*/ 

    public Process(String processId, int cpuTime) {
        this.processId = new SimpleStringProperty(processId);
        this.cpuTime = new SimpleIntegerProperty(cpuTime);
        this.waitingTime = new SimpleIntegerProperty(0);
        this.turnaroundTime = new SimpleIntegerProperty(0);
      /*  this.priority = new SimpleStringProperty(priority); */
    }

    public StringProperty processIdProperty() {
        return processId;
    }

    public IntegerProperty cpuTimeProperty() {
        return cpuTime;
    }

    public IntegerProperty waitingTimeProperty() {
        return waitingTime;
    }

    public IntegerProperty turnaroundTimeProperty() {
        return turnaroundTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime.set(waitingTime);
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime.set(turnaroundTime);
    }
    /*public IntegerProperty PriorityProperty() {
        return priority;
    } */
}
