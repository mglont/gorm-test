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
import org.springframework.orm.hibernate4.LocalSessionFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.support.TransactionTemplate

import javax.activation.DataSource
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

    @Bean(destroyMethod = "close")
    DataSource rawDataSource() {
        new DriverManagerDataSource(url, username, password)
    }

    @Bean
    LazyConnectionDataSourceProxy lazyConnectionDataSourceProxy() {
        new LazyConnectionDataSourceProxy(rawDataSource())
    }

    @Bean
    TransactionAwareDataSourceProxy dataSource() {
        new TransactionAwareDataSourceProxy(lazyConnectionDataSourceProxy())
    }

    @Bean
    SessionFactory sessionFactory() {
        new LocalSessionFactoryBean(lazyConnectionDataSourceProxy())
    }

    @Bean
    TransactionTemplate transactionTemplate() {
        new TransactionTemplate(transactionManager())
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        new GrailsHibernateTransactionManager(lazyConnectionDataSourceProxy())
    }

    public static void main(String[] args) {
        System.out.println('testApp config done')
    }
}
