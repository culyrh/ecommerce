package ecommerce.domain.seller.service;

import ecommerce.common.enums.Role;
import ecommerce.common.exception.DuplicateResourceException;
import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.ResourceNotFoundException;
import ecommerce.domain.seller.dto.SellerRequest;
import ecommerce.domain.seller.dto.SellerResponse;
import ecommerce.domain.seller.entity.Seller;
import ecommerce.domain.seller.repository.SellerRepository;
import ecommerce.domain.user.entity.User;
import ecommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    /**
     * 판매자 등록
     */
    @Transactional
    public SellerResponse registerSeller(String email, SellerRequest request) {
        log.info("판매자 등록 시도: email={}, businessNumber={}", email, request.getBusinessNumber());

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 이미 판매자로 등록되어 있는지 확인
        if (sellerRepository.findByUserId(user.getId()).isPresent()) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_RESOURCE, "이미 판매자로 등록되어 있습니다");
        }

        // 사업자 번호 중복 확인
        if (sellerRepository.existsByBusinessNumber(request.getBusinessNumber())) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }

        // 판매자 생성
        Seller seller = Seller.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessNumber(request.getBusinessNumber())
                .minStockThreshold(request.getMinStockThreshold() != null ? request.getMinStockThreshold() : 10)
                .build();

        Seller savedSeller = sellerRepository.save(seller);

        // 사용자 역할에 SELLER 추가
        if (user.getRole() == Role.ROLE_USER) {
            user.setRole(Role.ROLE_SELLER);
            userRepository.save(user);
        }

        log.info("판매자 등록 완료: sellerId={}, userId={}", savedSeller.getId(), user.getId());

        return SellerResponse.from(savedSeller);
    }

    /**
     * 내 판매자 정보 조회
     */
    @Transactional(readOnly = true)
    public SellerResponse getMySeller(String email) {
        log.info("내 판매자 정보 조회: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        return SellerResponse.from(seller);
    }

    /**
     * 내 판매자 정보 수정
     */
    @Transactional
    public SellerResponse updateMySeller(String email, SellerRequest request) {
        log.info("내 판매자 정보 수정: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 사업자 번호가 변경되는 경우 중복 체크
        if (request.getBusinessNumber() != null &&
                !request.getBusinessNumber().equals(seller.getBusinessNumber())) {
            if (sellerRepository.existsByBusinessNumber(request.getBusinessNumber())) {
                throw new DuplicateResourceException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
            }
            seller.setBusinessNumber(request.getBusinessNumber());
        }

        // 변경 사항 적용
        if (request.getBusinessName() != null) {
            seller.setBusinessName(request.getBusinessName());
        }
        if (request.getMinStockThreshold() != null) {
            seller.setMinStockThreshold(request.getMinStockThreshold());
        }

        Seller updatedSeller = sellerRepository.save(seller);
        log.info("내 판매자 정보 수정 완료: sellerId={}", updatedSeller.getId());

        return SellerResponse.from(updatedSeller);
    }

    /**
     * 판매자 등록 해제
     */
    @Transactional
    public void deleteMySeller(String email) {
        log.info("판매자 등록 해제: email={}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        Seller seller = sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SELLER_NOT_FOUND));

        // 판매자 삭제
        sellerRepository.delete(seller);

        // 사용자 역할을 다시 USER로 변경
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        log.info("판매자 등록 해제 완료: sellerId={}, userId={}", seller.getId(), user.getId());
    }
}