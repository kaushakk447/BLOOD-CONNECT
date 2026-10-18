package com.bloodconnect.api.controller;

import com.bloodconnect.api.dto.DonorProfileResponse;
import com.bloodconnect.api.dto.UserResponse;
import com.bloodconnect.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/donors/pending")
    public ResponseEntity<List<DonorProfileResponse>> getPendingDonors() {
        return ResponseEntity.ok(adminService.getPendingDonors());
    }

    @PostMapping("/donors/{id}/verify")
    public ResponseEntity<DonorProfileResponse> verifyDonor(
        @PathVariable Long id,
        @RequestBody Map<String, Object> body) {

        boolean verified = body.get("verified") == null || Boolean.TRUE.equals(body.get("verified"));
        log.info("Admin verifying donor profile {}: verified={}", id, verified);

        DonorProfileResponse response = adminService.verifyDonor(id, verified);
        return ResponseEntity.ok(response);
    }
}
