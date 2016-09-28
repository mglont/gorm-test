/*
 * This Spock specification was auto generated by running the Gradle 'init' task
 * by 'mglont' at '22/09/16 23:32' with Gradle 3.1
 *
 * @author mglont, @date 22/09/16 23:32
 */

package gormclient

import grails.gorm.annotation.Entity
import org.grails.orm.hibernate.HibernateDatastore
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.PropertySource
import org.springframework.core.env.PropertySources
import org.springframework.core.env.PropertySourcesPropertyResolver
import org.springframework.orm.jpa.EntityManagerFactoryUtils
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.context.WebApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@TestExecutionListeners
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = PersistenceContextConfig.class)
@IntegrationTest
class ThingSpec extends Specification {

    @Shared @AutoCleanup HibernateDatastore datastore

    @Shared PlatformTransactionManager txManager
    @Shared SessionFactory sessionFactory

    @Shared ApplicationContext ctx

    void setupSpec() {
        ctx = new AnnotationConfigApplicationContext();
        ctx.register(PersistenceContextConfig.class)
        ctx.refresh()
        datastore = new HibernateDatastore(Pet, Thing)
        txManager = ctx.getBean("transactionManager", PlatformTransactionManager)
        println "setupSpec done"
    }


    @Transactional
    def setup() {
        sessionFactory.openSession()
        boolean haveTx = TransactionSynchronizationManager.isSynchronizationActive()
        println "haveTx = $haveTx"
        def p = new Pet(name: 'doggy')
        p.save()
        println "setup  done"
    }

    @Rollback
    void "gorm works"() {
        expect:
        Pet.count() == 1

        when:
        Pet doggy = Pet.findByName('doggy')
        then:
        doggy.counter == 0

        when:
        doggy.counter--
        then:
        doggy.validate() == false

        when:
        doggy.counter = 2
        then:
        Pet.withNewSession { doggy.save() }
        Pet.withNewSession { Pet.get(1).counter } == 2
    }

    def tearDown() {
        println "tear down invoked"
        sessionFactory.currentSession.close()
        boolean haveTx = TransactionSynchronizationManager.isSynchronizationActive()
        println "haveTx = $haveTx"
    }
}

@Entity
class Pet {
    String name
    int age = 0

    String toString() {
        "$name the pet"
    }

    static constraints = {
        name unique: true
        age min: 0
    }
}
