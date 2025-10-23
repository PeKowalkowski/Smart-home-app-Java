package com.smartHomeApp.SmartHomeApp.config.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class JwtConfig {

  private final RSAKeyRecord rsaKeyRecord;

  public JwtConfig(RSAKeyRecord rsaKeyRecord) {
    this.rsaKeyRecord = rsaKeyRecord;
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withPublicKey(rsaKeyRecord.rsaPublicKey()).build();
  }


  @Bean
  public JwtEncoder jwtEncoder() {
    var jwk = new RSAKey.Builder(rsaKeyRecord.rsaPublicKey())
      .privateKey(rsaKeyRecord.rsaPrivateKey())
      .build();
    var jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwkSource);
  }
}
