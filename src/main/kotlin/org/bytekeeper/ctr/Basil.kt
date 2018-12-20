package org.bytekeeper.ctr

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties

@SpringBootApplication
@EnableConfigurationProperties
class Basil {

}

fun main(args: Array<String>) {
    SpringApplication.run(Basil::class.java, *args)
}
