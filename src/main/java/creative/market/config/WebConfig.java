package creative.market.config;

import creative.market.argumentresolver.LoginUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
       resolvers.add(new LoginUserArgumentResolver());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000"
                        ,"https://bkkang1.github.io:443"
                        ,"https://bkkang1.github.io"
                        ,"http://market-kumoh.shop"
                        ,"112.217.167.202"
                        ,"http://112.217.167.202"
                        ,"https://112.217.167.202"
                        ,"https://localhost:3000"
                        ,"localhost:3000"

                )
                .allowCredentials(true);
    }
}
