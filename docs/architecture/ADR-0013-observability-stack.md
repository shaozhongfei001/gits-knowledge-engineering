# ADR-0013: Observability Stack Selection

## Status

Accepted

## Context

The GITS Knowledge Engineering system requires observability across three pillars: metrics, tracing, and logging. The system is a Spring Boot 3.x monolith (api + worker) deployed via Docker Compose, with plans for Kubernetes orchestration.

Requirements:
- **Metrics**: Application and business metrics exposed in Prometheus format for alerting and dashboards.
- **Tracing**: Distributed request tracing across API endpoints, background workers, and external service calls (LLM, CRM).
- **Logging**: Structured JSON logs for centralized aggregation and searching.
- **Low overhead**: Observability must not degrade application performance.
- **Spring Boot integration**: Native support via Spring Boot Actuator and Micrometer.

Considered options:
1. **Micrometer + Prometheus + Zipkin + Logstash encoder** (Spring-native)
2. **OpenTelemetry SDK + Jaeger + Fluentd** (vendor-neutral)
3. **Datadog / New Relic APM** (commercial SaaS)

## Decision

Use **Micrometer + Prometheus + Zipkin + Logstash encoder** — the Spring-native observability stack.

- **Metrics**: Micrometer with `micrometer-registry-prometheus` exposes a `/actuator/prometheus` endpoint. Prometheus scrapes metrics; Grafana visualizes them.
- **Tracing**: Micrometer Tracing bridge (`micrometer-tracing-bridge-brave`) + Zipkin reporter (`zipkin-reporter-brave`). Trace data sent to Zipkin via HTTP.
- **Logging**: `logstash-logback-encoder` produces structured JSON logs. Logs collected by Docker logging driver or Filebeat.

## Consequences

### Positive

- **Spring-native**: Micrometer and Actuator are first-class Spring Boot citizens; zero custom instrumentation for common metrics (HTTP, JDBC, JVM).
- **Prometheus ecosystem**: Widely adopted, powerful query language (PromQL), extensive alerting, and Grafana dashboards.
- **Zipkin simplicity**: Lightweight trace collector; Brave bridge avoids direct OpenTelemetry SDK complexity.
- **Structured logging**: JSON logs are machine-parseable, enabling log aggregation and correlation with traces.
- **Cost**: All components are open-source; no per-host or per-span pricing.

### Negative

- **Zipkin vs Jaeger**: Zipkin has a simpler feature set than Jaeger (no adaptive sampling, limited tail-based sampling). Migration to Jaeger or Tempo would require changing the reporter.
- **Micrometer Tracing is a bridge**: It abstracts over Brave (Zipkin) or OTel (Jaeger/Tempo). Switching tracing backends requires configuration changes but not code changes.
- **No built-in log-trace correlation**: Trace IDs must be injected into MDC manually (via Micrometer Tracing's `Slf4jScopeDecorator`) for log-trace correlation.

### Mitigations

- Micrometer Tracing's abstraction layer makes switching from Zipkin to Jaeger/Tempo a configuration change, not a code change.
- `Slf4jScopeDecorator` is configured to automatically inject trace/span IDs into MDC for log correlation.
- Prometheus alerting rules cover SLA breaches (error rate, latency, saturation).
