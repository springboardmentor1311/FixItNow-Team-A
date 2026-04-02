package com.fixitnow.backend.dto;

import com.fixitnow.backend.entity.ServiceEntity;

public class ServiceResponse {

    private Long id;
    private String category;
    private String subcategory;
    private String description;
    private Double price;
    private String availability;
    private String status;

    private String providerName;
    private String providerLocation;
    private Long providerId;
    private Double providerLat;
    private Double providerLng;
    private String serviceArea;
    private String memberSince;
    private long completedJobs;
    private boolean verified;
    private double rating;
    private long reviews;

    public ServiceResponse(ServiceEntity service) {
        this.id = service.getId();
        this.category = service.getCategory();
        this.subcategory = service.getSubcategory();
        this.description = service.getDescription();
        this.price = service.getPrice();
        this.availability = service.getAvailability();
        this.status = service.getStatus();
        this.providerName = service.getProvider().getName();
        this.providerId = service.getProvider().getId();
        this.providerLocation = service.getProvider().getLocation();

        if (service.getProvider() != null && service.getProvider().getProviderProfile() != null) {
            this.providerLat = service.getProvider().getProviderProfile().getLatitude();
            this.providerLng = service.getProvider().getProviderProfile().getLongitude();
            this.serviceArea = service.getProvider().getProviderProfile().getServiceArea();
                this.verified = "APPROVED".equalsIgnoreCase(service.getStatus());
            
            if (service.getCreatedAt() != null) {
                this.memberSince = String.valueOf(service.getCreatedAt().getYear());
            } else {
                this.memberSince = "2024";
            }
        } else {
            this.verified = "APPROVED".equalsIgnoreCase(service.getStatus());
        }
    }

    public ServiceResponse(ServiceEntity service, long completedJobs) {
        this(service);
        this.completedJobs = completedJobs;
    }

    public Long getId() { return id; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public String getAvailability() { return availability; }
    public String getProviderName() { return providerName; }
    public String getProviderLocation() { return providerLocation; }
    public String getStatus() { return status; }
    public Long getProviderId() { return providerId; }
    public Double getProviderLat() { return providerLat; }
    public Double getProviderLng() { return providerLng; }
    public long getCompletedJobs() { return completedJobs; }
    public String getServiceArea() { return serviceArea; }
    public String getMemberSince() { return memberSince; }
    public boolean isVerified() { return verified; }
    public double getRating() { return rating; }
    public long getReviews() { return reviews; }

    public void setVerified(boolean verified) { this.verified = verified; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviews(long reviews) { this.reviews = reviews; }
}