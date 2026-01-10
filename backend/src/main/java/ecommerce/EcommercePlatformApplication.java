package ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 스케줄러 활성화를 위해 추가
@EnableAsync       // 비동기 처리 활성화
public class EcommercePlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommercePlatformApplication.class, args);
	}

}