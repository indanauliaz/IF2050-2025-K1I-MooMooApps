package moomoo.apps.model;

import java.time.LocalDate;

public class ProductionRecord {
    private int id; 
    private String kategori;
    private double jumlah; 
    private String satuan; 
    private LocalDate tanggal;
    private String lokasi;
    private String kualitas; 
    private String catatan; 


    public ProductionRecord(String kategori, double jumlah, String satuan, LocalDate tanggal, String lokasi, String kualitas, String catatan) {
        this.kategori = kategori;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.tanggal = tanggal;
        this.lokasi = lokasi;
        this.kualitas = kualitas;
        this.catatan = catatan;
    }


    public ProductionRecord(int id, String kategori, double jumlah, String satuan, LocalDate tanggal, String lokasi, String kualitas, String catatan) {
        this.id = id;
        this.kategori = kategori;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.tanggal = tanggal;
        this.lokasi = lokasi;
        this.kualitas = kualitas;
        this.catatan = catatan;
    }


    // Setter getter standard
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }

    public double getJumlah() { return jumlah; }
    public void setJumlah(double jumlah) { this.jumlah = jumlah; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }
    
    public String getJumlahDenganSatuan() {

        if (satuan == null || satuan.trim().isEmpty()) {
            return String.format("%.0f", jumlah); 
        }
        return String.format("%.0f %s", jumlah, satuan);
    }


    public LocalDate getTanggal() { return tanggal; }
    public void setTanggal(LocalDate tanggal) { this.tanggal = tanggal; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getKualitas() { return kualitas; }
    public void setKualitas(String kualitas) { this.kualitas = kualitas; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    @Override
    public String toString() {
        return "ProductionRecord{" +
                "id=" + id +
                ", kategori='" + kategori + '\'' +
                ", jumlah=" + jumlah +
                ", satuan='" + satuan + '\'' +
                ", tanggal=" + tanggal +
                ", lokasi='" + lokasi + '\'' +
                ", kualitas='" + kualitas + '\'' +
                ", catatan='" + catatan + '\'' +
                '}';
    }
}