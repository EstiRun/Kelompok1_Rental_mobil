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

 public class panelhalamandashboardowner extends javax.swing.JPanel {
 
        Connection conn = Projectuas.getKoneksi();
    /**
     * Creates new form halamandashboardowner
     */
    public panelhalamandashboardowner() {
        initComponents();
        loadMobilTerlaris();
        tampilLaporan("");
 
    }
 
    private void loadMobilTerlaris() {
        try {
            cmbMobilTerlaris.removeAllItems();
            cmbMobilTerlaris.addItem("===Mobil Terlaris===");
 
            String sql = "SELECT nama_mobil, COUNT(*) AS total "
                       + "FROM pemesanan "
                       + "GROUP BY nama_mobil "
                       + "ORDER BY total DESC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                cmbMobilTerlaris.addItem(rs.getString("nama_mobil"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat mobil terlaris: " + e.getMessage());
        }
    }
 
    private void tampilLaporan(String filterMobil) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
 
        model.addColumn("Bulan");
        model.addColumn("Pendapatan");
        model.addColumn("Total Booking");
        model.addColumn("Pengeluaran");
        model.addColumn("Keuntungan Bersih");
 
        tblDashboardOwner.setModel(model);
        tblDashboardOwner.setRowHeight(25);
 
        try {
            String sql;
            PreparedStatement pst;
 
            if (filterMobil == null || filterMobil.isEmpty() || filterMobil.equals("===Mobil Terlaris===")) {
                sql = "SELECT DATE_FORMAT(COALESCE(tgl_kembali_aktual, tgl_selesai), '%Y-%m') AS bulan, "
                    + "SUM(biaya_sewa + denda) AS pendapatan, COUNT(*) AS total_booking "
                    + "FROM pemesanan "
                    + "WHERE status = 'Selesai' "
                    + "GROUP BY bulan "
                    + "ORDER BY bulan DESC";
                pst = conn.prepareStatement(sql);
            } else {
                sql = "SELECT DATE_FORMAT(COALESCE(tgl_kembali_aktual, tgl_selesai), '%Y-%m') AS bulan, "
                    + "SUM(biaya_sewa + denda) AS pendapatan, COUNT(*) AS total_booking "
                    + "FROM pemesanan "
                    + "WHERE status = 'Selesai' AND nama_mobil = ? "
                    + "GROUP BY bulan "
                    + "ORDER BY bulan DESC";
                pst = conn.prepareStatement(sql);
                pst.setString(1, filterMobil);
            }
 
            
            java.util.LinkedHashMap<String, int[]> dataBulan = new java.util.LinkedHashMap<>(); 
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                dataBulan.put(rs.getString("bulan"), new int[]{ rs.getInt("pendapatan"), rs.getInt("total_booking") });
            }
 
            
            String sqlPengeluaran;
            PreparedStatement pstPengeluaran;
            if (filterMobil == null || filterMobil.isEmpty() || filterMobil.equals("===Mobil Terlaris===")) {
                sqlPengeluaran = "SELECT DATE_FORMAT(tanggal, '%Y-%m') AS bulan, SUM(jumlah) AS total_pengeluaran "
                               + "FROM pengeluaran GROUP BY bulan";
                pstPengeluaran = conn.prepareStatement(sqlPengeluaran);
            } else {
                sqlPengeluaran = "SELECT DATE_FORMAT(tanggal, '%Y-%m') AS bulan, SUM(jumlah) AS total_pengeluaran "
                               + "FROM pengeluaran WHERE nama_mobil = ? GROUP BY bulan";
                pstPengeluaran = conn.prepareStatement(sqlPengeluaran);
                pstPengeluaran.setString(1, filterMobil);
            }
 
            java.util.Map<String, Integer> pengeluaranBulan = new java.util.HashMap<>();
            ResultSet rsPengeluaran = pstPengeluaran.executeQuery();
            while (rsPengeluaran.next()) {
                pengeluaranBulan.put(rsPengeluaran.getString("bulan"), rsPengeluaran.getInt("total_pengeluaran"));
            }
 
            
            java.util.Set<String> semuaBulan = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
            semuaBulan.addAll(dataBulan.keySet());
            semuaBulan.addAll(pengeluaranBulan.keySet());
 
            for (String bulan : semuaBulan) {
                int pendapatan = dataBulan.containsKey(bulan) ? dataBulan.get(bulan)[0] : 0;
                int totalBooking = dataBulan.containsKey(bulan) ? dataBulan.get(bulan)[1] : 0;
                int pengeluaran = pengeluaranBulan.getOrDefault(bulan, 0);
                int keuntunganBersih = pendapatan - pengeluaran;
 
                model.addRow(new Object[]{
                    bulan,
                    "Rp " + pendapatan,
                    totalBooking,
                    "Rp " + pengeluaran,
                    "Rp " + keuntunganBersih
                });
            }
 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblDashboardOwner = new javax.swing.JTable();
        cmbMobilTerlaris = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();

        jPanel1.setBackground(new java.awt.Color(47, 164, 215));

        jLabel1.setFont(new java.awt.Font("Segoe UI Black", 1, 14)); // NOI18N
        jLabel1.setText("Rental Mobil Jaya Abadi");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("DASHBOARD OWNER");

        tblDashboardOwner.setBackground(new java.awt.Color(231, 111, 46));
        tblDashboardOwner.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Bulan", "Pendapatan", "Total Booking", "Pengeluaran", "Keuntungan bersih"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblDashboardOwner);

        cmbMobilTerlaris.setBackground(new java.awt.Color(245, 233, 216));
        cmbMobilTerlaris.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "===Mobil Terlaris===", "1. Avanza", "2. Innova", "3. Pajero" }));
        cmbMobilTerlaris.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbMobilTerlarisActionPerformed(evt);
            }
        });

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/car-rent (2).png"))); // NOI18N

        jLabel3.setFont(new java.awt.Font("Segoe UI Black", 2, 14)); // NOI18N
        jLabel3.setText("Pilih Mobilnya, Mulai Petualangannya.");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel1))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(cmbMobilTerlaris, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(108, 108, 108))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addGap(44, 44, 44)))))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbMobilTerlaris, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cmbMobilTerlarisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbMobilTerlarisActionPerformed
        // TODO add your handling code here:
        String dipilih = (String) cmbMobilTerlaris.getSelectedItem();
        tampilLaporan(dipilih);
    }//GEN-LAST:event_cmbMobilTerlarisActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbMobilTerlaris;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDashboardOwner;
    // End of variables declaration//GEN-END:variables
}
