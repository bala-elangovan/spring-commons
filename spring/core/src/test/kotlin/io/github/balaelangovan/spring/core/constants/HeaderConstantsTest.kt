package io.github.balaelangovan.spring.core.constants

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class HeaderConstantsTest :
    DescribeSpec({

        describe("HeaderConstants") {
            it("should have correct X_TRANSACTION_ID header") {
                HeaderConstants.X_TRANSACTION_ID shouldBe "X-Transaction-Id"
            }

            it("should have correct X_CLIENT_TRANSACTION_ID header") {
                HeaderConstants.X_CLIENT_TRANSACTION_ID shouldBe "X-Client-Transaction-Id"
            }

            it("should have correct X_USER_ID header") {
                HeaderConstants.X_USER_ID shouldBe "X-User-Id"
            }

            it("should have correct X_USER_EMAIL header") {
                HeaderConstants.X_USER_EMAIL shouldBe "X-User-Email"
            }

            it("should have correct X_USER_GROUPS header") {
                HeaderConstants.X_USER_GROUPS shouldBe "X-User-Groups"
            }

            it("should have correct X_CLIENT_ID header") {
                HeaderConstants.X_CLIENT_ID shouldBe "X-Client-Id"
            }

            it("should have correct X_FORWARDED_FOR header") {
                HeaderConstants.X_FORWARDED_FOR shouldBe "X-Forwarded-For"
            }

            it("should have correct AUTHORIZATION header") {
                HeaderConstants.AUTHORIZATION shouldBe "Authorization"
            }
        }
    })
