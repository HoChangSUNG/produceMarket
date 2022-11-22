package creative.market;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

@SpringBootApplication
@Slf4j
public class ProducemarketApplication {

	public static void main(String[] args) {SpringApplication.run(ProducemarketApplication.class, args);}

	@PostConstruct
	public void setTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		log.info("LocalDateTime.now()={}",LocalDateTime.now());
	}
}