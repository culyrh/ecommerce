package ecommerce.infrastructure.naver;

import ecommerce.common.exception.ErrorCode;
import ecommerce.common.exception.UnprocessableEntityException;
import ecommerce.domain.product.dto.NaverProductSearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class NaverShoppingApiClient {

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.api.shop-search-url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 네이버 쇼핑 API 상품 검색
     *
     * @param keyword 검색 키워드
     * @param display 검색 결과 개수 (기본 10, 최대 100)
     * @param start   검색 시작 위치 (기본 1, 최대 1000)
     * @return 검색 결과
     */
    public NaverProductSearchResponse searchProducts(String keyword, Integer display, Integer start) {
        log.info("네이버 쇼핑 API 호출: keyword={}, display={}, start={}", keyword, display, start);

        try {
            // 요청 URL 생성
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("query", keyword)
                    .queryParam("display", display != null ? display : 10)
                    .queryParam("start", start != null ? start : 1)
                    .queryParam("sort", "sim") // 정확도순
                    .build()
                    .toUriString();

            // 요청 헤더 생성
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Naver-Client-Id", clientId);
            headers.set("X-Naver-Client-Secret", clientSecret);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // API 호출
            ResponseEntity<NaverProductSearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverProductSearchResponse.class
            );

            NaverProductSearchResponse result = response.getBody();
            log.info("네이버 쇼핑 API 호출 성공: total={}, display={}",
                    result != null ? result.getTotal() : 0,
                    result != null ? result.getDisplay() : 0);

            return result;

        } catch (Exception e) {
            log.error("네이버 쇼핑 API 호출 실패", e);
            throw new UnprocessableEntityException(ErrorCode.EXTERNAL_API_ERROR, "네이버 쇼핑 API 호출에 실패했습니다");
        }
    }
}