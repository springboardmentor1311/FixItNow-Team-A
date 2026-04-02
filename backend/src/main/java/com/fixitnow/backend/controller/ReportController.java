package com.fixitnow.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fixitnow.backend.entity.Report;
import com.fixitnow.backend.entity.Role;
import com.fixitnow.backend.entity.User;
import com.fixitnow.backend.repository.ReportRepository;
import com.fixitnow.backend.repository.UserRepository;
import com.fixitnow.backend.service.NotificationService;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @GetMapping("/mine")
    public ResponseEntity<List<Report>> getMyReports(
            @RequestParam(defaultValue = "booking") String targetType,
            Principal principal) {

        User me = getLoggedInUser(principal);
        List<Report> reports = reportRepository
                .findByReportedByAndTargetTypeIgnoreCaseOrderByCreatedAtDesc(me.getId(), targetType);
        return ResponseEntity.ok(reports);
    }

    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody Report report, Principal principal) {
        User me = getLoggedInUser(principal);

        if (report.getTargetType() == null || report.getTargetType().isBlank()) {
            throw new IllegalArgumentException("targetType is required");
        }
        if (report.getTargetId() == null) {
            throw new IllegalArgumentException("targetId is required");
        }
        if (report.getReason() == null || report.getReason().trim().isEmpty()) {
            throw new IllegalArgumentException("reason is required");
        }

        if (reportRepository.existsByReportedByAndTargetTypeIgnoreCaseAndTargetId(
                me.getId(), report.getTargetType(), report.getTargetId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Issue already reported for this booking.");
        }

        report.setReportedBy(me.getId());
        report.setReason(report.getReason().trim());
        report.setStatus("OPEN");

        // assign report time if not already set
        if (report.getCreatedAt() == null) {
            report.setCreatedAt(LocalDateTime.now());
        }

        Report saved = reportRepository.save(report);

        notificationService.notifyAdmins(
            null,
            "🚩",
            "New report submitted: " + saved.getTargetType() + " #" + saved.getTargetId(),
            NotificationService.EVENT_SYSTEM,
            "/admin/disputes");

        User reporter = userRepository.findById(saved.getReportedBy()).orElse(null);
        String reporterRole = reporter != null && reporter.getRole() == Role.PROVIDER ? "provider" : "customer";
        String reporterPath = "provider".equals(reporterRole) ? "/provider/bookings" : "/customer/bookings";

        notificationService.notifyUser(
            saved.getReportedBy(),
            null,
            reporterRole,
            "📝",
            "Your report has been submitted and will be reviewed by admin.",
            NotificationService.EVENT_SYSTEM,
            reporterPath);

        return ResponseEntity.ok(saved);
    }

    private User getLoggedInUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new SecurityException("Unauthorized");
        }

        return userRepository.findByEmailIgnoreCase(principal.getName().trim())
                .orElseThrow(() -> new RuntimeException("User not found for authenticated session"));
    }
}