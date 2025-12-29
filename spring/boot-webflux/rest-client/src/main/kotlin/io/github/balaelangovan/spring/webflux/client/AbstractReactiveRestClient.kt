package io.github.balaelangovan.spring.webflux.client

import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * Abstract base class for reactive REST clients using WebClient.
 *
 * @param webClient the WebClient instance
 * @param baseUrl the base URL for requests
 */
abstract class AbstractReactiveRestClient(
    protected val webClient: WebClient,
    protected val baseUrl: String,
) {
    /**
     * Performs a GET request.
     *
     * @param T the response type
     * @param path the relative path
     * @param headers additional headers
     * @return Mono emitting the response
     */
    protected inline fun <reified T : Any> get(
        path: String,
        headers: Map<String, String> = emptyMap(),
    ): Mono<T> {
        logger.debug(GET_REQUEST_TO, baseUrl, path)

        var requestSpec =
            webClient
                .get()
                .uri("$baseUrl$path")

        headers.forEach { (key, value) ->
            requestSpec = requestSpec.header(key, value)
        }

        return requestSpec
            .retrieve()
            .bodyToMono(T::class.java)
            .doOnSuccess { logger.debug(GET_REQUEST_SUCCESS, baseUrl, path) }
            .doOnError { error -> logger.error(GET_REQUEST_FAILED, baseUrl, path, error) }
    }

    /**
     * Performs a POST request.
     *
     * @param T the request body type
     * @param R the response type
     * @param path the relative path
     * @param body the request body
     * @param headers additional headers
     * @return Mono emitting the response
     */
    protected inline fun <reified T : Any, reified R : Any> post(
        path: String,
        body: T,
        headers: Map<String, String> = emptyMap(),
    ): Mono<R> {
        logger.debug(POST_REQUEST_TO, baseUrl, path)

        var requestSpec =
            webClient
                .post()
                .uri("$baseUrl$path")

        headers.forEach { (key, value) ->
            requestSpec = requestSpec.header(key, value)
        }

        return requestSpec
            .bodyValue(body as Any)
            .retrieve()
            .bodyToMono(R::class.java)
            .doOnSuccess { logger.debug(POST_REQUEST_SUCCESS, baseUrl, path) }
            .doOnError { error -> logger.error(POST_REQUEST_FAILED, baseUrl, path, error) }
    }

    /**
     * Performs a PUT request.
     *
     * @param T the request body type
     * @param R the response type
     * @param path the relative path
     * @param body the request body
     * @param headers additional headers
     * @return Mono emitting the response
     */
    protected inline fun <reified T : Any, reified R : Any> put(
        path: String,
        body: T,
        headers: Map<String, String> = emptyMap(),
    ): Mono<R> {
        logger.debug(PUT_REQUEST_TO, baseUrl, path)

        var requestSpec =
            webClient
                .put()
                .uri("$baseUrl$path")

        headers.forEach { (key, value) ->
            requestSpec = requestSpec.header(key, value)
        }

        return requestSpec
            .bodyValue(body as Any)
            .retrieve()
            .bodyToMono(R::class.java)
            .doOnSuccess { logger.debug(PUT_REQUEST_SUCCESS, baseUrl, path) }
            .doOnError { error -> logger.error(PUT_REQUEST_FAILED, baseUrl, path, error) }
    }

    /**
     * Performs a DELETE request.
     *
     * @param path the relative path
     * @param headers additional headers
     * @return Mono completing when done
     */
    protected fun delete(
        path: String,
        headers: Map<String, String> = emptyMap(),
    ): Mono<Void> {
        logger.debug(DELETE_REQUEST_TO, baseUrl, path)

        var requestSpec =
            webClient
                .delete()
                .uri("$baseUrl$path")

        headers.forEach { (key, value) ->
            requestSpec = requestSpec.header(key, value)
        }

        return requestSpec
            .retrieve()
            .bodyToMono(Void::class.java)
            .doOnSuccess { logger.debug(DELETE_REQUEST_SUCCESS, baseUrl, path) }
            .doOnError { error -> logger.error(DELETE_REQUEST_FAILED, baseUrl, path, error) }
    }

    protected val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        protected const val GET_REQUEST_TO = "GET request to: {}{}"
        protected const val GET_REQUEST_SUCCESS = "GET request successful: {}{}"
        protected const val GET_REQUEST_FAILED = "GET request failed: {}{}"

        protected const val POST_REQUEST_TO = "POST request to: {}{}"
        protected const val POST_REQUEST_SUCCESS = "POST request successful: {}{}"
        protected const val POST_REQUEST_FAILED = "POST request failed: {}{}"

        protected const val PUT_REQUEST_TO = "PUT request to: {}{}"
        protected const val PUT_REQUEST_SUCCESS = "PUT request successful: {}{}"
        protected const val PUT_REQUEST_FAILED = "PUT request failed: {}{}"

        protected const val DELETE_REQUEST_TO = "DELETE request to: {}{}"
        protected const val DELETE_REQUEST_SUCCESS = "DELETE request successful: {}{}"
        protected const val DELETE_REQUEST_FAILED = "DELETE request failed: {}{}"
    }
}
