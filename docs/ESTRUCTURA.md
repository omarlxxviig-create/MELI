# 📁 Estructura del Proyecto - Inventory Service

Este documento explica la organización de carpetas y archivos del proyecto.

## 🏗️ Estructura Principal

```
inventory-service/
├── 📂 src/                          # Código fuente de la aplicación
│   ├── main/java/                   # Código principal
│   ├── main/resources/              # Recursos (configs, SQL, etc)
│   └── test/                        # Tests unitarios e integración
│
├── 📂 docs/                         # 📚 Documentación completa
│   ├── Arquitectura y diseño
│   ├── Guías de configuración
│   ├── Soluciones técnicas
│   ├── Colecciones Postman
│   └── Scripts JMeter
│
├── 📂 scripts/                      # 🔧 Scripts de automatización
│   ├── start-app.ps1               # Script principal para iniciar app
│   ├── setup/                      # Scripts de configuración
│   └── testing/                    # Scripts de prueba
│
├── 📂 config/                       # ⚙️ Configuración de servicios
│   ├── prometheus.yml              # Config de Prometheus
│   ├── alerts.yml                  # Alertas de Prometheus
│   └── application.properties      # Props adicionales (legacy)
│
├── 📂 utils/                        # 🛠️ Utilidades de desarrollo
│   ├── GenerateBCryptHashes.java   # Generador de hashes
│   ├── GenerateSecureKey.java      # Generador de claves JWT
│   └── fix-password.sql            # Script SQL de corrección
│
├── 📂 grafana/                      # 📊 Dashboards de Grafana
├── 📂 jmx-exporter/                 # Exportador JMX para Kafka
├── 📂 target/                       # Build output (generado)
│
├── 📄 docker-compose.yml            # Orquestación de servicios
├── 📄 pom.xml                       # Dependencias Maven
├── 📄 README.md                     # Documentación principal
└── 📄 COPILOT_GUIDELINES.md         # Guías para GitHub Copilot
```

## 📚 Navegación Rápida

### Para Desarrolladores

- **Empezar**: Lee [`README.md`](README.md)
- **Arquitectura**: Ve a [`docs/HEXAGONAL_ARCHITECTURE.md`](docs/HEXAGONAL_ARCHITECTURE.md)
- **Código fuente**: Navega a `src/main/java/com/meli/`
- **Tests**: Revisa `src/test/java/com/meli/`

### Para DevOps

- **Docker**: [`docker-compose.yml`](docker-compose.yml)
- **Monitoreo**: [`docs/docker-compose-monitoring.yml`](docs/docker-compose-monitoring.yml)
- **Prometheus**: [`config/prometheus.yml`](config/prometheus.yml)
- **Grafana**: Carpeta `grafana/`

### Para Testing

- **Scripts de prueba**: `scripts/testing/`
- **Colección Postman**: [`docs/MELI.postman_collection.json`](docs/MELI.postman_collection.json)
- **JMeter**: `docs/Jmeter/`
- **Requests HTTP**: [`docs/sample-requests.http`](docs/sample-requests.http)

### Para Troubleshooting

- **Solución Auth**: [`docs/SOLUCION_FINAL_AUTH.md`](docs/SOLUCION_FINAL_AUTH.md)
- **Guía Grafana**: [`docs/GUIA-GRAFANA-REDIS.md`](docs/GUIA-GRAFANA-REDIS.md)
- **H2 Console**: [`docs/INSTRUCCIONES-H2-CONSOLE.md`](docs/INSTRUCCIONES-H2-CONSOLE.md)

## 🚀 Inicio Rápido

### 1. Iniciar la aplicación

```powershell
# Desde la raíz del proyecto
.\scripts\start-app.ps1
```

### 2. Ejecutar pruebas

```powershell
# Diagnóstico completo
.\scripts\testing\diagnostico-simple.ps1

# Probar login
.\scripts\testing\test-login.ps1

# Probar monitoreo
.\scripts\testing\test-monitoring.ps1
```

### 3. Configurar servicios

```powershell
# Configurar Grafana
.\scripts\setup\configurar-grafana.ps1

# Configurar PowerShell (primera vez)
.\scripts\setup\configure-powershell.ps1
```

## 📖 READMEs por Carpeta

Cada carpeta principal tiene su propio README con detalles específicos:

- 📂 [`docs/README.md`](docs/README.md) - Índice de documentación
- 📂 [`scripts/README.md`](scripts/README.md) - Guía de scripts
- 📂 [`config/README.md`](config/README.md) - Configs de servicios
- 📂 [`utils/README.md`](utils/README.md) - Utilidades de desarrollo

## 🔄 Cambios Recientes

Esta estructura fue reorganizada para mejorar la claridad y mantenibilidad:

- ✅ Scripts agrupados por función (setup vs testing)
- ✅ Documentación centralizada en `docs/`
- ✅ Configuraciones de servicios en `config/`
- ✅ Utilidades de desarrollo en `utils/`
- ✅ READMEs descriptivos en cada carpeta

## ❓ ¿No encuentras algo?

1. Revisa este documento primero
2. Busca en el README de la carpeta correspondiente
3. Usa la búsqueda de archivos de VS Code (Ctrl+P)
4. Consulta el [`README.md`](README.md) principal

---

**Última actualización**: Reorganización de estructura del proyecto
