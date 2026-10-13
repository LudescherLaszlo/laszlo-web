package mr_white.backend;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan("com.mrwhite")
@EntityScan("com.mrwhite.words")
@EnableJpaRepositories("com.mrwhite.words")
public class BackendApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(BackendApplication.class, args);
    }
}
