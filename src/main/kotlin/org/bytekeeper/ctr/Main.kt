package org.bytekeeper.ctr

import org.springframework.boot.SpringApplication

fun main(args: Array<String>) {
    SpringApplication.run(Basil::class.java, *args).getBean(GameRunner::class.java)
            .run()
}
