package manajemenkuliner;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class MainAplikasi extends JFrame {
    private Pengguna currentUser;
    private JTabbedPane tabbedPane;

    public MainAplikasi(Pengguna user) {
        this.currentUser = user;
        setTitle("Sistem Manajemen Usaha Kuliner - Login: " + user.getNama() + " [" + user.getRole() + "]");
        setSize(980, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header Atas
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel lblJudul = new JLabel("MANJERR KULINER - SISTEM TERINTEGRASI");
        lblJudul.setForeground(Color.WHITE);
        lblJudul.setFont(new Font("SansSerif", Font.BOLD, 18));

        JLabel lblSub = new JLabel(user.getNama() + " | " + user.getHakAksesInfo());
        lblSub.setForeground(new Color(236, 240, 241));
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(lblJudul);
        titleBox.add(lblSub);

        JButton btnLogout = new JButton("Logout / Ganti Akun");
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            dispose();
            tampilkanFormLogin();
        });

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Tab Navigasi Berdasarkan Role (RBAC)
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        if (user instanceof Owner) {
            tabbedPane.addTab("Dashboard & Grafik Penjualan (Owner)", buatPanelDashboardOwner());
            tabbedPane.addTab("Laporan Keuangan", buatPanelKeuangan(false));
            tabbedPane.addTab("Pantau Stok Produk", buatPanelProduk(false));
        } else if (user instanceof AdminBisnis) {
            tabbedPane.addTab("Kelola Produk & Stok", buatPanelProduk(true));
            tabbedPane.addTab("Kelola Shift Pegawai", buatPanelShift(true));
        } else if (user instanceof AdminKeuangan) {
            tabbedPane.addTab("Laporan & Arus Kas Keuangan", buatPanelKeuangan(true));
            tabbedPane.addTab("Grafik Penjualan", buatPanelDashboardOwner());
        } else if (user instanceof Pegawai) {
            tabbedPane.addTab("Catat Transaksi Penjualan", buatPanelKasirPegawai());
            tabbedPane.addTab("Jadwal Shift & Absensi", buatPanelShift(false));
        }

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- PANEL 1: DASHBOARD OWNER (GRAFIK + PERSENTASE PRODUK) ---
    private JPanel buatPanelDashboardOwner() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Bagian Atas: Grafik Harian 30 Hari
        PanelGrafikBulanan grafik = new PanelGrafikBulanan();
        panel.add(grafik, BorderLayout.NORTH);

        // Bagian Bawah: Tabel Persentase Penjualan per Produk
        String[] kolom = {"Nama Produk", "Total Terjual (Porsi)", "Total Omzet (Rp)", "Persentase Kontribusi (%)"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);

        Map<String, Integer> qtyMap = new HashMap<>();
        Map<String, Double> omzetMap = new HashMap<>();
        double grandTotal = 0;

        for (Transaksi t : DatabaseSementara.listTransaksi) {
            qtyMap.put(t.getNamaProduk(), qtyMap.getOrDefault(t.getNamaProduk(), 0) + t.getJumlah());
            omzetMap.put(t.getNamaProduk(), omzetMap.getOrDefault(t.getNamaProduk(), 0.0) + t.getTotalHarga());
            grandTotal += t.getTotalHarga();
        }

        for (String namaProd : omzetMap.keySet()) {
            double omzet = omzetMap.get(namaProd);
            double persen = (grandTotal > 0) ? (omzet / grandTotal) * 100 : 0;
            model.addRow(new Object[]{
                namaProd,
                qtyMap.get(namaProd) + " porsi",
                String.format("Rp %,.0f", omzet),
                String.format("%.2f %%", persen)
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Persentase Penjualan Berdasarkan Produk (1 Bulan)"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // --- PANEL 2: KEUANGAN (ARUS KAS) ---
    private JPanel buatPanelKeuangan(boolean bisaEdit) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        String[] kolom = {"Tanggal", "Jenis Arus Kas", "Keterangan", "Nominal (Rp)"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);

        double totalMasuk = 0, totalKeluar = 0;
        for (int i = DatabaseSementara.listArusKas.size() - 1; i >= 0; i--) {
            ArusKas ak = DatabaseSementara.listArusKas.get(i);
            model.addRow(new Object[]{ak.getTanggal(), ak.getJenis(), ak.getKeterangan(), String.format("Rp %,.0f", ak.getNominal())});
            if (ak.getJenis().equals("Pemasukan")) totalMasuk += ak.getNominal();
            else totalKeluar += ak.getNominal();
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel lblSummary = new JLabel(String.format(
            "Total Pemasukan: Rp %,.0f   |   Total Pengeluaran: Rp %,.0f   |   Laba Bersih: Rp %,.0f",
            totalMasuk, totalKeluar, (totalMasuk - totalKeluar)
        ));
        lblSummary.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSummary.setForeground(new Color(39, 174, 96));
        panel.add(lblSummary, BorderLayout.NORTH);

        if (bisaEdit) {
            JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JComboBox<String> cbJenis = new JComboBox<>(new String[]{"Pengeluaran", "Pemasukan"});
            JTextField txtKet = new JTextField(20);
            JTextField txtNominal = new JTextField(10);
            JButton btnSimpan = new JButton("Catat Keuangan");

            formPanel.add(new JLabel("Jenis:")); formPanel.add(cbJenis);
            formPanel.add(new JLabel("Keterangan:")); formPanel.add(txtKet);
            formPanel.add(new JLabel("Nominal:")); formPanel.add(txtNominal);
            formPanel.add(btnSimpan);

            btnSimpan.addActionListener(e -> {
                try {
                    double nom = Double.parseDouble(txtNominal.getText());
                    DatabaseSementara.listArusKas.add(new ArusKas(LocalDate.now(), cbJenis.getSelectedItem().toString(), txtKet.getText(), nom));
                    JOptionPane.showMessageDialog(this, "Data keuangan berhasil dicatat!");
                    tabbedPane.setComponentAt(0, buatPanelKeuangan(true));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Masukkan nominal angka yang valid!");
                }
            });
            panel.add(formPanel, BorderLayout.SOUTH);
        }
        return panel;
    }

    // --- PANEL 3: KELOLA PRODUK & STOK (DENGAN FITUR RESTOCK) ---
    private JPanel buatPanelProduk(boolean bisaEdit) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        String[] kolom = {"Kode", "Nama Produk", "Harga Jual (Rp)", "Sisa Stok"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        for (Produk p : DatabaseSementara.listProduk) {
            model.addRow(new Object[]{p.getKode(), p.getNama(), String.format("Rp %,.0f", p.getHarga()), p.getStok()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(25);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        if (bisaEdit) {
            JPanel formContainer = new JPanel(new GridLayout(2, 1, 5, 5));
            
            // Form 1: Tambah Produk Baru
            JPanel formBaru = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField txtKode = new JTextField(5);
            JTextField txtNama = new JTextField(12);
            JTextField txtHarga = new JTextField(8);
            JTextField txtStok = new JTextField(4);
            JButton btnTambahProd = new JButton("Tambah Produk Baru");

            formBaru.setBorder(BorderFactory.createTitledBorder("Tambah Produk Baru"));
            formBaru.add(new JLabel("Kode:")); formBaru.add(txtKode);
            formBaru.add(new JLabel("Nama:")); formBaru.add(txtNama);
            formBaru.add(new JLabel("Harga:")); formBaru.add(txtHarga);
            formBaru.add(new JLabel("Stok Awal:")); formBaru.add(txtStok);
            formBaru.add(btnTambahProd);

            // Form 2: Tambah Stok / Restock Produk yang Sudah Ada
            JPanel formStok = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JComboBox<String> cbPilihProduk = new JComboBox<>();
            for (Produk p : DatabaseSementara.listProduk) {
                cbPilihProduk.addItem(p.getKode() + " - " + p.getNama());
            }
            JTextField txtTambahStok = new JTextField(5);
            JButton btnRestock = new JButton("Tambah Stok (Restock)");

            formStok.setBorder(BorderFactory.createTitledBorder("Tambah Stok / Restock Barang"));
            formStok.add(new JLabel("Pilih Produk:")); formStok.add(cbPilihProduk);
            formStok.add(new JLabel("Jumlah Tambahan:")); formStok.add(txtTambahStok);
            formStok.add(btnRestock);

            formContainer.add(formBaru);
            formContainer.add(formStok);
            panel.add(formContainer, BorderLayout.SOUTH);

            // Aksi Tambah Produk Baru
            btnTambahProd.addActionListener(e -> {
                try {
                    DatabaseSementara.listProduk.add(new Produk(
                        txtKode.getText(), txtNama.getText(),
                        Double.parseDouble(txtHarga.getText()), Integer.parseInt(txtStok.getText())
                    ));
                    JOptionPane.showMessageDialog(this, "Produk baru berhasil ditambahkan!");
                    tabbedPane.setComponentAt(0, buatPanelProduk(true));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Pastikan harga dan stok berupa angka!");
                }
            });

            // Aksi Tambah Stok (Restock)
            btnRestock.addActionListener(e -> {
                try {
                    int selectedIndex = cbPilihProduk.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        int tambahan = Integer.parseInt(txtTambahStok.getText());
                        Produk p = DatabaseSementara.listProduk.get(selectedIndex);
                        p.setStok(p.getStok() + tambahan);

                        JOptionPane.showMessageDialog(this, "Stok " + p.getNama() + " berhasil ditambah! Stok sekarang: " + p.getStok());
                        tabbedPane.setComponentAt(0, buatPanelProduk(true));
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Masukkan jumlah tambahan stok berupa angka yang valid!");
                }
            });
        }
        return panel;
    }

    // --- PANEL 4: PENCATATAN PENJUALAN (PEGAWAI) ---
    private JPanel buatPanelKasirPegawai() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> cbProduk = new JComboBox<>();
        for (Produk p : DatabaseSementara.listProduk) cbProduk.addItem(p.getNama());

        JTextField txtJumlah = new JTextField(5);
        JButton btnCatat = new JButton("Simpan Transaksi Penjualan");

        form.add(new JLabel("Pilih Produk:")); form.add(cbProduk);
        form.add(new JLabel("Jumlah Terjual:")); form.add(txtJumlah);
        form.add(btnCatat);

        String[] kolom = {"Tanggal", "Nama Produk", "Jumlah", "Total Harga"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        for (int i = DatabaseSementara.listTransaksi.size() - 1; i >= Math.max(0, DatabaseSementara.listTransaksi.size() - 15); i--) {
            Transaksi t = DatabaseSementara.listTransaksi.get(i);
            model.addRow(new Object[]{t.getTanggal(), t.getNamaProduk(), t.getJumlah(), String.format("Rp %,.0f", t.getTotalHarga())});
        }
        JTable table = new JTable(model);

        btnCatat.addActionListener(e -> {
            try {
                int idx = cbProduk.getSelectedIndex();
                Produk p = DatabaseSementara.listProduk.get(idx);
                int qty = Integer.parseInt(txtJumlah.getText());
                if (qty > p.getStok()) {
                    JOptionPane.showMessageDialog(this, "Stok tidak mencukupi! Sisa stok: " + p.getStok());
                    return;
                }
                double total = p.getHarga() * qty;
                p.setStok(p.getStok() - qty); // Kurangi stok otomatis
                DatabaseSementara.listTransaksi.add(new Transaksi(LocalDate.now(), p.getNama(), qty, total));
                DatabaseSementara.listArusKas.add(new ArusKas(LocalDate.now(), "Pemasukan", "Penjualan " + p.getNama() + " (" + qty + "x)", total));

                JOptionPane.showMessageDialog(this, "Transaksi dicatat! Data grafik Owner & Keuangan otomatis terbarui.");
                tabbedPane.setComponentAt(0, buatPanelKasirPegawai());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Masukkan jumlah angka yang valid!");
            }
        });

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- PANEL 5: SHIFT & ABSENSI ---
    private JPanel buatPanelShift(boolean isAdmin) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] kolom = {"Nama Pegawai", "Hari Kerja", "Jam Shift"};
        DefaultTableModel model = new DefaultTableModel(kolom, 0);
        for (Shift s : DatabaseSementara.listShift) {
            model.addRow(new Object[]{s.getNamaPegawai(), s.getHari(), s.getJamShift()});
        }
        JTable table = new JTable(model);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        if (isAdmin) {
            JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField txtNama = new JTextField(12);
            JTextField txtHari = new JTextField(12);
            JTextField txtJam = new JTextField(15);
            JButton btnAdd = new JButton("Tambah Jadwal Shift");

            form.add(new JLabel("Pegawai:")); form.add(txtNama);
            form.add(new JLabel("Hari:")); form.add(txtHari);
            form.add(new JLabel("Jam:")); form.add(txtJam);
            form.add(btnAdd);

            btnAdd.addActionListener(e -> {
                DatabaseSementara.listShift.add(new Shift(txtNama.getText(), txtHari.getText(), txtJam.getText()));
                tabbedPane.setComponentAt(1, buatPanelShift(true));
            });
            panel.add(form, BorderLayout.SOUTH);
        } else {
            JButton btnAbsen = new JButton("Klik untuk Absensi Masuk Hari Ini");
            btnAbsen.setBackground(new Color(39, 174, 96));
            btnAbsen.setForeground(Color.WHITE);
            btnAbsen.addActionListener(e -> {
                String log = currentUser.getNama() + " hadir pada " + LocalDate.now();
                DatabaseSementara.listAbsensi.add(log);
                JOptionPane.showMessageDialog(this, "Absensi Berhasil!\n" + log);
            });
            panel.add(btnAbsen, BorderLayout.SOUTH);
        }
        return panel;
    }

    // --- FORM LOGIN AWAL ---
    public static void tampilkanFormLogin() {
        JFrame loginFrame = new JFrame("Login - Sistem Manajemen Usaha Kuliner");
        loginFrame.setSize(420, 280);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));

        JTextField txtUser = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cbQuickLogin = new JComboBox<>(new String[]{
            "-- Pilih Cepat Akun Demo --",
            "Owner (owner / owner1234)",
            "Admin Bisnis (bisnis / bisnis1234)",
            "Admin Keuangan (keuangan / keuangan1234)",
            "Pegawai (pegawai / pegawai1234)"
        });

        cbQuickLogin.addActionListener(e -> {
            int idx = cbQuickLogin.getSelectedIndex();
            if (idx == 1) { txtUser.setText("owner"); txtPass.setText("owner1234"); }
            else if (idx == 2) { txtUser.setText("bisnis"); txtPass.setText("bisnis1234"); }
            else if (idx == 3) { txtUser.setText("keuangan"); txtPass.setText("keuangan1234"); }
            else if (idx == 4) { txtUser.setText("pegawai"); txtPass.setText("pegawai1234"); }
        });

        panel.add(new JLabel("Isi Otomatis (Demo):")); panel.add(cbQuickLogin);
        panel.add(new JLabel("Username:")); panel.add(txtUser);
        panel.add(new JLabel("Password:")); panel.add(txtPass);

        JButton btnLogin = new JButton("Masuk ke Platform");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);

        btnLogin.addActionListener(e -> {
            String u = txtUser.getText();
            String p = new String(txtPass.getPassword());
            for (Pengguna user : DatabaseSementara.listPengguna) {
                if (user.cekLogin(u, p)) {
                    loginFrame.dispose();
                    new MainAplikasi(user).setVisible(true);
                    return;
                }
            }
            JOptionPane.showMessageDialog(loginFrame, "Username atau Password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
        });

        loginFrame.add(panel, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnLogin);
        loginFrame.add(btnPanel, BorderLayout.SOUTH);

        loginFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> tampilkanFormLogin());
    }
}