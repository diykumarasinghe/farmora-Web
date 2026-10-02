package com.farmora.controller;

import com.farmora.dto.invoice.InvoiceResponse;
import com.farmora.dto.invoice.UpdateInvoiceRequest;
import com.farmora.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/generate/{orderId}")
    public ResponseEntity<InvoiceResponse> generateInvoice(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(invoiceService.generateInvoiceForOrder(orderId, userDetails.getUsername()));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(invoiceService.getInvoiceByOrderId(orderId, userDetails.getUsername()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<InvoiceResponse>> getMyInvoices(
            @RequestParam(required = false) Boolean archived,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(invoiceService.getMyInvoices(archived, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoiceDetails(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvoiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(invoiceService.updateInvoiceDetails(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<InvoiceResponse> toggleArchive(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(invoiceService.toggleArchive(id, userDetails.getUsername()));
    }
}
