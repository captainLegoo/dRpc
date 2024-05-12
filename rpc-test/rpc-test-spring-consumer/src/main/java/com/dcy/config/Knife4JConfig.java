package com.dcy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @author Kyle
 * @date 2024/02/23
 *
 * Knife4j configuration class
 */
@Data
@Configuration
@EnableSwagger2WebMvc
@ConfigurationProperties("swagger.config")
public class Knife4JConfig {

    private String basePackage;

    @Bean(value = "defaultApi")
    public Docket defaultApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("RPC-Testing-Spring-Consumer")
                        .description("")
                        .termsOfServiceUrl("http://localhost:8080/swagger-ui.html")
                        .version("1.0")
                        .build())
                .groupName("V1.0")
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePackage))
                .paths(PathSelectors.any())
                .build();
    }

}
