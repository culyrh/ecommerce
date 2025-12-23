package ecommerce.domain.user.dto;

import ecommerce.common.enums.Role;
import ecommerce.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private Role role;
    private String provider;
    private BigDecimal totalPurchaseAmount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .role(user.getRole())
                .provider(user.getProvider())
                .totalPurchaseAmount(user.getTotalPurchaseAmount())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}