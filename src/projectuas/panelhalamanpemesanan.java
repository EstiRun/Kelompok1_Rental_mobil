/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projectuas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

/**
 *
 * @author HP
 */
   public class panelhalamanpemesanan extends javax.swing.JPanel {
 
    Connection conn = Projectuas.getKoneksi();
    /**
     * Creates new form halamanpemesanan
     */
    public panelhalamanpemesanan() {
        initComponents();
        loadDataMobil();
        pasangListenerAutoHitung();
    }
    public void refreshData() {
    loadDataMobil();
}
 
   
    public void setMobilTerpilih(String namaMobil) {
        cmbMobil.setSelectedItem(namaMobil);
        hitungTotalHariDanBiaya();
    }
 
    private void pasangListenerAutoHitung() {
        DocumentListener autoHitung = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { hitungTotalHariDanBiaya(); }
            @Override
            public void removeUpdate(DocumentEvent e) { hitungTotalHariDanBiaya(); }
            @Override
            public void changedUpdate(DocumentEvent e) { hitungTotalHariDanBiaya(); }
        };
        txtTanggalMulai.getDocument().addDocumentListener(autoHitung);
        txtTanggalSelesai.getDocument().addDocumentListener(autoHitung);
 
        cmbMobil.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hitungTotalHariDanBiaya();
            }
        });
    }
 
    private void loadDataMobil() {
        try {
            cmbMobil.removeAllItems();
            String sql = "SELECT nama_mobil FROM mobil WHERE status = 'Tersedia'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                cmbMobil.addItem(rs.getString("nama_mobil"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data mobil: " + e.getMessage());
        }
    }
 
    private void hitungTotalHariDanBiaya() {
        try {
            String tglMulaiStr   = txtTanggalMulai.getText().trim();
            String tglSelesaiStr = txtTanggalSelesai.getText().trim();
 
            if (tglMulaiStr.isEmpty() || tglSelesaiStr.isEmpty()) return;
            if (tglMulaiStr.length() < 10 || tglSelesaiStr.length() < 10) return;
 
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            java.util.Date tglMulai   = sdf.parse(tglMulaiStr);
            java.util.Date tglSelesai = sdf.parse(tglSelesaiStr);
 
            long diff  = tglSelesai.getTime() - tglMulai.getTime();
            long hari  = diff / (1000 * 60 * 60 * 24);
 
            if (hari <= 0) {
                txtTotalHari.setText("");
                txtBiayaSewa.setText("");
                return;
            }
 
            txtTotalHari.setText(String.valueOf(hari));
 
           
            String namaMobil = (String) cmbMobil.getSelectedItem();
            if (namaMobil == null) return;
 
            String sql = "SELECT harga_sewa FROM mobil WHERE nama_mobil = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, namaMobil);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                long harga      = rs.getLong("harga_sewa");
                long totalBiaya = harga * hari;
                txtBiayaSewa.setText(String.valueOf(totalBiaya));
            }
 
        } catch (java.text.ParseException pe) {
           
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghitung: " + e.getMessage());
        }
    }
 
    
    private void simpanDataCustomer(String nama, String nik, String noHp, String alamat) {
    try {
        String cekSql = "SELECT id_customer FROM customer WHERE NIK = ?";
        PreparedStatement cekPst = conn.prepareStatement(cekSql);
        cekPst.setString(1, nik);
        ResultSet rs = cekPst.executeQuery();
 
        if (rs.next()) {
            
            String updateSql = "UPDATE customer SET nama = ?, nomor_hp = ?, alamat = ? WHERE NIK = ?";
            PreparedStatement updPst = conn.prepareStatement(updateSql);
            updPst.setString(1, nama);
            updPst.setString(2, noHp);
            updPst.setString(3, alamat);
            updPst.setString(4, nik);
            updPst.executeUpdate();
        } else {
            
            String insertSql = "INSERT INTO customer (nama, NIK, nomor_hp, alamat) VALUES (?, ?, ?, ?)";
            PreparedStatement insPst = conn.prepareStatement(insertSql);
            insPst.setString(1, nama);
            insPst.setString(2, nik);
            insPst.setString(3, noHp);
            insPst.setString(4, alamat);
            insPst.executeUpdate();
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan data customer: " + e.getMessage());
    }
}
    
    private void simpanBooking() {
    try {
        String namaCustomer  = txtNamaCustomer.getText().trim();
        String nik            = txtNik.getText().trim();
        String noHp           = txtNohp.getText().trim();
        String alamat         = txtAlamat.getText().trim();
        String namaMobil     = (String) cmbMobil.getSelectedItem();
        String tglMulai      = txtTanggalMulai.getText().trim();
        String tglSelesai    = txtTanggalSelesai.getText().trim();
        String totalHari     = txtTotalHari.getText().trim();
        String biayaSewa     = txtBiayaSewa.getText().trim();
 
        if (namaCustomer.isEmpty() || nik.isEmpty() || noHp.isEmpty() || alamat.isEmpty()
                || tglMulai.isEmpty() || tglSelesai.isEmpty()
                || totalHari.isEmpty() || biayaSewa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harap isi semua field!");
            return;
        }
 
        
        String cekStatusSql = "SELECT status FROM mobil WHERE nama_mobil = ?";
        PreparedStatement cekStatusPst = conn.prepareStatement(cekStatusSql);
        cekStatusPst.setString(1, namaMobil);
        ResultSet rsStatus = cekStatusPst.executeQuery();
 
        if (!rsStatus.next() || !"Tersedia".equalsIgnoreCase(rsStatus.getString("status"))) {
            JOptionPane.showMessageDialog(this, "Mobil ini sudah tidak tersedia (sedang disewa). Silakan pilih mobil lain.");
            loadDataMobil();
            return;
        }
 
        simpanDataCustomer(namaCustomer, nik, noHp, alamat);
 
        String sql = "INSERT INTO pemesanan (nama_customer, nama_mobil, tgl_mulai, tgl_selesai, total_hari, biaya_sewa, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 'Menunggu Konfirmasi')";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, namaCustomer);
        pst.setString(2, namaMobil);
        pst.setString(3, tglMulai);
        pst.setString(4, tglSelesai);
        pst.setInt(5, Integer.parseInt(totalHari));
        pst.setLong(6, Long.parseLong(biayaSewa));
        pst.executeUpdate();
 
        JOptionPane.showMessageDialog(this, "Booking berhasil dikirim! Menunggu konfirmasi Admin.");
        bersihkanForm();
 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menyimpan booking: " + e.getMessage());
    }
}
 
private void ubahStatusMobil(String namaMobil, String statusBaru) {
    try {
        String sql = "UPDATE mobil SET status = ? WHERE nama_mobil = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setString(1, statusBaru);
        pst.setString(2, namaMobil);
        pst.executeUpdate();
    } catch (Exception e) {
        System.out.println("Gagal update status mobil: " + e.getMessage());
    }
}
 
   private void bersihkanForm() {
    txtNamaCustomer.setText("");
    txtNik.setText("");
    txtNohp.setText("");
    txtAlamat.setText("");
    txtTanggalMulai.setText("");
    txtTanggalSelesai.setText("");
    txtTotalHari.setText("");
    txtBiayaSewa.setText("");
    if (cmbMobil.getItemCount() > 0) cmbMobil.setSelectedIndex(0);
}
 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txtNamaCustomer = new javax.swing.JTextField();
        cmbMobil = new javax.swing.JComboBox<>();
        txtTanggalMulai = new javax.swing.JTextField();
        txtTanggalSelesai = new javax.swing.JTextField();
        txtTotalHari = new javax.swing.JTextField();
        txtBiayaSewa = new javax.swing.JTextField();
        btnSimpan = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        txtNik = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        txtNohp = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        txtAlamat = new javax.swing.JTextField();

        jPanel1.setBackground(new java.awt.Color(47, 164, 215));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setText("Rental Mobil Jaya Abadi");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("FORM PEMESANAN");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Nama Customer  :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Mobil  :");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Tanggal Mulai  :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Tanggal Selesai");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText("Total Hari  :");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Biaya Sewa  :");

        txtNamaCustomer.setBackground(new java.awt.Color(245, 233, 216));

        cmbMobil.setBackground(new java.awt.Color(245, 233, 216));
        cmbMobil.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Avanza", "Innova", "Pajero" }));

        txtTanggalMulai.setBackground(new java.awt.Color(231, 111, 46));

        txtTanggalSelesai.setBackground(new java.awt.Color(245, 233, 216));

        txtTotalHari.setBackground(new java.awt.Color(231, 111, 46));

        txtBiayaSewa.setBackground(new java.awt.Color(245, 233, 216));

        btnSimpan.setBackground(new java.awt.Color(62, 44, 35));
        btnSimpan.setForeground(new java.awt.Color(231, 111, 46));
        btnSimpan.setText("SIMPAN BOOKING");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        jLabel9.setText("NIK:");

        txtNik.setBackground(new java.awt.Color(231, 111, 46));

        jLabel10.setText("NO HP:");

        txtNohp.setBackground(new java.awt.Color(245, 233, 216));

        jLabel11.setText("Alamat:");

        txtAlamat.setBackground(new java.awt.Color(231, 111, 46));
        txtAlamat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtAlamatActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel4)
                            .addComponent(jLabel9)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addGap(34, 34, 34)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSimpan)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(txtNamaCustomer)
                                .addComponent(cmbMobil, 0, 273, Short.MAX_VALUE)
                                .addComponent(txtTanggalMulai)
                                .addComponent(txtTanggalSelesai)
                                .addComponent(txtTotalHari)
                                .addComponent(txtBiayaSewa)
                                .addComponent(txtNik)
                                .addComponent(txtNohp)
                                .addComponent(txtAlamat)))))
                .addContainerGap(290, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(txtNamaCustomer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel9)
                                    .addComponent(txtNik, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel11))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtNohp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtAlamat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbMobil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtTanggalMulai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTanggalSelesai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtTotalHari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBiayaSewa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(32, 32, 32)
                .addComponent(btnSimpan, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(52, Short.MAX_VALUE))
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
          simpanBooking();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void txtAlamatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtAlamatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtAlamatActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cmbMobil;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtAlamat;
    private javax.swing.JTextField txtBiayaSewa;
    private javax.swing.JTextField txtNamaCustomer;
    private javax.swing.JTextField txtNik;
    private javax.swing.JTextField txtNohp;
    private javax.swing.JTextField txtTanggalMulai;
    private javax.swing.JTextField txtTanggalSelesai;
    private javax.swing.JTextField txtTotalHari;
    // End of variables declaration//GEN-END:variables
}
