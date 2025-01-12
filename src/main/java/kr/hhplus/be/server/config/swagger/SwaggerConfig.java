package kr.hhplus.be.server.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("콘서트 예약 서비스 API")
                .version("1.0.0")
                .description("콘서트 예약 서비스의 API 문서입니다.");
        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("http://localhost:8080").description("로컬"));
    }
}
