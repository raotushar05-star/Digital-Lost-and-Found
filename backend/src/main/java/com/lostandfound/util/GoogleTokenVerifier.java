package com.lostandfound.util;

import com.lostandfound.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** Verifies a Google sign-in token with Google and returns the verified identity. */
@Component
public class GoogleTokenVerifier {

    public record GoogleIdentity(String email, String name) {}

    @Value("${app.google.client-id:}")
    private String clientId;

    private final RestClient rest = RestClient.create();

    /** When no client id is configured, Google verification is off. */
    public boolean isEnabled() {
        return StringUtils.hasText(clientId);
    }

    @SuppressWarnings("unchecked")
    public GoogleIdentity verify(String idToken) {
        Map<String, Object> body;
        try {
            body = rest.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={t}", idToken)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException ex) {
            throw new BadRequestException("Google verification failed or expired. Please verify with Google again");
        }
        if (body == null || !clientId.equals(String.valueOf(body.get("aud")))) {
            throw new BadRequestException("This Google sign-in is not valid for this application");
        }
        String issuer = String.valueOf(body.get("iss"));
        if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
            throw new BadRequestException("This Google sign-in is not valid");
        }
        if (!"true".equals(String.valueOf(body.get("email_verified")))) {
            throw new BadRequestException("Your Google email is not verified");
        }
        Object email = body.get("email");
        if (email == null) {
            throw new BadRequestException("Google did not provide an email address");
        }
        Object name = body.get("name");
        return new GoogleIdentity(email.toString(), name == null ? null : name.toString());
    }
}