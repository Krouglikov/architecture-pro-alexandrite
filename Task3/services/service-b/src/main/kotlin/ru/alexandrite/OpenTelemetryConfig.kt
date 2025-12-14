package ru.alexandrite

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.semconv.ServiceAttributes

object OpenTelemetryConfig {
    fun init(serviceName: String): OpenTelemetry {
        val resource: Resource = Resource.create(
            Attributes.of(
                ServiceAttributes.SERVICE_NAME, serviceName
            )
        )

        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(
                SimpleSpanProcessor.create(
                    OtlpGrpcSpanExporter.builder()
                        .setEndpoint("http://simplest-collector.default.svc.cluster.local:4317")
                        .build()
                )
            )
            .build()

        val sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal()

        return sdk
    }
}