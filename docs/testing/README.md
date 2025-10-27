# 🧪 Testing Resources

Esta carpeta contiene todos los recursos para realizar pruebas del servicio de transporte.

---

## 📊 JMeter - Pruebas de Carga

### Archivos Disponibles

- **`Jmeter.jmx`** - Plan de pruebas principal de JMeter
- **`SmokeTest.jmx`** - Pruebas de smoke test
- **`test-users.csv`** - Datos de usuarios de prueba

### ⚠️ IMPORTANTE: Actualización Necesaria

Los archivos de JMeter actualmente contienen endpoints del **sistema antiguo de inventario**. Es necesario actualizarlos para el **sistema de transporte**:

#### Cambios Requeridos en los .jmx:

1. **Endpoints a actualizar**:

   ```
   ANTES (Inventario):
   - GET  /api/products
   - POST /api/inventory/reserve
   - POST /api/inventory/release

   DESPUÉS (Transporte):
   - GET  /api/posts
   - POST /api/reservations
   - DELETE /api/reservations/{id}
   ```

2. **Payloads a actualizar**:

   ```json
   ANTES (Inventario):
   {
     "productId": 1,
     "quantity": 5
   }

   DESPUÉS (Transporte):
   {
     "servicePostId": 1,
     "userId": 1,
     "seatsRequested": 2,
     "pickupLocation": "Location A",
     "dropoffLocation": "Location B"
   }
   ```

3. **Variables a revisar**:
   - Actualizar nombres de variables que hagan referencia a productos/inventario
   - Ajustar a conceptos de transporte (posts, reservations, seats)

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

## 🎯 Checklist de Actualización

- [ ] Actualizar `Jmeter.jmx` con nuevos endpoints
- [ ] Actualizar `SmokeTest.jmx` con nuevos endpoints
- [ ] Actualizar payloads JSON en ambos archivos
- [ ] Revisar y ajustar `test-users.csv`
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

---

**Última Actualización**: 27 de Octubre, 2025  
**Estado**: ⚠️ Requiere actualización para endpoints de transporte
