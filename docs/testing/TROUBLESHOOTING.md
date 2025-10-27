# 🔧 JMeter - Troubleshooting Guide

## ❌ Error: `java.net.SocketException: Socket closed`

### 📋 Descripción del Problema

```
java.net.SocketException: Socket closed
	at java.base/sun.nio.ch.NioSocketImpl.endRead(NioSocketImpl.java:253)
	...
```

Este error ocurre cuando JMeter intenta conectarse al servidor pero la conexión se cierra inesperadamente.

### 🔍 Causas Comunes

1. **La aplicación no está corriendo** ❌ (Más común)
2. **El puerto 8080 está bloqueado** por firewall
3. **Timeout demasiado corto** en las configuraciones HTTP
4. **La aplicación se reinició** durante la ejecución del test

---

## ✅ Soluciones Implementadas

### 1. Verificar que la Aplicación Esté Corriendo

**Problema identificado**: La aplicación se detuvo (Exit Code: 1)

**Comandos para verificar**:

```powershell
# Verificar si el puerto 8080 está abierto
Test-NetConnection -ComputerName localhost -Port 8080 -InformationLevel Quiet

# Resultado esperado: True
# Si es False, la aplicación no está corriendo
```

**Solución**: Iniciar la aplicación

```powershell
# Opción 1: En la misma terminal
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# Opción 2: En una terminal nueva (recomendado)
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev" -WindowStyle Minimized
```

**Esperar 20-25 segundos** hasta ver en los logs:

```
Started InventoryServiceApplication in X.XXX seconds
Tomcat started on port 8080 (http)
```

### 2. Configuración HTTP Request Defaults Agregada

Se agregó configuración para manejar mejor los timeouts y conexiones:

```xml
<ConfigTestElement guiclass="HttpDefaultsGui" testclass="ConfigTestElement" testname="HTTP Request Defaults">
  <stringProp name="HTTPSampler.connect_timeout">20000</stringProp>  <!-- 20 segundos -->
  <stringProp name="HTTPSampler.response_timeout">30000</stringProp> <!-- 30 segundos -->
  <stringProp name="HTTPSampler.protocol">http</stringProp>
  <stringProp name="HTTPSampler.implementation">HttpClient4</stringProp>
</ConfigTestElement>
```

**Beneficios**:

- ✅ Timeout de conexión: 20 segundos (suficiente para conexiones lentas)
- ✅ Timeout de respuesta: 30 segundos (para operaciones de BD)
- ✅ Usa HttpClient4 (más estable que Java implementation)

---

## 📋 Checklist Pre-Ejecución de JMeter

Antes de ejecutar cualquier test de JMeter:

- [ ] **Verificar aplicación corriendo**:

  ```powershell
  Test-NetConnection localhost -Port 8080 -InformationLevel Quiet
  ```

  Debe devolver `True`

- [ ] **Verificar logs de la aplicación**:
      Buscar: `Tomcat started on port 8080`

- [ ] **Probar un request manual**:

  ```powershell
  Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get
  ```

  Debe devolver: `{"status":"UP"}`

- [ ] **Verificar que existan datos de prueba**:

  - Service Post con ID 1
  - Usuarios registrados

- [ ] **Asegurarse que test-users.csv existe** en la carpeta `docs/testing/`

---

## 🚀 Orden Correcto de Ejecución

### 1. Iniciar Aplicación

```powershell
cd C:\Users\omaroalvaradoc\Documents\Personal\Proyectos\MELI\inventory-service

# Iniciar en ventana separada
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev" -WindowStyle Minimized

# Esperar 25 segundos
Start-Sleep -Seconds 25
```

### 2. Verificar Aplicación

```powershell
# Verificar puerto
Test-NetConnection localhost -Port 8080 -InformationLevel Quiet

# Verificar health
Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health'
```

### 3. Ejecutar JMeter

```bash
# GUI Mode (para desarrollo)
cd docs/testing
jmeter -t Jmeter.jmx

# CLI Mode (para resultados)
jmeter -n -t Jmeter.jmx -l results.jtl -e -o report/

# Smoke Test
jmeter -n -t SmokeTest.jmx -l smoke-results.jtl
```

---

## 🔧 Otros Errores Comunes

### Error: `Connection refused`

**Causa**: Puerto 8080 ocupado por otra aplicación

**Solución**:

```powershell
# Ver qué proceso usa el puerto 8080
netstat -ano | findstr :8080

# Matar el proceso (usar el PID del comando anterior)
taskkill /PID <PID> /F
```

### Error: `401 Unauthorized` en Login

**Causa**: Usuario no existe en la base de datos

**Solución**: Ejecutar el SetupThreadGroup primero para registrar usuarios

### Error: `No seats available` inmediatamente

**Causa**: No existe Service Post con ID 1 o no tiene asientos

**Solución**: Crear Service Post con datos de prueba

```sql
INSERT INTO service_posts (id, driver_id, origin, destination, departure_time, available_seats, price_per_seat, status)
VALUES (1, 1, 'Ciudad A', 'Ciudad B', CURRENT_TIMESTAMP + INTERVAL '1 DAY', 50, 25.50, 'ACTIVE');
```

---

## 📊 Monitoreo Durante Ejecución

Mientras JMeter corre, monitorear:

1. **Logs de la aplicación** - Ver requests entrantes
2. **Conexiones HikariCP** - No deben exceder el máximo (20)
3. **Memory/CPU** - Verificar que no haya problemas de recursos
4. **Errores HTTP** - 4xx/5xx en View Results Tree

---

## 💡 Tips Adicionales

### Reducir Carga para Testing Inicial

Si encuentras problemas, reduce la carga:

```xml
<!-- En Jmeter.jmx -->
<intProp name="ThreadGroup.num_threads">10</intProp>  <!-- De 500 a 10 -->
<intProp name="ThreadGroup.ramp_time">5</intProp>     <!-- De 60 a 5 -->
```

### Habilitar Logs Detallados en JMeter

En GUI mode, agregar "View Results Tree" listener para ver:

- Request headers/body
- Response headers/body
- Errores detallados

### Usar Postman Primero

Antes de JMeter, probar endpoints con Postman o `transport-sample-requests.http` para asegurarse que funcionan.

---

**Última Actualización**: 27 de Octubre, 2025  
**Errores Resueltos**: Socket closed, Connection refused, Timeouts
