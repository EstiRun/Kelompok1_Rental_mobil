/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projectuas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;

/**
 *
 * @author HP
 */
public class panelhalamanpembayaran extends javax.swing.JPanel {
 
        Connection conn = Projectuas.getKoneksi();
    private String lastIdBooking, lastNamaMobil, lastCustomer, lastMetodeBayar, lastTotalTagihan;
    private boolean isPembayaranDenda = false;
    /**
     * Creates new form halamanpembayaran
     */
    public panelhalamanpembayaran() {
        initComponents();
        bgMetodeBayar = new ButtonGroup();
        bgMetodeBayar.add(rbTransfer);
        bgMetodeBayar.add(rbTunai);
        bgMetodeBayar.add(rbWallet);
        loadIDBooking();
 
    }
 
    private void loadIDBooking() {
        try {
            cmbIDBooking.removeAllItems();
      
            String sqlSewa = "SELECT id_pemesanan FROM pemesanan "
                       + "WHERE status = 'Disewa' "
                       + "AND id_pemesanan NOT IN ("
                       + "  SELECT id_pemesanan FROM pembayaran WHERE id_pemesanan IS NOT NULL"
                       + ")";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sqlSewa);
            while (rs.next()) {
                cmbIDBooking.addItem(rs.getString("id_pemesanan"));
            }
 
            String sqlDenda = "SELECT id_pemesanan FROM pemesanan "
                       + "WHERE status = 'Selesai' AND status_denda = 'Belum Bayar'";
            ResultSet rsDenda = st.executeQuery(sqlDenda);
            while (rsDenda.next()) {
                cmbIDBooking.addItem(rsDenda.getString("id_pemesanan") + " (DENDA)");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat ID Booking: " + e.getMessage());
        }
    }
 
  private void loadDetailBooking() {
    try {
        String pilihan = (String) cmbIDBooking.getSelectedItem();
        if (pilihan == null) return;

        isPembayaranDenda = pilihan.endsWith("(DENDA)");
        String idBooking = isPembayaranDenda
                ? pilihan.replace("(DENDA)", "").trim()
                : pilihan;

        String kolomTagihan = isPembayaranDenda ? "denda" : "biaya_sewa";
        String sql = "SELECT nama_customer, nama_mobil, " + kolomTagihan + " AS tagihan, keterangan_denda "
                   + "FROM pemesanan WHERE id_pemesanan = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, idBooking);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            txtCustomer.setText(rs.getString("nama_customer"));
            txtTotalTagihan.setText(rs.getString("tagihan"));

            if (isPembayaranDenda) {
                String namaMobil = rs.getString("nama_mobil");
                String keterangan = rs.getString("keterangan_denda");
                txtKeterangan.setText("Denda mobil " + namaMobil + "\n"
                        + (keterangan != null ? keterangan : "Keterangan tidak tersedia."));
            } else {
                txtKeterangan.setText("Pembayaran sewa normal (tidak ada denda).");
            }
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal memuat detail booking: " + e.getMessage());
    }
}
    
    private void bersihkanForm() {
    txtCustomer.setText("");
    txtTotalTagihan.setText("");
    txtKeterangan.setText("");
    bgMetodeBayar.clearSelection();
}
    
   private void konfirmasiPembayaran() {
    try {
        String pilihan = (String) cmbIDBooking.getSelectedItem();
        if (pilihan == null) {
            JOptionPane.showMessageDialog(this, "Pilih ID Booking terlebih dahulu!");
            return;
        }
        String idBooking = isPembayaranDenda ? pilihan.replace("(DENDA)", "").trim() : pilihan;
 
        String metodeBayar = "";
        if (rbTransfer.isSelected()) metodeBayar = "Transfer";
        else if (rbTunai.isSelected())   metodeBayar = "Tunai";
        else if (rbWallet.isSelected())  metodeBayar = "E-Wallet";
 
        if (metodeBayar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih metode pembayaran!");
            return;
        }
 
        String namaMobil = null;
        String cekSql = "SELECT nama_mobil FROM pemesanan WHERE id_pemesanan = ?";
        PreparedStatement cekPst = conn.prepareStatement(cekSql);
        cekPst.setString(1, idBooking);
        ResultSet rsMobil = cekPst.executeQuery();
        if (rsMobil.next()) {
            namaMobil = rsMobil.getString("nama_mobil");
        }
 
        if (isPembayaranDenda) {
            
            String sql = "UPDATE pemesanan SET status_denda = 'Lunas' WHERE id_pemesanan = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, idBooking);
            pst.executeUpdate();
        } else {
            
            String sql = "UPDATE pemesanan SET metode_bayar = ? WHERE id_pemesanan = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, metodeBayar);
            pst.setString(2, idBooking);
            pst.executeUpdate();
        }
 
        
        String defaultTglBayar = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String inputTglBayar = JOptionPane.showInputDialog(this,
                "Tanggal pembayaran (format yyyy-MM-dd):", defaultTglBayar);
        if (inputTglBayar == null || inputTglBayar.trim().isEmpty()) {
            return; 
        }
        java.sql.Date tglBayar;
        try {
            tglBayar = java.sql.Date.valueOf(inputTglBayar.trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah. Gunakan format yyyy-MM-dd, contoh: 2026-07-10");
            return;
        }
 
     
        String insertBayarSql = "INSERT INTO pembayaran (id_pemesanan, metode_bayar, jumlah_bayar, tanggal_bayar, status_bayar) "
                               + "VALUES (?, ?, ?, ?, 'Lunas')";
        PreparedStatement insBayarPst = conn.prepareStatement(insertBayarSql);
        insBayarPst.setString(1, idBooking);
        insBayarPst.setString(2, metodeBayar);
        insBayarPst.setString(3, txtTotalTagihan.getText());
        insBayarPst.setDate(4, tglBayar);
        insBayarPst.executeUpdate();
 
        
        lastIdBooking = idBooking + (isPembayaranDenda ? " (Denda)" : "");
        lastNamaMobil = namaMobil;
        lastCustomer = txtCustomer.getText();
        lastMetodeBayar = metodeBayar;
        lastTotalTagihan = txtTotalTagihan.getText();
 
        JOptionPane.showMessageDialog(this, "Pembayaran " + (isPembayaranDenda ? "denda " : "") + "berhasil dikonfirmasi!\nMetode: " + metodeBayar);
        btnCetakKwitansi.setEnabled(true); // aktifkan tombol cetak
        bersihkanForm();
        loadIDBooking();
 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal konfirmasi pembayaran: " + e.getMessage());
    }
}
   private void cetakKwitansi() {
    if (lastIdBooking == null) {
        JOptionPane.showMessageDialog(this, "Belum ada transaksi yang dikonfirmasi!");
        return;
    }
 
    String isiKwitansi =
              "================================================\n"
            + "         RENTAL MOBIL JAYA ABADI\n"
            + "              KWITANSI PEMBAYARAN\n"
            + "================================================\n\n"
            + "No. Booking     : " + lastIdBooking + "\n"
            + "Nama Customer   : " + lastCustomer + "\n"
            + "Mobil Disewa    : " + lastNamaMobil + "\n"
            + "Metode Bayar    : " + lastMetodeBayar + "\n"
            + "Total Tagihan   : Rp " + lastTotalTagihan + "\n"
            + "Tanggal Cetak   : " + new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm").format(new java.util.Date()) + "\n\n"
            + "------------------------------------------------\n"
            + "      Terima kasih telah menggunakan jasa kami\n"
            + "================================================";
 
    javax.swing.JTextArea textArea = new javax.swing.JTextArea(isiKwitansi);
    textArea.setEditable(false);
    textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
 
    javax.swing.JButton btnPrint = new javax.swing.JButton("Print");
    btnPrint.addActionListener(e -> {
        try {
            textArea.print();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak: " + ex.getMessage());
        }
    });
 
    javax.swing.JPanel panelKwitansi = new javax.swing.JPanel(new java.awt.BorderLayout());
    panelKwitansi.add(new javax.swing.JScrollPane(textArea), java.awt.BorderLayout.CENTER);
    panelKwitansi.add(btnPrint, java.awt.BorderLayout.SOUTH);
 
    javax.swing.JOptionPane.showMessageDialog(this, panelKwitansi, "Preview Kwitansi", javax.swing.JOptionPane.PLAIN_MESSAGE);
}
 
   
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bgMetodeBayar = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cmbIDBooking = new javax.swing.JComboBox<>();
        txtCustomer = new javax.swing.JTextField();
        txtTotalTagihan = new javax.swing.JTextField();
        btnKonfirmasiPembayaran = new javax.swing.JButton();
        rbTransfer = new javax.swing.JRadioButton();
        rbTunai = new javax.swing.JRadioButton();
        rbWallet = new javax.swing.JRadioButton();
        btnCetakKwitansi = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtKeterangan = new javax.swing.JTextArea();

        jPanel1.setBackground(new java.awt.Color(47, 164, 215));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Rental Jaya Abadi");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("PEMBAYARAN");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("ID Booking      :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Customer        :");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Total Tagihan  :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Metode Bayar  :");

        cmbIDBooking.setBackground(new java.awt.Color(231, 111, 46));
        cmbIDBooking.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "BK001", "BK002", "BK003" }));
        cmbIDBooking.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbIDBookingActionPerformed(evt);
            }
        });

        txtCustomer.setBackground(new java.awt.Color(245, 233, 216));

        txtTotalTagihan.setBackground(new java.awt.Color(231, 111, 46));

        btnKonfirmasiPembayaran.setBackground(new java.awt.Color(231, 111, 46));
        btnKonfirmasiPembayaran.setForeground(new java.awt.Color(62, 44, 35));
        btnKonfirmasiPembayaran.setText("KONFIRMASI PEMBAYARAN");
        btnKonfirmasiPembayaran.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKonfirmasiPembayaranActionPerformed(evt);
            }
        });

        rbTransfer.setText("Transfer");

        rbTunai.setText("Tunai");

        rbWallet.setText("Wallet");

        btnCetakKwitansi.setBackground(new java.awt.Color(62, 44, 35));
        btnCetakKwitansi.setForeground(new java.awt.Color(231, 111, 46));
        btnCetakKwitansi.setText("CETAK BUKTI PEMBAYRAN");
        btnCetakKwitansi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCetakKwitansiActionPerformed(evt);
            }
        });

        jLabel7.setText("Keterangan          :");

        txtKeterangan.setEditable(false);
        txtKeterangan.setBackground(new java.awt.Color(245, 233, 216));
        txtKeterangan.setColumns(20);
        txtKeterangan.setLineWrap(true);
        txtKeterangan.setRows(5);
        txtKeterangan.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtKeterangan);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(209, 209, 209)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(226, 226, 226)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addGap(18, 18, 18)
                                    .addComponent(cmbIDBooking, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel5)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtTotalTagihan))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addGap(18, 18, 18)
                                    .addComponent(txtCustomer))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(rbTunai)
                                        .addComponent(rbTransfer)
                                        .addComponent(rbWallet))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(105, 105, 105)
                        .addComponent(btnKonfirmasiPembayaran)
                        .addGap(57, 57, 57)
                        .addComponent(btnCetakKwitansi)))
                .addContainerGap(82, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbIDBooking, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtTotalTagihan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(rbTransfer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbTunai)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbWallet)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel7))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnKonfirmasiPembayaran, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCetakKwitansi, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(74, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbIDBookingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbIDBookingActionPerformed
        // TODO add your handling code here:
        loadDetailBooking();
    }//GEN-LAST:event_cmbIDBookingActionPerformed

    private void btnKonfirmasiPembayaranActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKonfirmasiPembayaranActionPerformed
        // TODO add your handling code here:
        konfirmasiPembayaran();
    }//GEN-LAST:event_btnKonfirmasiPembayaranActionPerformed

    private void btnCetakKwitansiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCetakKwitansiActionPerformed
        // TODO add your handling code here:
         cetakKwitansi();
    }//GEN-LAST:event_btnCetakKwitansiActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgMetodeBayar;
    private javax.swing.JButton btnCetakKwitansi;
    private javax.swing.JButton btnKonfirmasiPembayaran;
    private javax.swing.JComboBox<String> cmbIDBooking;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton rbTransfer;
    private javax.swing.JRadioButton rbTunai;
    private javax.swing.JRadioButton rbWallet;
    private javax.swing.JTextField txtCustomer;
    private javax.swing.JTextArea txtKeterangan;
    private javax.swing.JTextField txtTotalTagihan;
    // End of variables declaration//GEN-END:variables
}
