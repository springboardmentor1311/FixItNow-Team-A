package com.fixitnow.backend.service;

import java.security.Principal;
import java.util.List;

import com.fixitnow.backend.dto.BookingCreateRequest;
import com.fixitnow.backend.dto.BookingResponse;
import com.fixitnow.backend.entity.Role;
import com.fixitnow.backend.entity.ServiceEntity;
import com.fixitnow.backend.entity.User;
import com.fixitnow.backend.entity.ProviderProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fixitnow.backend.entity.Booking;
import com.fixitnow.backend.repository.BookingRepository;
import com.fixitnow.backend.repository.ProviderProfileRepository;
import com.fixitnow.backend.repository.ServiceRepository;
import com.fixitnow.backend.repository.UserRepository;

@Service
@SuppressWarnings("null")
public class BookingService {

    private static final String PENDING = "PENDING";
    private static final String CONFIRMED = "CONFIRMED";
    private static final String REJECTED = "REJECTED";
    private static final String COMPLETED = "COMPLETED";
    private static final String CANCELLED = "CANCELLED";

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ProviderProfileRepository providerProfileRepository;

    @Autowired
    private NotificationService notificationService;

    private void ensureBookingAccess(User user) {
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new RuntimeException("Your account is suspended. Please wait for admin approval");
        }

        if (user.getRole() == Role.PROVIDER) {
            ProviderProfile profile = providerProfileRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Provider profile not found"));

            if (!"APPROVED".equalsIgnoreCase(profile.getApprovalStatus())) {
                throw new RuntimeException("Your provider account is pending admin approval");
            }
        }
    }

    public BookingResponse createBooking(BookingCreateRequest request, Principal principal) {

        User customer = getLoggedInUser(principal);
        ensureBookingAccess(customer);
        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Only CUSTOMER can create booking");
        }

        ServiceEntity service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        User provider = service.getProvider();
        if (provider == null || provider.getRole() != Role.PROVIDER) {
            throw new RuntimeException("Service does not have a valid PROVIDER");
        }

        if (request.getTimeSlot() == null || request.getTimeSlot().isBlank()) {
            throw new RuntimeException("timeSlot is required");
        }

        // FIX: Pass the date into the new Repository check!
        boolean alreadyBooked = bookingRepository.existsByProviderIdAndBookingDateAndTimeSlotAndStatusIn(
                provider.getId(),
                request.getBookingDate(), 
                request.getTimeSlot(),
                List.of(PENDING, CONFIRMED)
        );

        if (alreadyBooked) {
            throw new RuntimeException("Time slot already booked!");
        }

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setProvider(provider);
        booking.setService(service);
        booking.setTimeSlot(request.getTimeSlot());
        booking.setStatus(PENDING);

        booking.setBookingDate(request.getBookingDate());
        booking.setCustomerLocation(
            request.getCustomerLocation() != null && !request.getCustomerLocation().isBlank()
                ? request.getCustomerLocation().trim()
                : customer.getLocation());
        booking.setCustomerLat(request.getCustomerLat() != null ? request.getCustomerLat() : customer.getLatitude());
        booking.setCustomerLng(request.getCustomerLng() != null ? request.getCustomerLng() : customer.getLongitude());

        Booking saved = bookingRepository.save(booking);

        notificationService.notifyUser(
            provider.getId(),
            saved.getId(),
            "provider",
            "📅",
            "New booking request for " + service.getCategory() + " on " + saved.getBookingDate() + " at " + saved.getTimeSlot(),
            NotificationService.EVENT_BOOKING,
            "/provider/bookings");

        return new BookingResponse(saved);
    }

    public List<BookingResponse> getCustomerBookings(Principal principal) {
        User customer = getLoggedInUser(principal);
        ensureBookingAccess(customer);
        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Only CUSTOMER can view customer bookings");
        }

        return bookingRepository.findByCustomerId(customer.getId())
                .stream()
                .map(BookingResponse::new)
                .toList();
    }

    public List<BookingResponse> getProviderBookings(Principal principal) {
        User provider = getLoggedInUser(principal);
        ensureBookingAccess(provider);
        if (provider.getRole() != Role.PROVIDER) {
            throw new RuntimeException("Only PROVIDER can view provider bookings");
        }

        return bookingRepository.findByProviderId(provider.getId())
                .stream()
                .map(BookingResponse::new)
                .toList();
    }

    public BookingResponse cancelBooking(Long bookingId, Principal principal) {
        User customer = getLoggedInUser(principal);
        ensureBookingAccess(customer);
        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Only CUSTOMER can cancel booking");
        }

        Booking booking = getBookingById(bookingId);
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized to cancel this booking");
        }

        if (COMPLETED.equals(booking.getStatus()) || CANCELLED.equals(booking.getStatus())) {
            throw new RuntimeException("Booking cannot be cancelled in current status");
        }

        booking.setStatus(CANCELLED);
        Booking saved = bookingRepository.save(booking);

        notificationService.notifyUser(
            booking.getProvider().getId(),
            saved.getId(),
            "provider",
            "❌",
            "Booking was cancelled by customer for " + booking.getService().getCategory(),
            NotificationService.EVENT_BOOKING,
            "/provider/bookings");

        return new BookingResponse(saved);
    }

    public BookingResponse acceptBooking(Long bookingId, Principal principal) {
        User provider = getLoggedInUser(principal);
        ensureBookingAccess(provider);
        if (provider.getRole() != Role.PROVIDER) {
            throw new RuntimeException("Only PROVIDER can accept booking");
        }

        Booking booking = getBookingById(bookingId);
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized to accept this booking");
        }

        if (!PENDING.equals(booking.getStatus())) {
            throw new RuntimeException("Only PENDING booking can be accepted");
        }

        booking.setStatus(CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        notificationService.notifyUser(
            booking.getCustomer().getId(),
            saved.getId(),
            "customer",
            "✅",
            "Your booking was accepted by " + booking.getProvider().getName(),
            NotificationService.EVENT_BOOKING,
            "/customer/bookings");

        return new BookingResponse(saved);
    }

    public BookingResponse rejectBooking(Long bookingId, Principal principal) {
        User provider = getLoggedInUser(principal);
        ensureBookingAccess(provider);
        if (provider.getRole() != Role.PROVIDER) {
            throw new RuntimeException("Only PROVIDER can reject booking");
        }

        Booking booking = getBookingById(bookingId);
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized to reject this booking");
        }

        if (!PENDING.equals(booking.getStatus())) {
            throw new RuntimeException("Only PENDING booking can be rejected");
        }

        booking.setStatus(REJECTED);
        Booking saved = bookingRepository.save(booking);

        notificationService.notifyUser(
            booking.getCustomer().getId(),
            saved.getId(),
            "customer",
            "❌",
            "Your booking was declined by " + booking.getProvider().getName(),
            NotificationService.EVENT_BOOKING,
            "/customer/bookings");

        return new BookingResponse(saved);
    }

    public BookingResponse completeBooking(Long bookingId, Principal principal) {
        User provider = getLoggedInUser(principal);
        ensureBookingAccess(provider);
        if (provider.getRole() != Role.PROVIDER) {
            throw new RuntimeException("Only PROVIDER can complete booking");
        }

        Booking booking = getBookingById(bookingId);
        if (!booking.getProvider().getId().equals(provider.getId())) {
            throw new RuntimeException("Unauthorized to complete this booking");
        }

        if (!CONFIRMED.equals(booking.getStatus())) {
            throw new RuntimeException("Only CONFIRMED booking can be completed");
        }

        booking.setStatus(COMPLETED);
        Booking saved = bookingRepository.save(booking);

        notificationService.notifyUser(
            booking.getCustomer().getId(),
            saved.getId(),
            "customer",
            "⭐",
            "Work marked completed for " + booking.getService().getCategory() + ". Please proceed to payment.",
            NotificationService.EVENT_BOOKING,
            "/customer/bookings");

        return new BookingResponse(saved);
    }

    public BookingResponse notifyPayment(Long bookingId, Principal principal) {
        User customer = getLoggedInUser(principal);
        ensureBookingAccess(customer);
        if (customer.getRole() != Role.CUSTOMER) {
            throw new RuntimeException("Only CUSTOMER can confirm payment");
        }

        Booking booking = getBookingById(bookingId);
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Unauthorized to confirm payment for this booking");
        }

        notificationService.notifyUser(
            booking.getProvider().getId(),
            booking.getId(),
            "provider",
            "💰",
            "Payment received for " + booking.getService().getCategory() + " booking.",
            NotificationService.EVENT_BOOKING,
            "/provider/bookings");

        return new BookingResponse(booking);
    }

    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + id));
    }

    private User getLoggedInUser(Principal principal) {
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}