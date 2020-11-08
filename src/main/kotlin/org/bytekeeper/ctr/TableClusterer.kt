package org.bytekeeper.ctr

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class TableClusterer(private val datasource: DataSource) {

    @Scheduled(cron = "0 0 2 * * *")
    fun cluster() {
        datasource.connection.use {
            it.createStatement().executeUpdate("cluster")
        }
    }

}