package moomoo.apps;

import moomoo.apps.model.EmployeeModel;
import moomoo.apps.utils.ValidationUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class ValidationUtilsTest {

    @Test
    void testValidateTransactionInput_ValidData() {
        // Arrange
        String deskripsi = "Penjualan Susu";
        LocalDate tanggal = LocalDate.now();
        String jumlah = "500000";
        String metode = "Transfer Bank";
        String kategori = "Penjualan Produk";

        // Act
        boolean result = ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlah, metode, kategori);

        // Assert
        assertTrue(result, "Input yang valid seharusnya mengembalikan true.");
    }

    @Test
    void testValidateTransactionInput_EmptyFields() {
        // Assert
        assertFalse(ValidationUtils.validateTransactionInput("", LocalDate.now(), "100", "Tunai", "Test"), "Deskripsi kosong harus false.");
        assertFalse(ValidationUtils.validateTransactionInput("Test", null, "100", "Tunai", "Test"), "Tanggal null harus false.");
        assertFalse(ValidationUtils.validateTransactionInput("Test", LocalDate.now(), "", "Tunai", "Test"), "Jumlah kosong harus false.");
        assertFalse(ValidationUtils.validateTransactionInput("Test", LocalDate.now(), "100", null, "Test"), "Metode null harus false.");
        assertFalse(ValidationUtils.validateTransactionInput("Test", LocalDate.now(), "100", "Tunai", ""), "Kategori kosong harus false.");
    }

    @Test
    void testValidateTransactionInput_InvalidAmount() {
        // Arrange
        String deskripsi = "Penjualan Susu";
        LocalDate tanggal = LocalDate.now();
        String jumlahInvalid = "bukan angka";
        String jumlahNol = "0";
        String jumlahNegatif = "-1000";

        // Assert
        assertFalse(ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlahInvalid, "Tunai", "Penjualan"), "Jumlah non-numerik harus false.");
        assertFalse(ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlahNol, "Tunai", "Penjualan"), "Jumlah nol harus false.");
        assertFalse(ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlahNegatif, "Tunai", "Penjualan"), "Jumlah negatif harus false.");
    }
    
    @Test
    void testValidateTransactionInput_AmountWithSeparators() {
        // Arrange
        String deskripsi = "Penjualan Susu";
        LocalDate tanggal = LocalDate.now();
        String jumlahDenganTitik = "500.000";
        String jumlahDenganKoma = "500,000"; 

        // Assert
        assertTrue(ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlahDenganTitik, "Tunai", "Penjualan"), "Jumlah dengan titik separator harus valid.");
        assertTrue(ValidationUtils.validateTransactionInput(deskripsi, tanggal, jumlahDenganKoma, "Tunai", "Penjualan"), "Jumlah dengan koma separator harus valid.");
    }

    @Test
    void testValidateProductionInput_ValidData() {
        // Arrange
        String kategori = "Susu";
        LocalDate tanggal = LocalDate.now();
        String jumlahStr = "100.5";
        String satuan = "Liter";
        String lokasi = "Kandang A1";
        String kualitas = "Super A";

        // Act
        boolean result = ValidationUtils.validateProductionInput(kategori, tanggal, jumlahStr, satuan, lokasi, kualitas);

        // Assert
        assertTrue(result, "Input produksi yang valid seharusnya mengembalikan true.");
    }

    @Test
    void testValidateProductionInput_EmptyFields() {
        // Arrange
        LocalDate tanggal = LocalDate.now();

        // Assert
        assertFalse(ValidationUtils.validateProductionInput(null, tanggal, "100", "Liter", "A1", "A"), "Kategori null harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", null, "100", "Liter", "A1", "A"), "Tanggal null harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "", "Liter", "A1", "A"), "Jumlah kosong harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "100", " ", "A1", "A"), "Satuan kosong harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "100", "Liter", null, "A"), "Lokasi null harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "100", "Liter", "A1", null), "Kualitas null harus false.");
    }

    @Test
    void testValidateProductionInput_InvalidAmount() {
        // Arrange
        LocalDate tanggal = LocalDate.now();

        // Assert
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "nol", "Liter", "A1", "A"), "Jumlah non-numerik harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "0", "Liter", "A1", "A"), "Jumlah nol harus false.");
        assertFalse(ValidationUtils.validateProductionInput("Susu", tanggal, "-50", "Liter", "A1", "A"), "Jumlah negatif harus false.");
    }

    private EmployeeModel dummyEmployee;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        dummyEmployee = new EmployeeModel(1, "Budi Santoso", "Staf Kandang");
        today = LocalDate.now();
    }

    @Test
    @DisplayName("Tugas: Input valid harus mengembalikan true")
    void testValidateNewTaskInput_ValidData() {
        assertTrue(ValidationUtils.validateNewTaskInput("Pemerahan Pagi", today, dummyEmployee, "08:00"), "Data valid dengan waktu harus true.");
        assertTrue(ValidationUtils.validateNewTaskInput("Bersihkan kandang", today, dummyEmployee, null), "Data valid tanpa waktu harus true.");
        assertTrue(ValidationUtils.validateNewTaskInput("Cek stok", today, dummyEmployee, ""), "Data valid dengan waktu kosong harus true.");
    }

    @Test
    @DisplayName("Tugas: Input dengan kolom wajib yang kosong harus false")
    void testValidateNewTaskInput_MissingRequiredFields() {
        assertFalse(ValidationUtils.validateNewTaskInput("", today, dummyEmployee, "09:00"), "Nama tugas kosong harus false.");
        assertFalse(ValidationUtils.validateNewTaskInput("Tugas penting", null, dummyEmployee, "09:00"), "Tanggal null harus false.");
        assertFalse(ValidationUtils.validateNewTaskInput("Tugas penting", today, null, "09:00"), "Karyawan null harus false.");
    }

    @Test
    @DisplayName("Tugas: Input dengan format waktu salah harus false")
    void testValidateNewTaskInput_InvalidTimeFormat() {
        assertFalse(ValidationUtils.validateNewTaskInput("Tugas sore", today, dummyEmployee, "15:00:00"), "Format waktu HH:mm:ss harus false.");
        assertFalse(ValidationUtils.validateNewTaskInput("Tugas sore", today, dummyEmployee, "sore"), "Format waktu non-numerik harus false.");
        assertFalse(ValidationUtils.validateNewTaskInput("Tugas sore", today, dummyEmployee, "15.00"), "Format waktu dengan titik harus false.");
    }
    

    @Test
    @DisplayName("Presensi: Input valid untuk status 'Hadir' dan 'Izin' harus true")
    void testValidateNewAttendanceInput_ValidCases() {
        assertTrue(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Hadir", "08:00", "17:00"), "Status 'Hadir' dengan waktu valid harus true.");
        assertTrue(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Terlambat", "08:30", null), "Status 'Terlambat' dengan waktu masuk saja harus true.");
        assertTrue(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Izin", "", ""), "Status 'Izin' tanpa waktu harus true.");
        assertTrue(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Sakit", null, null), "Status 'Sakit' dengan waktu null harus true.");
    }

    @Test
    @DisplayName("Presensi: Input dengan kolom wajib yang kosong harus false")
    void testValidateNewAttendanceInput_MissingRequiredFields() {
        assertFalse(ValidationUtils.validateNewAttendanceInput(null, today, "Hadir", "08:00", "17:00"), "Karyawan null harus false.");
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, null, "Hadir", "08:00", "17:00"), "Tanggal null harus false.");
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "", "08:00", "17:00"), "Status kosong harus false.");
    }

    @Test
    @DisplayName("Presensi: Status 'Hadir' atau 'Terlambat' tanpa waktu masuk harus false")
    void testValidateNewAttendanceInput_MissingClockInWhenRequired() {
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Hadir", "", "17:00"), "Status 'Hadir' tanpa waktu masuk harus false.");
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Terlambat", null, "17:00"), "Status 'Terlambat' dengan waktu masuk null harus false.");
    }

    @Test
    @DisplayName("Presensi: Waktu keluar sebelum waktu masuk harus false")
    void testValidateNewAttendanceInput_ClockOutBeforeClockIn() {
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Hadir", "09:00", "08:00"), "Waktu keluar sebelum masuk harus false.");
    }
    
    @Test
    @DisplayName("Presensi: Format waktu salah harus false")
    void testValidateNewAttendanceInput_InvalidTimeFormat() {
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Hadir", "delapan pagi", "17:00"), "Waktu masuk non-numerik harus false.");
        assertFalse(ValidationUtils.validateNewAttendanceInput(dummyEmployee, today, "Hadir", "08:00", "17.00"), "Waktu keluar dengan titik harus false.");
    }


}