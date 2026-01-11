package io.github.balaelangovan.spring.webflux.starter.security

import io.github.balaelangovan.spring.core.exception.ForbiddenException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.aspectj.lang.ProceedingJoinPoint
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class AuthorizationAspectTest :
    DescribeSpec({

        val aspect = AuthorizationAspect()

        describe("AuthorizationAspect") {
            describe("authorize") {
                it("should allow access when no authorized groups are specified") {
                    val joinPoint = mockk<ProceedingJoinPoint>()
                    val authorization = mockk<Authorization>()

                    every { authorization.authorizedGroups } returns emptyArray()
                    every { joinPoint.proceed() } returns "result"

                    val result = aspect.authorize(joinPoint, authorization)

                    result shouldBe "result"
                }

                describe("with ServerWebExchange in arguments") {
                    it("should allow access when user has required group") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "admin,developer")

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups")
                        every { authorization.delimiter } returns ","
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { joinPoint.proceed() } returns "result"
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        shouldNotThrowAny {
                            aspect.authorize(joinPoint, authorization)
                        }
                    }

                    it("should allow access when user has one of multiple required groups") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "developer")

                        every { authorization.authorizedGroups } returns arrayOf("admin", "developer", "manager")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups")
                        every { authorization.delimiter } returns ","
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { joinPoint.proceed() } returns "result"
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        shouldNotThrowAny {
                            aspect.authorize(joinPoint, authorization)
                        }
                    }

                    it("should throw ForbiddenException when user lacks required group") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "user,guest")

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups")
                        every { authorization.delimiter } returns ","
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        val exception = shouldThrow<ForbiddenException> {
                            aspect.authorize(joinPoint, authorization)
                        }
                        exception.message shouldContain "does not have required permissions"
                    }

                    it("should throw ForbiddenException when groups header is empty") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "")

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups")
                        every { authorization.delimiter } returns ","
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        shouldThrow<ForbiddenException> {
                            aspect.authorize(joinPoint, authorization)
                        }
                    }

                    it("should handle custom delimiter") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "admin;user;developer")

                        every { authorization.authorizedGroups } returns arrayOf("developer")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups")
                        every { authorization.delimiter } returns ";"
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { joinPoint.proceed() } returns "result"
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        shouldNotThrowAny {
                            aspect.authorize(joinPoint, authorization)
                        }
                    }

                    it("should check multiple headers") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "user")
                        headers.add("X-User-Roles", "admin")

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups", "X-User-Roles")
                        every { authorization.delimiter } returns ","
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { joinPoint.proceed() } returns "result"
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        shouldNotThrowAny {
                            aspect.authorize(joinPoint, authorization)
                        }
                    }

                    it("should trim whitespace from groups") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()
                        val exchange = mockk<ServerWebExchange>()
                        val request = mockk<ServerHttpRequest>()
                        val headers = HttpHeaders()

                        headers.add("X-User-Groups", "  admin  ,  user  ")

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { authorization.headerNames } returns arrayOf("X-User-Groups")
                        every { authorization.delimiter } returns ","
                        every { joinPoint.args } returns arrayOf(exchange)
                        every { joinPoint.proceed() } returns "result"
                        every { exchange.request } returns request
                        every { request.headers } returns headers

                        shouldNotThrowAny {
                            aspect.authorize(joinPoint, authorization)
                        }
                    }
                }

                describe("with Mono return type") {
                    it("should return Mono when method returns Mono") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { joinPoint.args } returns emptyArray()
                        every { joinPoint.proceed() } returns Mono.just("result")

                        val result = aspect.authorize(joinPoint, authorization)

                        result.shouldBeInstanceOf<Mono<*>>()
                    }
                }

                describe("with Flux return type") {
                    it("should return Flux when method returns Flux") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { joinPoint.args } returns emptyArray()
                        every { joinPoint.proceed() } returns Flux.just("result1", "result2")

                        val result = aspect.authorize(joinPoint, authorization)

                        result.shouldBeInstanceOf<Flux<*>>()
                    }
                }

                describe("with non-reactive return type") {
                    it("should return result for non-reactive methods without exchange") {
                        val joinPoint = mockk<ProceedingJoinPoint>()
                        val authorization = mockk<Authorization>()

                        every { authorization.authorizedGroups } returns arrayOf("admin")
                        every { joinPoint.args } returns emptyArray()
                        every { joinPoint.proceed() } returns "plain result"

                        val result = aspect.authorize(joinPoint, authorization)

                        result shouldBe "plain result"
                    }
                }
            }
        }
    })
