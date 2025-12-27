package com.oopsw.simplechat.listener; // Nhớ sửa package nếu đặt chỗ khác

import com.oopsw.simplechat.model.ChatMessage;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.beans.factory.annotation.Autowired;

@Component // Phải có cái này Spring mới nhận diện được
public class WebSocketEventListener {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // Lấy username đã lưu trong session (lúc user join)
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if(username != null) {
            System.out.println("User Disconnected: " + username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);

            // Gửi tin nhắn LEAVE cho mọi người
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
