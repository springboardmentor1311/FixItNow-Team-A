package com.fixitnow.backend.dto;

import java.time.LocalDateTime;

import com.fixitnow.backend.entity.User;

public class UserSettingsResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String location;
    private String phoneNumber;
    private LocalDateTime createdAt;

    private boolean bookingUpdates;
    private boolean chatMessages;
    private boolean promotions;
    private boolean sms;

    public UserSettingsResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole() != null ? user.getRole().name().toLowerCase() : null;
        this.location = user.getLocation();
        this.phoneNumber = user.getPhoneNumber();
        this.createdAt = user.getCreatedAt();

        this.bookingUpdates = user.getNotifBookingUpdates() == null || Boolean.TRUE.equals(user.getNotifBookingUpdates());
        this.chatMessages = user.getNotifChatMessages() == null || Boolean.TRUE.equals(user.getNotifChatMessages());
        this.promotions = Boolean.TRUE.equals(user.getNotifPromotions());
        this.sms = user.getNotifSms() == null || Boolean.TRUE.equals(user.getNotifSms());
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getLocation() {
        return location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isBookingUpdates() {
        return bookingUpdates;
    }

    public boolean isChatMessages() {
        return chatMessages;
    }

    public boolean isPromotions() {
        return promotions;
    }

    public boolean isSms() {
        return sms;
    }
}
