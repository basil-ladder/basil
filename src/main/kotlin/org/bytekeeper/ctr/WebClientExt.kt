package org.bytekeeper.ctr

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Scope
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.net.URI
import java.util.concurrent.TimeUnit

private const val MAX_REDIRECTS = 5

@Component
@Scope("prototype")
class RedirectingWebClient {
    private val tcpClient = TcpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(1, TimeUnit.MINUTES))
                it.addHandlerLast(WriteTimeoutHandler(1, TimeUnit.MINUTES))
            }
    private val webClient = WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(HttpClient.from(tcpClient)))
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs { config -> config.defaultCodecs().maxInMemorySize(1024 * 1024 * 100) }
                    .build())
            .build()

    operator fun get(uri: URI): Mono<ClientResponse> {
        return redirectingRequest(uri, 0)
    }

    private fun redirectingRequest(uri: URI, redirects: Int): Mono<ClientResponse> {
        if (redirects > MAX_REDIRECTS) throw TooManyRedirectsException()

        return webClient
                .get()
                .uri(uri)
                .exchange()
                .flatMap { response ->
                    if (response.statusCode().is3xxRedirection) {
                        val redirectUrl = response.headers().asHttpHeaders().location!!
                        redirectingRequest(redirectUrl, redirects + 1)
                    } else
                        response.toMono()
                }
    }
}

class TooManyRedirectsException : Exception()