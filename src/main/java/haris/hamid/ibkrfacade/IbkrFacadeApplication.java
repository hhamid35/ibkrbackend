package haris.hamid.ibkrfacade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = { ErrorMvcAutoConfiguration.class })
public class IbkrFacadeApplication {

    public static void main(String[] args) {
        SpringApplication.run(IbkrFacadeApplication.class, args);
    }
}
