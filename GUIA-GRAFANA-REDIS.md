# ========================================

# GUÍA: CONFIGURAR GRAFANA MANUALMENTE

# ========================================

## ✅ ESTADO ACTUAL (Todo funcionando):

- Redis: ✓ Funcionando (puerto 6379)
- Prometheus: ✓ Recolectando métricas (puerto 9090)
- Grafana: ✓ Funcionando (puerto 3000)
- Aplicación: ✓ Corriendo con 448 métricas disponibles

## 📊 CONFIGURAR GRAFANA (5 PASOS SIMPLES):

### 1. Abre Grafana

- URL: http://localhost:3000
- Usuario: admin
- Contraseña: admin (o la que hayas establecido)

### 2. Agregar Datasource de Prometheus

- En el menú lateral, ve a: "Connections" > "Data sources"
- Click en "Add data source"
- Selecciona "Prometheus"

### 3. Configurar la conexión

- Name: `Prometheus`
- URL: `http://prometheus:9090` (¡IMPORTANTE! Usa "prometheus", NO "localhost")
- Access: `Server (default)`
- Deja el resto de opciones por defecto

### 4. Guardar y probar

- Click en "Save & test" al final
- Deberías ver: "Successfully queried the Prometheus API"

### 5. Crear tu primer Dashboard

- En el menú lateral, ve a: "Dashboards" > "New" > "New Dashboard"
- Click en "Add visualization"
- Selecciona "Prometheus" como datasource
- En la query, escribe algunas de estas métricas:

```
# Solicitudes HTTP
rate(http_server_requests_seconds_count[5m])

# Uso de memoria JVM
jvm_memory_used_bytes{area="heap"}

# CPU del sistema
system_cpu_usage

# Uptime de la aplicación
process_uptime_seconds
```

## 🔍 VERIFICAR DATOS DE REDIS:

### Opción 1: Usar Redis Insight (Interfaz gráfica)

- URL: http://localhost:5540
- Agregar conexión:
  - Host: redis
  - Port: 6379
  - Name: Local Redis

### Opción 2: Usar Redis CLI desde PowerShell

```powershell
# Ver todas las keys
docker exec redis redis-cli KEYS "*"

# Ver información del servidor
docker exec redis redis-cli INFO

# Ver estadísticas
docker exec redis redis-cli INFO stats

# Monitorear comandos en tiempo real
docker exec redis redis-cli MONITOR

# Ver configuración
docker exec redis redis-cli CONFIG GET "*"

# Ver número de keys en la base de datos
docker exec redis redis-cli DBSIZE
```

### Opción 3: Comandos útiles para debugging

```powershell
# Verificar conexión
docker exec redis redis-cli PING

# Crear datos de prueba
docker exec redis redis-cli SET test:key1 "valor1"
docker exec redis redis-cli SET test:key2 "valor2"
docker exec redis redis-cli LPUSH test:list "item1" "item2" "item3"

# Listar las keys
docker exec redis redis-cli KEYS "test:*"

# Obtener un valor
docker exec redis redis-cli GET test:key1

# Ver tipo de dato
docker exec redis redis-cli TYPE test:list

# Ver elementos de la lista
docker exec redis redis-cli LRANGE test:list 0 -1

# Limpiar datos de prueba
docker exec redis redis-cli DEL test:key1 test:key2 test:list
```

## 📝 MÉTRICAS IMPORTANTES PARA MONITOREAR:

### Performance de la Aplicación:

```
# Latencia de requests
http_server_requests_seconds_bucket

# Throughput
rate(http_server_requests_seconds_count[1m])

# Errores (códigos 4xx y 5xx)
sum(rate(http_server_requests_seconds_count{status=~"4.."}[5m]))
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
```

### Recursos JVM:

```
# Memoria heap usada vs máxima
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

# Garbage Collection
rate(jvm_gc_pause_seconds_count[5m])

# Threads
jvm_threads_live_threads
```

### Sistema:

```
# CPU
system_cpu_usage
process_cpu_usage

# Disco
disk_free_bytes
disk_total_bytes
```

## 🚀 DASHBOARDS RECOMENDADOS:

Puedes importar dashboards predefinidos:

1. En Grafana, ve a "Dashboards" > "New" > "Import"
2. Ingresa uno de estos IDs de dashboard:
   - **4701** - JVM (Micrometer)
   - **6756** - Spring Boot Statistics
   - **11378** - Spring Boot APM Dashboard
   - **11892** - Spring Boot 2.1 System Monitor

## 📚 URLs IMPORTANTES:

- Grafana: http://localhost:3000
- Prometheus: http://localhost:9090
- Redis Insight: http://localhost:5540
- Aplicación: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Métricas Raw: http://localhost:8080/actuator/prometheus
- Health Check: http://localhost:8080/actuator/health

## 🔧 COMANDOS ÚTILES:

```powershell
# Ejecutar diagnóstico completo
.\diagnostico-simple.ps1

# Ver logs de Prometheus
docker logs prometheus

# Ver logs de Grafana
docker logs grafana

# Ver logs de Redis
docker logs redis

# Reiniciar servicios
docker restart prometheus grafana redis

# Ver targets en Prometheus
Start-Process "http://localhost:9090/targets"

# Abrir Grafana
Start-Process "http://localhost:3000"

# Abrir Redis Insight
Start-Process "http://localhost:5540"
```

## ❓ TROUBLESHOOTING:

### No veo métricas en Grafana:

1. Verifica que el datasource esté configurado correctamente
2. Asegúrate de usar `http://prometheus:9090` (no localhost)
3. Verifica que Prometheus tenga el target "up": http://localhost:9090/targets
4. Espera 30-60 segundos para que se acumulen métricas

### Redis no muestra datos:

1. Tu aplicación puede no estar usando Redis aún
2. Redis puede estar configurado para otro propósito (cache, sesiones)
3. Usa los comandos de prueba para crear datos de ejemplo

### Prometheus muestra "down":

1. Verifica que tu aplicación esté corriendo: `jps -l`
2. Verifica el endpoint: http://localhost:8080/actuator/prometheus
3. Reinicia Prometheus: `docker restart prometheus`
