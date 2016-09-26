package gormclient

import grails.transaction.Transactional
import org.springframework.context.annotation.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import static org.springframework.web.bind.annotation.RequestMethod.GET

@RestController
class ThingController {

    @RequestMapping(value="/thing/check", method = GET)
    String check(String name) {
        def t = Thing.findByName(name)
        t ? "${t.counter}!" : "No item $name found"
    }

    @RequestMapping(value = '/thing/add', method = GET)
    ResponseEntity create(String name) {
        Thing t
        Thing.withTransaction {
            t = new Thing(name: name).save()
        }
        if(t) {
            return new ResponseEntity(HttpStatus.CREATED)
        }
        else {
            println t.errors.dump()
            return new ResponseEntity(HttpStatus.BAD_REQUEST)
        }
    }
}

