package fr.gouv.vitam.sipservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SipserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SipserviceApplication.class, args);
	}

	@Profile("dev")
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/sip/*")
						.allowedOrigins("http://localhost:4200")
						.exposedHeaders("Content-Disposition");
			}
		};
	}
}
