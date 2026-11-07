# Gestión de capacidad y rendimiento

## Estado actual

| Indicador | Estado de evidencia | Medición necesaria |
| --- | --- | --- |
| Tiempo de respuesta | Sin medición validada | Promedio, P95 y P99 por endpoint |
| CPU del backend | Sin registro de monitoreo | Uso promedio y máximo durante la prueba |
| Memoria JVM | Sin registro de monitoreo | Heap utilizado y máximo, actividad del GC |
| Pool HikariCP | Configurado con 10 conexiones | Conexiones activas, inactivas, máximas y esperas |
| Errores HTTP | Sin tasa de error medida | Porcentaje de 4xx y 5xx en picos |

Los objetivos de SLA (P95 ≤ 600 ms y disponibilidad ≥ 99,50 %) son objetivos de diseño: aún no están verificados con mediciones reales.

## Pruebas planificadas

1. **Carga gradual:** aumentar las peticiones concurrentes para medir latencia y recursos.
2. **Estrés:** superar la carga habitual para hallar el punto de saturación.
3. **Resistencia:** carga sostenida para descartar fugas de memoria o saturación de HikariCP.
4. **Revisión de consultas:** filtros, índices y separación efectiva lectura/escritura.

## Plan de capacidad

| Situación | Acción |
| --- | --- |
| Carga habitual | Establecer la línea base de latencia y uso de CPU en login, catálogos y búsquedas |
| Crecimiento sostenido | Ajustar índices, revisar timeouts de HikariCP y ampliar memoria |
| Picos de demanda | Aumentar recursos de forma controlada y evaluar escalado horizontal del backend |
| Crecimiento de datos | Monitorear el lag de la réplica MySQL, el almacenamiento y la integridad de lecturas |
