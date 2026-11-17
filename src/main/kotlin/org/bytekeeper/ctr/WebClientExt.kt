package org.bytekeeper.ctr

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

@Component
@Scope("prototype")
class RedirectingWebClient {
    private val httpClient = HttpClient.create()
            .followRedirect(true)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(1, TimeUnit.MINUTES))
                it.addHandlerLast(WriteTimeoutHandler(1, TimeUnit.MINUTES))
            }
    val webClient: WebClient = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs { config -> config.defaultCodecs().maxInMemorySize(1024 * 1024 * 100) }
                    .build())
            .build()
}
