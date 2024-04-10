package com.nhnacademy.store99.gateway.property;

import com.nhnacademy.store99.gateway.util.SecureKeyManagerUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {
    private final SecureKeyManagerUtil secureKeyManagerUtil;

    private String host;
    private int port;
    private String password;
    private int database;

    public void setHost(final String host) {
        if (secureKeyManagerUtil.isEncrypted(host)) {
            this.host = secureKeyManagerUtil.loadConfidentialData(host);
        } else {
            this.host = host;
        }
    }

    public void setPort(final String port) {
        if (secureKeyManagerUtil.isEncrypted(port)) {
            this.port = Integer.parseInt(secureKeyManagerUtil.loadConfidentialData(port));
        } else {
            this.port = Integer.parseInt(port);
        }
    }

    public void setPassword(final String password) {
        if (secureKeyManagerUtil.isEncrypted(password)) {
            this.password = secureKeyManagerUtil.loadConfidentialData(password);
        } else {
            this.password = password;
        }
    }
}
