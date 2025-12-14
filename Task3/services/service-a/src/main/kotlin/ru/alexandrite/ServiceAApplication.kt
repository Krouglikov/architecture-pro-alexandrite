package ru.alexandrite

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.context.Scope

fun main() {
    println("🚀 Starting Service A...")

    OpenTelemetryConfig.init("service-a")

    val tracer: Tracer = GlobalOpenTelemetry.getTracer("service-a-tracer")

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }

    embeddedServer(Netty, port = 8080) {
        println("🔌 Service A: Embedded server initialized on port 8080")

        routing {
            get("/api/quote") {
                println("📨 Received request to /api/quote")
                application.log.info("Handling quote request")

                val parentContext = Context.current()
                val span = tracer.spanBuilder("calculate-quote").startSpan()

                var result: Map<String, String>? = null
                try {
                    val withSpan: Context = parentContext.with(span)
                    val scope: Scope = withSpan.makeCurrent()

                    try {
                        span.addEvent("Starting quote calculation")
                        println("🔧 Calculating quote...")

                        // Ручная передача контекста через заголовки
                        result = client.get("http://service-b:8080/api/order") {
                            header("Accept", "application/json")
                            // Вручную добавляем заголовок для distributed tracing
                            val spanContext = Span.current().spanContext
                            header(
                                "traceparent",
                                "00-${spanContext.traceId}-${spanContext.spanId}-01"
                            )
                        }.body<Map<String, String>>()

                        span.addEvent(
                            "Received order from service-b",
                            io.opentelemetry.api.common.Attributes.of(
                                io.opentelemetry.api.common.AttributeKey.stringKey("order.id"),
                                result["orderId"]
                            )
                        )

                        call.respond(
                            mapOf(
                                "quoteId" to "Q-456",
                                "relatedOrder" to result["orderId"],
                                "total" to 99.99
                            )
                        )
                    } catch (e: Exception) {
                        span.recordException(e)
                        throw e
                    } finally {
                        scope.close()
                        span.end()
                    }
                } catch (e: Exception) {
                    call.respond(mapOf("error" to e.message))
                }
            }
        }
    }.start(wait = true)
}