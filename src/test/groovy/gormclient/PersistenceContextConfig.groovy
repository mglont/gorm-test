package gormclient

import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.orm.hibernate4.LocalSessionFactoryBean
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.interceptor.TransactionInterceptor

import javax.sql.DataSource

/**
 * Persistence-related bean definitions.
 *
 * This class is responsible for declaring the beans should be managed by the container.
 * Methods annotated with @Bean correspond to <bean> declarations in Spring XML.
 *
 * Configuration options are loaded from application-test.properties and bound to Java variables
 * using @Value.
 *
 * @author Mihai Glonț
 */
@Configuration
@ComponentScan("gormclient")
@PropertySource("classpath:/config/application-test.properties")
@EnableTransactionManagement(proxyTargetClass = true)
class PersistenceContextConfig {
    @Value('${spring.datasource.url}')
    private String url
    @Value('${spring.datasource.username}')
    private String username
    @Value('${spring.datasource.password}')
    private String password

    @Bean
    /*
     * We use a file-based H2 data source which does not use a connection pool.
     * A new JDBC connection is created for each request.
     */
    DataSource dataSource() {
        new DriverManagerDataSource(url, username, password)
    }

    @Bean
    /*
     * The bean responsible for creating the Hibernate Session Factory.
     */
    LocalSessionFactoryBean sessionFactory() {
        new LocalSessionFactoryBean(dataSource: dataSource())
    }

    @Bean
    /*
     * The bean that kickstarts GORM initialisation.
     *
     * Wires up a HibernateDatastore that talks to a dataSource bean and has its own
     * PlatformTransactionManager so it can participate in transactional contexts.
     *
     * See
     *      http://gorm.grails.org/6.0.x/hibernate/manual/index.html#_configuration_example
     *      http://gorm.grails.org/6.0.x/hibernate/manual/index.html#_hibernate_customization
     *      https://github.com/grails/gorm-hibernate4/blob/80fc4cec/grails-datastore-gorm-hibernate4/src/main/groovy/grails/orm/bootstrap/HibernateDatastoreSpringInitializer.groovy#L142
     */
    HibernateDatastore hibernateDatastore() {
        new HibernateDatastore(Pet, Thing)
    }

    @Bean
    /*
     * Bean declaration telling Spring how to detect methods that should exhibit transactional behaviour.
     * Here we are using Spring's @Transactional annotation.
     * See also
     *      http://docs.spring.io/spring-framework/docs/4.2.6.RELEASE/javadoc-api/org/springframework/transaction/interceptor/TransactionAttributeSource.html
     */
    AnnotationTransactionAttributeSource txAttributes() {
        new AnnotationTransactionAttributeSource()
    }

    @Bean
    /*
     * Bean that intercepts methods according to the TransactionAttributeSource rules and
     * invokes the PlatformTransactionManager for implementing transaction synchronisation.
     * See
     *      http://docs.spring.io/spring/docs/4.2.6.RELEASE/spring-framework-reference/html/transaction.html#transaction-declarative
     *      https://github.com/spring-projects/spring-framework/blob/53441f/spring-tx/src/main/java/org/springframework/transaction/interceptor/TransactionInterceptor.java
     *      https://github.com/spring-projects/spring-framework/blob/53441f/spring-tx/src/main/java/org/springframework/transaction/interceptor/TransactionAspectSupport.java
     */
    TransactionInterceptor transactionInterceptor() {
        new TransactionInterceptor(hibernateDatastore().transactionManager, txAttributes())
    }
}
