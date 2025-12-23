package ecommerce.domain.user.service;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.user.dto.UserResponse;
import ecommerce.domain.user.dto.UserUpdateRequest;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 내 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponse getMyInfo(String email) {
        log.info("내 정보 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        return UserResponse.from(user);
    }

    /**
     * 내 정보 수정
     */
    @Transactional
    public UserResponse updateMyInfo(String email, UserUpdateRequest request) {
        log.info("내 정보 수정: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 변경 사항 적용
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBirthDate() != null) {
            user.setBirthDate(request.getBirthDate());
        }

        User updatedUser = userRepository.save(user);
        log.info("내 정보 수정 완료: userId={}", updatedUser.getId());

        return UserResponse.from(updatedUser);
    }

    /**
     * 회원 탈퇴 (소프트 삭제)
     */
    @Transactional
    public void deleteMyAccount(String email) {
        log.info("회원 탈퇴: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 소프트 삭제 (is_active = false)
        user.setIsActive(false);
        userRepository.save(user);

        log.info("회원 탈퇴 완료: userId={}", user.getId());
    }

    /**
     * 회원 목록 조회 (관리자 전용)
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("회원 목록 조회: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);

        return users.map(UserResponse::from);
    }
}