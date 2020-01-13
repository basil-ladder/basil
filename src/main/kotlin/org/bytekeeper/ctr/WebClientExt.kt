package org.bytekeeper.ctr

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.net.URI

@Component
@Scope("prototype")
class RedirectingWebClient {
    private val webClient = WebClient.create()
    private val MAX_REDIRECTS = 5

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