package server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Thay đổi thông tin phù hợp với máy bạn
            String url = "jdbc:sqlserver://localhost:1433;databaseName=ChatAppDB;encrypt=false;trustServerCertificate=true";
            String user = "sa";
            String pass = "12345"; 
            
            conn = DriverManager.getConnection(url, user, pass);
            System.out.println("Kết nối Database thành công!");
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối: " + e.getMessage());
        }
        return conn;
    }
    // public static void main(String[] args) {
    // try {
    //     // Lệnh này để nạp Driver vào hệ thống
    //     Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    //     System.out.println("Driver đã sẵn sàng!");
        
    //     // Thử kết nối thật
    //     if (getConnection() != null) {
    //         System.out.println("Kết nối tới SQL Server thành công rực rỡ!");
    //     }
    // } catch (ClassNotFoundException e) {
    //     System.out.println("Chưa tìm thấy file JDBC Driver!");
    // }
    // }
}