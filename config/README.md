# Configuración

Esta carpeta contiene archivos de configuración para servicios externos.

## Archivos

- **prometheus.yml** - Configuración de Prometheus para scraping de métricas
- **alerts.yml** - Definición de alertas de Prometheus
- **application.properties** - Propiedades de configuración adicionales (legacy)

## Nota

Los archivos de configuración principales de la aplicación Spring Boot se encuentran en:

- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-jwt.yml`

Esta carpeta solo contiene configuraciones para servicios de infraestructura externos (Prometheus, Grafana, etc.)
