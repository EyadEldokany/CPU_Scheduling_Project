package com.example.demo;

import javafx.animation.Animation;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;  //   isn't needed
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class CPUSchedulerGUI extends Application {
    private int processCounter = 1; // Counter for generating unique Process IDs
    private Queue<Process> readyQueue = new LinkedList<>(); // Ready Queue for processes
    //     private ObservableList<Process> processList = FXCollections.observableArrayList();   isn't needed because you don't use processlist
    private Timeline timeline;
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
                // Generate Auto-Incremented Process ID
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


                // Create Process and Add to Ready Queue and Table
                Process process = new Process(processId,  cpuTime,  priority);
                readyQueue.add(process); // Add to Ready Queue
                table.getItems().add(process); // Add to TableView

                // Clear Input Fields
                processIdField.clear();
                cpuTimeField.clear();
                priorityField.clear();


            } catch (NumberFormatException ex) {
                showAlert("Input Error", "CPU Time and Priority must be valid integers!");
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
        TableColumn<Process, String> statusColumn = new TableColumn<>("Status");
        // statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));  this  is not correct
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        table.getColumns().addAll(idColumn, cpuTimeColumn, priorityColumn, waitingTimeColumn, turnaroundTimeColumn,statusColumn);
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
            fcfsScheduling(table.getItems()); // Create a copy of the queue
            // table.refresh(); // Update the table to show the new values
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
                    rrScheduling(table.getItems(), quantum);
                    table.refresh(); // Refresh the table to display the updated values

                } catch (NumberFormatException ex) {
                    showAlert("Input Error", "Quantum Time must be a valid integer!");
                }
            });
        });

        priorityButton.setOnAction(e -> {
            priorityScheduling(new LinkedList<>(readyQueue));
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

        // Set all processes to "Ready" initially
        for (Process process : sortedProcesses) {
            process.setStatus("Ready");
        }

        // Update the UI table
        table.getItems().clear();
        table.getItems().addAll(sortedProcesses);

        readyQueue.clear(); // Clear the Ready Queue

        // Create a Timeline to process jobs one by one
        Timeline timeline = new Timeline();

        // Simulation variables
        final int[] currentTime = {0}; // Track the current time
        final int[] index = {0}; // Track the current process index

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(sortedProcesses.get(index[0]).cpuTimeProperty().get() / 10.0), event -> {
            // Check if all processes are completed
            if (index[0] >= sortedProcesses.size()) {
                timeline.stop();
                return;
            }

            // Process the current job
            Process currentProcess = sortedProcesses.get(index[0]);

            if ("Ready".equals(currentProcess.getStatus())) {
                currentProcess.setStatus("Running");
                table.refresh(); // Update the UI
            } else {
                currentProcess.setWaitingTime(currentTime[0]);
                currentTime[0] += currentProcess.cpuTimeProperty().get();
                currentProcess.setTurnaroundTime(currentTime[0]);
                currentProcess.setStatus("Completed");
                table.refresh(); // Update the UI
                index[0]++; // Move to the next process

                // Adjust the duration for the next process
                if (index[0] < sortedProcesses.size()) {
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(sortedProcesses.get(index[0]).cpuTimeProperty().get() / 10.0)));
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE); // Run indefinitely until stopped
        timeline.play();
    }

    private   int  currentTime = 0;
    private void rrScheduling(ObservableList<Process> processes, int quantum) {
        int n = processes.size();
        int[] remainingBurstTime = new int[n];
        Queue<Process> tempQueue = new LinkedList<>(readyQueue); // Create a temporary queue

        // Initialize remaining burst times and set initial statuses
        for (int i = 0; i < n; i++) {
            remainingBurstTime[i] = processes.get(i).cpuTimeProperty().get();
            processes.get(i).setStatus("Ready");
        }



        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (!tempQueue.isEmpty()) { // While there are processes in the temporary queue
                Process currentProcess = tempQueue.poll(); // Get the next process from the queue

                if (currentProcess != null) {
                    currentProcess.setStatus("Running"); // Update status to Running
                    int burstTime = remainingBurstTime[processes.indexOf(currentProcess)];

                    if (burstTime > quantum) {
                        currentTime += quantum; // Increment current time by quantum
                        remainingBurstTime[processes.indexOf(currentProcess)] -= quantum; // Decrease remaining burst time
                        tempQueue.add(currentProcess); // Re-add process to the queue for next round
                    } else {
                        currentTime += burstTime; // Increment time by remaining burst time
                        currentProcess.setWaitingTime(currentTime - currentProcess.cpuTimeProperty().get()); // Set waiting time
                        currentProcess.setTurnaroundTime(currentTime); // Set turnaround time
                        remainingBurstTime[processes.indexOf(currentProcess)] = 0; // Mark as completed
                        currentProcess.setStatus("Completed"); // Update status to Completed
                    }

                    System.out.println(currentProcess.toString()); // Print process details after execution

                    // Update the ObservableList directly to reflect changes in GUI
                    processes.set(processes.indexOf(currentProcess), currentProcess);
                }
            } else {
                timeline.stop(); // Stop the timeline when all processes are completed
            }

            // Update statuses of other processes in the queue
            for (Process p : processes) {
                if (remainingBurstTime[processes.indexOf(p)] > 0 && !tempQueue.contains(p)) {
                    p.setStatus("Ready"); // Update status back to Ready if not in queue
                }
            }

            table.refresh(); // Refresh the table to display updated values

        }));

        timeline.setCycleCount(Timeline.INDEFINITE); // Run indefinitely until stopped
        timeline.play(); // Start the timeline
    }


    public void fcfsScheduling(ObservableList<Process> processes) {
        // Initialize all processes to the "Ready" state
        for (Process process : processes) {
            process.setStatus("Ready");
        }

        Timeline timeline = new Timeline();
        int[] currentTime = {0}; // Using an array to allow modification inside the lambda

        for (Process currentProcess : processes) {
            // Add a KeyFrame for each process
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(currentTime[0]/10), event -> {
                currentProcess.setStatus("Running"); // Transition to Running state
                currentProcess.setWaitingTime(currentTime[0]); // Set waiting time
            }));

            // Add a KeyFrame for process completion
            int executionTime = currentProcess.cpuTimeProperty().get();
            timeline.getKeyFrames().add(new KeyFrame(Duration.seconds((currentTime[0] + executionTime) /10), event -> {
                currentTime[0] += executionTime; // Update current time
                currentProcess.setTurnaroundTime(currentTime[0]); // Set turnaround time
                currentProcess.setStatus("Completed"); // Transition to Completed state
            }));

            currentTime[0] += executionTime; // Increment for the next process
        }

        timeline.setCycleCount(1); // Run the timeline once
        timeline.play(); // Start the timeline
    }
    private void priorityScheduling(Queue<Process> readyQueue) {
        List<Process> sortedProcesses = new ArrayList<>(readyQueue);
        sortedProcesses.sort(Comparator.comparingInt(p -> p.priorityProperty().get()));

        // Set all processes to "Ready" initially
        for (Process process : sortedProcesses) {
            process.setStatus("Ready");
        }

        // Update the UI table
        table.getItems().clear();
        table.getItems().addAll(sortedProcesses);

        readyQueue.clear(); // Clear the Ready Queue

        // Create a Timeline to process jobs one by one
        Timeline timeline = new Timeline();
        final int[] currentTime = {0}; // Track the current time
        final int[] index = {0}; // Track the current process index

        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(sortedProcesses.get(index[0]).cpuTimeProperty().get() / 10.0), event -> {
            if (index[0] >= sortedProcesses.size()) {
                timeline.stop();
                return;
            }

            Process currentProcess = sortedProcesses.get(index[0]);

            if ("Ready".equals(currentProcess.getStatus())) {
                currentProcess.setStatus("Running");
                table.refresh(); // Update the UI
            } else if ("Running".equals(currentProcess.getStatus())) {
                // Simulate process execution
                currentTime[0] += currentProcess.cpuTimeProperty().get();
                currentProcess.setWaitingTime(currentTime[0] - currentProcess.cpuTimeProperty().get());
                currentProcess.setTurnaroundTime(currentTime[0]);
                currentProcess.setStatus("Completed");
                table.refresh(); // Update the UI
                index[0]++; // Move to the next process

                // Adjust the duration for the next process
                if (index[0] < sortedProcesses.size()) {
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(sortedProcesses.get(index[0]).cpuTimeProperty().get() / 10.0)));
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE); // Run indefinitely until stopped
        timeline.play();
    }



    public static void main(String[] args) {
        launch(args);
    }
}
