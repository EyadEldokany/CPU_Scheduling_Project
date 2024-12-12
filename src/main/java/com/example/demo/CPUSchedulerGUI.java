package com.example.demo;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;  //   isnt not needed
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
//     private ObservableList<Process> processList = FXCollections.observableArrayList();   isnt needed because you dont uses processlist
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
       // statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));  this  isnot correct
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
          //  fcfsScheduling(new LinkedList<>(readyQueue)); // Create a copy of the queue
         //   table.refresh(); // Update the table to show the new values
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

       /* priorityButton.setOnAction(e -> {
            priorityScheduling(table.getItems());
            table.refresh();
        });*/





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



    /*   private void fcfsScheduling(Queue<Process> readyQueue) {
           int currentTime = 0;

           for (Process currentProcess : readyQueue) {
               currentProcess.setWaitingTime(currentTime);
               currentTime += currentProcess.cpuTimeProperty().get();
               currentProcess.setTurnaroundTime(currentTime);
           }
           readyQueue.clear();
       }
   */
    private void fcfsScheduling(ObservableList<Process> processes) {

        int currentTime = 0;

        // Initialize all processes to the "Ready" state
        for (Process process : processes) {
            process.setStatus("Ready");
        }

        for (Process currentProcess : processes) {
            currentProcess.setStatus("Running"); // Transition to Running state
            currentProcess.setWaitingTime(currentTime); // Set waiting time
            currentTime += currentProcess.cpuTimeProperty().get(); // Execute process
            currentProcess.setTurnaroundTime(currentTime); // Set turnaround time
            currentProcess.setStatus("Completed"); // Transition to Completed state
        }

        // Update ObservableList (if necessary for UI bindings)
        processes.clear();
        processes.addAll(processes); // Re-add updated processes
    }

    public static void main(String[] args) {
        launch(args);
    }
}
