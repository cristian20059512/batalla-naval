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
- `view`: representación visual del tablero con figuras 2D de JavaFX
  (`BoardView`)
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

## Integrantes

- Cristian Camilo Criollo (`cristian20059512`)
