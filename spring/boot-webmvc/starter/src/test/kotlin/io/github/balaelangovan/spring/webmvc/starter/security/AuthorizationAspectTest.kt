package io.github.balaelangovan.spring.webmvc.starter.security

import io.github.balaelangovan.spring.core.exception.ForbiddenException
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

class AuthorizationAspectTest :
    DescribeSpec({

        val aspect = AuthorizationAspect()

        beforeSpec {
            mockkStatic(RequestContextHolder::class)
        }

        afterSpec {
            unmockkStatic(RequestContextHolder::class)
        }

        describe("AuthorizationAspect") {
            describe("authorize") {
                it("should allow access when no authorized groups are specified") {
                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns emptyArray()

                    shouldNotThrowAny {
                        aspect.authorize(authorization)
                    }
                }

                it("should throw ForbiddenException when no request context is available") {
                    every { RequestContextHolder.getRequestAttributes() } returns null

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")

                    val exception = shouldThrow<ForbiddenException> {
                        aspect.authorize(authorization)
                    }
                    exception.message shouldContain "Unable to verify authorization"
                }

                it("should allow access when user has required group") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns "admin,user"

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ","

                    shouldNotThrowAny {
                        aspect.authorize(authorization)
                    }
                }

                it("should allow access when user has one of multiple required groups") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns "developer"

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin", "developer", "manager")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ","

                    shouldNotThrowAny {
                        aspect.authorize(authorization)
                    }
                }

                it("should throw ForbiddenException when user lacks required group") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns "user,guest"

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ","

                    val exception = shouldThrow<ForbiddenException> {
                        aspect.authorize(authorization)
                    }
                    exception.message shouldContain "does not have required permissions"
                }

                it("should throw ForbiddenException when groups header is empty") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns ""

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ","

                    shouldThrow<ForbiddenException> {
                        aspect.authorize(authorization)
                    }
                }

                it("should throw ForbiddenException when groups header is null") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns null

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ","

                    shouldThrow<ForbiddenException> {
                        aspect.authorize(authorization)
                    }
                }

                it("should handle custom delimiter") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns "admin;user;developer"

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("developer")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ";"

                    shouldNotThrowAny {
                        aspect.authorize(authorization)
                    }
                }

                it("should check multiple headers") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns "user"
                    every { request.getHeader("X-User-Roles") } returns "admin"

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups", "X-User-Roles")
                    every { authorization.delimiter } returns ","

                    shouldNotThrowAny {
                        aspect.authorize(authorization)
                    }
                }

                it("should trim whitespace from groups") {
                    val request = mockk<HttpServletRequest>()
                    val requestAttributes = mockk<ServletRequestAttributes>()

                    every { RequestContextHolder.getRequestAttributes() } returns requestAttributes
                    every { requestAttributes.request } returns request
                    every { request.getHeader("X-User-Groups") } returns "  admin  ,  user  "

                    val authorization = mockk<Authorization>()
                    every { authorization.authorizedGroups } returns arrayOf("admin")
                    every { authorization.headerNames } returns arrayOf("X-User-Groups")
                    every { authorization.delimiter } returns ","

                    shouldNotThrowAny {
                        aspect.authorize(authorization)
                    }
                }
            }
        }
    })
