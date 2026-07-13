/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package projectuas;
 import java.sql.Connection;
import java.sql.DriverManager;
 
/**
 *
 * @author HP
 */
public class Projectuas {
     private static Connection koneksi;
     
     public static Connection getKoneksi() {

        try {
            String url = "jdbc:mysql://localhost:3306/db_rental_mobil";
            String user = "root";
            String pass = "";

            DriverManager.registerDriver(
                    new com.mysql.cj.jdbc.Driver());

            koneksi = DriverManager.getConnection(
                    url, user, pass);

        } catch (Exception e) {
            System.out.println(e);
        }

        return koneksi;
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       new halamanlogin().setVisible(true);
}
}
