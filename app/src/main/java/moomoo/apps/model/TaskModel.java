package moomoo.apps.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TaskModel {
    private final IntegerProperty id;
    private final StringProperty namaTugas;
    private final StringProperty deskripsiTugas;
    private final ObjectProperty<Integer> employeeId;
    private final StringProperty namaKaryawanPenanggungJawab;
    private final StringProperty departemen; // Properti baru
    private final ObjectProperty<LocalDate> tanggalTugas;
    private final ObjectProperty<LocalTime> waktuTugas;
    private final StringProperty prioritas;
    private final StringProperty status;
    private final ObjectProperty<LocalDate> tanggalSelesai;

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm"); 

    // Konstruktor Utama (Lengkap)
    public TaskModel(int id, String namaTugas, String deskripsiTugas, Integer employeeId, String namaKaryawanPenanggungJawab, String departemen, LocalDate tanggalTugas, LocalTime waktuTugas, String prioritas, String status, LocalDate tanggalSelesai) {
        this.id = new SimpleIntegerProperty(id);
        this.namaTugas = new SimpleStringProperty(namaTugas);
        this.deskripsiTugas = new SimpleStringProperty(deskripsiTugas);
        this.employeeId = new SimpleObjectProperty<>(employeeId);
        this.namaKaryawanPenanggungJawab = new SimpleStringProperty(namaKaryawanPenanggungJawab);
        this.departemen = new SimpleStringProperty(departemen);
        this.tanggalTugas = new SimpleObjectProperty<>(tanggalTugas);
        this.waktuTugas = new SimpleObjectProperty<>(waktuTugas);
        this.prioritas = new SimpleStringProperty(prioritas);
        this.status = new SimpleStringProperty(status);
        this.tanggalSelesai = new SimpleObjectProperty<>(tanggalSelesai);
    }

    public TaskModel(String namaTugas, String deskripsiTugas, Integer employeeId, String namaKaryawanPenanggungJawab, String departemen,
                     LocalDate tanggalTugas, LocalTime waktuTugas, String prioritas, String status) {
        this(0, // ID default untuk tugas baru
             namaTugas,
             deskripsiTugas,
             employeeId,
             namaKaryawanPenanggungJawab,
             departemen,
             tanggalTugas,
             waktuTugas,
             prioritas,
             status,
             null); // tanggalSelesai default null untuk tugas baru
    }
    
    // Konstruktor untuk dummy data yang dipakai di TaskManagementController sebelumnya
    // (Mengkonversi waktu String ke LocalTime)
    public TaskModel(int id, String namaTugas, String deskripsi, String ditugaskanKe, String departemen,
                     LocalDate tanggal, String waktuStr, String prioritas, String status) {
        this(id, 
             namaTugas, 
             deskripsi, 
             null, // employeeId sementara null untuk dummy data ini
             ditugaskanKe, 
             departemen,
             tanggal, 
             (waktuStr != null && !waktuStr.trim().isEmpty() ? LocalTime.parse(waktuStr, TIME_FORMATTER) : null), // Konversi String ke LocalTime
             prioritas, 
             status, 
             ("Selesai".equals(status) ? tanggal : null)); // Jika status "Selesai", set tanggalSelesai sama dengan tanggalTugas
    }


    // --- Getters untuk JavaFX Properties ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty namaTugasProperty() { return namaTugas; }
    public StringProperty deskripsiTugasProperty() { return deskripsiTugas; }
    public ObjectProperty<Integer> employeeIdProperty() { return employeeId; }
    public StringProperty namaKaryawanPenanggungJawabProperty() { return namaKaryawanPenanggungJawab; }
    public StringProperty departemenProperty() { return departemen; } // Getter untuk departemen
    public ObjectProperty<LocalDate> tanggalTugasProperty() { return tanggalTugas; }
    public ObjectProperty<LocalTime> waktuTugasProperty() { return waktuTugas; }
    public StringProperty prioritasProperty() { return prioritas; }
    public StringProperty statusProperty() { return status; }
    public ObjectProperty<LocalDate> tanggalSelesaiProperty() { return tanggalSelesai; }

    // --- Getters Standar ---
    public int getId() { return id.get(); }
    public String getNamaTugas() { return namaTugas.get(); }
    public String getDeskripsiTugas() { return deskripsiTugas.get(); }
    public Integer getEmployeeId() { return employeeId.get(); }
    public String getNamaKaryawanPenanggungJawab() { return namaKaryawanPenanggungJawab.get(); }
    public String getDepartemen() { return departemen.get(); }
    public LocalDate getTanggalTugas() { return tanggalTugas.get(); }
    public LocalTime getWaktuTugas() { return waktuTugas.get(); }
    public String getPrioritas() { return prioritas.get(); }
    public String getStatus() { return status.get(); }
    public LocalDate getTanggalSelesai() { return tanggalSelesai.get(); }

    // --- Setters Standar ---
    public void setId(int id) { this.id.set(id); }
    public void setNamaTugas(String namaTugas) { this.namaTugas.set(namaTugas); }
    public void setDeskripsiTugas(String deskripsiTugas) { this.deskripsiTugas.set(deskripsiTugas); }
    public void setEmployeeId(Integer employeeId) { this.employeeId.set(employeeId); }
    public void setNamaKaryawanPenanggungJawab(String namaKaryawanPenanggungJawab) { this.namaKaryawanPenanggungJawab.set(namaKaryawanPenanggungJawab); }
    public void setTanggalTugas(LocalDate tanggalTugas) { this.tanggalTugas.set(tanggalTugas); }
    public void setWaktuTugas(LocalTime waktuTugas) { this.waktuTugas.set(waktuTugas); }
    public void setPrioritas(String prioritas) { this.prioritas.set(prioritas); }
    public void setStatus(String status) { this.status.set(status); }
    public void setTanggalSelesai(LocalDate tanggalSelesai) { this.tanggalSelesai.set(tanggalSelesai); }

    // Metode format waktu yang sudah ada
    public String getWaktuTugasFormatted() {
        LocalTime wt = getWaktuTugas(); // Ambil nilai dari property
        return wt != null ? wt.format(TIME_FORMATTER) : "-";
    }

    @Override
    public String toString() {
        return "TaskModel{" +
               "id=" + getId() +
               ", namaTugas='" + getNamaTugas() + '\'' +
               ", status='" + getStatus() + '\'' +
               '}';
    }
}