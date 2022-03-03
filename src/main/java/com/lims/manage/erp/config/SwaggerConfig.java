package com.lims.manage.erp.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author robin
 * @version 1.0
 * @description: TODO
 * @date 2022/2/21 18:13
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.lims.manage.erp.controller"))   // 指定controller包
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("交科院ERP-Api")
                .contact(new Contact("badcat",
                        "https://www.xxxx.com",
                        "xxxx@xxxx.com"))
                .description("交科院ERP-Api")
                .version("1.0.1")
                .termsOfServiceUrl("https://www.xxxx.com")
                .build();
    }
}
