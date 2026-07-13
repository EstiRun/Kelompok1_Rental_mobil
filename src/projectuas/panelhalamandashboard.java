/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projectuas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
 
/**
 *
 * @author HP
 */

 

 public class panelhalamandashboard extends javax.swing.JPanel {
    
    Connection conn = Projectuas.getKoneksi();
    /**
     * Creates new form halamandashboard
     */
    public panelhalamandashboard() {
        initComponents();
        tampilDataBooking();
        tampilStatistik();
 
        tblDataBookingMobil.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    prosesPengembalian();
                }
            }
        });
    }
 
  
    private void prosesPengembalian() {
        int row = tblDataBookingMobil.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris booking yang mau diproses!");
            return;
        }
 
        String statusSaatIni = (String) tblDataBookingMobil.getValueAt(row, 8);
        if (!"Disewa".equalsIgnoreCase(statusSaatIni)) {
            JOptionPane.showMessageDialog(this, "Hanya booking berstatus 'Disewa' yang bisa diproses pengembalian.\nStatus saat ini: " + statusSaatIni);
            return;
        }
 
        int idPemesanan = (int) tblDataBookingMobil.getValueAt(row, 0);
        String namaMobil = (String) tblDataBookingMobil.getValueAt(row, 2);
        java.sql.Date tglSelesai = (java.sql.Date) tblDataBookingMobil.getValueAt(row, 4);
 
        String defaultTgl = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String inputTgl = JOptionPane.showInputDialog(this,
                "Tanggal pengembalian aktual (format yyyy-MM-dd):\nJatuh tempo sewa: " + tglSelesai,
                defaultTgl);
        if (inputTgl == null || inputTgl.trim().isEmpty()) {
            return;
        }
 
        java.sql.Date tglKembali;
        try {
            tglKembali = java.sql.Date.valueOf(inputTgl.trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah. Gunakan format yyyy-MM-dd, contoh: 2026-07-10");
            return;
        }
 
        long selisihHari = (tglKembali.getTime() - tglSelesai.getTime()) / (1000L * 60 * 60 * 24);
        int hariTerlambat = selisihHari > 0 ? (int) selisihHari : 0;
        final int DENDA_PER_HARI = 50000; 
        int totalDenda = hariTerlambat * DENDA_PER_HARI;
 
        String[] opsiKondisi = {"Tersedia", "Perbaikan"};
        String pesanKondisi = hariTerlambat > 0
                ? "Terlambat " + hariTerlambat + " hari.\nDenda keterlambatan: Rp " + totalDenda + "\n\nKondisi mobil saat kembali?"
                : "Mobil kembali tepat waktu.\n\nKondisi mobil saat kembali?";
        int pilihKondisi = JOptionPane.showOptionDialog(this, pesanKondisi, "Kondisi Mobil",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opsiKondisi, opsiKondisi[0]);
        if (pilihKondisi == -1) {
            return;
        }
        String statusMobilBaru = opsiKondisi[pilihKondisi];
 
        String statusDenda = totalDenda > 0 ? "Belum Bayar" : "Lunas";
 
        try {
            PreparedStatement pst1 = conn.prepareStatement(
                    "UPDATE pemesanan SET status = 'Selesai', tgl_kembali_aktual = ?, denda = ?, status_denda = ? WHERE id_pemesanan = ?");
            pst1.setDate(1, tglKembali);
            pst1.setInt(2, totalDenda);
            pst1.setString(3, statusDenda);
            pst1.setInt(4, idPemesanan);
            pst1.executeUpdate();
 
            PreparedStatement pst2 = conn.prepareStatement(
                    "UPDATE mobil SET status = ? WHERE nama_mobil = ?");
            pst2.setString(1, statusMobilBaru);
            pst2.setString(2, namaMobil);
            pst2.executeUpdate();
 
            
            if ("Perbaikan".equals(statusMobilBaru)) {
                String inputKeterangan = JOptionPane.showInputDialog(this,
                        "Keterangan kondisi/kerusakan " + namaMobil + " (misal: lecet depan, ban kempes, dll):", "");
                if (inputKeterangan == null) {
                    inputKeterangan = ""; // dibatalkan, tetap lanjut tanpa keterangan spesifik
                }
                String keteranganFinal = inputKeterangan.trim().isEmpty()
                        ? "Servis setelah pengembalian booking #" + idPemesanan
                        : inputKeterangan.trim() + " (booking #" + idPemesanan + ")";
 
                String inputBiaya = JOptionPane.showInputDialog(this,
                        "Estimasi biaya servis untuk " + namaMobil + " (Rp):", "0");
                if (inputBiaya != null && !inputBiaya.trim().isEmpty()) {
                    try {
                        int biayaServis = Integer.parseInt(inputBiaya.trim());
                        if (biayaServis > 0) {
                            PreparedStatement pstBiaya = conn.prepareStatement(
                                    "INSERT INTO pengeluaran (nama_mobil, keterangan, jumlah, tanggal) VALUES (?, ?, ?, ?)");
                            pstBiaya.setString(1, namaMobil);
                            pstBiaya.setString(2, keteranganFinal);
                            pstBiaya.setInt(3, biayaServis);
                            pstBiaya.setDate(4, tglKembali);
                            pstBiaya.executeUpdate();
                        }
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(this, "Biaya servis diabaikan (input bukan angka).");
                    }
                }
            }
 
            String pesanHasil = "Pengembalian berhasil diproses.";
            if (totalDenda > 0) {
                pesanHasil += "\nDenda keterlambatan: Rp " + totalDenda
                        + "\nSilakan tagih di menu Pembayaran (booking ini akan muncul sebagai \"" + idPemesanan + " (DENDA)\").";
            }
            JOptionPane.showMessageDialog(this, pesanHasil);
 
            tampilDataBooking();
            tampilStatistik();
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memproses pengembalian: " + e.getMessage());
        }
    }
     
    private void prosesApproval(boolean disetujui) {
        int row = tblDataBookingMobil.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pilih dulu baris booking yang mau diproses!");
            return;
        }
 
        int idPemesanan = (int) tblDataBookingMobil.getValueAt(row, 0);
        String namaMobil = (String) tblDataBookingMobil.getValueAt(row, 2);
        String statusSaatIni = (String) tblDataBookingMobil.getValueAt(row, 8);
 
        if (!"Menunggu Konfirmasi".equalsIgnoreCase(statusSaatIni)) {
            JOptionPane.showMessageDialog(this, "Booking ini sudah diproses sebelumnya (status: " + statusSaatIni + ")");
            return;
        }
 
        try {
            if (disetujui) {
                PreparedStatement pst1 = conn.prepareStatement(
                        "UPDATE pemesanan SET status = 'Disewa' WHERE id_pemesanan = ?");
                pst1.setInt(1, idPemesanan);
                pst1.executeUpdate();
 
                PreparedStatement pst2 = conn.prepareStatement(
                        "UPDATE mobil SET status = 'Disewa' WHERE nama_mobil = ?");
                pst2.setString(1, namaMobil);
                pst2.executeUpdate();
 
                JOptionPane.showMessageDialog(this, "Booking disetujui. Mobil " + namaMobil + " sekarang berstatus Disewa.");
            } else {
                PreparedStatement pst1 = conn.prepareStatement(
                        "UPDATE pemesanan SET status = 'Ditolak' WHERE id_pemesanan = ?");
                pst1.setInt(1, idPemesanan);
                pst1.executeUpdate();
 
                JOptionPane.showMessageDialog(this, "Booking ditolak.");
            }
 
            tampilDataBooking();
            tampilStatistik();
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memproses booking: " + e.getMessage());
        }
    }
 
    private void tampilStatistik() {
        try {
            Statement st = conn.createStatement();
 
            ResultSet rsTotalMobil = st.executeQuery("SELECT COUNT(*) AS total FROM mobil");
            if (rsTotalMobil.next()) {
                txtTotalMobil.setText(String.valueOf(rsTotalMobil.getInt("total")));
            }
 
            ResultSet rsDisewa = st.executeQuery("SELECT COUNT(*) AS total FROM pemesanan WHERE status = 'Disewa'");
            if (rsDisewa.next()) {
                txtDisewa.setText(String.valueOf(rsDisewa.getInt("total")));
            }
 
            ResultSet rsCustomer = st.executeQuery("SELECT COUNT(*) AS total FROM customer");
            if (rsCustomer.next()) {
                txtCustomer.setText(String.valueOf(rsCustomer.getInt("total")));
            }
 
            ResultSet rsPendapatan = st.executeQuery("SELECT SUM(biaya_sewa + denda) AS total FROM pemesanan WHERE status = 'Selesai'");
            if (rsPendapatan.next()) {
                txtPendapatan.setText(String.valueOf(rsPendapatan.getInt("total")));
            }
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat statistik: " + e.getMessage());
        }
    }
 
  private void tampilDataBooking() {
    DefaultTableModel model = new DefaultTableModel() {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
 
    model.addColumn("ID");
    model.addColumn("Customer");
    model.addColumn("Mobil");
    model.addColumn("Tgl Sewa");
    model.addColumn("Tgl Selesai");
    model.addColumn("Total Hari");
    model.addColumn("Biaya Sewa");
    model.addColumn("Metode Bayar");
    model.addColumn("Status");
    model.addColumn("Denda");
    model.addColumn("Status Denda");
    model.addColumn("Kondisi Mobil");
 
    tblDataBookingMobil.setModel(model);
 
    try {
        String sql = "SELECT p.id_pemesanan, p.nama_customer, p.nama_mobil, p.tgl_mulai, p.tgl_selesai, "
                   + "p.total_hari, p.biaya_sewa, p.metode_bayar, p.status, p.denda, p.status_denda, m.status AS kondisi_mobil "
                   + "FROM pemesanan p "
                   + "LEFT JOIN mobil m ON p.nama_mobil = m.nama_mobil "
                   + "ORDER BY p.id_pemesanan DESC";
 
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
 
        while (rs.next()) {
            String metodeBayar = rs.getString("metode_bayar");
            if (metodeBayar == null) {
                metodeBayar = "-";
            }
 
            int denda = rs.getInt("denda");
            String dendaText = denda > 0 ? "Rp " + denda : "-";
 
            String statusDenda = rs.getString("status_denda");
            if (statusDenda == null) {
                statusDenda = "-";
            }
 
            String kondisiMobil = rs.getString("kondisi_mobil");
            if (kondisiMobil == null) {
                kondisiMobil = "-";
            }
 
            model.addRow(new Object[]{
                rs.getInt("id_pemesanan"),
                rs.getString("nama_customer"),
                rs.getString("nama_mobil"),
                rs.getDate("tgl_mulai"),
                rs.getDate("tgl_selesai"),
                rs.getInt("total_hari"),
                rs.getInt("biaya_sewa"),
                metodeBayar,
                rs.getString("status"),
                dendaText,
                statusDenda,
                kondisiMobil
            });
        }
 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat data booking: " + e.getMessage());
    }
}
 

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        txtRentalSystem = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDataBookingMobil = new javax.swing.JTable();
        txtTotalMobil = new javax.swing.JTextField();
        txtDisewa = new javax.swing.JTextField();
        txtCustomer = new javax.swing.JTextField();
        txtPendapatan = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        Jlabel1 = new javax.swing.JLabel();
        Jlabel2 = new javax.swing.JLabel();
        Jlabel3 = new javax.swing.JLabel();
        Jlabel4 = new javax.swing.JLabel();
        btnSetuju = new javax.swing.JButton();
        btnTolak = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        jPanel1.setBackground(new java.awt.Color(47, 164, 215));

        txtRentalSystem.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        txtRentalSystem.setText("Dashboard Rental Mobil  Jaya Abadi");

        tblDataBookingMobil.setBackground(new java.awt.Color(231, 111, 46));
        tblDataBookingMobil.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Customer", "Mobil", "Tgl Sewa", "Status", "Tgl Mulai", "Total Hari", "Biaya Sewa", "Metode bayar", "Status", "Denda", "Status Denda", "Kondisi Mobil"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDataBookingMobil);

        txtTotalMobil.setBackground(new java.awt.Color(245, 233, 216));
        txtTotalMobil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalMobilActionPerformed(evt);
            }
        });

        txtDisewa.setBackground(new java.awt.Color(245, 233, 216));
        txtDisewa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDisewaActionPerformed(evt);
            }
        });

        txtCustomer.setBackground(new java.awt.Color(245, 233, 216));
        txtCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCustomerActionPerformed(evt);
            }
        });

        txtPendapatan.setBackground(new java.awt.Color(245, 233, 216));
        txtPendapatan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPendapatanActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel5.setText("DATA BOOKING MOBIL");

        Jlabel1.setText("Total Mobil");

        Jlabel2.setText("Disewa");

        Jlabel3.setText("Customer");

        Jlabel4.setText("Pendapatan");

        btnSetuju.setText("SETUJU");
        btnSetuju.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSetujuActionPerformed(evt);
            }
        });

        btnTolak.setText("TOLAK");
        btnTolak.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTolakActionPerformed(evt);
            }
        });

        jLabel2.setText("KONFIRMASI PEMBAYARAN");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtTotalMobil, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Jlabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 300, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtDisewa, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Jlabel2))
                .addGap(41, 41, 41)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Jlabel3))
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Jlabel4)
                    .addComponent(txtPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtRentalSystem)
                        .addGap(0, 578, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(btnTolak)
                                .addGap(37, 37, 37)
                                .addComponent(btnSetuju))
                            .addComponent(jLabel5)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(59, 59, 59)
                        .addComponent(jLabel2)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(txtRentalSystem)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Jlabel1)
                    .addComponent(Jlabel2)
                    .addComponent(Jlabel3)
                    .addComponent(Jlabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTotalMobil, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDisewa, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPendapatan, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTolak)
                    .addComponent(btnSetuju))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtTotalMobilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalMobilActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalMobilActionPerformed

    private void txtDisewaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDisewaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDisewaActionPerformed

    private void txtCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCustomerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCustomerActionPerformed

    private void txtPendapatanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPendapatanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPendapatanActionPerformed

    private void btnTolakActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTolakActionPerformed
        // TODO add your handling code here:
        prosesApproval(false);
    }//GEN-LAST:event_btnTolakActionPerformed

    private void btnSetujuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSetujuActionPerformed
        // TODO add your handling code here:
        prosesApproval(true);
    }//GEN-LAST:event_btnSetujuActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Jlabel1;
    private javax.swing.JLabel Jlabel2;
    private javax.swing.JLabel Jlabel3;
    private javax.swing.JLabel Jlabel4;
    private javax.swing.JButton btnSetuju;
    private javax.swing.JButton btnTolak;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDataBookingMobil;
    private javax.swing.JTextField txtCustomer;
    private javax.swing.JTextField txtDisewa;
    private javax.swing.JTextField txtPendapatan;
    private javax.swing.JLabel txtRentalSystem;
    private javax.swing.JTextField txtTotalMobil;
    // End of variables declaration//GEN-END:variables
}
