# ========================================

# ✅ SOLUCIÓN COMPLETA - RESUMEN

# ========================================

## 🎯 PROBLEMAS RESUELTOS:

### 1. ✅ No veías métricas en Grafana

**Causa:** Tu aplicación Spring Boot NO estaba corriendo
**Solución:** Se inició la aplicación con `.\mvnw.cmd spring-boot:run`

### 2. ✅ Prometheus no podía conectarse a la aplicación

**Causa:** Estaba usando la IP de tu Wi-Fi (172.21.212.120) en lugar de host.docker.internal
**Solución:** Se actualizó `prometheus.yml` para usar `host.docker.internal:8080`

### 3. ✅ No veías datos en Redis

**Causa:** Redis estaba vacío (sin keys)
**Solución:** Se crearon datos de prueba y se mostraron comandos para interactuar con Redis

## 📊 ESTADO ACTUAL (TODO FUNCIONANDO):

```
✅ Redis:       Corriendo en puerto 6379 - PING responde
✅ Prometheus:  Corriendo en puerto 9090 - Target "inventory-service" UP
✅ Grafana:     Corriendo en puerto 3000 - Listo para configurar
✅ Aplicación:  Corriendo en puerto 8080 - 448 métricas disponibles
```

## 🔧 ARCHIVOS MODIFICADOS:

### prometheus.yml

```yaml
- targets: ["host.docker.internal:8080"] # ← CAMBIO IMPORTANTE
```

## 📝 SCRIPTS CREADOS:

### 1. diagnostico-simple.ps1

Ejecuta todas las pruebas de conectividad y muestra el estado completo

```powershell
.\diagnostico-simple.ps1
```

### 2. generar-metricas.ps1

Genera tráfico de prueba para crear métricas visibles en Grafana

```powershell
.\generar-metricas.ps1
```

### 3. configurar-grafana.ps1

Intenta configurar automáticamente el datasource de Prometheus (requiere credenciales)

```powershell
.\configurar-grafana.ps1
```

## 🚀 PRÓXIMOS PASOS - CONFIGURAR GRAFANA:

### Paso 1: Abrir Grafana

```
URL: http://localhost:3000
Usuario: admin
Contraseña: admin (o la que hayas establecido)
```

### Paso 2: Agregar Datasource

1. Menú lateral → "Connections" → "Data sources"
2. Click en "Add data source"
3. Seleccionar "Prometheus"
4. Configurar:
   - **Name:** Prometheus
   - **URL:** http://prometheus:9090 ← ¡IMPORTANTE! Usar "prometheus", NO "localhost"
   - **Access:** Server (default)
5. Click en "Save & test"
6. Deberías ver: "Successfully queried the Prometheus API"

### Paso 3: Crear Dashboard

1. Menú lateral → "Dashboards" → "New" → "New Dashboard"
2. Click en "Add visualization"
3. Seleccionar "Prometheus"
4. En el campo de query, escribir una de estas:

```promql
# Requests por segundo
rate(http_server_requests_seconds_count[1m])

# Memoria JVM
jvm_memory_used_bytes{area="heap"}

# CPU
system_cpu_usage

# Uptime
process_uptime_seconds
```

## 💾 COMANDOS DE REDIS:

### Ver datos en Redis:

```powershell
# Listar todas las keys
docker exec redis redis-cli KEYS "*"

# Ver datos de ejemplo creados
docker exec redis redis-cli GET "inventory:product:1"
docker exec redis redis-cli GET "inventory:stock:1"
docker exec redis redis-cli LRANGE "inventory:updates" 0 -1

# Monitorear en tiempo real
docker exec redis redis-cli MONITOR

# Estadísticas
docker exec redis redis-cli INFO stats
```

### Usar Redis Insight (GUI):

```
URL: http://localhost:5540

Agregar conexión:
- Host: redis
- Port: 6379
- Name: Local Redis
```

## 📈 DASHBOARDS RECOMENDADOS PARA IMPORTAR:

En Grafana → Dashboards → New → Import

```
4701  - JVM (Micrometer)
6756  - Spring Boot Statistics
11378 - Spring Boot APM Dashboard
11892 - Spring Boot 2.1 System Monitor
```

## 🌐 URLs IMPORTANTES:

```
Grafana:        http://localhost:3000 (admin/admin)
Prometheus:     http://localhost:9090
Prometheus UI:  http://localhost:9090/graph
Targets:        http://localhost:9090/targets
Redis Insight:  http://localhost:5540
Aplicación:     http://localhost:8080
Swagger:        http://localhost:8080/swagger-ui.html
Métricas:       http://localhost:8080/actuator/prometheus
Health:         http://localhost:8080/actuator/health
```

## 🔍 VERIFICACIÓN RÁPIDA:

```powershell
# Ver que todo esté UP
.\diagnostico-simple.ps1

# Generar más métricas
.\generar-metricas.ps1

# Ver targets en Prometheus
Start-Process "http://localhost:9090/targets"
```

## 📚 MÉTRICAS ÚTILES PARA GRAFANA:

### Performance:

```promql
# Throughput
rate(http_server_requests_seconds_count[5m])

# Latencia P95
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Tasa de errores
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
```

### Recursos:

```promql
# Memoria heap usada %
100 * (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"})

# GC time
rate(jvm_gc_pause_seconds_sum[5m])

# Threads activos
jvm_threads_live_threads
```

## ❓ TROUBLESHOOTING:

### "No data" en Grafana:

1. Verifica que Prometheus esté UP: http://localhost:9090/targets
2. Asegúrate de usar `http://prometheus:9090` en el datasource
3. Ejecuta `.\generar-metricas.ps1` para generar tráfico
4. Espera 30-60 segundos para acumular datos

### Redis vacío:

```powershell
# Crear datos de prueba
docker exec redis redis-cli SET test "hola"
docker exec redis redis-cli KEYS "*"
```

### Aplicación no responde:

```powershell
# Verificar que esté corriendo
jps -l | findstr inventory

# Si no está, iniciarla
.\mvnw.cmd spring-boot:run
```

## 🎉 ¡LISTO!

Todo está funcionando correctamente. Ahora puedes:

- ✅ Ver métricas en Grafana
- ✅ Consultar Prometheus directamente
- ✅ Interactuar con Redis
- ✅ Monitorear tu aplicación en tiempo real
