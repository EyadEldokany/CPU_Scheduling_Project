package com.example.demo;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;
import java.util.Comparator;

public class CPUSchedulerGUI extends Application {
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

        // Add Process Button
        Button addButton = new Button("Add Process");
        addButton.setOnAction(e -> {
            try {
                // Get Input Values
                String processId = processIdField.getText().trim();
                int cpuTime = Integer.parseInt(cpuTimeField.getText().trim());

                // Validate Inputs
                if (processId.isEmpty()) {
                    showAlert("Validation Error", "Process ID cannot be empty!");
                    return;
                }
                if (cpuTime <= 0) {
                    showAlert("Validation Error", "CPU Time must be greater than 0!");
                    return;
                }

                // Add Process to Table
                Process process = new Process(processId, cpuTime);
                table.getItems().add(process);

                // Clear Input Fields
                processIdField.clear();
                cpuTimeField.clear();

            } catch (NumberFormatException ex) {
                showAlert("Input Error", "CPU Time must be a valid integer!");
            }
        });

        form.getChildren().addAll(
                new Label("Add Process"),
                processIdField,
                cpuTimeField,
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

        TableColumn<Process, Integer> waitingTimeColumn = new TableColumn<>("Waiting Time");
        waitingTimeColumn.setCellValueFactory(data -> data.getValue().waitingTimeProperty().asObject());

        TableColumn<Process, Integer> turnaroundTimeColumn = new TableColumn<>("Turnaround Time");
        turnaroundTimeColumn.setCellValueFactory(data -> data.getValue().turnaroundTimeProperty().asObject());

        table.getColumns().addAll(idColumn, cpuTimeColumn, waitingTimeColumn, turnaroundTimeColumn);
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

        resetButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to reset the table?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait();

            if (alert.getResult() == ButtonType.YES) {
                table.getItems().clear();
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
/*

public class FCFSApp {

    // Class to represent a process
    static class Process {
        int id;               
        int burstTime;       
        int turnaroundTime;   
        int waitingTime;      

        // Constructor
        public Process(int id, int burstTime) {
            this.id = id;
            this.burstTime = burstTime;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // To read input from the user
        List<Process> processes = new ArrayList<>();

        // Step 1: Input the number of processes
        System.out.print("Enter the number of processes: ");
        int n = scanner.nextInt();

        // Step 2: Input burst time for each process
        for (int i = 0; i < n; i++) {
            System.out.println("Enter details for Process " + (i + 1) + ":");
            System.out.print("Burst Time: ");
            int burstTime = scanner.nextInt();
            processes.add(new Process(i + 1, burstTime)); // Add process to the list
        }

        // Step 3: Simulate FCFS scheduling
        int currentTime = 0;        // Tracks the cumulative time
        int totalWaitingTime = 0;   // Sum of waiting times for all processes
        int totalTurnaroundTime = 0; // Sum of turn around times for all processes

        for (Process process : processes) {
            // Calculate turn around time 
            currentTime += process.burstTime;
            process.turnaroundTime = currentTime;

            // Calculate waiting time
            process.waitingTime = process.turnaroundTime - process.burstTime;

            // Update totals for averages
            totalWaitingTime += process.waitingTime;
            totalTurnaroundTime += process.turnaroundTime;
        }

        // Step 4: Output results
        System.out.println("\nProcess\tBurst\tWaiting\tTurnaround");
        for (Process process : processes) {
            System.out.printf("%d\t%d\t%d\t%d\n",
                    process.id, process.burstTime,
                    process.waitingTime, process.turnaroundTime);
        }

        // Calculate and print averages
        double avgWaitingTime = (double) totalWaitingTime / n;
        double avgTurnaroundTime = (double) totalTurnaroundTime / n;

        System.out.printf("\nAverage Waiting Time: %.2f\n", avgWaitingTime);
        System.out.printf("Average Turnaround Time: %.2f\n", avgTurnaroundTime);

        scanner.close(); // Close the scanner
    }
}

*/
    public static void main(String[] args) {
        launch(args);
    }
}
