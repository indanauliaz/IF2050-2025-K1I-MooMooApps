package moomoo.apps.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TaskModel {
    private int id;
    private String namaTugas;
    private String deskripsiTugas;
    private Integer employeeId;
    private String namaKaryawanPenanggungJawab;
    private LocalDate tanggalTugas;
    private LocalTime waktuTugas;
    private String prioritas;
    private String status;
    private LocalDate tanggalSelesai;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public TaskModel(int id, String namaTugas, String deskripsiTugas, Integer employeeId, String namaKaryawanPenanggungJawab,
                     LocalDate tanggalTugas, LocalTime waktuTugas, String prioritas, String status, LocalDate tanggalSelesai) { 
        this.id = id;
        this.namaTugas = namaTugas;
        this.deskripsiTugas = deskripsiTugas;
        this.employeeId = employeeId;
        this.namaKaryawanPenanggungJawab = namaKaryawanPenanggungJawab;
        this.tanggalTugas = tanggalTugas;
        this.waktuTugas = waktuTugas;
        this.prioritas = prioritas;
        this.status = status;
        this.tanggalSelesai = tanggalSelesai; 
    }

    public TaskModel(String namaTugas, String deskripsiTugas, Integer employeeId, String namaKaryawanPenanggungJawab,
                     LocalDate tanggalTugas, LocalTime waktuTugas, String prioritas, String status) {
        this.namaTugas = namaTugas;
        this.deskripsiTugas = deskripsiTugas;
        this.employeeId = employeeId;
        this.namaKaryawanPenanggungJawab = namaKaryawanPenanggungJawab;
        this.tanggalTugas = tanggalTugas;
        this.waktuTugas = waktuTugas;
        this.prioritas = prioritas;
        this.status = status;
        this.tanggalSelesai = null; // Default null untuk tugas baru
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNamaTugas() { return namaTugas; }
    public void setNamaTugas(String namaTugas) { this.namaTugas = namaTugas; }

    public String getDeskripsiTugas() { return deskripsiTugas; }
    public void setDeskripsiTugas(String deskripsiTugas) { this.deskripsiTugas = deskripsiTugas; }

    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

    public String getNamaKaryawanPenanggungJawab() { return namaKaryawanPenanggungJawab; }
    public void setNamaKaryawanPenanggungJawab(String namaKaryawanPenanggungJawab) { this.namaKaryawanPenanggungJawab = namaKaryawanPenanggungJawab; }

    public LocalDate getTanggalTugas() { return tanggalTugas; }
    public void setTanggalTugas(LocalDate tanggalTugas) { this.tanggalTugas = tanggalTugas; }

    public LocalTime getWaktuTugas() { return waktuTugas; }
    public void setWaktuTugas(LocalTime waktuTugas) { this.waktuTugas = waktuTugas; }

    public String getPrioritas() { return prioritas; }
    public void setPrioritas(String prioritas) { this.prioritas = prioritas; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getTanggalSelesai() { return tanggalSelesai; }
    public void setTanggalSelesai(LocalDate tanggalSelesai) { this.tanggalSelesai = tanggalSelesai; }

    public String getWaktuTugasFormatted() {
        return waktuTugas != null ? waktuTugas.format(TIME_FORMATTER) : "-";
    }
}