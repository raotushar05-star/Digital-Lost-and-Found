package com.lostandfound.service;

import com.lostandfound.entity.User;
import com.lostandfound.entity.enums.NotificationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.EnumSet;
import java.util.Set;

/** Sends short SMS or WhatsApp messages through Twilio. Off unless app.sms.enabled=true. */
@Service
@Slf4j
public class SmsService {

    private static final Set<NotificationType> SENT_TYPES = EnumSet.of(
            NotificationType.MATCH_FOUND, NotificationType.CLAIM_UPDATE,
            NotificationType.DISPUTE_UPDATE, NotificationType.ITEM_RECOVERED);

    @Value("${app.sms.enabled:false}")
    private boolean enabled;
    @Value("${app.sms.channel:sms}")
    private String channel;
    @Value("${app.sms.account-sid:}")
    private String accountSid;
    @Value("${app.sms.auth-token:}")
    private String authToken;
    @Value("${app.sms.from:}")
    private String from;

    private final RestClient rest = RestClient.create();

    @Async("emailTaskExecutor")
    public void sendIfImportant(User user, NotificationType type, String title, String message) {
        if (!enabled || user == null || !SENT_TYPES.contains(type)) {
            return;
        }
        String to = toE164(user.getPhone());
        if (to == null) {
            return;
        }
        String prefix = "whatsapp".equalsIgnoreCase(channel) ? "whatsapp:" : "";
        String body = title + ": " + message;
        if (body.length() > 300) {
            body = body.substring(0, 297) + "...";
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("To", prefix + to);
            form.add("From", prefix + from);
            form.add("Body", body);
            rest.post()
                    .uri("https://api.twilio.com/2010-04-01/Accounts/{sid}/Messages.json", accountSid)
                    .headers(h -> h.setBasicAuth(accountSid, authToken))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.warn("Failed to send {} to {}: {}", channel, to, ex.getMessage());
        }
    }

    /** 10-digit Indian numbers get +91. Returns null when the number can't be used. */
    private static String toE164(String phone) {
        if (phone == null) return null;
        String p = phone.replaceAll("[^0-9+]", "");
        if (p.startsWith("+")) return p.length() >= 8 ? p : null;
        if (p.length() == 10) return "+91" + p;
        if (p.length() == 12 && p.startsWith("91")) return "+" + p;
        return null;
    }
}