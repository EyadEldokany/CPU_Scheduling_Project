package com.example.demo;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.Comparator;

public class CPUSchedulerGUI extends Application {
    private int processCounter = 1; // Counter for generating unique Process IDs
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
        TextField processIdField = new TextField();
        processIdField.setPromptText("Process ID");
        TextField cpuTimeField = new TextField();
        cpuTimeField.setPromptText("CPU Time");
        TextField priorityField = new TextField();
        priorityField.setPromptText("Priority");

        // Add Process Button
        Button addButton = new Button("Add Process");
        addButton.setOnAction(e -> {
            try {
                // Get Input Values
                String processId = processIdField.getText().trim();
                int cpuTime = Integer.parseInt(cpuTimeField.getText().trim());
                int priority = Integer.parseInt(priorityField.getText().trim());
                // Validate Inputs
                if (processId.isEmpty()) {
                    showAlert("Validation Error", "Process ID cannot be empty!");
                    return;
                }
                if (cpuTime <= 0) {
                    showAlert("Validation Error", "CPU Time must be greater than 0!");
                    return;
                }
                if (priority <= 0) {
                    showAlert("Validation Error", "Priority must be greater than 0!");
                    return;
                }

                // Add Process to Table
                Process process = new Process(processId, cpuTime, priority);
                table.getItems().add(process);

                // Clear Input Fields
                processIdField.clear();
                cpuTimeField.clear();
                priorityField.clear();

            } catch (NumberFormatException ex) {
                showAlert("Input Error", "All numeric fields must contain valid integers!");
            }
        });

        form.getChildren().addAll(
                new Label("Add Process"),
                processIdField,
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
            sjfScheduling(table.getItems());
            table.refresh();
        });
        fcfsButton.setOnAction(e -> {
            fcfsScheduling(table.getItems());
            table.refresh();
        });
        rrButton.setOnAction(e -> {
            // Create a TextInputDialog to ask the user for quantum time
            TextInputDialog quantumDialog = new TextInputDialog();
            quantumDialog.setTitle("Round Robin Scheduling");
            quantumDialog.setHeaderText("Enter Quantum Time");
            quantumDialog.setContentText("Quantum Time:");

            // Show the dialog and get the user input
            quantumDialog.showAndWait().ifPresent(input -> {
                try {
                    // Parse the input as an integer
                    int quantum = Integer.parseInt(input.trim());

                    // Validate the quantum value
                    if (quantum <= 0) {
                        showAlert("Validation Error", "Quantum Time must be greater than 0!");
                        return;
                    }

                    // Call Round Robin scheduling with the quantum value
                    roundRobinScheduling(table.getItems(), quantum);
                    table.refresh(); // Refresh the table to display the updated values

                } catch (NumberFormatException ex) {
                    showAlert("Input Error", "Quantum Time must be a valid integer!");
                }
            });
        });


        priorityButton.setOnAction(e -> {
            priorityScheduling(table.getItems());
            table.refresh();
        });
        
        resetButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset the table?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                table.getItems().clear();
                processCounter = 1;
            }
        });


        buttons.getChildren().addAll(fcfsButton, sjfButton, rrButton, priorityButton, resetButton);
        return buttons;
    }


    private void sjfScheduling(ObservableList<Process> processes) {
        // Sort processes by CPU time
        processes.sort(Comparator.comparingInt(p -> p.cpuTimeProperty().get()));

        int currentTime = 0;

        // Calculate waiting and turnaround times
        for (Process process : processes) {
            process.setWaitingTime(currentTime);
            currentTime += process.cpuTimeProperty().get();
            process.setTurnaroundTime(currentTime);
        }
    }
    
    private void roundRobinScheduling(ObservableList<Process> processes, int quantum) {
    int s = processes.size();
    int[] remainingBurstTime = new int[s];
    for (int i = 0; i < s; i++) {
        remainingBurstTime[i] = processes.get(i).cpuTimeProperty().get();
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
                    processes.get(i).setWaitingTime(currentTime - processes.get(i).cpuTimeProperty().get());
                    processes.get(i).setTurnaroundTime(currentTime);
                    remainingBurstTime[i] = 0; // Process completed
                }
            }
        }
    } while (!done); //  until all processes are completed
}


    

    private void fcfsScheduling(ObservableList<Process> processes) {
        int currentTime = 0;

        for (Process currentProcess : processes) {
            currentTime += currentProcess.cpuTimeProperty().get();
            currentProcess.setWaitingTime(currentTime - currentProcess.cpuTimeProperty().get());
            currentProcess.setTurnaroundTime(currentTime);
        }
    }

    
   private void priorityScheduling(ObservableList<Process> processes) {
        // Sort processes by priority (higher priority first)
        processes.sort((p1, p2) -> {
            if (p1.priorityProperty().get() == p2.priorityProperty().get()) {
                return p2.cpuTimeProperty().get() - p1.cpuTimeProperty().get(); // FCFS for same priority
            }
            return p1.priorityProperty().get() - p2.priorityProperty().get(); // Higher priority first
        });

        int currentTime = 0;

        // Calculate waiting and turnaround times
        for (Process prt : processes) {
            prt.setWaitingTime(currentTime);
            currentTime += prt.cpuTimeProperty().get();
            prt.setTurnaroundTime(currentTime);
        }
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}
