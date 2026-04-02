package com.fixitnow.backend.controller;

import com.fixitnow.backend.entity.Notification;
import com.fixitnow.backend.entity.User;
import com.fixitnow.backend.repository.NotificationRepository;
import com.fixitnow.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@SuppressWarnings("null")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{userId}")
    public List<Notification> getUserNotifications(@PathVariable Long userId, Principal principal) {
        User me = getLoggedInUser(principal);
        if (!me.getId().equals(userId)) {
            throw new SecurityException("Unauthorized");
        }
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @PostMapping("/create")
    public Notification createNotification(@RequestBody Notification notification) {
        return notificationRepository.save(notification);
    }

    @PutMapping("/{userId}/read")
    public void markAllAsRead(@PathVariable Long userId, Principal principal) {
        User me = getLoggedInUser(principal);
        if (!me.getId().equals(userId)) {
            throw new SecurityException("Unauthorized");
        }

        List<Notification> notifs = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for(Notification n : notifs) {
            n.setViewed(true);
        }
        notificationRepository.saveAll(notifs);
    }

    private User getLoggedInUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new SecurityException("Unauthorized");
        }

        return userRepository.findByEmailIgnoreCase(principal.getName().trim())
                .orElseThrow(() -> new RuntimeException("User not found for authenticated session"));
    }
}