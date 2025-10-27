# 📚 Transport Booking Service - Documentation

Esta carpeta contiene toda la documentación del servicio de reservas de transporte (carpooling) organizada por categorías.

## � Documentación Principal

### Guías Core

- **[TRANSPORT-README.md](TRANSPORT-README.md)** - Documentación principal del servicio de transporte
- **[EXECUTIVE-SUMMARY.md](EXECUTIVE-SUMMARY.md)** - Resumen ejecutivo del proyecto
- **[TRANSFORMATION-GUIDE.md](TRANSFORMATION-GUIDE.md)** - Guía de transformación del sistema de inventario a transporte
- **[VERIFICATION-CHECKLIST.md](VERIFICATION-CHECKLIST.md)** - Checklist de verificación para deployment
- **[GIT-COMMITS-GUIDE.md](GIT-COMMITS-GUIDE.md)** - Convenciones y guía de commits Git

## 🏗️ Arquitectura

Toda la documentación de arquitectura y diagramas está en **`architecture/`**:

- `HEXAGONAL_ARCHITECTURE.md` - Guía de arquitectura hexagonal implementada
- `architecture-diagram.*` - Diagramas de arquitectura del sistema (mmd, png, svg)
- `data-model.*` - Diagramas del modelo de datos
- `transport-data-model.mmd` - Modelo de datos específico de transporte
- `event-flow.*` - Diagramas de flujo de eventos
- `packages-diagram.*` - Diagramas de estructura de paquetes
- `secuences-diagram.*` - Diagramas de secuencia

## 🧪 Testing

Todos los recursos de testing están en **`testing/`**:

### Pruebas de Carga con JMeter

- **`testing/jmeter/`** - Test plans de JMeter para pruebas de carga
  - `Jmeter.jmx` - Plan de pruebas principal de JMeter
  - `SmokeTest.jmx` - Configuración de smoke tests
  - `test-users.csv` - Datos de usuarios de prueba

### Pruebas de API

- **`transport-sample-requests.http`** - Ejemplos de requests HTTP para endpoints de transporte (nivel raíz)

## � Monitoreo

Todas las configuraciones de monitoreo están en **`monitoring/`**:

- `prometheus.yml` - Configuración de Prometheus
- `alertmanager.yml` - Configuración del Alert Manager
- `dashboards/` - Definiciones de dashboards de Grafana
- `provisioning/` - Configuraciones de provisioning de Grafana

## 🗄️ Migraciones

Los scripts de migración de base de datos están en **`migrations/`**.

## 📁 Archivo

Documentación histórica de troubleshooting archivada en **`archive/`** para referencia:

- `archive/troubleshooting/` - Documentos antiguos de troubleshooting de autenticación y monitoreo
  - Issues que ya fueron resueltos
  - Útiles para contexto histórico

---

## 🚀 Inicio Rápido

1. **Lee la guía principal**: Comienza con [TRANSPORT-README.md](TRANSPORT-README.md)
2. **Entiende la arquitectura**: Revisa `architecture/HEXAGONAL_ARCHITECTURE.md`
3. **Prueba la API**: Usa `transport-sample-requests.http` con REST Client
4. **Pruebas de carga**: Ejecuta los tests de JMeter desde `testing/jmeter/`
5. **Monitorea**: Configura dashboards de Grafana desde `monitoring/`

## � Archivos Relacionados

- **Root `/config`** - Archivos de configuración de la aplicación (application.properties, etc.)
- **Root `/scripts`** - Scripts PowerShell para setup y testing
- **Root `/docker-compose.yml`** - Docker compose para Kafka, Redis, etc.

---

**Última Actualización**: 27 de Octubre, 2025  
**Servicio**: Transport Booking API (anteriormente Inventory Service)  
**Versión**: 0.0.1-SNAPSHOT
