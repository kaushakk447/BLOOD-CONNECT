package com.bloodconnect.service;

import com.bloodconnect.api.dto.NotificationResponse;
import com.bloodconnect.domain.entity.BloodRequest;
import com.bloodconnect.domain.entity.Notification;
import com.bloodconnect.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Transactional
    public void notifyDonor(Long donorUserId, BloodRequest bloodRequest, double distance) {
        log.info("Sending blood request notification to donor: {}", donorUserId);
        
        String title = "Emergency Blood Request";
        String message = String.format(
            "A patient urgently needs %s blood at %s (%.1f km away). "
            + "%d units required. Contact: %s",
            bloodRequest.getBloodGroup(),
            bloodRequest.getLocation(),
            distance,
            bloodRequest.getUnitsRequired(),
            bloodRequest.getContactNumber()
        );
        
        Notification notification = Notification.builder()
            .userId(donorUserId)
            .title(title)
            .message(message)
            .type(Notification.Type.BLOOD_REQUEST)
            .relatedEntityType("BloodRequest")
            .relatedEntityId(bloodRequest.getId())
            .read(false)
            .build();
        
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void notifyRequestStatusUpdate(Long requesterId, Long requestId, String status) {
        log.info("Notifying requester of status update for request: {}", requestId);
        
        String title = "Blood Request Status Update";
        String message = String.format("Your blood request status: %s", status);
        
        Notification notification = Notification.builder()
            .userId(requesterId)
            .title(title)
            .message(message)
            .type(Notification.Type.BLOOD_REQUEST)
            .relatedEntityType("BloodRequest")
            .relatedEntityId(requestId)
            .read(false)
            .build();
        
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void notifyDonorResponse(Long requesterId, Long donorId, String donorName, boolean accepted) {
        log.info("Notifying requester of donor response: {}", requesterId);
        
        String title = accepted ? "Donor Accepted Your Request" : "Donor Declined Your Request";
        String message = String.format("%s %s your blood request.", donorName, accepted ? "accepted" : "declined");
        
        Notification notification = Notification.builder()
            .userId(requesterId)
            .title(title)
            .message(message)
            .type(Notification.Type.DONOR_RESPONSE)
            .read(false)
            .build();
        
        notificationRepository.save(notification);
    }
    
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserId(userId);
        
        return notifications.stream()
            .map(this::mapNotificationToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndRead(userId, false);
        
        return notifications.stream()
            .map(this::mapNotificationToResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new com.bloodconnect.exception.ResourceNotFoundException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new com.bloodconnect.exception.ForbiddenException("Not authorized to mark this notification");
        }
        
        notification.setRead(true);
        notification.setReadAt(java.time.LocalDateTime.now());
        notification = notificationRepository.save(notification);
        
        return mapNotificationToResponse(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndRead(userId, false);
        unreadNotifications.forEach(n -> {
            n.setRead(true);
            n.setReadAt(java.time.LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }
    
    private NotificationResponse mapNotificationToResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .type(notification.getType().toString())
            .read(notification.getRead())
            .createdAt(notification.getCreatedAt())
            .relatedEntityType(notification.getRelatedEntityType())
            .relatedEntityId(notification.getRelatedEntityId())
            .build();
    }
}
