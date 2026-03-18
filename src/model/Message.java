package model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {
    private static final long serialVersionUID = 2L;

    private int messageID;
    private int senderID;
    private int receiverID;
    private String senderName;
    private String content;
    private Timestamp timeSent;
    private byte[] fileData; 

    public Message() {
    }

    public Message(int messageID, int senderID, int receiverID, String content, Timestamp timeSent) {
        this.messageID = messageID;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.content = content;
        this.timeSent = timeSent;
    }

    public int getMessageID() { return messageID; }
    public void setMessageID(int messageID) { this.messageID = messageID; }

    public int getSenderID() { return senderID; }
    public void setSenderID(int senderID) { this.senderID = senderID; }

    public int getReceiverID() { return receiverID; }
    public void setReceiverID(int receiverID) { this.receiverID = receiverID; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimeSent() { return timeSent; }
    public void setTimeSent(Timestamp timeSent) { this.timeSent = timeSent; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }
}
