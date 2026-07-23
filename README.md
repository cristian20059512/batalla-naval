# Batalla Naval

Mini proyecto académico: el clásico juego de Batalla Naval, jugado contra una
máquina, con interfaz gráfica en JavaFX.

Cada jugador (humano y máquina) despliega una flota de 10 barcos (1
portaaviones, 2 submarinos, 3 destructores y 4 fragatas) en un tablero de
10x10. El jugador humano coloca su flota manualmente (o de forma aleatoria
con un solo clic) y luego dispara sobre el tablero de la máquina tratando de
hundir toda su flota antes de que ella hunda la suya.

## Tecnologías

- Java 17
- JavaFX 21 (interfaz gráfica, FXML + Scene Builder)
- Maven
- JUnit 5 (pruebas unitarias)

## Cómo ejecutar el juego

```bash
mvn clean javafx:run
```

## Cómo correr las pruebas unitarias

```bash
mvn test
```

## Estructura del proyecto

- `model`: entidades del dominio (`Board`, `Ship` y sus tipos, `Fleet`,
  `Player`, `Cell`, etc.)
- `view`: representación visual 3D del tablero (`Board3DView`: Box/Cylinder/
  Sphere, cámara en perspectiva y luces).
- `controller`: controladores FXML y el orquestador de la partida
  (`GameController`)
- `persistence`: guardado/carga de la partida (archivos serializables y
  archivo plano)
- `ai`: estrategias de disparo de la máquina
- `util`: utilidades y tipos de apoyo (coordenadas, fases de juego, el
  cronómetro de partida, etc.)
- `exception`: excepciones propias del juego

## Historias de usuario implementadas

- **HU-1** Colocación de barcos en el tablero de posición (manual u
  aleatoria), con validación de superposición y límites del tablero.
- **HU-2** Disparos del jugador humano sobre el tablero de la máquina, con
  resultado de agua/tocado/hundido y manejo de turnos.
- **HU-3** Visualización del tablero de la máquina con fines de
  verificación (solo antes o después de la partida, no durante el juego).
- **HU-4** Máquina que coloca su flota y dispara de forma autónoma.
- **HU-5** Guardado automático del estado de la partida tras cada jugada, y
  reanudación exacta al volver a abrir el juego.

## Heurísticas de usabilidad aplicadas

1. **Visibilidad del estado del sistema**: `statusLabel` informa en todo
   momento qué hacer o qué acaba de pasar (qué barco toca colocar, resultado
   de cada disparo, de quién es el turno); `timerLabel` muestra el tiempo
   transcurrido; el botón brújula se resalta cuando el modo verificación
   está activo.
2. **Coincidencia entre el sistema y el mundo real**: temática pirata/naval
   consistente (mapa del tesoro, timón, botones de madera) y lenguaje del
   dominio ("Levantar anclas", "Girar barco", "Bitácora de viaje").
3. **Control y libertad del usuario**: botón "volver al menú" en la pantalla
   de juego (salida de emergencia sin perder progreso, ya que la partida se
   autoguarda) y "Bitácora de viaje" para retomarla después.
4. **Consistencia y estándares**: mismos estilos de botón (madera, brújula),
   cursor de mano y paleta de colores en ambas pantallas.
5. **Prevención de errores**: previsualización del barco en verde/rojo antes
   de colocarlo; los botones que ya no aplican (girar barco, colocar flota
   aleatoria, ver tablero enemigo) se deshabilitan en la fase donde no
   tienen efecto, en vez de quedar activos sin hacer nada.
6. **Reconocer en lugar de recordar**: los atajos de teclado se muestran en
   el propio texto de los botones ("Girar barco (R)", "... (ESPACIO)") y en
   tooltips, no hay que memorizarlos de otra parte.
7. **Flexibilidad y eficiencia de uso**: cada acción principal tiene mouse
   (botón) y teclado (R, ESPACIO, V, ESC) para jugadores nuevos y expertos.
8. **Estética y diseño minimalista**: paneles agrupados por tablero
   (`board-frame`), sin controles sueltos ni ruido visual adicional.
9. **Ayudar a reconocer, diagnosticar y recuperarse de errores**: los
   mensajes de `InvalidPlacementException`/`InvalidShotException` se
   muestran en lenguaje claro en `statusLabel` en vez de solo fallar en
   silencio.
10. **Ayuda y documentación**: el botón "Opciones" del menú principal
    muestra las reglas del juego, la terminología (agua/tocado/hundido) y
    los atajos de teclado disponibles.

## Integrantes

- Cristian Camilo Criollo (`cristian20059512`)
