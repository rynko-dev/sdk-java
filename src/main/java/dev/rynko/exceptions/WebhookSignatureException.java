package dev.rynko.exceptions;

/**
 * Exception thrown when webhook signature verification fails.
 */
public class WebhookSignatureException extends RuntimeException {

    public WebhookSignatureException(String message) {
        super(message);
    }

    public WebhookSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
