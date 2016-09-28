package gormclient

import org.grails.orm.hibernate.GrailsHibernateTransactionManager
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.orm.jpa.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import org.springframework.orm.hibernate4.HibernateTransactionManager
import org.springframework.orm.hibernate4.LocalSessionFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate

import javax.sql.DataSource

/**
 * Created by mglont on 27/09/16.
 */
@Configuration
@EnableTransactionManagement
@EntityScan("gormclient")
@PropertySource("classpath:/config/application-test.properties")
class PersistenceContextConfig {
    @Value('${spring.datasource.url}')
    private String url
    @Value('${spring.datasource.username}')
    private String username
    @Value('${spring.datasource.username}')
    private String password

    @Bean()
    DriverManagerDataSource dataSource() {
        new DriverManagerDataSource(url, username, password)
    }

    @Bean
    TransactionAwareDataSourceProxy txDataSource() {
        new TransactionAwareDataSourceProxy(dataSource())
    }

    @Bean
    LocalSessionFactoryBean sessionFactory() {
        def factory = new LocalSessionFactoryBean(dataSource: dataSource())
        factory.hibernateProperties = ['hibernate.dialect': 'org.hibernate.dialect.H2Dialect'] as Properties
        factory
    }

    @Bean
    TransactionTemplate transactionTemplate() {
        new TransactionTemplate(transactionManager())
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        //new GrailsHibernateTransactionManager(sessionFactory: sessionFactory().getObject())
        new HibernateTransactionManager(sessionFactory: sessionFactory().object)
    }

    public static void main(String[] args) {
        System.out.println('testApp config done')
    }
}
