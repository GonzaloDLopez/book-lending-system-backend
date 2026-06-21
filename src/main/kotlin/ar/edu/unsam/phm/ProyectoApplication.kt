package ar.edu.unsam.phm

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["ar.edu.unsam.phm.repository"])
@EnableMongoRepositories(basePackages = ["ar.edu.unsam.phm.repository.mongo"])
@EnableScheduling
class ProyectoApplication

fun main(args: Array<String>) {
    runApplication<ProyectoApplication>(*args)
}
