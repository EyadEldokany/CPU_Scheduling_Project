package com.example.demo;

import javafx.beans.property.*;

public class Process {
    private StringProperty processId;
    private IntegerProperty cpuTime;
    private IntegerProperty waitingTime;
    private IntegerProperty turnaroundTime;
    private IntegerProperty  priority;
    private IntegerProperty  remainingBurstTime;
    private SimpleStringProperty status;
    
    public Process(String processId, int cpuTime , int priority) {
        this.processId = new SimpleStringProperty(processId);
        this.cpuTime = new SimpleIntegerProperty(cpuTime);
        this.waitingTime = new SimpleIntegerProperty(0);
        this.turnaroundTime = new SimpleIntegerProperty(0);
        this.priority = new SimpleIntegerProperty(priority);
     this.remainingBurstTime = new SimpleIntegerProperty(cpuTime);
        this.status = new SimpleStringProperty("New");
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
    public IntegerProperty priorityProperty(){
        return priority;
    }
     public IntegerProperty remainingBurstTimeProperty() {
        return remainingBurstTime;
    }
     public String getStatus() {
        return status;
    }
    
    public void setWaitingTime(int waitingTime) {
        this.waitingTime.set(waitingTime);
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime.set(turnaroundTime);
    }
     public void setStatus(String status) {
        this.status.set(status);

}
