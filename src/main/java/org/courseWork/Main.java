package org.courseWork;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@OpenAPIDefinition
@EnableCaching
@EnableAsync
public class Main {

    
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        log.info("✅ Product Recommendation Service started!");
        log.info("✅ Available endpoints:");
        log.info("✅   GET  /management/info");
        log.info("✅   GET  /api/products/recommendations/{userId}");
        log.info("✅   POST /management/clear-caches");
        log.info("✅   GET  /rule/stats");

    }

}
