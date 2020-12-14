package com.iflytek.integrated.common.config;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
/**
* Swagger配置
* @author weihe9
* @date 2020/12/12 19:00
*/
@Configuration
@EnableSwagger2
public class SwaggerConfig  {

//	@Bean
//	public Docket createPlatformApi() {
//		return new Docket(DocumentationType.SWAGGER_2)
//				.groupName("项目平台相关接口")
//				.apiInfo(platformApiInfo())
//				.useDefaultResponseMessages(false)
//				.select()
//				.apis(RequestHandlerSelectors.withClassAnnotation(PlatformApi.class))
//				.build();
//	}
//	/**
//	 * 管理接口描述
//	 */
//	private ApiInfo platformApiInfo() {
//		return new ApiInfoBuilder()
//				.title("Platform")// 大标题
//				.version("1.0")// 版本
//				.build();
//	}


}
