package com.oopsw.simplechat.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity // 1. Báo hiệu: Class này tương ứng với 1 bảng trong SQL
@Table(name = "chat_messages") // Đặt tên bảng là "chat_messages"
@Data
public class ChatMessage {

    @Id // 2. Khóa chính (Primary Key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự động tăng (1, 2, 3...)
    private Long id;

    private String content;
    private String sender;

    @Enumerated(EnumType.STRING) // 3. Lưu Enum dưới dạng chữ ("CHAT", "JOIN") thay vì số
    private MessageType type;
    
    // Enum giữ nguyên
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}