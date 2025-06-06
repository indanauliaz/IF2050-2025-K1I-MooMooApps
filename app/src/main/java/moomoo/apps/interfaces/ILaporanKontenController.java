package moomoo.apps.interfaces; // Atau paket lain yang sesuai untuk interface

/**
 * Interface untuk semua controller yang menangani konten spesifik di dalam tab Laporan.
 * Setiap controller konten (Produksi, Keuangan, SDM) harus mengimplementasikan interface ini.
 */
public interface ILaporanKontenController {

    /**
     * Metode untuk menerima dan menerapkan filter periode dari LaporanController utama.
     * Implementasi di kelas controller anak akan memuat ulang data sesuai periode yang diberikan.
     *
     * @param periode String yang merepresentasikan periode filter (misalnya "Bulan Ini", "Minggu Ini").
     */
    void terapkanFilterPeriode(String periode);

    /**
     * (Opsional) Metode yang bisa dipanggil saat konten pertama kali dimuat atau di-refresh.
     * Berguna jika ada inisialisasi data atau tampilan yang perlu dilakukan
     * setelah FXML di-load dan sebelum filter pertama diterapkan.
     */
    void inisialisasiKonten();


}
