package com.proyecto.servicios.model.pagos.brevo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrevoEmailRequest {
    private BrevoSender sender;
    private List<BrevoRecipient> to;
    private String subject;
    private String htmlContent;
    private String textContent;
}
