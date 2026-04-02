package com.fixitnow.backend.dto;

public class UpdateUserNotificationSettingsRequest {

    private Boolean bookingUpdates;
    private Boolean chatMessages;
    private Boolean promotions;
    private Boolean sms;

    public Boolean getBookingUpdates() {
        return bookingUpdates;
    }

    public void setBookingUpdates(Boolean bookingUpdates) {
        this.bookingUpdates = bookingUpdates;
    }

    public Boolean getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(Boolean chatMessages) {
        this.chatMessages = chatMessages;
    }

    public Boolean getPromotions() {
        return promotions;
    }

    public void setPromotions(Boolean promotions) {
        this.promotions = promotions;
    }

    public Boolean getSms() {
        return sms;
    }

    public void setSms(Boolean sms) {
        this.sms = sms;
    }
}
