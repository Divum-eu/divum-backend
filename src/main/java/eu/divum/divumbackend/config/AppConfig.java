package eu.divum.divumbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.PropertyNamingStrategies;

import java.net.http.HttpClient;

import java.time.Duration;

@Configuration
public class AppConfig {
    @Bean
    @Qualifier("defaultHttpClient")
    HttpClient defaultHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Bean
    @Profile("dev")
    @Qualifier("httpClient")
    HttpClient devHttpClient() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Bean
    @Profile("!dev")
    @Qualifier("httpClient")
    HttpClient prodHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Bean
    @Primary
    JsonMapper jsonMapper() {
        return new JsonMapper();
    }

    @Bean
    JsonMapper snakeCaseJsonMapper() {
        return JsonMapper
                .builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();
    }
}
