package manajemenkuliner;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Random;

public class DatabaseSementara {
    public static ArrayList<Pengguna> listPengguna = new ArrayList<>();
    public static ArrayList<Produk> listProduk = new ArrayList<>();
    public static ArrayList<Transaksi> listTransaksi = new ArrayList<>();
    public static ArrayList<ArusKas> listArusKas = new ArrayList<>();
    public static ArrayList<Shift> listShift = new ArrayList<>();
    public static ArrayList<String> listAbsensi = new ArrayList<>();

    static {
        // 1. Akun Login Default (Password min 8 karakter sesuai kebutuhan non-fungsional)
        listPengguna.add(new Owner("Zulfikar (Owner)", "owner", "owner1234"));
        listPengguna.add(new AdminBisnis("Aqila (Admin Bisnis)", "bisnis", "bisnis1234"));
        listPengguna.add(new AdminKeuangan("Imam (Admin Keuangan)", "keuangan", "keuangan1234"));
        listPengguna.add(new Pegawai("Faiz (Pegawai)", "pegawai", "pegawai1234"));

        // 2. Data Produk Awal
        listProduk.add(new Produk("MKN-01", "Ayam Bakar Madu", 25000, 45));
        listProduk.add(new Produk("MKN-02", "Nasi Goreng Kampung", 18000, 60));
        listProduk.add(new Produk("MKN-03", "Mie Nyemek Spesial", 16000, 50));
        listProduk.add(new Produk("MNM-01", "Es Teh Manis Jumbo", 5000, 120));
        listProduk.add(new Produk("MNM-02", "Kopi Susu Gula Aren", 15000, 80));

        // 3. Data Shift Awal
        listShift.add(new Shift("Faiz", "Senin - Rabu", "08:00 - 16:00 (Pagi)"));
        listShift.add(new Shift("Rina", "Kamis - Sabtu", "14:00 - 22:00 (Sore)"));

        // 4. Generate Data Penjualan & Keuangan Realistis 30 Hari Terakhir
        Random rand = new Random();
        LocalDate hariIni = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate tgl = hariIni.minusDays(i);
            // Simulasi 2-4 transaksi per hari
            int jumlahTransaksiHarian = 2 + rand.nextInt(3);
            double totalHarian = 0;

            for (int j = 0; j < jumlahTransaksiHarian; j++) {
                Produk p = listProduk.get(rand.nextInt(listProduk.size()));
                int qty = 2 + rand.nextInt(6);
                double subtotal = p.getHarga() * qty;
                listTransaksi.add(new Transaksi(tgl, p.getNama(), qty, subtotal));
                totalHarian += subtotal;
            }
            // Otomatis masuk ke Arus Kas Pemasukan
            listArusKas.add(new ArusKas(tgl, "Pemasukan", "Rekap Penjualan Harian", totalHarian));
            
            // Simulasi pengeluaran bahan baku tiap 3 hari
            if (i % 3 == 0) {
                listArusKas.add(new ArusKas(tgl, "Pengeluaran", "Belanja Bahan Baku Dapur", 150000 + (rand.nextInt(5) * 25000)));
            }
        }
    }
}