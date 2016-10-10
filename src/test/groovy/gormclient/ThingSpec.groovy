/*
 * Example domain class unit test (specification) using Spock.
 *
 * The test relies on the Spock-Spring integration which turns
 * each specification into a Spring bean.
 *
 * These tests can work equally well against domain classes annotated with
 * @grails.persistence.Entity as well as inline classes annotated with
 * @grails.gorm.annotation.Entity.
 *
 * See also
 *      http://gorm.grails.org/6.0.x/hibernate/manual/index.html#testing
 *      http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html
 *      https://github.com/grails/gorm-hibernate4/blob/80fc4cec/grails-datastore-gorm-hibernate4/src/test/groovy/grails/orm/bootstrap/HibernateDatastoreSpringInitializerSpec.groovy
 *
 * @author Mihai Glonț
 */
package gormclient

import grails.gorm.annotation.Entity
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.connections.HibernateConnectionSourceFactory
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.DirtiesContextTestExecutionListener
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import spock.lang.Specification

// tell Spring to load our bean definitions
@ContextConfiguration(loader = SpringApplicationContextLoader.class,
        classes = PersistenceContextConfig.class)

/*
 * Default Spring unit test mixins:
 *      DependencyInjectionTestExecutionListener -- builds a mock application context
 *      TransactionalTestExecutionListener -- sets up a transaction for every @Transactional method,
 *          see also @BeforeTransaction and @AfterTransaction, as well as @Commit and @Rollback
 *      DirtiesContextTestExecutionListener --
 * Can be customised with @TestExecutionListeners(listeners = [...])
  */
class ThingSpec extends Specification {

    @Transactional
    void "gorm works"() {
        expect: "we are in a transactional context"
        /*
         * The Hibernate session [for this thread] exists in the registry of ThreadLocal objects
         * that TransactionSynchronizationManager maintains in order to enforce transactional
         * behaviour.
         */
        TransactionSynchronizationManager.isSynchronizationActive()

        when: "a new domain class instance is persisted"
        def p = new Pet(name: 'doggy')
        // use withNewSession and flush:true to force reading from db, not Hibernate cache
        Pet.withNewSession { p.save(flush: true) }

        then: "it is present in the database"
        Pet.withNewSession { Pet.count() } == 1

        when: "the object is retrieved from the database and modified"
        Pet doggy = Pet.withNewSession { Pet.findByName('doggy') }
        doggy.age--

        then: "dynamic finders and validation work"
        doggy.age == -1
        doggy.validate() == false

        when: "the object is made valid"
        doggy.age = 2

        then: "it can be successfully saved in the database again."
        Pet.withNewSession { doggy.save(flush: true) }
        !doggy.hasErrors()

        when: "the table is cleared"
        Pet.withNewSession { Pet.list()*.delete(flush: true) }

        then: "there are no entries left"
        Pet.withNewSession { Pet.count() } == 0
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
