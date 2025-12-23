package ecommerce.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2UserInfo {

    private String email;
    private String name;
    private String provider;
    private String providerId;
}