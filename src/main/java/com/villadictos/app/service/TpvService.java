package com.villadictos.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class TpvService {

    @Value("${tpv.api.url}")
    private String tpvApiUrl;

    @Value("${tpv.api.key}")
    private String tpvApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TpvService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Response class for payment initialization
     */
    public static class PaymentInitResponse {
        private String paymentUrl;
        private String token;

        public PaymentInitResponse(String paymentUrl, String token) {
            this.paymentUrl = paymentUrl;
            this.token = token;
        }

        public String getPaymentUrl() {
            return paymentUrl;
        }

        public String getToken() {
            return token;
        }
    }

    /**
     * Response class for payment verification
     */
    public static class PaymentStatusResponse {
        private String status;
        private String failureReason;

        public PaymentStatusResponse(String status, String failureReason) {
            this.status = status;
            this.failureReason = failureReason;
        }

        public String getStatus() {
            return status;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public boolean isCompleted() {
            return "COMPLETED".equals(status);
        }
    }

    /**
     * Initialize a payment with the TPV API
     * 
     * @param amount            Amount to charge
     * @param callbackUrl       URL where TPV will redirect after payment
     * @param externalReference Internal reference (e.g., pending payment ID)
     * @return PaymentInitResponse with paymentUrl and token
     */
    public PaymentInitResponse initPayment(BigDecimal amount, String callbackUrl, String externalReference) {
        String url = tpvApiUrl + "/api/v1/payments/init";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", tpvApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("amount", amount.doubleValue());
        requestBody.put("callbackUrl", callbackUrl);
        requestBody.put("externalReference", externalReference);

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String paymentUrl = jsonNode.get("paymentUrl").asText();
                String token = jsonNode.get("token").asText();
                return new PaymentInitResponse(paymentUrl, token);
            } else {
                throw new RuntimeException("Error initializing payment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with TPV API: " + e.getMessage(), e);
        }
    }

    /**
     * Verify the status of a payment
     * 
     * @param token TPV transaction token
     * @return PaymentStatusResponse with status and optional failure reason
     */
    public PaymentStatusResponse verifyPayment(String token) {
        String url = tpvApiUrl + "/api/v1/payments/verify/" + token;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", tpvApiKey);

        try {
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String status = jsonNode.get("status").asText();
                String failureReason = jsonNode.has("failureReason") && !jsonNode.get("failureReason").isNull()
                        ? jsonNode.get("failureReason").asText()
                        : null;
                return new PaymentStatusResponse(status, failureReason);
            } else {
                throw new RuntimeException("Error verifying payment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with TPV API: " + e.getMessage(), e);
        }
    }

    /**
     * Response class for refund operations
     */
    public static class RefundResponse {
        private Long id;
        private BigDecimal amount;
        private BigDecimal totalRefunded;
        private BigDecimal transactionAmount;
        private String reason;
        private String transactionStatus;

        public RefundResponse(Long id, BigDecimal amount, BigDecimal totalRefunded,
                BigDecimal transactionAmount, String reason, String transactionStatus) {
            this.id = id;
            this.amount = amount;
            this.totalRefunded = totalRefunded;
            this.transactionAmount = transactionAmount;
            this.reason = reason;
            this.transactionStatus = transactionStatus;
        }

        public Long getId() {
            return id;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getTotalRefunded() {
            return totalRefunded;
        }

        public BigDecimal getTransactionAmount() {
            return transactionAmount;
        }

        public String getReason() {
            return reason;
        }

        public String getTransactionStatus() {
            return transactionStatus;
        }
    }

    /**
     * Process a refund for a completed payment
     * 
     * @param transactionToken The token from the original payment
     * @param amount           Amount to refund
     * @return RefundResponse with refund details
     */
    public RefundResponse processRefund(String transactionToken, BigDecimal amount) {
        String url = tpvApiUrl + "/api/v1/refunds/external";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", tpvApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("transactionToken", transactionToken);
        requestBody.put("amount", amount.doubleValue());

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                Long id = jsonNode.has("id") ? jsonNode.get("id").asLong() : null;
                BigDecimal refundAmount = jsonNode.has("amount") ? BigDecimal.valueOf(jsonNode.get("amount").asDouble())
                        : BigDecimal.ZERO;
                BigDecimal totalRefunded = jsonNode.has("totalRefunded")
                        ? BigDecimal.valueOf(jsonNode.get("totalRefunded").asDouble())
                        : BigDecimal.ZERO;
                BigDecimal transactionAmount = jsonNode.has("transactionAmount")
                        ? BigDecimal.valueOf(jsonNode.get("transactionAmount").asDouble())
                        : BigDecimal.ZERO;
                String reason = jsonNode.has("reason") && !jsonNode.get("reason").isNull()
                        ? jsonNode.get("reason").asText()
                        : null;
                String transactionStatus = jsonNode.has("transactionStatus")
                        ? jsonNode.get("transactionStatus").asText()
                        : null;

                return new RefundResponse(id, refundAmount, totalRefunded, transactionAmount, reason,
                        transactionStatus);
            } else {
                throw new RuntimeException("Error processing refund: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error communicating with TPV API for refund: " + e.getMessage(), e);
        }
    }
}
