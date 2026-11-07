# Entrega final — 1RM Calculator

## Descripción general

1RM Calculator es una aplicación Android que estima la repetición máxima (1RM) a partir del peso levantado y las repeticiones realizadas. Los cálculos válidos se guardan automáticamente en un historial local.

## Tecnologías utilizadas

- Kotlin.
- Jetpack Compose y Material 3.
- Android ViewModel, StateFlow y coroutines.
- Room y Flow para persistencia reactiva local.
- JUnit y pruebas instrumentadas de Android.

## Arquitectura general

La regla de cálculo reside en `domain/calculator/Calculadora1RM`. Las pantallas de Compose observan estados expuestos por sus ViewModels. El repositorio valida y centraliza el acceso al DAO de Room, mientras `OneRMApplication` construye la dependencia compartida.

## Funcionalidades implementadas

- Cálculo de 1RM desde peso y entre una y diez repeticiones.
- Validación de entradas numéricas y de rangos.
- Presentación de cargas para 50 %, 60 %, 70 %, 80 %, 90 % y 100 %.
- Guardado automático de cálculos válidos.
- Historial local, eliminación individual y eliminación completa con confirmación.
- Actualización reactiva del historial mediante Flow.

## Cálculo de 1RM

Para más de una repetición se utiliza la fórmula de Epley: `peso × (1 + repeticiones / 30)`. Cuando se realiza una repetición, el resultado es exactamente el peso levantado. El dominio rechaza pesos no finitos o no positivos y repeticiones fuera del rango de 1 a 10.

## Estructura principal de paquetes

- `domain`: modelo del cálculo y regla matemática.
- `data/local`: entidad, DAO y configuración de Room.
- `data/repository`: validación de persistencia y acceso al historial.
- `ui/calculadora`: estado, ViewModel y pantalla de cálculo.
- `ui/historial`: estado, ViewModel y pantalla de historial.
- Paquete raíz: actividad principal e inicialización de dependencias.

## Funcionamiento de Room

`CalculoEntity` representa una fila de `calculos_1rm`. `CalculoDao` inserta, consulta, elimina por identificador y elimina el historial completo. `OneRMDatabase` mantiene una única instancia de Room para el proceso. El repositorio inserta resultados consistentes y expone el `Flow` del DAO, por lo que el historial se actualiza cuando cambian los registros.

## Flujo de navegación

`MainActivity` mantiene la pantalla actual en estado Compose. La calculadora abre el historial mediante su acción correspondiente; desde el historial, el botón de volver y el gesto Atrás retornan a la calculadora. Ambas pantallas usan el mismo repositorio creado por `OneRMApplication`.

## Resultados de pruebas automatizadas

Los XML de resultados disponibles en `app/build/test-results/testDebugUnitTest` registran 64 pruebas JVM, 0 fallos y 0 errores. El XML disponible de pruebas instrumentadas registra 19 pruebas, 0 fallos y 0 errores. La regresión de esta entrega no pudo completarse desde el entorno de automatización: los procesos Gradle fueron interrumpidos antes de finalizar y ADB no pudo iniciar su daemon. Por ello, estos registros se informan como evidencia previa disponible, no como una nueva ejecución de esta entrega.

## Instrucciones para compilar

Desde la raíz del proyecto, ejecutar:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain --stacktrace
```

Para las pruebas JVM e instrumentadas, usar respectivamente `:app:testDebugUnitTest` y `:app:connectedDebugAndroidTest` con los mismos argumentos.

## Ubicación del APK

El APK de esta entrega es de tipo debug y se genera en `app/build/outputs/apk/debug/app-debug.apk`. No es una release firmada para distribución.

## Limitaciones conocidas

- La estimación depende de la fórmula de Epley y del rango admitido de una a diez repeticiones.
- Esta entrega no incluye pruebas manuales, evaluación visual, firma de producción ni publicación.
- El directorio entregado no contiene metadatos de Git; por ello no es posible obtener `git status` ni validar el diff con Git desde este entorno.
