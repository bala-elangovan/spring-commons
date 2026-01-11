package io.github.balaelangovan.spring.webflux.client.oauth

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class OAuthPropertiesTest :
    DescribeSpec({

        describe("OAuthProperties") {
            it("should have default values") {
                val properties = OAuthProperties()

                properties.enabled shouldBe false
                properties.tokenUrl shouldBe null
                properties.clientId shouldBe null
                properties.clientSecret shouldBe null
                properties.grantType shouldBe "client_credentials"
                properties.scope shouldBe null
            }

            it("should allow setting all properties") {
                val properties = OAuthProperties(
                    enabled = true,
                    tokenUrl = "https://auth.example.com/oauth/token",
                    clientId = "my-client-id",
                    clientSecret = "my-client-secret",
                    grantType = "password",
                    scope = "read write",
                )

                properties.enabled shouldBe true
                properties.tokenUrl shouldBe "https://auth.example.com/oauth/token"
                properties.clientId shouldBe "my-client-id"
                properties.clientSecret shouldBe "my-client-secret"
                properties.grantType shouldBe "password"
                properties.scope shouldBe "read write"
            }

            it("should be mutable") {
                val properties = OAuthProperties()

                properties.enabled = true
                properties.tokenUrl = "https://auth.example.com/token"
                properties.clientId = "updated-client"
                properties.clientSecret = "updated-secret"
                properties.grantType = "authorization_code"
                properties.scope = "openid profile"

                properties.enabled shouldBe true
                properties.tokenUrl shouldBe "https://auth.example.com/token"
                properties.clientId shouldBe "updated-client"
                properties.clientSecret shouldBe "updated-secret"
                properties.grantType shouldBe "authorization_code"
                properties.scope shouldBe "openid profile"
            }

            it("should support data class operations") {
                val properties1 = OAuthProperties(
                    enabled = true,
                    tokenUrl = "https://auth.example.com/token",
                    clientId = "client1",
                )

                val properties2 = OAuthProperties(
                    enabled = true,
                    tokenUrl = "https://auth.example.com/token",
                    clientId = "client1",
                )

                val properties3 = properties1.copy(clientId = "client2")

                properties1 shouldBe properties2
                properties3.clientId shouldBe "client2"
                properties3.tokenUrl shouldBe "https://auth.example.com/token"
            }
        }
    })
