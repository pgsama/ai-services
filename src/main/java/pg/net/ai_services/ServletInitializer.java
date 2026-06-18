package pg.net.ai_services;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import pg.net.ai_services.config.DotenvLoader;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		DotenvLoader.load();
		return application.sources(AiServicesApplication.class);
	}

}
