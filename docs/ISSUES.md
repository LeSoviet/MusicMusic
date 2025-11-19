# 🐛 Issues y Inconsistencias Encontradas

Documento para rastrear bugs, problemas y inconsistencias en MusicMusic.

---

## 🔴 Críticos (Bloquean funcionalidad principal)

### 1. Base de Datos SQLDelight no se inicializa correctamente
**Estado**: 🔴 Pendiente  
**Descripción**: Las tablas de RadioEntity no se crean aunque el esquema dice que se creó correctamente.  
**Impacto**: Las radios online no funcionan  
**Solución Propuesta**: Revisar la generación de código de SQLDelight y el Schema.create()

### 2. Escaneo de archivos MP3
**Estado**: 🔴 Pendiente  
**Descripción**: Los archivos MP3 no aparecen después de seleccionar la carpeta  
**Impacto**: No se pueden reproducir archivos locales  
**Solución Propuesta**: Verificar el FileScanner y la integración con MusicRepository

---

## 🟡 Importantes (Afectan experiencia de usuario)

### 3. [Agregar aquí mientras pruebas]
**Estado**: 🟡 Pendiente  
**Descripción**: 
**Impacto**: 
**Solución Propuesta**: 

---

## 🟢 Menores (Mejoras visuales o de usabilidad)

### [Agregar aquí]

---

## ✅ Resueltos

### [Los issues que se vayan resolviendo]

---

## 📝 Notas de Testing

Agrega aquí tus observaciones mientras pruebas:

- [ ] Reproducción de MP3
- [ ] Reproducción de FLAC
- [ ] Reproducción de otros formatos
- [ ] Búsqueda de canciones
- [ ] Filtros por género/artista
- [ ] Playlists
- [ ] Cola de reproducción
- [ ] Controles de reproducción (play, pause, next, prev)
- [ ] Volumen
- [ ] Seek (barra de progreso)
- [ ] Radios online
- [ ] Favoritos
- [ ] Modo oscuro
- [ ] Navegación entre pantallas

---

**Última actualización**: Noviembre 18, 2025
