package io.github.balaelangovan.spring.core.constants

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class MdcKeysTest :
    DescribeSpec({

        describe("MdcKeys") {
            it("should have correct USER_ID key") {
                MdcKeys.USER_ID shouldBe "user_id"
            }

            it("should have correct USER_EMAIL key") {
                MdcKeys.USER_EMAIL shouldBe "user_email"
            }

            it("should have correct USER_GROUPS key") {
                MdcKeys.USER_GROUPS shouldBe "user_groups"
            }

            it("should have correct TRANSACTION_ID key") {
                MdcKeys.TRANSACTION_ID shouldBe "transaction_id"
            }

            it("should have correct CLIENT_TRANSACTION_ID key") {
                MdcKeys.CLIENT_TRANSACTION_ID shouldBe "client_transaction_id"
            }

            it("should have correct CLIENT_ID key") {
                MdcKeys.CLIENT_ID shouldBe "client_id"
            }

            it("should have correct REQUEST_METHOD key") {
                MdcKeys.REQUEST_METHOD shouldBe "request_method"
            }

            it("should have correct REQUEST_URL key") {
                MdcKeys.REQUEST_URL shouldBe "request_url"
            }

            it("should have correct REQUEST_DURATION_MS key") {
                MdcKeys.REQUEST_DURATION_MS shouldBe "request_duration_ms"
            }

            it("should have correct CLIENT_IP key") {
                MdcKeys.CLIENT_IP shouldBe "client_ip"
            }

            it("should have correct RESPONSE_STATUS key") {
                MdcKeys.RESPONSE_STATUS shouldBe "response_status"
            }

            it("should have correct SERVICE_NAME key") {
                MdcKeys.SERVICE_NAME shouldBe "service_name"
            }

            it("should have correct SERVICE_VERSION key") {
                MdcKeys.SERVICE_VERSION shouldBe "service_version"
            }
        }
    })
