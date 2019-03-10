package org.bytekeeper.ctr

import io.micrometer.core.instrument.logging.LoggingMeterRegistry
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableConfigurationProperties
class Basil {
    @Bean
    @ConditionalOnProperty(prefix = "basil", name = ["metrics.debugLog"])
    fun loggingMetrics() = LoggingMeterRegistry()
}

fun main(args: Array<String>) {
    SpringApplication.run(Basil::class.java, *args)
}
