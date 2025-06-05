package moomoo.apps.model; // Atau package yang sesuai

import javafx.beans.property.SimpleStringProperty;

public class KinerjaData {
    private final SimpleStringProperty karyawan;
    private final SimpleStringProperty departemen;
    private final SimpleStringProperty kehadiran;
    private final SimpleStringProperty produktivitas;
    private final SimpleStringProperty peringkat;

    public KinerjaData(String karyawan, String departemen, String kehadiran, String produktivitas, String peringkat) {
        this.karyawan = new SimpleStringProperty(karyawan);
        this.departemen = new SimpleStringProperty(departemen);
        this.kehadiran = new SimpleStringProperty(kehadiran);
        this.produktivitas = new SimpleStringProperty(produktivitas);
        this.peringkat = new SimpleStringProperty(peringkat);
    }

    // WAJIB ada getter untuk PropertyValueFactory
    public String getKaryawan() { return karyawan.get(); }
    public SimpleStringProperty karyawanProperty() { return karyawan; } // Optional tapi best practice

    public String getDepartemen() { return departemen.get(); }
    public SimpleStringProperty departemenProperty() { return departemen; }

    public String getKehadiran() { return kehadiran.get(); }
    public SimpleStringProperty kehadiranProperty() { return kehadiran; }

    public String getProduktivitas() { return produktivitas.get(); }
    public SimpleStringProperty produktivitasProperty() { return produktivitas; }

    public String getPeringkat() { return peringkat.get(); }
    public SimpleStringProperty peringkatProperty() { return peringkat; }
}