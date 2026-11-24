package manajemenkuliner;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class PanelGrafikBulanan extends JPanel {

    public PanelGrafikBulanan() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 340));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Hitung penjualan per hari selama 30 hari terakhir
        Map<LocalDate, Double> dataHarian = new LinkedHashMap<>();
        LocalDate hariIni = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            dataHarian.put(hariIni.minusDays(i), 0.0);
        }

        double maxOmzet = 1.0;
        double totalSebulan = 0.0;
        for (Transaksi t : DatabaseSementara.listTransaksi) {
            if (dataHarian.containsKey(t.getTanggal())) {
                double baru = dataHarian.get(t.getTanggal()) + t.getTotalHarga();
                dataHarian.put(t.getTanggal(), baru);
                if (baru > maxOmzet) maxOmzet = baru;
                totalSebulan += t.getTotalHarga();
            }
        }

        // Judul Grafik
        g2.setColor(new Color(33, 37, 41));
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.drawString("GRAFIK TREN PENJUALAN HARIAN (30 HARI TERAKHIR)", 25, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.setColor(new Color(100, 100, 100));
        g2.drawString(String.format("Total Omzet 30 Hari: Rp %,.0f  |  Rata-rata Harian: Rp %,.0f", totalSebulan, totalSebulan / 30), 25, 48);

        // Area Grafik
        int marginLeft = 50;
        int marginRight = 30;
        int marginTop = 70;
        int marginBottom = 45;
        int chartWidth = width - marginLeft - marginRight;
        int chartHeight = height - marginTop - marginBottom;

        // Garis Sumbu X & Y
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartHeight);
        g2.drawLine(marginLeft, marginTop + chartHeight, marginLeft + chartWidth, marginTop + chartHeight);

        // Gambar Batang (Bar) untuk 30 hari
        int barWidth = Math.max(8, (chartWidth / 30) - 6);
        int index = 0;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");

        for (Map.Entry<LocalDate, Double> entry : dataHarian.entrySet()) {
            double nilai = entry.getValue();
            int barHeight = (int) ((nilai / maxOmzet) * (chartHeight - 20));
            int x = marginLeft + 8 + index * (chartWidth / 30);
            int y = marginTop + chartHeight - barHeight;

            // Warna batang (Highlight hari ini dengan warna oranye/emas, lainnya biru modern)
            if (index == 29) {
                g2.setColor(new Color(230, 126, 34));
            } else {
                g2.setColor(new Color(41, 128, 185));
            }
            g2.fillRoundRect(x, y, barWidth, barHeight, 4, 4);

            // Persentase kontribusi harian terhadap total sebulan
            double persen = (totalSebulan > 0) ? (nilai / totalSebulan) * 100 : 0;
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
            if (index % 2 == 0 || index == 29) { // Tampilkan tiap selang 1 hari agar tidak bertumpuk
                g2.drawString(String.format("%.1f%%", persen), x - 2, y - 5);
                g2.drawString(entry.getKey().format(fmt), x - 3, marginTop + chartHeight + 15);
            }
            index++;
        }
    }
}