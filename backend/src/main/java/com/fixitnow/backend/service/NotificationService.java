package com.fixitnow.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fixitnow.backend.entity.Notification;
import com.fixitnow.backend.entity.Role;
import com.fixitnow.backend.entity.User;
import com.fixitnow.backend.repository.NotificationRepository;
import com.fixitnow.backend.repository.UserRepository;

@Service
@SuppressWarnings("null")
public class NotificationService {

    public static final String EVENT_BOOKING = "BOOKING";
    public static final String EVENT_CHAT = "CHAT";
    public static final String EVENT_PROMOTION = "PROMOTION";
    public static final String EVENT_SYSTEM = "SYSTEM";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyUser(
            Long userId,
            Long bookingId,
            String role,
            String icon,
            String text,
            String eventType,
            String targetPath) {

        if (userId == null || text == null || text.isBlank()) {
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }

        if (!isNotificationAllowed(user, eventType)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setBookingId(bookingId);
        notification.setRole(role);
        notification.setIcon(icon);
        notification.setText(text.trim());
        notification.setViewed(false);
        notification.setCreatedAt(System.currentTimeMillis());
        notification.setEventType(eventType);
        notification.setTargetPath(targetPath);

        notificationRepository.save(notification);
    }

    public void notifyAdmins(
            Long bookingId,
            String icon,
            String text,
            String eventType,
            String targetPath) {

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notifyUser(admin.getId(), bookingId, "admin", icon, text, eventType, targetPath);
        }
    }

    private boolean isNotificationAllowed(User user, String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return true;
        }

        return switch (eventType.toUpperCase()) {
            case EVENT_BOOKING -> defaultTrue(user.getNotifBookingUpdates());
            case EVENT_CHAT -> defaultTrue(user.getNotifChatMessages());
            case EVENT_PROMOTION -> defaultFalse(user.getNotifPromotions());
            default -> true;
        };
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || Boolean.TRUE.equals(value);
    }

    private boolean defaultFalse(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}
