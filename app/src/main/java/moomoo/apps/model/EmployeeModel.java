package moomoo.apps.model;

public class EmployeeModel {
    private int id;
    private String namaLengkap;
    private String posisi;

    public EmployeeModel(int id, String namaLengkap, String posisi) {
        this.id = id;
        this.namaLengkap = namaLengkap;
        this.posisi = posisi;
    }

    public EmployeeModel(String namaLengkap, String posisi) {
        this.namaLengkap = namaLengkap;
        this.posisi = posisi;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNamaLengkap() { return namaLengkap; }
    public void setNamaLengkap(String namaLengkap) { this.namaLengkap = namaLengkap; }
    public String getPosisi() { return posisi; }
    public void setPosisi(String posisi) { this.posisi = posisi; }

    @Override
    public String toString() { 
        return namaLengkap + (posisi != null && !posisi.isEmpty() ? " (" + posisi + ")" : "");
    }
}