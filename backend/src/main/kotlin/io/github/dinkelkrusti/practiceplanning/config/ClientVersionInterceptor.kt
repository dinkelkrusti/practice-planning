package io.github.dinkelkrusti.practiceplanning.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Part of the API evolution machinery (docs/decisions/0004-api-evolution.md):
 * every client sends a `Client-Version` header. It is added to the logging
 * context, and every call to an operation marked `deprecated: true` in the
 * spec is logged with that version, so we know when an element is safe to remove.
 */
class ClientVersionInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientVersion = request.getHeader(HEADER) ?: UNKNOWN
        MDC.put(MDC_KEY, clientVersion)
        if (handler is HandlerMethod && handler.isDeprecated()) {
            log.warn(
                "Deprecated operation called: {} {} by client version {}",
                request.method,
                request.requestURI,
                clientVersion,
            )
        }
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        MDC.remove(MDC_KEY)
    }

    // The generator marks deprecated operations with Kotlin's @Deprecated on the API interface.
    private fun HandlerMethod.isDeprecated(): Boolean =
        method.isAnnotationPresent(Deprecated::class.java) ||
            beanType.interfaces.any { api ->
                runCatching { api.getMethod(method.name, *method.parameterTypes) }
                    .getOrNull()
                    ?.isAnnotationPresent(Deprecated::class.java) == true
            }

    companion object {
        const val HEADER = "Client-Version"
        const val MDC_KEY = "clientVersion"
        private const val UNKNOWN = "unknown"
        private val log = LoggerFactory.getLogger(ClientVersionInterceptor::class.java)
    }
}

@Configuration
class ClientVersionConfig : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(ClientVersionInterceptor())
    }
}
