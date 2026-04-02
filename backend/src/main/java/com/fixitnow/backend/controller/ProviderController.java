package com.fixitnow.backend.controller;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixitnow.backend.entity.ProviderProfile;
import com.fixitnow.backend.entity.Role;
import com.fixitnow.backend.entity.User;
import com.fixitnow.backend.repository.ProviderProfileRepository;
import com.fixitnow.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    private final UserRepository userRepository;
    private final ProviderProfileRepository providerProfileRepository;

    public ProviderController(UserRepository userRepository, ProviderProfileRepository providerProfileRepository) {
        this.userRepository = userRepository;
        this.providerProfileRepository = providerProfileRepository;
    }

    @GetMapping("/dashboard")
    public String providerDashboard() {
        return "Welcome Provider!";
    }

    @GetMapping("/availability")
    public Map<String, Boolean> getAvailability(Principal principal) {
        ProviderProfile profile = getProviderProfile(principal);
        return Map.of("online", profile.getOnline() == null || profile.getOnline());
    }

    @PutMapping("/availability")
    public ResponseEntity<Map<String, Boolean>> updateAvailability(
            @RequestParam boolean online,
            Principal principal) {

        ProviderProfile profile = getProviderProfile(principal);
        profile.setOnline(online);
        providerProfileRepository.save(profile);
        return ResponseEntity.ok(Map.of("online", online));
    }

    private ProviderProfile getProviderProfile(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new SecurityException("Unauthorized");
        }

        User user = userRepository.findByEmailIgnoreCase(principal.getName().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.PROVIDER) {
            throw new SecurityException("Only providers can update availability");
        }

        return providerProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Provider profile not found"));
    }
}
