package com.fixitnow.backend.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixitnow.backend.dto.ChangePasswordRequest;
import com.fixitnow.backend.dto.UpdateUserNotificationSettingsRequest;
import com.fixitnow.backend.dto.UpdateUserProfileRequest;
import com.fixitnow.backend.dto.UserSettingsResponse;
import com.fixitnow.backend.entity.User;
import com.fixitnow.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/users/me")
@CrossOrigin(origins = "*")
@SuppressWarnings("null")
public class UserSettingsController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserSettingsController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public UserSettingsResponse getMySettings(Principal principal) {
        User user = getLoggedInUser(principal);
        return new UserSettingsResponse(user);
    }

    @PutMapping("/profile")
    public UserSettingsResponse updateProfile(
            @RequestBody UpdateUserProfileRequest request,
            Principal principal) {

        User user = getLoggedInUser(principal);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }

        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
        user.setLocation(request.getLocation() != null ? request.getLocation().trim() : null);

        userRepository.save(user);
        return new UserSettingsResponse(user);
    }

    @PutMapping("/notifications")
    public UserSettingsResponse updateNotificationSettings(
            @RequestBody UpdateUserNotificationSettingsRequest request,
            Principal principal) {

        User user = getLoggedInUser(principal);

        if (request.getBookingUpdates() != null) {
            user.setNotifBookingUpdates(request.getBookingUpdates());
        }
        if (request.getChatMessages() != null) {
            user.setNotifChatMessages(request.getChatMessages());
        }
        if (request.getPromotions() != null) {
            user.setNotifPromotions(request.getPromotions());
        }
        if (request.getSms() != null) {
            user.setNotifSms(request.getSms());
        }

        userRepository.save(user);
        return new UserSettingsResponse(user);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            Principal principal) {

        User user = getLoggedInUser(principal);

        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Current password is required");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body("New password must be at least 6 characters");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

    private User getLoggedInUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new SecurityException("Unauthorized");
        }

        String email = principal.getName().trim();
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found for authenticated session"));
    }
}
