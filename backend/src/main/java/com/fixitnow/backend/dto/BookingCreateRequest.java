package com.fixitnow.backend.dto;

public class BookingCreateRequest {

    private Long serviceId;
    private String timeSlot;
    private String bookingDate;
    private String customerLocation;
    private Double customerLat;
    private Double customerLng;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

   
    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getCustomerLocation() {
        return customerLocation;
    }

    public void setCustomerLocation(String customerLocation) {
        this.customerLocation = customerLocation;
    }

    public Double getCustomerLat() {
        return customerLat;
    }

    public void setCustomerLat(Double customerLat) {
        this.customerLat = customerLat;
    }

    public Double getCustomerLng() {
        return customerLng;
    }

    public void setCustomerLng(Double customerLng) {
        this.customerLng = customerLng;
    }
}