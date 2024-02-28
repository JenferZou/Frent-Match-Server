package com.jenfer.frentmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
@Profile({"dev", "test"})   //版本控制访问
public class SwaggerConfig {

    @Bean(value = "defaultApi2")
    public Docket creatApi(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select() //选择哪些路径和api会生成document
                .apis(RequestHandlerSelectors.basePackage("com.jenfer.frentmatch.controller"))//controller路径
                .paths(PathSelectors.any())  //对所有路径进行监控
                .build();
    }

    private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
            .title("frentmatch")//文档主标题
            .description("匹配系统")//文档描述
            .version("1.0.0")//API的版本
            .termsOfServiceUrl("###")
            .license("LICENSE")
            .licenseUrl("###")
            .build();
}

}