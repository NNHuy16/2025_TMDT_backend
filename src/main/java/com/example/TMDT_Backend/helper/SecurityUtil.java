package com.example.TMDT_Backend.helper;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class SecurityUtil {
    public static String getEmailFromAuth(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            return oauth2.getPrincipal().getAttribute("email");
        } else {
            return authentication.getName();
        }
    }

    public static String extractEmail(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            return oauth2.getPrincipal().getAttribute("email");
        } else {
            return authentication.getName(); // Dành cho JWT
        }
    }
}
