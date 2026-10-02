package com.farmora.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceRequest {
    private String billingName;
    private String billingAddress;
    private String taxId;
    private String businessNotes;
}
