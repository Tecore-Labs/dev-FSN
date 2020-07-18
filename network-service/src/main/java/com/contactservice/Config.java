package com.contactservice;

import com.contactservice.interceptor.AccessTokenValidator;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.contactservice.constants.ContactConstants.InterceptorApiUrls.API_URLS;


@Configuration
@EnableNeo4jRepositories("com.contactservice.neo4j.repository")
@EnableTransactionManagement
@EnableWebMvc
public class Config implements WebMvcConfigurer {

    @Value("${neo4j.username}")
    private String USERNAME;
    @Value("${neo4j.password}")
    private String PASSWORD;
    @Value("${neo4j.uri}")
    private String DRIVER_URI;

    //=========================CORS==============CONFIGURATIONS====================================
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");

        System.out.println("");
        System.out.println("CORS CONFIGURATIONS ADDED");
        System.out.println("");
    }

    @Bean
    public SessionFactory sessionFactory() {
        System.out.println("");
        System.out.println("IM IN GET SESSION FACTORY");
        System.out.println("");
        System.out.println(getConfiguration());
        System.out.println("");

        SessionFactory sessionFactory = new SessionFactory(getConfiguration(), "com.contactservice.neo4j.domain");

        System.out.println("");
        System.out.println("SESSION FACTORY");
        System.out.println(sessionFactory.getLoadStrategy());
        System.out.println("");

        return sessionFactory;
    }

    @Bean
    public org.neo4j.ogm.config.Configuration getConfiguration() {

        org.neo4j.ogm.config.Configuration configuration = new org.neo4j.ogm.config.Configuration.Builder().uri(DRIVER_URI).connectionPoolSize(150).
                credentials(USERNAME, PASSWORD).
                build();

        System.out.println("");
        System.out.println("DRIVER CLASS NAME");
        System.out.println(configuration.getDriverClassName());
        System.out.println("");

        return configuration;
    }

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }


    @Bean
    AccessTokenValidator accessTokenInterceptor() {
        return new AccessTokenValidator();
    }

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(accessTokenInterceptor()).addPathPatterns(API_URLS);
//    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();

    }
}
