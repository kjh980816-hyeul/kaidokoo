package app.kaidoku.fancafe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 씨미 라이브 폴링 + 만료 세션 정리
public class FancafeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FancafeApplication.class, args);
	}

}
