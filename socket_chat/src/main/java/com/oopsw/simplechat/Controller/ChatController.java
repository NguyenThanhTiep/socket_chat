package com.oopsw.simplechat.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oopsw.simplechat.model.ChatMessage;
import com.oopsw.simplechat.repository.ChatRepository;

@Controller
public class ChatController {
  @Autowired 
    private ChatRepository chatRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // 1. Lưu vào MySQL trước
        chatRepository.save(chatMessage);
        
        // 2. Rồi mới gửi cho mọi người
        return chatMessage;
    }
  public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){
    headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
    return chatMessage;
  }
  @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatMessage> getHistory() {
    return chatRepository.findAll();
    }
}
