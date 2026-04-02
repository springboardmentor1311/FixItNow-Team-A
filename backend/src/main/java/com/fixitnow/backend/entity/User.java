package com.fixitnow.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    private String location;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private Role role;

    @JsonIgnore
    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private ProviderProfile providerProfile;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "notif_booking_updates")
    private Boolean notifBookingUpdates = true;

    @Column(name = "notif_chat_messages")
    private Boolean notifChatMessages = true;

    @Column(name = "notif_promotions")
    private Boolean notifPromotions = false;

    @Column(name = "notif_sms")
    private Boolean notifSms = true;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public ProviderProfile getProviderProfile() {
        return providerProfile;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getNotifBookingUpdates() {
        return notifBookingUpdates;
    }

    public void setNotifBookingUpdates(Boolean notifBookingUpdates) {
        this.notifBookingUpdates = notifBookingUpdates;
    }

    public Boolean getNotifChatMessages() {
        return notifChatMessages;
    }

    public void setNotifChatMessages(Boolean notifChatMessages) {
        this.notifChatMessages = notifChatMessages;
    }

    public Boolean getNotifPromotions() {
        return notifPromotions;
    }

    public void setNotifPromotions(Boolean notifPromotions) {
        this.notifPromotions = notifPromotions;
    }

    public Boolean getNotifSms() {
        return notifSms;
    }

    public void setNotifSms(Boolean notifSms) {
        this.notifSms = notifSms;
    }
}
