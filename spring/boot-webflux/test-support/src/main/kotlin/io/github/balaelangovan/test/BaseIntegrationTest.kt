package io.github.balaelangovan.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import tools.jackson.databind.ObjectMapper

/**
 * Abstract base class for integration tests.
 * Provides a common setup with WebTestClient and ObjectMapper.
 */
@SpringBootTest
@AutoConfigureWebTestClient
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    /**
     * Converts an object to JSON string.
     *
     * @param obj The object to convert.
     * @return The JSON string.
     * @throws Exception if conversion fails
     */
    protected fun toJson(obj: Any): String = objectMapper.writeValueAsString(obj)

    /**
     * Converts JSON string to object.
     *
     * @param json The JSON string.
     * @param clazz The target class.
     * @return The converted object.
     * @throws Exception if conversion fails
     */
    protected fun <T> fromJson(
        json: String,
        clazz: Class<T>,
    ): T = objectMapper.readValue(json, clazz)
}
