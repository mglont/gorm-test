package gormclient

import grails.persistence.Entity

@Entity
class Thing {
    String name
    int counter = 0

    static constraints = {
        counter min: 0
        name unique: true
    }
}
