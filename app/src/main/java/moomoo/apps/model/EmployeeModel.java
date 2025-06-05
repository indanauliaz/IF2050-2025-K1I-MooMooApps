package moomoo.apps.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class EmployeeModel {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty namaLengkap;
    private final SimpleStringProperty posisi; // Jabatan spesifik
    private final SimpleStringProperty departemen; // Untuk pengelompokan di laporan
    private final SimpleDoubleProperty persentaseKehadiran;
    private final SimpleDoubleProperty persentaseProduktivitas;
    private final SimpleStringProperty peringkatKinerja;

    // Konstruktor lengkap
    public EmployeeModel(int id, String namaLengkap, String posisi, String departemen,
                         double persentaseKehadiran, double persentaseProduktivitas, String peringkatKinerja) {
        this.id = new SimpleIntegerProperty(id);
        this.namaLengkap = new SimpleStringProperty(namaLengkap);
        this.posisi = new SimpleStringProperty(posisi);
        this.departemen = new SimpleStringProperty(departemen);
        this.persentaseKehadiran = new SimpleDoubleProperty(persentaseKehadiran);
        this.persentaseProduktivitas = new SimpleDoubleProperty(persentaseProduktivitas);
        this.peringkatKinerja = new SimpleStringProperty(peringkatKinerja);
    }

    // Konstruktor sederhana (nilai kinerja bisa diisi default atau di-set nanti)
    public EmployeeModel(int id, String namaLengkap, String posisi, String departemen) {
        this(id, namaLengkap, posisi, departemen, 0.0, 0.0, "N/A"); // Nilai default
    }
    
    // Konstruktor yang mungkin sudah kamu pakai (dengan penyesuaian departemen)
    public EmployeeModel(int id, String namaLengkap, String posisi) {
        this(id, namaLengkap, posisi, determineDepartemenFromPosisi(posisi), 0.0, 0.0, "N/A");
    }

    public EmployeeModel(String namaLengkap, String posisi) {
        this(0, namaLengkap, posisi, determineDepartemenFromPosisi(posisi), 0.0, 0.0, "N/A"); // ID 0 untuk karyawan baru
    }
    
    // Helper sederhana untuk menentukan departemen dari posisi (bisa kamu sesuaikan)
    private static String determineDepartemenFromPosisi(String posisi) {
        if (posisi == null) return "Lainnya";
        String lowerPosisi = posisi.toLowerCase();
        if (lowerPosisi.contains("produksi")) return "Produksi";
        if (lowerPosisi.contains("pemasaran")) return "Pemasaran";
        if (lowerPosisi.contains("peternakan")) return "Peternakan";
        if (lowerPosisi.contains("keuangan")) return "Keuangan";
        if (lowerPosisi.contains("administrasi")) return "Administrasi";
        return "Lainnya"; // Default
    }

    // --- Getters untuk JavaFX Properties (PENTING untuk TableView) ---
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty namaLengkapProperty() { return namaLengkap; }
    public SimpleStringProperty posisiProperty() { return posisi; }
    public SimpleStringProperty departemenProperty() { return departemen; }
    public SimpleDoubleProperty persentaseKehadiranProperty() { return persentaseKehadiran; }
    public SimpleDoubleProperty persentaseProduktivitasProperty() { return persentaseProduktivitas; }
    public SimpleStringProperty peringkatKinerjaProperty() { return peringkatKinerja; }

    // --- Getters standar ---
    public int getId() { return id.get(); }
    public String getNamaLengkap() { return namaLengkap.get(); }
    public String getPosisi() { return posisi.get(); }
    public String getDepartemen() { return departemen.get(); }
    public double getPersentaseKehadiran() { return persentaseKehadiran.get(); }
    public double getPersentaseProduktivitas() { return persentaseProduktivitas.get(); }
    public String getPeringkatKinerja() { return peringkatKinerja.get(); }

    // --- Setters (jika data bisa diubah setelah objek dibuat) ---
    public void setId(int id) { this.id.set(id); }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap.set(namaLengkap); }
    public void setPosisi(String posisi) { 
        this.posisi.set(posisi);
        // Otomatis update departemen jika posisi berubah (opsional)
        // this.departemen.set(determineDepartemenFromPosisi(posisi)); 
    }
    public void setDepartemen(String departemen) { this.departemen.set(departemen); }
    public void setPersentaseKehadiran(double persentaseKehadiran) { this.persentaseKehadiran.set(persentaseKehadiran); }
    public void setPersentaseProduktivitas(double persentaseProduktivitas) { this.persentaseProduktivitas.set(persentaseProduktivitas); }
    public void setPeringkatKinerja(String peringkatKinerja) { this.peringkatKinerja.set(peringkatKinerja); }

    @Override
    public String toString() { 
        return getNamaLengkap() + (getPosisi() != null && !getPosisi().isEmpty() ? " (" + getPosisi() + ")" : "");
    }
}