package com.bloodconnect.api.controller;

import com.bloodconnect.api.dto.NotificationResponse;
import com.bloodconnect.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<NotificationResponse> response = notificationService.getNotifications(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<NotificationResponse> response = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
        @PathVariable Long id,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
    }
}
