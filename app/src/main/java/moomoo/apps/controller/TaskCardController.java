package moomoo.apps.controller;

import java.time.LocalDate;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import moomoo.apps.model.TaskModel;
import moomoo.apps.utils.DatabaseManager;
import javafx.scene.layout.VBox; 

public class TaskCardController {

    @FXML private Label namaTugasLabel;
    @FXML private Label deskripsiLabel;
    @FXML private Label ditugaskanKeLabel;
    @FXML private Label prioritasLabel;
    @FXML private Circle prioritasIndicator; 

    @FXML private Button detailButton;
    @FXML private Button tundaButton;
    @FXML private Button selesaikanButton;

    @FXML private VBox rootTaskCardVBox;

    private TaskModel currentTask;
    private HRManagementController taskManagementController;

    public void setData(TaskModel task, HRManagementController mainController) {
        this.currentTask = task;
        this.taskManagementController = mainController;

        namaTugasLabel.setText(task.getNamaTugas());
        String deskripsiFull = task.getDeskripsiTugas();
        deskripsiLabel.setText(deskripsiFull.length() > 60 ? deskripsiFull.substring(0, 60) + "..." : deskripsiFull); 

        String penanggungJawab = task.getNamaKaryawanPenanggungJawab();
        ditugaskanKeLabel.setText(penanggungJawab != null && !penanggungJawab.isEmpty() ? penanggungJawab : "Belum Ditugaskan");

        setPrioritasVisual(task.getPrioritas());
        setStatusVisual(task.getStatus());
    }

    private void setPrioritasVisual(String prioritas) {
        prioritasLabel.setText(prioritas);
        prioritasLabel.getStyleClass().removeAll("prioritas-tinggi", "prioritas-normal", "prioritas-rendah", "prioritas-default");

        switch (prioritas.toLowerCase()) {
            case "tinggi":
                prioritasIndicator.setFill(Color.web("#EF5350")); 
                prioritasLabel.getStyleClass().add("prioritas-tinggi");
                break;
            case "normal":
                prioritasIndicator.setFill(Color.web("#FFB74D")); 
                prioritasLabel.getStyleClass().add("prioritas-normal");
                break;
            case "rendah":
                prioritasIndicator.setFill(Color.web("#81C784")); 
                prioritasLabel.getStyleClass().add("prioritas-rendah");
                break;
            default:
                prioritasIndicator.setFill(Color.LIGHTGRAY);
                prioritasLabel.getStyleClass().add("prioritas-default");
                break;
        }
    }

    private void setStatusVisual(String status) {
        if (rootTaskCardVBox != null) {
            rootTaskCardVBox.getStyleClass().removeAll("status-akan-dilakukan", "status-sedang-dikerjakan", "status-selesai");
        }
        
        selesaikanButton.setVisible(true);
        selesaikanButton.setManaged(true);
        tundaButton.setVisible(true);
        tundaButton.setManaged(true);

        switch (status) {
            case "Belum Dimulai":
            case "Akan Dilakukan":
                selesaikanButton.setText("Mulai"); 
                if (rootTaskCardVBox != null) rootTaskCardVBox.getStyleClass().add("status-akan-dilakukan");
                break;
            case "Sedang Dikerjakan":
                selesaikanButton.setText("Selesai");
                if (rootTaskCardVBox != null) rootTaskCardVBox.getStyleClass().add("status-sedang-dikerjakan");
                break;
            case "Selesai":
                selesaikanButton.setVisible(false);
                selesaikanButton.setManaged(false);
                tundaButton.setVisible(false);
                tundaButton.setManaged(false);
                if (rootTaskCardVBox != null) rootTaskCardVBox.getStyleClass().add("status-selesai");
                break;
            default:
                break;
        }
    }


    @FXML
    private void handleDetailTugas(ActionEvent event) {
        System.out.println("Detail tugas: " + currentTask.getNamaTugas());
        taskManagementController.showPlaceholderDialog("Detail Tugas: " + currentTask.getNamaTugas(),
                "Nama: " + currentTask.getNamaTugas() +
                "\nDeskripsi: " + currentTask.getDeskripsiTugas() +
                "\nDitugaskan ke: " + (currentTask.getNamaKaryawanPenanggungJawab() != null ? currentTask.getNamaKaryawanPenanggungJawab() : "Belum ada") +
                "\nTanggal: " + currentTask.getTanggalTugas() + (currentTask.getWaktuTugas() != null ? " " + currentTask.getWaktuTugasFormatted() : "") +
                "\nPrioritas: " + currentTask.getPrioritas() +
                "\nStatus: " + currentTask.getStatus() +
                (currentTask.getTanggalSelesai() != null ? "\nSelesai pada: " + currentTask.getTanggalSelesai() : "")
        );
    }

    @FXML
    private void handleTundaTugas(ActionEvent event) {
        System.out.println("Tunda tugas: " + currentTask.getNamaTugas() + " (Fitur belum diimplementasikan penuh)");
        taskManagementController.showPlaceholderDialog("Tunda Tugas", "Fitur tunda tugas belum diimplementasikan sepenuhnya.");
    }

    @FXML
    private void handleSelesaikanTugas(ActionEvent event) {
        String statusLama = currentTask.getStatus();
        String statusTargetBaru = "";
        LocalDate tanggalSelesai = currentTask.getTanggalSelesai();

        if ("Akan Dilakukan".equals(statusLama) || "Belum Dimulai".equals(statusLama)) {
            statusTargetBaru = "Sedang Dikerjakan";
            tanggalSelesai = null;
        } else if ("Sedang Dikerjakan".equals(statusLama)) {
            statusTargetBaru = "Selesai";
            tanggalSelesai = LocalDate.now(); 
        } else {
            System.out.println("Tugas '" + currentTask.getNamaTugas() + "' sudah selesai atau status tidak dikenal.");
            return; 
        }

        currentTask.setStatus(statusTargetBaru);
        currentTask.setTanggalSelesai(tanggalSelesai);


        boolean success = DatabaseManager.updateTaskStatus(currentTask.getId(), statusTargetBaru, tanggalSelesai);

        if (success) {
            System.out.println("Status tugas '" + currentTask.getNamaTugas() + "' berhasil diubah ke '" + statusTargetBaru + "' di database.");
            if (taskManagementController != null) {
                taskManagementController.refreshKanbanBoard(); 
            }
        } else {
            System.err.println("Gagal mengubah status tugas '" + currentTask.getNamaTugas() + "' di database.");

            currentTask.setStatus(statusLama); // Revert
            currentTask.setTanggalSelesai( ("Selesai".equals(statusLama)) ? currentTask.getTanggalTugas() : null ); 
            taskManagementController.showPlaceholderDialog("Update Gagal", "Gagal mengubah status tugas di database.");
        }
    }
}
