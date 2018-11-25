package org.bytekeeper.ctr

import org.springframework.boot.SpringApplication


fun main(args: Array<String>) {
    val context = SpringApplication.run(Basil::class.java, *args)
    val sscaitClient = context.getBean(SscaitClient::class.java)
    val scbwConfig = context.getBean(ScbwConfig::class.java)
    println(scbwConfig)
//    val listOfBots = sscaitClient.retrieveListOfBots()
//
//    val flatMap = listOfBots.take(3)
//            .flatMap { bot -> sscaitClient.downloadBinary(bot).map { bot to it } }
//            .collectList()
//            .block()
//
//    println(flatMap)

}