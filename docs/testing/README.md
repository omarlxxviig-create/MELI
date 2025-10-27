# 🧪 Testing Resources

Esta carpeta contiene todos los recursos para realizar pruebas del servicio de transporte.

---

## 📊 JMeter - Pruebas de Carga

### Archivos Disponibles

- **`Jmeter.jmx`** - Plan de pruebas principal de JMeter
- **`SmokeTest.jmx`** - Pruebas de smoke test
- **`test-users.csv`** - Datos de usuarios de prueba

### ✅ Archivos JMeter Actualizados

Los archivos de JMeter han sido **actualizados exitosamente** para el **sistema de transporte**:

✅ **Cambios Completados**:

- Endpoints cambiados a `/api/reservations`
- Payloads actualizados con `servicePostId`, `userId`, `seatsRequested`, etc.
- JSON extractors actualizados (`$.id` en lugar de `$.reservationId`)
- Mensajes de error adaptados ("No seats available")
- Títulos y descripciones actualizados

📄 **Ver detalles completos**: [JMETER-UPDATE-SUMMARY.md](JMETER-UPDATE-SUMMARY.md)

### ⚠️ Requisitos Previos

Antes de ejecutar los tests, asegúrate de tener:

1. **Service Post activo** con ID 1 en la base de datos:

   ```sql
   INSERT INTO service_posts (id, driver_id, origin, destination, departure_time, available_seats, price_per_seat, status)
   VALUES (1, 1, 'Ciudad A', 'Ciudad B', CURRENT_TIMESTAMP + INTERVAL '1 DAY', 50, 25.50, 'ACTIVE');
   ```

2. **Usuarios registrados** según `test-users.csv`

3. **Aplicación corriendo** en `localhost:8080`

### 🚀 Cómo Ejecutar JMeter

#### Opción 1: GUI Mode (para edición)

```bash
jmeter -t Jmeter.jmx
```

#### Opción 2: CLI Mode (para ejecución)

```bash
jmeter -n -t Jmeter.jmx -l results.jtl -e -o report/
```

#### Opción 3: Smoke Test

```bash
jmeter -n -t SmokeTest.jmx -l smoke-results.jtl
```

### 📝 Modificación de test-users.csv

El archivo `test-users.csv` puede necesitar ajustes dependiendo de los campos que uses:

```csv
username,password,userId
user1,pass123,1
user2,pass456,2
```

---

## 📮 Postman

### Archivos Disponibles

- **`MELI.postman_collection.json`** - Colección de Postman con endpoints

### ⚠️ También Requiere Actualización

La colección de Postman también necesita actualización para reflejar los nuevos endpoints de transporte.

#### Endpoints Actuales (usar transport-sample-requests.http como referencia)

```http
### Crear un Post de Servicio de Transporte
POST http://localhost:8080/api/posts
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "driverId": 1,
  "origin": "Ciudad A",
  "destination": "Ciudad B",
  "departureTime": "2025-10-28T10:00:00",
  "availableSeats": 4,
  "pricePerSeat": 25.50
}

### Crear una Reserva
POST http://localhost:8080/api/reservations
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "servicePostId": 1,
  "userId": 2,
  "seatsRequested": 2,
  "pickupLocation": "Punto A",
  "dropoffLocation": "Punto B"
}
```

---

## 🎯 Checklist de Ejecución

- [x] ✅ Actualizar `Jmeter.jmx` con nuevos endpoints
- [x] ✅ Actualizar `SmokeTest.jmx` con nuevos endpoints
- [x] ✅ Actualizar payloads JSON en ambos archivos
- [ ] Crear Service Post de prueba en la BD
- [ ] Verificar usuarios en `test-users.csv` existen
- [ ] Actualizar `MELI.postman_collection.json`
- [ ] Probar ejecución de JMeter en modo GUI
- [ ] Validar resultados de smoke test
- [ ] Ejecutar prueba de carga completa

---

## 📊 Métricas a Monitorear

Al ejecutar las pruebas de carga, enfócate en:

1. **Throughput**: Requests por segundo
2. **Response Time**: Tiempo de respuesta (p50, p95, p99)
3. **Error Rate**: % de errores
4. **Concurrent Users**: Usuarios simultáneos
5. **Database Connections**: Conexiones activas (HikariCP)

---

## 🔗 Referencias

- Documentación principal: `../TRANSPORT-README.md`
- Sample requests HTTP: `../transport-sample-requests.http`
- Arquitectura: `../architecture/HEXAGONAL_ARCHITECTURE.md`
- **Troubleshooting**: `TROUBLESHOOTING.md` - Solución de problemas comunes
- **Detalles de actualización**: `JMETER-UPDATE-SUMMARY.md`

---

## ⚠️ Problemas Comunes

### Error: `java.net.SocketException: Socket closed`

**Causa**: La aplicación no está corriendo

**Solución rápida**:

```powershell
# Verificar si el puerto está abierto
Test-NetConnection localhost -Port 8080 -InformationLevel Quiet

# Si devuelve False, iniciar la aplicación
java -jar target/inventory-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

**Ver más**: [TROUBLESHOOTING.md](TROUBLESHOOTING.md) para soluciones detalladas

---

**Última Actualización**: 27 de Octubre, 2025  
**Estado**: ✅ Archivos JMeter actualizados para sistema de transporte  
**Pendiente**: Crear datos de prueba y ejecutar tests
