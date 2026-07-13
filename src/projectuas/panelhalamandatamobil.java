/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package projectuas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author HP
 */
public class panelhalamandatamobil extends javax.swing.JPanel {
 
    Connection conn = Projectuas.getKoneksi();
    private int idMobilTerpilih = -1; 
    /**
     * Creates new form halamandatamobil
     */
    public panelhalamandatamobil() {
        initComponents();
        tampilDataMobil();
        pasangListenerHapus();
    }
   
private void tampilDataMobil() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
 
        model.addColumn("ID");
        model.addColumn("Merek");
        model.addColumn("Tipe");
        model.addColumn("Plat");
        model.addColumn("Harga");
        model.addColumn("Status");
        model.addColumn("Aksi");
 
        tblDataMobil.setModel(model);
        tblDataMobil.setRowHeight(25);
 
        try {
            String sql = "SELECT * FROM mobil ORDER BY id_mobil DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
 
              while (rs.next()) {
        model.addRow(new Object[]{
        rs.getInt("id_mobil"),
        rs.getString("nama_mobil"),
        rs.getString("tipe"),
        rs.getString("no_plat"),
        rs.getString("harga_sewa"),
        rs.getString("status"),
        "Hapus"
    });
}
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data mobil: " + e.getMessage());
        }
    }
 
private void simpanMobil() {
        try {
            String merek    = txtMerek.getText().trim();
            String tipe     = txtTipe.getText().trim();
            String noPolisi = txtNoPolisi.getText().trim();
            String harga    = txtHarga.getText().trim();
            String status   = (String) cmbStatus.getSelectedItem();
 
            if (merek.isEmpty() || tipe.isEmpty() || noPolisi.isEmpty() || harga.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Harap isi semua field!");
                return;
            }
 
            if (idMobilTerpilih == -1) {
                
                String sql = "INSERT INTO mobil (nama_mobil, tipe, no_plat, harga_sewa, status) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, merek);
                pst.setString(2, tipe);
                pst.setString(3, noPolisi);
                pst.setString(4, harga);
                pst.setString(5, status);
                pst.executeUpdate();
 
                JOptionPane.showMessageDialog(this, "Data mobil berhasil disimpan!");
            } else {
                
                String sql = "UPDATE mobil SET nama_mobil = ?, tipe = ?, no_plat = ?, harga_sewa = ?, status = ? WHERE id_mobil = ?";
                PreparedStatement pst = conn.prepareStatement(sql);
                pst.setString(1, merek);
                pst.setString(2, tipe);
                pst.setString(3, noPolisi);
                pst.setString(4, harga);
                pst.setString(5, status);
                pst.setInt(6, idMobilTerpilih);
                pst.executeUpdate();
 
                JOptionPane.showMessageDialog(this, "Data mobil berhasil diperbarui!");
            }
 
            bersihkanForm();
            tampilDataMobil();
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data: " + e.getMessage());
        }
    }
      
     private void hapusMobil(int idMobil) {
    int konfirmasi = JOptionPane.showConfirmDialog(
            this,
            "Yakin ingin menghapus data mobil ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
    );
 
    if (konfirmasi != JOptionPane.YES_OPTION) {
        return;
    }
 
    try {
        String sql = "DELETE FROM mobil WHERE id_mobil = ?";
        PreparedStatement pst = conn.prepareStatement(sql);
        pst.setInt(1, idMobil);
        pst.executeUpdate();
 
        JOptionPane.showMessageDialog(this, "Data mobil berhasil dihapus!");
        if (idMobil == idMobilTerpilih) {
            bersihkanForm();
        }
        tampilDataMobil();
 
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Gagal menghapus data: " + e.getMessage());
    }
}
    private void tampilRiwayatPerbaikan() {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model.addColumn("Tanggal");
        model.addColumn("Mobil");
        model.addColumn("Keterangan");
        model.addColumn("Biaya");
 
        try {
            String sql = "SELECT tanggal, nama_mobil, keterangan, jumlah FROM pengeluaran ORDER BY tanggal DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getDate("tanggal"),
                    rs.getString("nama_mobil"),
                    rs.getString("keterangan"),
                    "Rp " + rs.getInt("jumlah")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat perbaikan: " + e.getMessage());
            return;
        }
 
          javax.swing.JTable tabelRiwayat = new javax.swing.JTable(model);
          tabelRiwayat.setRowHeight(25);
          tabelRiwayat.getColumnModel().getColumn(0).setPreferredWidth(90);  
          tabelRiwayat.getColumnModel().getColumn(1).setPreferredWidth(80);  
          tabelRiwayat.getColumnModel().getColumn(2).setPreferredWidth(350); 
          tabelRiwayat.getColumnModel().getColumn(3).setPreferredWidth(90);  
 
javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(tabelRiwayat);
scroll.setPreferredSize(new java.awt.Dimension(720, 300));
 
JOptionPane.showMessageDialog(this, scroll, "Riwayat Perbaikan / Pengeluaran Mobil", JOptionPane.PLAIN_MESSAGE);
     
    }
 
    private void bersihkanForm() {
        txtMerek.setText("");
        txtTipe.setText("");
        txtNoPolisi.setText("");
        txtHarga.setText("");
        cmbStatus.setSelectedIndex(0);
        idMobilTerpilih = -1;
        btnSimpan.setText("SIMPAN");
    }
private void pasangListenerHapus() {
    tblDataMobil.addMouseListener(new java.awt.event.MouseAdapter() {
        @Override
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            int row = tblDataMobil.rowAtPoint(evt.getPoint());
            int col = tblDataMobil.columnAtPoint(evt.getPoint());
 
            if (row < 0) return;
 
            if (col == 6) {
                
                int idMobil = (int) tblDataMobil.getValueAt(row, 0);
                hapusMobil(idMobil);
            } else {
                
                idMobilTerpilih = (int) tblDataMobil.getValueAt(row, 0);
                txtMerek.setText(String.valueOf(tblDataMobil.getValueAt(row, 1)));
                txtTipe.setText(String.valueOf(tblDataMobil.getValueAt(row, 2)));
                txtNoPolisi.setText(String.valueOf(tblDataMobil.getValueAt(row, 3)));
                txtHarga.setText(String.valueOf(tblDataMobil.getValueAt(row, 4)));
                cmbStatus.setSelectedItem(String.valueOf(tblDataMobil.getValueAt(row, 5)));
                btnSimpan.setText("UPDATE");
            }
        }
    });
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDataMobil = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnSimpan = new javax.swing.JButton();
        txtMerek = new javax.swing.JTextField();
        txtTipe = new javax.swing.JTextField();
        txtNoPolisi = new javax.swing.JTextField();
        txtHarga = new javax.swing.JTextField();
        cmbStatus = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnRiwayatPerbaikan = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(47, 164, 215));

        tblDataMobil.setBackground(new java.awt.Color(231, 111, 46));
        tblDataMobil.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Merek", "Tipe", "Plat", "Harga", "Status", "Aksi"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDataMobil);

        jLabel1.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        jLabel1.setText("Rental Mobil Jaya Abadi");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Data Mobil");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Merek             :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Tipe                : ");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("No Polisi         :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Harga             :");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel7.setText(" Status             :");

        btnSimpan.setBackground(new java.awt.Color(62, 44, 35));
        btnSimpan.setForeground(new java.awt.Color(231, 111, 46));
        btnSimpan.setText("SIMPAN");
        btnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSimpanActionPerformed(evt);
            }
        });

        txtMerek.setBackground(new java.awt.Color(245, 233, 216));

        txtTipe.setBackground(new java.awt.Color(231, 111, 46));
        txtTipe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTipeActionPerformed(evt);
            }
        });

        txtNoPolisi.setBackground(new java.awt.Color(245, 233, 216));
        txtNoPolisi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNoPolisiActionPerformed(evt);
            }
        });

        txtHarga.setBackground(new java.awt.Color(231, 111, 46));

        cmbStatus.setBackground(new java.awt.Color(245, 233, 216));
        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Tersedia", "Disewa", "Perbaikan" }));
        cmbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStatusActionPerformed(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/car-rent (3).png"))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI Black", 2, 12)); // NOI18N
        jLabel9.setText("Pilih Mobilnya, Mulai Petualangannya.");

        btnRiwayatPerbaikan.setBackground(new java.awt.Color(62, 44, 35));
        btnRiwayatPerbaikan.setForeground(new java.awt.Color(231, 111, 46));
        btnRiwayatPerbaikan.setText("RIWAYAT PERBAIKAN");
        btnRiwayatPerbaikan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRiwayatPerbaikanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel2)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txtMerek, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(btnSimpan)
                                        .addGap(46, 46, 46)
                                        .addComponent(btnRiwayatPerbaikan))
                                    .addComponent(txtTipe, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtNoPolisi, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel9)
                                        .addGap(95, 95, 95))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addGap(180, 180, 180))))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtMerek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtTipe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtNoPolisi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtHarga, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSimpan)
                    .addComponent(btnRiwayatPerbaikan))
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtTipeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTipeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTipeActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStatusActionPerformed

    private void txtNoPolisiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNoPolisiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNoPolisiActionPerformed

    private void btnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSimpanActionPerformed
        // TODO add your handling code here:
        simpanMobil();
    }//GEN-LAST:event_btnSimpanActionPerformed

    private void btnRiwayatPerbaikanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRiwayatPerbaikanActionPerformed
        // TODO add your handling code here:
        tampilRiwayatPerbaikan();
    }//GEN-LAST:event_btnRiwayatPerbaikanActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnRiwayatPerbaikan;
    private javax.swing.JButton btnSimpan;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDataMobil;
    private javax.swing.JTextField txtHarga;
    private javax.swing.JTextField txtMerek;
    private javax.swing.JTextField txtNoPolisi;
    private javax.swing.JTextField txtTipe;
    // End of variables declaration//GEN-END:variables
}
