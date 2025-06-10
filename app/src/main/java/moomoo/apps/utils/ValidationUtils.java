package moomoo.apps.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import moomoo.apps.model.EmployeeModel;

/**
 * Kelas utilitas untuk menampung logika validasi yang dapat diuji secara independen.
 */
public class ValidationUtils {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    /**
     * Memvalidasi input untuk transaksi keuangan.
     * Metode ini bersifat 'static' agar bisa diuji tanpa perlu membuat instance.
     * * @param deskripsi Deskripsi transaksi.
     * @param tanggal Tanggal transaksi.
     * @param jumlahStr Jumlah dalam bentuk String.
     * @param metode Metode pembayaran.
     * @param kategori Kategori transaksi.
     * @return true jika semua input valid, false jika sebaliknya.
     */
    public static boolean validateTransactionInput(String deskripsi, LocalDate tanggal, String jumlahStr, String metode, String kategori) {
        if (deskripsi == null || deskripsi.trim().isEmpty() || 
            tanggal == null || 
            jumlahStr == null || jumlahStr.trim().isEmpty() || 
            metode == null || metode.trim().isEmpty() || 
            kategori == null || kategori.trim().isEmpty()) {
            return false;
        }
        
        try {

            String cleanJumlahStr = jumlahStr.replace(".", "").replace(",", ".");
            double jumlah = Double.parseDouble(cleanJumlahStr);

            if (jumlah <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {

            return false;
        }
        
        return true;
    }

    public static boolean validateNewTaskInput(String namaTugas, LocalDate tanggal, EmployeeModel karyawan, String waktuStr) {
        if (namaTugas == null || namaTugas.trim().isEmpty() || tanggal == null || karyawan == null) {
            return false;
        }
        if (waktuStr != null && !waktuStr.trim().isEmpty()) {
            try {
                LocalTime.parse(waktuStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                return false; 
            }
        }
        return true;
    }

    public static boolean validateNewAttendanceInput(EmployeeModel karyawan, LocalDate tanggal, String status, String waktuMasukStr, String waktuKeluarStr) {
        if (karyawan == null || tanggal == null || status == null || status.trim().isEmpty()) {
            return false;
        }

        boolean timesRequired = "Hadir".equalsIgnoreCase(status) || "Terlambat".equalsIgnoreCase(status);
        if (timesRequired) {
            if (waktuMasukStr == null || waktuMasukStr.trim().isEmpty()) {
                return false; 
            }
            try {
                LocalTime waktuMasuk = LocalTime.parse(waktuMasukStr, TIME_FORMATTER);

                if (waktuKeluarStr != null && !waktuKeluarStr.trim().isEmpty()) {
                    LocalTime waktuKeluar = LocalTime.parse(waktuKeluarStr, TIME_FORMATTER);
                    if (waktuKeluar.isBefore(waktuMasuk)) {
                        return false; 
                    }
                }
            } catch (DateTimeParseException e) {
                return false; 
            }
        }
        return true;
    }

    public static boolean validateProductionInput(String kategori, LocalDate tanggal, String jumlahStr, String satuan, String lokasi, String kualitas) {
        if (kategori == null || kategori.trim().isEmpty() ||
            tanggal == null ||
            jumlahStr == null || jumlahStr.trim().isEmpty() ||
            satuan == null || satuan.trim().isEmpty() ||
            lokasi == null || lokasi.trim().isEmpty() ||
            kualitas == null || kualitas.trim().isEmpty()) {
            return false;
        }

        try {
            double jumlah = Double.parseDouble(jumlahStr.replace(",", "."));
            if (jumlah <= 0) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false; 
        }
        return true;
    }
}
