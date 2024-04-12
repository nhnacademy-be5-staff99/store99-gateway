package com.nhnacademy.store99.gateway.secure_key_manager.response;

import lombok.Setter;

@Setter
public class SecretBodyResponse {
    private String secret;

    public String getSecret() {
        return secret;
    }
}