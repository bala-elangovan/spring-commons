package io.github.balaelangovan.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import tools.jackson.databind.ObjectMapper

/**
 * Abstract base class for integration tests.
 * Provides MockMvc and ObjectMapper with JSON helper methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class BaseIntegrationTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    /**
     * Converts an object to JSON string.
     *
     * @param obj the object to convert
     * @return JSON string representation
     */
    protected fun toJson(obj: Any): String = objectMapper.writeValueAsString(obj)

    /**
     * Converts JSON string to object.
     *
     * @param T the target type
     * @param json the JSON string
     * @param clazz the target class
     * @return the deserialized object
     */
    protected fun <T> fromJson(
        json: String,
        clazz: Class<T>,
    ): T = objectMapper.readValue(json, clazz)
}
