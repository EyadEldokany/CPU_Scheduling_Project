package com.example.demo;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class CPUSchedulerGUI extends Application {
    private int processCounter = 1; // Counter for generating unique Process IDs
    private Queue<Process> readyQueue = new LinkedList<>(); // Ready Queue for processes
    public CPUSchedulerGUI() {
        // Default constructor
    }
    private TableView<Process> table;
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Initialize the table and add it to the layout
        root.setCenter(createProcessTable());

        // Add input form and buttons
        root.setLeft(createInputForm());
        root.setBottom(createAlgorithmButtons());

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("CPU Scheduling Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createInputForm() {
        VBox form = new VBox(10);
        form.setStyle("-fx-padding: 10; -fx-border-color: black;");

        // Input Fields
        TextField cpuTimeField = new TextField();
        cpuTimeField.setPromptText("CPU Time");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority");

        // Add Process Button
        Button addButton = new Button("Add Process");
        addButton.setOnAction(e -> {
            try {
                // Generate Auto-Incremented Process ID
                String processId = "P" + processCounter++;

                // Get Input Values
                int cpuTime = Integer.parseInt(cpuTimeField.getText().trim());
                int priority;

                if (cpuTime <= 0) {
                    showAlert("Validation Error", "CPU Time must be greater than 0!");
                    return;
                }

                if (priorityField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Priority cannot be empty!");
                    return;
                } else {
                    priority = Integer.parseInt(priorityField.getText().trim());
                }

                // Create Process and Add to Ready Queue and Table
                Process process = new Process(processId, cpuTime, priority);
                readyQueue.add(process); // Add to Ready Queue
                table.getItems().add(process); // Add to TableView

                // Clear Input Fields
                cpuTimeField.clear();
                priorityField.clear();

            } catch (NumberFormatException ex) {
                showAlert("Input Error", "CPU Time and Priority must be valid integers!");
            }
        });


        form.getChildren().addAll(
                new Label("Add Process"),
                cpuTimeField,
                priorityField,
                addButton
        );
        return form;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private TableView<Process> createProcessTable() {
        table = new TableView<>();

        TableColumn<Process, String> idColumn = new TableColumn<>("Process ID");
        idColumn.setCellValueFactory(data -> data.getValue().processIdProperty());

        TableColumn<Process, Integer> cpuTimeColumn = new TableColumn<>("CPU Time");
        cpuTimeColumn.setCellValueFactory(data -> data.getValue().cpuTimeProperty().asObject());

        TableColumn<Process, Integer> priorityColumn = new TableColumn<>("Priority");
        priorityColumn.setCellValueFactory(data -> data.getValue().priorityProperty().asObject());

        TableColumn<Process, Integer> waitingTimeColumn = new TableColumn<>("Waiting Time");
        waitingTimeColumn.setCellValueFactory(data -> data.getValue().waitingTimeProperty().asObject());

        TableColumn<Process, Integer> turnaroundTimeColumn = new TableColumn<>("Turnaround Time");
        turnaroundTimeColumn.setCellValueFactory(data -> data.getValue().turnaroundTimeProperty().asObject());

        table.getColumns().addAll(idColumn, cpuTimeColumn, priorityColumn, waitingTimeColumn, turnaroundTimeColumn);
        return table;
    }


    private HBox createAlgorithmButtons() {
        HBox buttons = new HBox(10);
        buttons.setStyle("-fx-padding: 10; -fx-border-color: black;");

        Button fcfsButton = new Button("Run FCFS");
        Button sjfButton = new Button("Run SJF");
        Button rrButton = new Button("Run Round Robin");
        Button priorityButton = new Button("Run Priority");
        Button resetButton = new Button("Reset Table");

        sjfButton.setOnAction(e -> {
            sjfScheduling(new LinkedList<>(readyQueue)); // Create a copy to sort and process
        });
        fcfsButton.setOnAction(e -> {
            fcfsScheduling(new LinkedList<>(readyQueue)); // Create a copy of the queue
            table.refresh(); // Update the table to show the new values
        });
        resetButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset the table?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                table.getItems().clear();
                processCounter = 1;
                readyQueue.clear(); // Clear the ready queue
            }
        });


        buttons.getChildren().addAll(fcfsButton, sjfButton, rrButton, priorityButton, resetButton);
        return buttons;
    }


    private void sjfScheduling(Queue<Process> readyQueue) {
        // Convert the Ready Queue to a List and sort it by CPU Time (ascending order)
        List<Process> sortedProcesses = new ArrayList<>(readyQueue);
        sortedProcesses.sort(Comparator.comparingInt(p -> p.cpuTimeProperty().get()));

        int currentTime = 0;

        for (Process process : sortedProcesses) {
            process.setWaitingTime(currentTime);
            currentTime += process.cpuTimeProperty().get();
            process.setTurnaroundTime(currentTime);
        }
        table.getItems().clear();
        table.getItems().addAll(sortedProcesses);
        readyQueue.clear();
    }


    /*
    private void roundRobinScheduling(ObservableList<Process> processes, int quantum) {
    int s = processes.size();
    int[] remainingBurstTime = new int[s];
    for (int i = 0; i < s; i++) {
        remainingBurstTime[i] = processes.get(i).getBurstTime();
    }

    int currentTime = 0;
    boolean done;

    do {
        done = true;
        for (int i = 0; i < s; i++) {
            if (remainingBurstTime[i] > 0) {
                done = false; //  a pending process
                if (remainingBurstTime[i] > quantum) {
                    currentTime += quantum;
                    remainingBurstTime[i] -= quantum;
                } else {
                    currentTime += remainingBurstTime[i];
                    processes.get(i).setWaitingTime(currentTime - processes.get(i).getBurstTime());
                    processes.get(i).setTurnaroundTime(currentTime);
                    remainingBurstTime[i] = 0; // Process completed
                }
            }
        }
    } while (!done); //  until all processes are completed
}


    */

    private void fcfsScheduling(Queue<Process> readyQueue) {
        int currentTime = 0;

        for (Process currentProcess : readyQueue) {
            currentProcess.setWaitingTime(currentTime);
            currentTime += currentProcess.cpuTimeProperty().get();
            currentProcess.setTurnaroundTime(currentTime);
        }
        readyQueue.clear();
    }
/*
private void fcfsScheduling(ObservableList<Process> processes) {
    int currentTime = 0;

    // Initialize all processes to the "Ready" state
    for (Process process : processes) {
        process.setState("Ready");
    }

    for (Process currentProcess : processes) {
        currentProcess.setState("Running"); // Transition to Running state
        currentProcess.setWaitingTime(currentTime); // Set waiting time
        currentTime += currentProcess.getBurstTime(); // Execute process
        currentProcess.setTurnaroundTime(currentTime); // Set turnaround time
        currentProcess.setState("Completed"); // Transition to Completed state
    }

    // Update ObservableList (if necessary for UI bindings)
    processes.clear();
    processes.addAll(processes); // Re-add updated processes
}
*/
    public static void main(String[] args) {
        launch(args);
    }
}
