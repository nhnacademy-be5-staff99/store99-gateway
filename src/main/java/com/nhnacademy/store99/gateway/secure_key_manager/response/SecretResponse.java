package com.nhnacademy.store99.gateway.secure_key_manager.response;

import lombok.Setter;

@Setter
public class SecretResponse {
    private SecretHeaderResponse header;
    private SecretBodyResponse body;

    public String getSecret() {
        return body.getSecret();
    }
}