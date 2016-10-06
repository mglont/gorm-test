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
 * Created by mglont on 27/09/16.
 */
@Configuration
@EnableTransactionManagement
@ComponentScan("gormclient")
@PropertySource("classpath:/config/application-test.properties")
class PersistenceContextConfig {
    @Value('${spring.datasource.url}')
    private String url
    @Value('${spring.datasource.username}')
    private String username
    @Value('${spring.datasource.password}')
    private String password

    @Bean
    DataSource dataSource() {
        new DriverManagerDataSource(url, username, password)
    }

    @Bean
    LocalSessionFactoryBean sessionFactory() {
        new LocalSessionFactoryBean(dataSource: dataSource())
    }

    @Bean
    HibernateDatastore hibernateDatastore() {
        new HibernateDatastore(Pet, Thing)
    }

    @Bean
    TransactionInterceptor transactionInterceptor() {
        new TransactionInterceptor(hibernateDatastore().transactionManager, txAttributes())
    }

    @Bean
    AnnotationTransactionAttributeSource txAttributes() {
        new AnnotationTransactionAttributeSource()
    }
}
