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

public class AttendanceRecordModel {
    private final IntegerProperty id; // ID unik untuk record kehadiran
    private final ObjectProperty<EmployeeModel> karyawan; // Referensi ke EmployeeModel
    private final StringProperty statusKehadiran; // "Hadir", "Terlambat", "Absen", "Izin"
    private final ObjectProperty<LocalTime> waktuMasuk;
    private final ObjectProperty<LocalTime> waktuKeluar;
    private final StringProperty catatan;
    private final ObjectProperty<LocalDate> tanggalAbsen; // Tanggal record ini

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AttendanceRecordModel(int id, EmployeeModel karyawan, LocalDate tanggalAbsen, String statusKehadiran,
                                 LocalTime waktuMasuk, LocalTime waktuKeluar, String catatan) {
        this.id = new SimpleIntegerProperty(id);
        this.karyawan = new SimpleObjectProperty<>(karyawan);
        this.tanggalAbsen = new SimpleObjectProperty<>(tanggalAbsen);
        this.statusKehadiran = new SimpleStringProperty(statusKehadiran);
        this.waktuMasuk = new SimpleObjectProperty<>(waktuMasuk);
        this.waktuKeluar = new SimpleObjectProperty<>(waktuKeluar);
        this.catatan = new SimpleStringProperty(catatan);
    }
    

    public AttendanceRecordModel(EmployeeModel karyawan, LocalDate tanggalAbsen, String statusKehadiran, 
                                 String waktuMasukStr, String waktuKeluarStr, String catatan) {
        this.id = new SimpleIntegerProperty(0); // ID default, bisa di-generate DB
        this.karyawan = new SimpleObjectProperty<>(karyawan);
        this.tanggalAbsen = new SimpleObjectProperty<>(tanggalAbsen);
        this.statusKehadiran = new SimpleStringProperty(statusKehadiran);
        this.waktuMasuk = new SimpleObjectProperty<>(waktuMasukStr != null && !waktuMasukStr.isEmpty() ? LocalTime.parse(waktuMasukStr, TIME_FORMATTER) : null);
        this.waktuKeluar = new SimpleObjectProperty<>(waktuKeluarStr != null && !waktuKeluarStr.isEmpty() ? LocalTime.parse(waktuKeluarStr, TIME_FORMATTER) : null);
        this.catatan = new SimpleStringProperty(catatan);
    }


    // --- Getters untuk Properties ---
    public IntegerProperty idProperty() { return id; }
    public ObjectProperty<EmployeeModel> karyawanProperty() { return karyawan; }
    public StringProperty namaKaryawanProperty() { // Helper untuk TableView
        return new SimpleStringProperty(karyawan.get() != null ? karyawan.get().getNamaLengkap() : "N/A");
    }
    public ObjectProperty<LocalDate> tanggalAbsenProperty() { return tanggalAbsen; }
    public StringProperty statusKehadiranProperty() { return statusKehadiran; }
    public ObjectProperty<LocalTime> waktuMasukProperty() { return waktuMasuk; }
    public StringProperty waktuMasukFormattedProperty() { // Helper untuk TableView
        return new SimpleStringProperty(getWaktuMasuk() != null ? getWaktuMasuk().format(TIME_FORMATTER) : "-");
    }
    public ObjectProperty<LocalTime> waktuKeluarProperty() { return waktuKeluar; }
    public StringProperty waktuKeluarFormattedProperty() { // Helper untuk TableView
        return new SimpleStringProperty(getWaktuKeluar() != null ? getWaktuKeluar().format(TIME_FORMATTER) : "-");
    }
    public StringProperty catatanProperty() { return catatan; }

    // --- Getters Standar ---
    public int getId() { return id.get(); }
    public EmployeeModel getKaryawan() { return karyawan.get(); }
    public LocalDate getTanggalAbsen() { return tanggalAbsen.get(); }
    public String getStatusKehadiran() { return statusKehadiran.get(); }
    public LocalTime getWaktuMasuk() { return waktuMasuk.get(); }
    public LocalTime getWaktuKeluar() { return waktuKeluar.get(); }
    public String getCatatan() { return catatan.get(); }

    // --- Setters Standar ---
    public void setId(int id) { this.id.set(id); }
    public void setKaryawan(EmployeeModel karyawan) { this.karyawan.set(karyawan); }
    public void setTanggalAbsen(LocalDate tanggalAbsen) { this.tanggalAbsen.set(tanggalAbsen); }
    public void setStatusKehadiran(String statusKehadiran) { this.statusKehadiran.set(statusKehadiran); }
    public void setWaktuMasuk(LocalTime waktuMasuk) { this.waktuMasuk.set(waktuMasuk); }
    public void setWaktuKeluar(LocalTime waktuKeluar) { this.waktuKeluar.set(waktuKeluar); }
    public void setCatatan(String catatan) { this.catatan.set(catatan); }
}
