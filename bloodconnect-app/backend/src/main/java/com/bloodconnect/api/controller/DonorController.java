package com.bloodconnect.api.controller;

import com.bloodconnect.api.dto.*;
import com.bloodconnect.service.DonorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/donors")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class DonorController {
    
    @Autowired
    private DonorService donorService;
    
    @PostMapping("/profile")
    public ResponseEntity<DonorProfileResponse> createDonorProfile(
        @Valid @RequestBody CreateDonorProfileDTO request,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Creating donor profile for user: {}", userId);
        
        DonorProfileResponse response = donorService.createDonorProfile(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<DonorProfileResponse> getDonorProfile(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        DonorProfileResponse response = donorService.getDonorProfile(userId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/availability")
    public ResponseEntity<DonorProfileResponse> updateAvailability(
        @Valid @RequestBody UpdateDonorAvailabilityDTO request,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        DonorProfileResponse response = donorService.updateDonorAvailability(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/requests")
    public ResponseEntity<List<DonorRequestResponse>> getDonorRequests(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<DonorRequestResponse> response = donorService.getDonorRequests(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/requests/pending")
    public ResponseEntity<List<DonorRequestResponse>> getPendingDonorRequests(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<DonorRequestResponse> response = donorService.getPendingDonorRequests(userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<DonorRequestResponse> acceptRequest(
        @PathVariable Long id,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Donor {} accepting request: {}", userId, id);
        
        DonorRequestResponse response = donorService.acceptDonorRequest(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/requests/{id}/decline")
    public ResponseEntity<DonorRequestResponse> declineRequest(
        @PathVariable Long id,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Donor {} declining request: {}", userId, id);
        
        DonorRequestResponse response = donorService.declineDonorRequest(id, userId);
        return ResponseEntity.ok(response);
    }
}
