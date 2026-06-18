package pg.net.ai_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import pg.net.ai_services.config.DotenvLoader;

@SpringBootApplication
public class AiServicesApplication {

	public static void main(String[] args) {
		DotenvLoader.load();
		SpringApplication.run(AiServicesApplication.class, args);
	}

}
