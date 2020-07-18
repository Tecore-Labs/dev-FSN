package com.cpms;

import com.cpms.constants.ServiceAPIs;
import com.cpms.interceptor.AccessTokenValidator;
import com.datastax.driver.core.Cluster;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CassandraSessionFactoryBean;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.cpms.constants.Constants.InterceptorApiUrls.API_URLS;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"com.cpms.*"})
public class Config implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
        System.out.println("");
        System.out.println("CORS CONFIGURATIONS ADDED");
        System.out.println("");
    }

    /*
     * Generate url for file service
     * Check for service url in console.
     *
     * */
    @Value("${services.cims.address}" + ":" + "${services.cims.port}" + ServiceAPIs.cims.fileService)
    private String accessMediaFile;

    /*
    * Generate url for company service
    * Check for service url in console.
    *
    * */
    @Value("${services.ccms.address}" + ":" + "${services.ccms.port}" + ServiceAPIs.Companyinfo.getCompanyDetails)
    private String accessCompanyInformation;

    /*
     * Generate url for account service
     * Check for service url in console.
     *
     * */
    @Value("${services.cams.address}" + ":" + "${services.cams.port}" + ServiceAPIs.Accontinfo.getAccountDetails)
    private String accessAccountInformation;

    /*
     * Cassandra Properties
     *
     * */
    @Value("${cassandra.keyspace}")
    private String KEYSPACE;
    @Value("${cassandra.userName}")
    private String USER_NAME;
    @Value("${cassandra.password}")
    private String PASSWORD;
    @Value("${cassandra.contactPoints}")
    private String CONTACT_POINTS;
    @Value("${cassandra.port}")
    private int PORT;
    public static final Logger logger = LoggerFactory.getLogger(Config.class);

    public Config() {
        System.out.println("");
        System.out.println("----------------------");
        System.out.println("");
        System.out.println("Config Constructor");
        System.out.println("");
        System.out.println("accessMediaFile : " +accessMediaFile);
        System.out.println("");
        System.out.println("accessCompanyInformation: "+accessCompanyInformation);
        System.out.println("");
        System.out.println("accessAccountInformation: "+accessAccountInformation);
        System.out.println("----------------------");
        System.out.println("");
    }

    /*
     * Cassandra Configurations
     *
     * */
    @Bean
    protected Cluster cluster() {

        System.out.println("");
        System.out.println("----------------------");
        System.out.println("");
        System.out.println("Check CIMS url");
        System.out.println("");
        System.out.println("accessMediaFile : " + accessMediaFile);
        System.out.println("");
        System.out.println("----------------------");
        System.out.println("");
        System.out.println("Check company url");
        System.out.println("");
        System.out.println("accessCompanyInformation: "+accessCompanyInformation);
        System.out.println("");
        System.out.println("----------------------");
        System.out.println("");
        System.out.println("Check account url");
        System.out.println("");
        System.out.println("accessCompanyInformation: "+accessCompanyInformation);
        System.out.println("");
        System.out.println("");
        System.out.println("----------------------");
        System.out.println("");


        Cluster cluster = Cluster.builder()
                .withoutJMXReporting()
                .addContactPoint(CONTACT_POINTS).withPort(PORT).withCredentials(USER_NAME, PASSWORD).build();
        return cluster;
    }

    @Bean
    protected CassandraMappingContext mappingContext() {
        return new CassandraMappingContext();
    }


    @Bean
    protected CassandraConverter converter() {

        return new MappingCassandraConverter(mappingContext());
    }

    @Bean
    protected CassandraSessionFactoryBean cassandraSession() {

        CassandraSessionFactoryBean session = new CassandraSessionFactoryBean();

        try {
            session.setCluster(cluster());
            session.setKeyspaceName(KEYSPACE);
            session.setConverter(converter());
            session.setSchemaAction(SchemaAction.NONE);

            System.out.println("");
            System.out.println("---------------------------------");
            System.out.println("");
            System.out.println("CASSANDRA CONNECTION ESTABLISHING");
            System.out.println("");
            System.out.println("---------------------------------");
            System.out.println("");


            logger.info("Cassandra connection is established");

        } catch (Exception execption) {

            System.out.println("");
            System.out.println("---------------------------------");
            System.out.println("");
            System.out.println("EXCEPTION OCCURED IN CASSANDRA CONNECTION ESTABLISHMENT");
            System.out.println("");
            System.out.println(execption.getMessage());
            System.out.println("");
            System.out.println("---------------------------------");
            System.out.println("");

            logger.error("Error in establishing cassandra connection. Message: {}, Cause: {}", execption.getMessage(),
                    execption.getCause());
        }

        return session;

    }

    @Bean
    protected CassandraOperations cassandraTemplate() {
        return new CassandraTemplate(cassandraSession().getObject());

    }


//    @Bean
//    public ConfigurableServletWebServerFactory webServerFactory() {
//        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
//        factory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
//            @Override
//            public void customize(Connector connector) {
//                connector.setProperty("relaxedQueryChars", "|{}[]");
//            }
//        });
//        return factory;
//    }

    @Bean
    public RestTemplate restTemplate() {

        return new RestTemplate();
    }

    @Bean
    AccessTokenValidator accessTokenValidator() {
        return new AccessTokenValidator();
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessTokenValidator()).addPathPatterns(API_URLS);
    }

    @Bean
    public TomcatServletWebServerFactory containerFactory() {
        return new TomcatServletWebServerFactory() {
            protected void customizeConnector(Connector connector) {
                int maxSize = 50000000;
                super.customizeConnector(connector);
//                connector.setMaxPostSize(maxSize);
//                connector.setMaxSavePostSize(maxSize);
                connector.setProperty("relaxedQueryChars", "|{}[]");
                if (connector.getProtocolHandler() instanceof AbstractHttp11Protocol) {
                      // -1 means unlimited, accept bytes
                    ((AbstractHttp11Protocol <?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
                    logger.info("Set MaxSwallowSize "+ maxSize);
                }
            }
        };

    }
}
