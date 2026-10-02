package com.farmora.service;

import com.farmora.dto.complaint.ComplaintRequest;
import com.farmora.dto.complaint.ComplaintResponse;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.*;
import com.farmora.repository.ComplaintRepository;
import com.farmora.repository.OrderRepository;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    @Transactional
    public ComplaintResponse submitComplaint(ComplaintRequest request, String complainantEmail) {
        User complainant = authService.getUserByEmail(complainantEmail);

        User targetUser = null;
        if (request.getTargetUserId() != null) {
            targetUser = userRepository.findById(request.getTargetUserId()).orElse(null);
        }

        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId()).orElse(null);
            if (targetUser == null && order != null) {
                targetUser = complainant.getId().equals(order.getBuyer().getId()) ? order.getFarmer() : order.getBuyer();
            }
        }

        Complaint complaint = Complaint.builder()
                .complainant(complainant)
                .targetUser(targetUser)
                .order(order)
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(ComplaintStatus.PENDING)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        // Notify user that complaint was logged
        notificationService.createNotification(
                complainant,
                "Complaint Ticket #" + saved.getId() + " Logged",
                "Your dispute regarding '" + saved.getSubject() + "' has been submitted to platform moderators.",
                "COMPLAINT_SUBMITTED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public List<ComplaintResponse> getMyComplaints(String complainantEmail) {
        User complainant = authService.getUserByEmail(complainantEmail);
        return complaintRepository.findByComplainantOrderByCreatedAtDesc(complainant).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ComplaintResponse> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintResponse updateComplaintStatus(Long id, ComplaintStatus status, String adminNotes) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        complaint.setStatus(status);
        if (adminNotes != null) {
            complaint.setAdminNotes(adminNotes);
        }
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.DISMISSED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        // Notify complainant
        notificationService.createNotification(
                saved.getComplainant(),
                "Complaint Ticket #" + saved.getId() + " Updated",
                "Status: " + status + (adminNotes != null ? " - Notes: " + adminNotes : ""),
                "COMPLAINT_UPDATED",
                saved.getId()
        );

        return mapToResponse(saved);
    }

    public ComplaintResponse mapToResponse(Complaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .complainant(authService.mapToUserResponse(c.getComplainant()))
                .targetUser(c.getTargetUser() != null ? authService.mapToUserResponse(c.getTargetUser()) : null)
                .orderId(c.getOrder() != null ? c.getOrder().getId() : null)
                .subject(c.getSubject())
                .description(c.getDescription())
                .status(c.getStatus())
                .adminNotes(c.getAdminNotes())
                .createdAt(c.getCreatedAt())
                .resolvedAt(c.getResolvedAt())
                .build();
    }
}
