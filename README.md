# Cappucare Stock

Aplicación Android académica para controlar el inventario ficticio de insumos y medicamentos, desarrollada mediante Jetpack Compose, Material 3, arquitectura MVVM y persistencia local con Room.

## Integrantes

- **Integrante 1:** Jose Diaz
- **Integrante 2:** Ignacio Arntz

## Repositorio GitHub

https://github.com/IgnaArntz/CappucareStock

## Tecnologías utilizadas

- Kotlin.
- Android Studio.
- Jetpack Compose.
- Material 3.
- Arquitectura MVVM.
- Room y SQLite para la persistencia local.
- Coroutines y Flow para las operaciones asíncronas y la observación de datos.
- Sistema de notificaciones de Android.

## Requisitos

- Android Studio compatible con la versión de Android Gradle Plugin configurada en el proyecto.
- JDK requerido por la versión de Gradle del proyecto.
- Emulador o dispositivo Android con API 24 o superior.

## Cómo ejecutar el proyecto

1. Abrir Android Studio.
2. Seleccionar **Open** y abrir la carpeta raíz del proyecto, donde se encuentra el archivo `settings.gradle.kts`.
3. Esperar a que finalice la sincronización de Gradle.
4. Aceptar la instalación de los componentes adicionales que solicite Android Studio, si corresponde.
5. Seleccionar la configuración de ejecución `app`.
6. Elegir un emulador o dispositivo Android conectado.
7. Presionar **Run** para ejecutar la aplicación.

## Usuarios de prueba

La aplicación incluye dos cuentas ficticias para probar el inicio de sesión.

| Perfil | Correo electrónico | Contraseña |
|---|---|---|
| Personal encargado | encargado@cappucare.cl | encargado123 |
| Usuario de ventas | ventas@cappucare.cl | ventas123 |

Las credenciales y los productos utilizados son datos ficticios con fines académicos. No corresponden a cuentas ni a información real.

## Funcionalidades

- Inicio de sesión con validación de campos y credenciales de prueba.
- Identificación del usuario activo.
- Panel principal con contador de productos que se encuentran bajo su nivel mínimo de stock.
- Consulta del inventario de productos e insumos médicos.
- Búsqueda de productos por código o nombre.
- Registro de ingresos de productos.
- Registro de salidas de productos con motivo.
- Validación de cantidades y existencia del producto antes de registrar una operación.
- Actualización transaccional del stock mediante Room.
- Prevención de salidas que superen el stock disponible.
- Actualización del estado del producto según su nivel mínimo de stock.
- Historial de movimientos ordenado desde el más reciente hasta el más antiguo.
- Filtrado del historial por código de producto.
- Registro del responsable y de la fecha y hora de cada movimiento.
- Persistencia local de productos y movimientos.
- Mensajes de validación y confirmación con animaciones en la interfaz.

## Arquitectura y organización

La aplicación utiliza el patrón arquitectónico MVVM (Model-View-ViewModel), que permite separar la interfaz de usuario, la lógica de presentación y el acceso a los datos.

- **Model:** contiene las entidades que representan los productos y los movimientos del inventario.
- **View:** corresponde a la interfaz desarrollada con Jetpack Compose y Material 3.
- **ViewModel:** administra el estado de la interfaz y coordina las operaciones del inventario.
- **Repository:** centraliza el acceso a los datos y comunica el ViewModel con el DAO.
- **DAO:** define las consultas y operaciones sobre la base de datos.
- **Database:** configura Room y las entidades que se almacenan localmente.

## Persistencia de datos

La aplicación utiliza Room sobre SQLite para almacenar los productos y los movimientos del inventario en el dispositivo.

Las operaciones de ingreso y salida se realizan mediante transacciones para mantener la consistencia entre la actualización del stock y el registro del movimiento correspondiente.

El proyecto incluye un mecanismo que intenta importar los productos y movimientos desde una base de datos anterior denominada `cappucare.db`, si esta existe. La importación depende de que la estructura de la base anterior sea compatible con las consultas utilizadas.

La migración debe verificarse en un dispositivo de prueba antes de utilizarla con información que se necesite conservar.

## Recurso nativo de Android: notificaciones

La aplicación incorpora el uso del sistema de notificaciones de Android para informar sobre situaciones de stock bajo después de registrar una salida.

Se contempla la creación del canal de notificaciones `Alertas de inventario` y la solicitud del permiso de notificaciones en Android 13 o superior.

El funcionamiento de las notificaciones fue comprobado en un dispositivo Android durante las pruebas del proyecto. En Android 13 o superior, el permiso de notificaciones debe estar concedido para que el sistema pueda mostrarlas.

## Pruebas funcionales recomendadas

Antes de entregar el proyecto, se recomienda comprobar los siguientes casos:

1. Inicio de sesión con las dos cuentas de prueba.
2. Rechazo de credenciales incorrectas.
3. Búsqueda de productos por nombre y código.
4. Registro de un ingreso y verificación del aumento de stock.
5. Registro de una salida y verificación de la disminución de stock.
6. Rechazo de una salida cuando la cantidad solicitada supera el stock disponible.
7. Actualización del estado del producto cuando alcanza el nivel mínimo.
8. Visualización del historial y comprobación del orden de los movimientos.
9. Filtrado del historial por código de producto.
10. Cierre y reapertura de la aplicación para comprobar la persistencia de los datos.
11. Verificación de las notificaciones nativas en las condiciones correspondientes.

## Consideraciones antes de la entrega

- Confirmar que el repositorio de GitHub contiene la versión final del código fuente.
- Verificar la participación de ambos integrantes mediante commits, de acuerdo con las instrucciones del profesor.
- Comprobar que el proyecto compila y se ejecuta sin errores.
- Verificar el funcionamiento de las notificaciones y del mecanismo de importación de la base de datos, si forman parte de los requisitos evaluados.
- Preparar el archivo ZIP con el código fuente y los archivos de configuración necesarios para abrir el proyecto en Android Studio.
- Excluir las carpetas generadas `build/` y `.gradle/` del ZIP de entrega.

## Uso académico

Cappucare Stock es un proyecto desarrollado con fines educativos para practicar el desarrollo de aplicaciones Android, la gestión de inventarios, la persistencia local, la arquitectura MVVM y el uso de componentes nativos de Android.

La aplicación utiliza datos ficticios y no debe considerarse un sistema de inventario clínico listo para producción.
