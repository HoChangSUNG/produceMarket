package creative.market.config;

import org.qlrm.mapper.JpaResultMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QLRMConfig {

    @Bean
    public JpaResultMapper jpaResultMapper() {
        return new JpaResultMapper();
    }
}
