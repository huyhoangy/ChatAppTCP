package server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.Message;
import model.User;

public class UserDAO {

    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM Users WHERE Username = ? AND Password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserID(rs.getInt("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    user.setStatus(rs.getString("Status"));
                    user.setAvatar(rs.getBytes("Avatar"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String password, String fullName) {
        String sql = "INSERT INTO Users (Username, Password, FullName, Status) VALUES (?, ?, ?, 'Offline')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<User> searchUsers(int currentUserID, String keyword) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT UserID, Username, FullName, Status FROM Users "
                + "WHERE (Username LIKE ? OR FullName LIKE ?) AND UserID != ? "
                + "AND UserID NOT IN ("
                + "SELECT UserID2 FROM Friends WHERE UserID1 = ? "
                + "UNION "
                + "SELECT UserID1 FROM Friends WHERE UserID2 = ?"
                + ")";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setInt(3, currentUserID);
            ps.setInt(4, currentUserID);
            ps.setInt(5, currentUserID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            null,
                            rs.getNString("FullName"),
                            null,
                            rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<User> getFriendList(int userID) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u "
                + "JOIN Friends f ON (u.UserID = f.UserID2 AND f.UserID1 = ?) "
                + "OR (u.UserID = f.UserID1 AND f.UserID2 = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            ps.setInt(2, userID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setUserID(rs.getInt("UserID"));
                    u.setFullName(rs.getString("FullName"));
                    u.setStatus(rs.getString("Status"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addFriend(int user1, int user2) {
        String sql = "INSERT INTO Friends (UserID1, UserID2, Status) VALUES (?, ?, N'Accepted')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user1);
            ps.setInt(2, user2);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public int createGroup(String groupName, int creatorID) {
        String sql = "INSERT INTO ChatGroups (GroupName, CreatedBy) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, groupName);
            ps.setInt(2, creatorID);
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean addGroupMembers(int groupID, int userID) {
        String sql = "INSERT INTO GroupMembers (GroupID, UserID) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            ps.setInt(2, userID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isGroupAdmin(int groupID, int userID) {
        String sql = "SELECT 1 FROM ChatGroups WHERE GroupID = ? AND CreatedBy = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            ps.setInt(2, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isGroupMember(int groupID, int userID) {
        String sql = "SELECT 1 FROM GroupMembers WHERE GroupID = ? AND UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            ps.setInt(2, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<User> getAddableFriendsForGroup(int currentUserID, int groupID) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.UserID, u.Username, u.FullName, u.Status "
                + "FROM Users u "
                + "JOIN Friends f ON (f.UserID1 = ? AND f.UserID2 = u.UserID) "
                + "OR (f.UserID2 = ? AND f.UserID1 = u.UserID) "
                + "WHERE u.UserID NOT IN (SELECT UserID FROM GroupMembers WHERE GroupID = ?) "
                + "AND u.UserID <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserID);
            ps.setInt(2, currentUserID);
            ps.setInt(3, groupID);
            ps.setInt(4, currentUserID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            null,
                            rs.getNString("FullName"),
                            null,
                            rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<User> getGroupMemberUsers(int groupID) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.UserID, u.Username, u.FullName, u.Status "
                + "FROM Users u "
                + "JOIN GroupMembers gm ON u.UserID = gm.UserID "
                + "WHERE gm.GroupID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new User(
                            rs.getInt("UserID"),
                            rs.getString("Username"),
                            null,
                            rs.getNString("FullName"),
                            null,
                            rs.getString("Status")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addMemberToGroup(int groupID, int userID) {
        if (isGroupMember(groupID, userID)) {
            return false;
        }
        return addGroupMembers(groupID, userID);
    }

    public boolean removeMemberFromGroup(int groupID, int userID) {
        String sqlOwner = "SELECT CreatedBy FROM ChatGroups WHERE GroupID = ?";
        String sqlDelete = "DELETE FROM GroupMembers WHERE GroupID = ? AND UserID = ?";
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ownerPs = conn.prepareStatement(sqlOwner)) {
                ownerPs.setInt(1, groupID);
                try (ResultSet rs = ownerPs.executeQuery()) {
                    if (rs.next() && rs.getInt("CreatedBy") == userID) {
                        return false;
                    }
                }
            }
            try (PreparedStatement deletePs = conn.prepareStatement(sqlDelete)) {
                deletePs.setInt(1, groupID);
                deletePs.setInt(2, userID);
                return deletePs.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<String> getUserGroups(int userID) {
        List<String> groups = new ArrayList<>();
        String sql = "SELECT g.GroupID, g.GroupName FROM ChatGroups g "
                + "JOIN GroupMembers gm ON g.GroupID = gm.GroupID "
                + "WHERE gm.UserID = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(rs.getInt("GroupID") + "|" + rs.getNString("GroupName"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groups;
    }

    public boolean saveMessage(int senderID, int receiverID, String content) {
        String sql = "INSERT INTO Messages (SenderID, ReceiverID, Content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderID);
            ps.setInt(2, receiverID);
            ps.setNString(3, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean saveGroupMessage(int senderID, int groupID, String content) {
        String sql = "INSERT INTO Messages (SenderID, GroupID, Content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, senderID);
            ps.setInt(2, groupID);
            ps.setNString(3, content);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Integer> getGroupMembers(int groupID) {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT UserID FROM GroupMembers WHERE GroupID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("UserID"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Message> getMessageHistory(int user1, int user2) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT * FROM Messages "
                + "WHERE (SenderID = ? AND ReceiverID = ?) OR (SenderID = ? AND ReceiverID = ?) "
                + "ORDER BY TimeSent ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, user1);
            ps.setInt(2, user2);
            ps.setInt(3, user2);
            ps.setInt(4, user1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setSenderID(rs.getInt("SenderID"));
                    m.setContent(rs.getNString("Content"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Message> getGroupMessageHistory(int groupID) {
        List<Message> list = new ArrayList<>();
        String sql = "SELECT m.SenderID, m.Content, u.FullName FROM Messages m "
                + "JOIN Users u ON m.SenderID = u.UserID "
                + "WHERE m.GroupID = ? ORDER BY m.TimeSent ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, groupID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setSenderID(rs.getInt("SenderID"));
                    m.setSenderName(rs.getNString("FullName"));
                    m.setContent(rs.getNString("Content"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean disbandGroup(int groupID, int adminID) {
        String sqlCheck = "SELECT CreatedBy FROM ChatGroups WHERE GroupID = ?";
        String sqlDelMsg = "DELETE FROM Messages WHERE GroupID = ?";
        String sqlDelMem = "DELETE FROM GroupMembers WHERE GroupID = ?";
        String sqlDelGrp = "DELETE FROM ChatGroups WHERE GroupID = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement check = conn.prepareStatement(sqlCheck)) {
                check.setInt(1, groupID);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next() || rs.getInt("CreatedBy") != adminID) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement delMsg = conn.prepareStatement(sqlDelMsg);
                 PreparedStatement delMem = conn.prepareStatement(sqlDelMem);
                 PreparedStatement delGrp = conn.prepareStatement(sqlDelGrp)) {
                delMsg.setInt(1, groupID);
                delMsg.executeUpdate();

                delMem.setInt(1, groupID);
                delMem.executeUpdate();

                delGrp.setInt(1, groupID);
                boolean deleted = delGrp.executeUpdate() > 0;
                conn.commit();
                return deleted;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean renameGroup(int groupID, String newName) {
        String sql = "UPDATE ChatGroups SET GroupName = ? WHERE GroupID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, newName);
            ps.setInt(2, groupID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
