package server.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;

public class UserDAO {

    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("UserID"));
                user.setUsername(rs.getString("Username"));
                user.setFullName(rs.getString("FullName"));
                user.setStatus(rs.getString("Status"));
                user.setAvatar(rs.getBytes("Avatar"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi checkLogin: " + e.getMessage());
        }
        return null;
    }

    public List<User> getFriendList(int userID) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.UserID, u.Username, u.FullName, u.Status, u.Avatar " +
                     "FROM Users u " +
                     "JOIN Friends f ON (u.UserID = f.UserID2 AND f.UserID1 = ?) " +
                     "OR (u.UserID = f.UserID1 AND f.UserID2 = ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, userID);
            ps.setInt(2, userID);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setUserID(rs.getInt("UserID"));
                u.setUsername(rs.getString("Username"));
                u.setFullName(rs.getString("FullName"));
                u.setStatus(rs.getString("Status"));
                u.setAvatar(rs.getBytes("Avatar"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getFriendList: " + e.getMessage());
        }
        return list;
    }
    public List<model.Message> getMessageHistory(int user1, int user2){
        List<model.Message> list = new ArrayList<>();
        String sql = "SELECT * FROM Messages WHERE (SenderID = ? AND ReceiverID = ?) " +
                 "OR (SenderID = ? AND ReceiverID = ?) ORDER BY TimeSent ASC";
        try(Connection conn=DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)){
                ps.setInt(1, user1);
                ps.setInt(2, user2);
                ps.setInt(3, user2);
                ps.setInt(4, user1);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.Message m = new model.Message();
                    m.setSenderID(rs.getInt("SenderID"));
                    m.setReceiverID(rs.getInt("ReceiverID"));
                    m.setContent(rs.getString("Content"));
                    list.add(m);
                    
                    
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        return list;
    }
    // Thêm vào server.dao.UserDAO
    public boolean saveMessage(int senderID, int receiverID, String content) {
        String sql = "INSERT INTO Messages (SenderID, ReceiverID, Content, TimeSent) VALUES (?, ?, ?, GETDATE())";
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderID);
            ps.setInt(2, receiverID);
            ps.setNString(3, content); // Dùng setNString để hỗ trợ tiếng Việt có dấu
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        System.err.println("Lỗi saveMessage: " + e.getMessage());
        return false;
        }
    }
    public boolean register(String username, String password,String fullName){
        String checkSql = "SELECT COUNT(*) FROM Users WHERE Username = ?";
        String insertSql = "INSERT INTO Users (Username, Password, FullName, Status) VALUES (?, ?, ?, 'Offline')";
        try(Connection conn =DBConnection.getConnection()){
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, username);
            ResultSet rs = psCheck.executeQuery();
            if(rs.next() && rs.getInt(1) > 0){
                return false; // Username đã tồn tại
            }
            PreparedStatement psInsert = conn.prepareStatement(insertSql);
            psInsert.setString(1, username);
            psInsert.setString(2, password);
            psInsert.setString(3, fullName);
            return psInsert.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi register: " + e.getMessage());
            return false;
        }
    }
}           