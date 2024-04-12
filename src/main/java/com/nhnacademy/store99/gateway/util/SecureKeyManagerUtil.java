package com.nhnacademy.store99.gateway.util;

import com.nhnacademy.store99.gateway.secure_key_manager.exception.SecureKeyMangerException;
import com.nhnacademy.store99.gateway.secure_key_manager.property.SecureKeyManagerProperties;
import com.nhnacademy.store99.gateway.secure_key_manager.response.SecretResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Objects;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class SecureKeyManagerUtil {
    private final SecureKeyManagerProperties secureKeyManagerProperties;
    private final RestTemplate sslRestTemplate;

    public SecureKeyManagerUtil(SecureKeyManagerProperties secureKeyManagerProperties) {
        this.secureKeyManagerProperties = secureKeyManagerProperties;

        // ssl rest template 생성
        try {
            sslRestTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(5))
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .defaultHeader("X-TC-AUTHENTICATION-ID", secureKeyManagerProperties.getXTcAuthenticationId())
                    .defaultHeader("X-TC-AUTHENTICATION-SECRET",
                            secureKeyManagerProperties.getXTcAuthenticationSecret())
                    .build();

            // 클라이언트 인증서를 로드하여 SSLContext 생성
            KeyStore clientStore = KeyStore.getInstance("PKCS12");
            InputStream inputStream = new ClassPathResource("store99.p12").getInputStream();
            clientStore.load(inputStream, secureKeyManagerProperties.getCertificatePassword().toCharArray());
            SSLContext sslContext = SSLContextBuilder.create()
                    .setProtocol("TLS")
                    .loadKeyMaterial(clientStore, secureKeyManagerProperties.getCertificatePassword().toCharArray())
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build();

            // SSLContext를 사용하여 SSLConnectionSocketFactory 생성
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslConnectionSocketFactory)
                    .build();

            // SSLConnectionSocketFactory를 사용하여 HttpComponentsClientHttpRequestFactory 생성
            HttpComponentsClientHttpRequestFactory requestFactory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            sslRestTemplate.setRequestFactory(requestFactory);
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException |
                 UnrecoverableKeyException | KeyManagementException ex) {
            throw new SecureKeyMangerException(ex.getMessage());
        }
    }

    // Secure Key Manager에 저장한 기밀 데이터를 조회합니다.
    public String loadConfidentialData(String key) {
        URI uri = UriComponentsBuilder
                .fromUriString(
                        "https://api-keymanager.nhncloudservice.com/keymanager/v1.2/appkey/{appkey}/secrets/{keyid}")
                .encode()
                .buildAndExpand(secureKeyManagerProperties.getAppKey(), key)
                .toUri();

        return Objects.requireNonNull(sslRestTemplate.getForObject(uri, SecretResponse.class)).getSecret();
    }

    public boolean isEncrypted(String key) {
        return key.matches("^[a-zA-Z0-9]{32}$");
    }
}
