package moomoo.apps.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import moomoo.apps.model.TaskModel;

public class TaskCardController {

    @FXML private Label namaTugasLabel;
    @FXML private Label deskripsiLabel;
    @FXML private Label ditugaskanKeLabel;
    @FXML private Label prioritasLabel; // Bisa juga diganti Circle atau bentuk lain
    @FXML private Circle prioritasIndicator;

    @FXML private Button detailButton;
    @FXML private Button tundaButton;
    @FXML private Button selesaikanButton;

    private TaskModel currentTask;
    private TaskManagementController taskManagementController; // Untuk callback refresh

    public void setData(TaskModel task, TaskManagementController mainController) {
        this.currentTask = task;
        this.taskManagementController = mainController;

        namaTugasLabel.setText(task.getNamaTugas());
        // === PERBAIKAN DI SINI ===
        String deskripsiFull = task.getDeskripsiTugas(); 
        deskripsiLabel.setText(deskripsiFull.length() > 70 ? deskripsiFull.substring(0, 70) + "..." : deskripsiFull);
        
        String penanggungJawab = task.getNamaKaryawanPenanggungJawab();
        ditugaskanKeLabel.setText(penanggungJawab != null && !penanggungJawab.isEmpty() ? penanggungJawab : "Belum ada");
        // =========================
        
        prioritasLabel.setText(task.getPrioritas());
        // ... (sisa kode untuk prioritasIndicator dan tombol) ...
    }

    @FXML
    private void handleDetailTugas(ActionEvent event) {
        System.out.println("Detail tugas: " + currentTask.getNamaTugas());
        // TODO: Implementasi buka detail tugas (bisa modal baru atau panel)
    }

    @FXML
    private void handleTundaTugas(ActionEvent event) {
        System.out.println("Tunda tugas: " + currentTask.getNamaTugas());
        // TODO: Update status tugas ke "Ditunda" atau logika lain
        // currentTask.setStatus("Ditunda");
        // taskManagementController.refreshKanbanBoard();
    }

    @FXML
    private void handleSelesaikanTugas(ActionEvent event) {
        System.out.println("Selesaikan/Update tugas: " + currentTask.getNamaTugas());
        if ("Akan Dilakukan".equals(currentTask.getStatus())) {
            currentTask.setStatus("Sedang Dikerjakan");
        } else if ("Sedang Dikerjakan".equals(currentTask.getStatus())) {
            currentTask.setStatus("Selesai");
        }
        // Update di database/model utama
        // ...
        if (taskManagementController != null) {
            taskManagementController.refreshKanbanBoard();
        }
    }
}