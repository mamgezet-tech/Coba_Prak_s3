package manajemenkuliner;

import java.time.LocalDate;

// 1. ABSTRACT CLASS & ENCAPSULATION
abstract class Pengguna {
    private String nama;
    private String username;
    private String password;
    private String role;

    public Pengguna(String nama, String username, String password, String role) {
        this.nama = nama;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getNama() { return nama; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean cekLogin(String u, String p) {
        return this.username.equals(u) && this.password.equals(p);
    }
    public abstract String getHakAksesInfo();
}

// 2. INHERITANCE & POLYMORPHISM
class Owner extends Pengguna {
    public Owner(String nama, String u, String p) { super(nama, u, p, "Owner"); }
    @Override
    public String getHakAksesInfo() { return "Akses Penuh: Pantau Grafik Bulanan, Laporan Keuangan & Stok"; }
}

class AdminBisnis extends Pengguna {
    public AdminBisnis(String nama, String u, String p) { super(nama, u, p, "Admin Bisnis"); }
    @Override
    public String getHakAksesInfo() { return "Kelola Produk, Update Stok & Jadwal Shift Pegawai"; }
}

class AdminKeuangan extends Pengguna {
    public AdminKeuangan(String nama, String u, String p) { super(nama, u, p, "Admin Keuangan"); }
    @Override
    public String getHakAksesInfo() { return "Kelola Arus Kas (Pemasukan & Pengeluaran) & Rekap Transaksi"; }
}

class Pegawai extends Pengguna {
    public Pegawai(String nama, String u, String p) { super(nama, u, p, "Pegawai"); }
    @Override
    public String getHakAksesInfo() { return "Lihat Shift, Absensi Kerja & Input Penjualan Harian"; }
}

// 3. CLASS DOMAIN PENDUKUNG
class Produk {
    private String kode;
    private String nama;
    private double harga;
    private int stok;

    public Produk(String kode, String nama, double harga, int stok) {
        this.kode = kode;
        this.nama = nama;
        this.harga = harga;
        this.stok = stok;
    }
    public String getKode() { return kode; }
    public String getNama() { return nama; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }
}

class Transaksi {
    private LocalDate tanggal;
    private String namaProduk;
    private int jumlah;
    private double totalHarga;

    public Transaksi(LocalDate tanggal, String namaProduk, int jumlah, double totalHarga) {
        this.tanggal = tanggal;
        this.namaProduk = namaProduk;
        this.jumlah = jumlah;
        this.totalHarga = totalHarga;
    }
    public LocalDate getTanggal() { return tanggal; }
    public String getNamaProduk() { return namaProduk; }
    public int getJumlah() { return jumlah; }
    public double getTotalHarga() { return totalHarga; }
}

class ArusKas {
    private LocalDate tanggal;
    private String jenis; // "Pemasukan" atau "Pengeluaran"
    private String keterangan;
    private double nominal;

    public ArusKas(LocalDate tanggal, String jenis, String keterangan, double nominal) {
        this.tanggal = tanggal;
        this.jenis = jenis;
        this.keterangan = keterangan;
        this.nominal = nominal;
    }
    public LocalDate getTanggal() { return tanggal; }
    public String getJenis() { return jenis; }
    public String getKeterangan() { return keterangan; }
    public double getNominal() { return nominal; }
}

class Shift {
    private String namaPegawai;
    private String hari;
    private String jamShift;

    public Shift(String namaPegawai, String hari, String jamShift) {
        this.namaPegawai = namaPegawai;
        this.hari = hari;
        this.jamShift = jamShift;
    }
    public String getNamaPegawai() { return namaPegawai; }
    public String getHari() { return hari; }
    public String getJamShift() { return jamShift; }
}