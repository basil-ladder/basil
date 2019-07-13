package org.bytekeeper.ctr

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class TableClusterer(private val datasource: DataSource) {

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24, initialDelay = 1000 * 60 * 60 * 24)
    fun cluster() {
        datasource.connection.use {
            it.createStatement().executeUpdate("cluster")
        }
    }

}