package ru.alexandrite

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.GlobalOpenTelemetry

fun main() {

    println("🚀 Starting Service B...")

    OpenTelemetryConfig.init("service-b")

    embeddedServer(Netty, port = 8080) {

        install(ContentNegotiation) {
            jackson()
        }

        println("🔌 Service B: Embedded server initialized on port 8080")

        routing {
            get("/api/order") {
                println("📨 Received request to /api/order")
                application.log.info("Handling order request")

                val tracer = GlobalOpenTelemetry.getTracer("service-b-tracer")
                val span = tracer.spanBuilder("process-order").startSpan()
                try {
                    span.addEvent("Fetching order details")
                    println("🔧 Fetching order details...")
                    call.respond(mapOf("orderId" to "B-123", "status" to "processed"))
                } catch (e: Exception) {
                    span.recordException(e)
                    throw e
                } finally {
                    span.end() // ✅ Обязательно завершаем спан
                }
            }
        }
    }.start(wait = true)
}