package moomoo.apps.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import moomoo.apps.model.TaskModel;
import javafx.scene.layout.VBox; // Untuk mengubah style kartu

public class TaskCardController {

    @FXML private Label namaTugasLabel;
    @FXML private Label deskripsiLabel;
    @FXML private Label ditugaskanKeLabel;
    @FXML private Label prioritasLabel; // Label teks untuk prioritas
    @FXML private Circle prioritasIndicator; // Lingkaran indikator prioritas

    @FXML private Button detailButton;
    @FXML private Button tundaButton;
    @FXML private Button selesaikanButton;

    @FXML private VBox rootTaskCardVBox; // fx:id untuk VBox root kartu

    private TaskModel currentTask;
    private TaskManagementController taskManagementController;

    public void setData(TaskModel task, TaskManagementController mainController) {
        this.currentTask = task;
        this.taskManagementController = mainController;

        namaTugasLabel.setText(task.getNamaTugas());
        String deskripsiFull = task.getDeskripsiTugas();
        deskripsiLabel.setText(deskripsiFull.length() > 60 ? deskripsiFull.substring(0, 60) + "..." : deskripsiFull); // Kurangi panjangnya sedikit

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
                prioritasIndicator.setFill(Color.web("#EF5350")); // Merah muda
                prioritasLabel.getStyleClass().add("prioritas-tinggi");
                break;
            case "normal":
                prioritasIndicator.setFill(Color.web("#FFB74D")); // Oranye muda
                prioritasLabel.getStyleClass().add("prioritas-normal");
                break;
            case "rendah":
                prioritasIndicator.setFill(Color.web("#81C784")); // Hijau muda
                prioritasLabel.getStyleClass().add("prioritas-rendah");
                break;
            default:
                prioritasIndicator.setFill(Color.LIGHTGRAY);
                prioritasLabel.getStyleClass().add("prioritas-default");
                break;
        }
    }

    private void setStatusVisual(String status) {
        // Atur visibilitas dan teks tombol berdasarkan status
        // Hapus style status sebelumnya
        if (rootTaskCardVBox != null) {
            rootTaskCardVBox.getStyleClass().removeAll("status-akan-dilakukan", "status-sedang-dikerjakan", "status-selesai");
        }
        
        selesaikanButton.setVisible(true);
        selesaikanButton.setManaged(true);
        tundaButton.setVisible(true);
        tundaButton.setManaged(true);

        switch (status) {
            case "Akan Dilakukan":
                selesaikanButton.setText("Mulai"); // Lebih singkat
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
                // Status tidak dikenal
                break;
        }
    }


    @FXML
    private void handleDetailTugas(ActionEvent event) {
        System.out.println("Detail tugas: " + currentTask.getNamaTugas());
        // TODO: Implementasi buka modal detail tugas
        // Contoh: taskManagementController.showTaskDetailModal(currentTask);
    }

    @FXML
    private void handleTundaTugas(ActionEvent event) {
        System.out.println("Tunda tugas: " + currentTask.getNamaTugas() + " (Belum diimplementasikan penuh)");
        // Logika untuk menunda tugas bisa ditambahkan di sini
        // Misalnya mengubah statusnya menjadi "Ditunda" dan memindahkannya ke kolom khusus
        // Untuk saat ini, kita bisa tampilkan pesan atau log saja
    }

    @FXML
    private void handleSelesaikanTugas(ActionEvent event) {
        String statusLama = currentTask.getStatus();
        String statusTargetBaru = "";

        if ("Akan Dilakukan".equals(statusLama)) {
            statusTargetBaru = "Sedang Dikerjakan";
        } else if ("Sedang Dikerjakan".equals(statusLama)) {
            statusTargetBaru = "Selesai";
        } else {
            System.out.println("Tugas sudah selesai atau status tidak dikenal.");
            return; // Tidak ada aksi jika sudah selesai atau status aneh
        }

        System.out.println("Mengubah status tugas '" + currentTask.getNamaTugas() + "' dari '" + statusLama + "' ke '" + statusTargetBaru + "'");
        currentTask.setStatus(statusTargetBaru); // Update status di model

        // TODO: Simpan perubahan status ini ke DATABASE kamu di sini
        // DatabaseManager.getInstance().updateTaskStatus(currentTask.getId(), statusTargetBaru);

        // Update tampilan tombol dan kartu secara langsung
        setStatusVisual(statusTargetBaru);

        // Beri tahu TaskManagementController untuk me-refresh seluruh Kanban Board
        // agar kartu pindah kolom
        if (taskManagementController != null) {
            taskManagementController.refreshKanbanBoard();
        }
    }
}
