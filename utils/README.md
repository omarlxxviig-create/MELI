# Utilidades

Esta carpeta contiene herramientas y scripts auxiliares para desarrollo.

## Archivos Java

- **GenerateBCryptHashes.java** - Genera hashes BCrypt para contraseñas
- **GenerateSecureKey.java** - Genera claves seguras para JWT (HS512)

## Scripts SQL

- **fix-password.sql** - Script SQL para corregir hashes de contraseñas en la BD

## Uso de Utilidades Java

Para compilar y ejecutar:

```powershell
# Generar hash BCrypt
javac GenerateBCryptHashes.java
java GenerateBCryptHashes

# Generar clave JWT segura
javac GenerateSecureKey.java
java GenerateSecureKey
```

Estas utilidades son herramientas de desarrollo y no forman parte del código principal de la aplicación.
