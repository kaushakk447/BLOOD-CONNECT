package com.bloodconnect.api.controller;

import com.bloodconnect.api.dto.*;
import com.bloodconnect.service.BloodRequestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/blood-requests")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BloodRequestController {
    
    @Autowired
    private BloodRequestService bloodRequestService;
    
    @PostMapping
    public ResponseEntity<BloodRequestResponse> createBloodRequest(
        @Valid @RequestBody CreateBloodRequestDTO request,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        log.info("Creating blood request from user: {}", userId);
        
        BloodRequestResponse response = bloodRequestService.createBloodRequest(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BloodRequestResponse> getBloodRequest(
        @PathVariable Long id,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        BloodRequestResponse response = bloodRequestService.getBloodRequestById(id, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<BloodRequestResponse>> getBloodRequests(
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<BloodRequestResponse> response = bloodRequestService.getBloodRequestsByRequester(userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<BloodRequestResponse>> getAllBloodRequests() {
        List<BloodRequestResponse> response = bloodRequestService.getAllBloodRequests();
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<BloodRequestResponse> updateBloodRequest(
        @PathVariable Long id,
        @RequestBody UpdateBloodRequestDTO request,
        HttpServletRequest httpRequest) {
        
        Long userId = (Long) httpRequest.getAttribute("userId");
        BloodRequestResponse response = bloodRequestService.updateBloodRequest(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
