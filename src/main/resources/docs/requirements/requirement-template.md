# 📄 Requerimientos del Sistema

## 1. Lista general de requerimientos

El sistema de TECHCUP FÚTBOL — Microservicio de Usuarios y Jugadores tiene los siguientes requerimientos (descripción a alto nivel):

### 1.1 Requerimientos funcionales

El microservicio de Usuarios y Jugadores debe tener la capacidad de:

1. Permitir a cada usuario actualizar su información personal básica (nombre, relación con la Escuela, programa académico y semestre), manteniendo inmutables el correo y la contraseña.
2. Permitir al administrador actualizar la información básica de cualquier usuario del sistema con el fin de corregir datos.
3. Permitir inactivar una cuenta de usuario, validando previamente que no esté vinculada a un equipo inscrito en un torneo Activo o En Progreso.
4. Permitir a los jugadores crear su perfil deportivo con posición de juego, número dorsal, foto y disponibilidad.
5. Permitir a los jugadores actualizar los datos de su perfil deportivo, siempre que no estén asignados a un equipo.
6. Permitir a jugadores, capitanes y organizadores consultar el perfil deportivo de cualquier jugador.
7. Permitir a los capitanes buscar jugadores aplicando filtros combinables por posición, edad, género, nombre, identificación y semestre.
8. Permitir a los jugadores recibir y visualizar las invitaciones que les envían los capitanes para unirse a sus equipos.
9. Permitir a los jugadores aceptar o rechazar una invitación, garantizando que un jugador pertenezca únicamente a un equipo a la vez.
10. Permitir al administrador consultar el historial de auditoría de las acciones realizadas sobre los usuarios, con filtros por fecha, actor y tipo de acción.

### 1.2 Requerimientos no funcionales

El microservicio de Usuarios y Jugadores debe tener:

1. Seguridad: autenticación de usuarios mediante token JWT y restricción de acceso a los endpoints según el rol del usuario (jugador, capitán, organizador, árbitro, administrador).
2. Privacidad: el correo y la contraseña no deben poder modificarse desde el servicio de usuarios y jugadores; cualquier intento debe rechazarse con código HTTP 400.
3. Disponibilidad: manejo de excepciones de los servicios externos (Servicio de Equipos/Torneos, MongoDB) sin caídas en cascada del sistema.
4. Mantenibilidad: arquitectura por capas (controladores, servicio, persistencia), aplicando los principios SOLID y los patrones de diseño Strategy, Specification, State y Observer cuando aplique.
5. Escalabilidad: las consultas de búsqueda de jugadores y de auditoría deben soportar paginación con un máximo de 20 resultados por página.
6. Almacenamiento: las imágenes de perfil deportivo se almacenan en MongoDB y los datos estructurados en PostgreSQL, con migraciones gestionadas mediante Flyway o Liquibase.
7. Trazabilidad: todas las operaciones de creación, actualización e inactivación de usuarios deben quedar registradas en una tabla de auditoría inmutable.
8. Usabilidad: la interfaz debe estar construida en React con TypeScript, ofreciendo formularios con validaciones en cliente y mensajes de error claros para el usuario final.

## 2. Diagramas de caso de uso

### 2.1 Requerimiento Funcional 1

| Campo | Descripción |
|------|-------------|
| **ID** | RF-01 |
| **Nombre del requerimiento** | Actualización del perfil propio |
| **Descripción** | *El sistema debe permitir a cualquier usuario autenticado actualizar su información básica (nombre completo, relación con la Escuela, programa académico y semestre), manteniendo inmutables el correo y la contraseña.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El usuario registrado en el sistema.<br>2. El usuario autenticado con un token JWT válido.<br>3. La información actual del usuario almacenada en la base de datos.<br>4. El módulo "Mi Perfil" habilitado. |
| **Actor** | *Usuario autenticado.* |
| **Flujo principal** | 1. El actor ingresa a la aplicación autenticado.<br>2. El sistema muestra el menú principal con la opción "Mi Perfil".<br>3. El actor da clic en "Mi Perfil".<br>4. El sistema redirige al formulario con los datos actuales precargados.<br>5. El actor modifica los campos editables y confirma la actualización.<br>6. El sistema valida los datos ingresados.<br>7. El sistema persiste los cambios en la base de datos.<br>8. El sistema muestra un mensaje de confirmación con los nuevos datos. |
| **Diagrama de caso de uso** | <img width="477" height="249" alt="image" src="https://github.com/user-attachments/assets/162a0d4d-308a-4775-b11e-a83b6b744329" />|
| **Poscondiciones** | *Se espera como resultado que el perfil del usuario quede actualizado en la base de datos con los nuevos valores y que el correo y la contraseña permanezcan inalterados.* |

### 2.2 Requerimiento Funcional 2

| Campo | Descripción |
|------|-------------|
| **ID** | RF-02 |
| **Nombre del requerimiento** | Actualización de usuarios por administrador |
| **Descripción** | *El sistema debe permitir al administrador actualizar la información básica de cualquier usuario para corregir datos erróneos y mantener la consistencia del sistema.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El administrador autenticado.<br>2. El rol ADMINISTRADOR asignado al actor.<br>3. El usuario a modificar registrado en la base de datos.<br>4. El módulo administrativo habilitado. |
| **Actor** | *Administrador.* |
| **Flujo principal** | 1. El administrador ingresa al panel de administración.<br>2. El sistema muestra el listado paginado de usuarios.<br>3. El administrador localiza al usuario deseado y selecciona "Editar".<br>4. El sistema muestra el formulario de edición con los datos actuales.<br>5. El administrador modifica los campos necesarios y confirma.<br>6. El sistema valida los datos y persiste los cambios.<br>7. El sistema muestra un mensaje de confirmación. |
| **Diagrama de caso de uso** |<img width="477" height="248" alt="image" src="https://github.com/user-attachments/assets/09d442f5-cb94-433f-b9c6-f24830034495" />|
| **Poscondiciones** | *Se espera como resultado que los datos del usuario objetivo queden actualizados y que se genere un registro de auditoría con el administrador como actor.* |

### 2.3 Requerimiento Funcional 3

| Campo | Descripción |
|------|-------------|
| **ID** | RF-03 |
| **Nombre del requerimiento** | Inactivación de cuenta con validación de torneos |
| **Descripción** | *El sistema debe permitir inactivar una cuenta de usuario validando previamente que no esté vinculada a un equipo inscrito en un torneo Activo o En Progreso.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El actor autenticado.<br>2. El usuario objetivo registrado y en estado Activo.<br>3. El Servicio de Equipos/Torneos disponible para consulta.<br>4. El módulo de gestión de cuenta habilitado. |
| **Actor** | *Usuario autenticado o Administrador.* |
| **Flujo principal** | 1. El actor ingresa a la sección "Mi cuenta" o al panel administrativo.<br>2. El actor selecciona la opción "Inactivar cuenta".<br>3. El sistema muestra un modal de confirmación.<br>4. El actor confirma la inactivación.<br>5. El sistema consulta al Servicio de Equipos/Torneos para verificar la no pertenencia a un torneo activo.<br>6. El sistema cambia el estado del usuario a Inactivo.<br>7. El sistema muestra un mensaje de confirmación. |
| **Diagrama de caso de uso** |<img width="471" height="429" alt="image" src="https://github.com/user-attachments/assets/34e24c0c-2952-4634-958e-064932ffbcbf" />|
| **Poscondiciones** | *Se espera como resultado que el estado del usuario quede marcado como Inactivo, que pierda la capacidad de iniciar sesión y que se registre el evento en la auditoría.* |

### 2.4 Requerimiento Funcional 4

| Campo | Descripción |
|------|-------------|
| **ID** | RF-04 |
| **Nombre del requerimiento** | Creación del perfil deportivo |
| **Descripción** | *El sistema debe permitir a un jugador crear su perfil deportivo indicando posición de juego, número dorsal, foto y disponibilidad para que sea visible ante los capitanes.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El jugador autenticado y en estado Activo.<br>2. El jugador sin perfil deportivo previo.<br>3. PostgreSQL y la colección de fotos en MongoDB disponibles.<br>4. El módulo de perfil deportivo habilitado. |
| **Actor** | *Jugador.* |
| **Flujo principal** | 1. El jugador ingresa a la sección "Mi Perfil Deportivo".<br>2. El jugador da clic en "Crear perfil".<br>3. El sistema muestra el formulario con los campos requeridos.<br>4. El jugador selecciona posición, dorsal, foto y disponibilidad.<br>5. El jugador confirma la creación.<br>6. El sistema valida tipo y tamaño de la imagen mediante el patrón Strategy.<br>7. El sistema almacena la imagen en MongoDB y persiste el perfil en PostgreSQL.<br>8. El sistema muestra un mensaje de confirmación. |
| **Diagrama de caso de uso** | <img width="474" height="236" alt="image" src="https://github.com/user-attachments/assets/ad68cdf2-330e-4118-b659-e4493ee188b6" />|
| **Poscondiciones** | *Se espera como resultado que el perfil deportivo quede creado, disponible para búsqueda por capitanes, y que la foto quede almacenada en MongoDB.* |

### 2.5 Requerimiento Funcional 5

| Campo | Descripción |
|------|-------------|
| **ID** | RF-05 |
| **Nombre del requerimiento** | Actualización del perfil deportivo |
| **Descripción** | *El sistema debe permitir al jugador actualizar los datos de su perfil deportivo, siempre y cuando no esté asignado a ningún equipo.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El jugador autenticado.<br>2. El jugador con un perfil deportivo previamente creado.<br>3. El jugador sin pertenencia a ningún equipo.<br>4. El Servicio de Equipos disponible. |
| **Actor** | *Jugador.* |
| **Flujo principal** | 1. El jugador ingresa a "Mi Perfil Deportivo".<br>2. El jugador selecciona "Editar".<br>3. El sistema consulta al Servicio de Equipos para verificar la no pertenencia a un equipo.<br>4. El sistema habilita el formulario con los datos precargados.<br>5. El jugador modifica los campos deseados y confirma.<br>6. Si se cambió la foto, el sistema reemplaza la imagen en MongoDB.<br>7. El sistema persiste los cambios y muestra un mensaje de confirmación. |
| **Diagrama de caso de uso** | <img width="473" height="231" alt="image" src="https://github.com/user-attachments/assets/21c63642-846b-40aa-8846-3aec8fcdadd4" />|
| **Poscondiciones** | *Se espera como resultado que el perfil deportivo quede actualizado en PostgreSQL y, si aplica, que la foto anterior se elimine y la nueva se registre en MongoDB.* |

### 2.6 Requerimiento Funcional 6

| Campo | Descripción |
|------|-------------|
| **ID** | RF-06 |
| **Nombre del requerimiento** | Consulta del perfil deportivo |
| **Descripción** | *El sistema debe permitir a jugadores, capitanes y organizadores consultar el perfil deportivo de cualquier jugador del torneo.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El actor autenticado.<br>2. El jugador objetivo con un perfil deportivo creado.<br>3. La colección de fotos en MongoDB disponible. |
| **Actor** | *Jugador, capitán u organizador.* |
| **Flujo principal** | 1. El actor ingresa a la aplicación.<br>2. El actor navega al perfil del jugador que desea consultar.<br>3. El sistema recupera los datos del perfil desde PostgreSQL.<br>4. El sistema recupera la imagen desde MongoDB usando el identificador asociado.<br>5. El sistema muestra el perfil con foto, posición, dorsal y disponibilidad. |
| **Diagrama de caso de uso** | <img width="472" height="490" alt="image" src="https://github.com/user-attachments/assets/93765b5c-5231-4648-8732-e5a1ffc10ec1" />|
| **Poscondiciones** | *Se espera como resultado que el perfil deportivo quede visible en pantalla con los datos y la imagen actuales.* |

### 2.7 Requerimiento Funcional 7

| Campo | Descripción |
|------|-------------|
| **ID** | RF-07 |
| **Nombre del requerimiento** | Búsqueda de jugadores con filtros |
| **Descripción** | *El sistema debe permitir a un capitán buscar jugadores filtrando por posición, edad, género, nombre, identificación y/o semestre, retornando solo jugadores disponibles.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El capitán autenticado y con el rol CAPITÁN.<br>2. Al menos un jugador con perfil deportivo en el sistema.<br>3. El módulo de búsqueda habilitado. |
| **Actor** | *Capitán.* |
| **Flujo principal** | 1. El capitán ingresa a la sección "Buscar jugadores".<br>2. El sistema muestra el panel de filtros.<br>3. El capitán define los filtros que desea aplicar.<br>4. El capitán ejecuta la búsqueda.<br>5. El sistema combina los filtros con lógica AND mediante el patrón Specification.<br>6. El sistema retorna los resultados paginados, considerando solo jugadores disponibles.<br>7. El capitán visualiza las tarjetas de jugador y navega entre páginas. |
| **Diagrama de caso de uso** | <img width="477" height="241" alt="image" src="https://github.com/user-attachments/assets/84226501-a7e6-4077-a6ee-453a0f1c26d2" />|
| **Poscondiciones** | *Se espera como resultado que el capitán obtenga un listado paginado de candidatos que cumplan todos los filtros aplicados.* |

### 2.8 Requerimiento Funcional 8

| Campo | Descripción |
|------|-------------|
| **ID** | RF-08 |
| **Nombre del requerimiento** | Recepción de invitaciones |
| **Descripción** | *El sistema debe permitir a un jugador recibir y visualizar las invitaciones que le han sido enviadas por los capitanes.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El jugador autenticado.<br>2. El jugador con un perfil deportivo.<br>3. Al menos un capitán que haya enviado una invitación al jugador.<br>4. La entidad Invitation disponible en la base de datos. |
| **Actor** | *Jugador.* |
| **Flujo principal** | 1. El jugador ingresa a la aplicación.<br>2. El jugador navega a la sección "Mis invitaciones".<br>3. El sistema consulta las invitaciones asociadas al jugador.<br>4. El sistema muestra cada invitación con equipo, capitán y fecha.<br>5. El jugador puede filtrar por estado (pendiente, aceptada, rechazada). |
| **Diagrama de caso de uso** | <img width="472" height="230" alt="image" src="https://github.com/user-attachments/assets/62c36cc4-30f8-4103-87d4-8b314d793a66" />|
| **Poscondiciones** | *Se espera como resultado que el jugador visualice todas las invitaciones recibidas con sus datos asociados.* |

### 2.9 Requerimiento Funcional 9

| Campo | Descripción |
|------|-------------|
| **ID** | RF-09 |
| **Nombre del requerimiento** | Aceptación o rechazo de invitaciones |
| **Descripción** | *El sistema debe permitir a un jugador aceptar o rechazar una invitación, garantizando que un jugador pertenezca a un único equipo.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El jugador autenticado.<br>2. Una invitación dirigida al jugador en estado PENDIENTE.<br>3. El Servicio de Equipos disponible. |
| **Actor** | *Jugador.* |
| **Flujo principal** | 1. El jugador ingresa a "Mis invitaciones".<br>2. El jugador selecciona una invitación pendiente.<br>3. El jugador da clic en "Aceptar" o "Rechazar".<br>4. El sistema muestra un modal de confirmación.<br>5. El jugador confirma la acción.<br>6. El sistema aplica el patrón State para validar la transición de estado.<br>7. Si la acción es aceptar, el sistema verifica que el jugador no pertenezca a otro equipo y notifica al Servicio de Equipos.<br>8. El sistema actualiza el estado de la invitación.<br>9. El sistema muestra un mensaje de confirmación. |
| **Diagrama de caso de uso** | <img width="473" height="230" alt="image" src="https://github.com/user-attachments/assets/9b8eee4b-0628-4d09-9983-2f52bc6ec73a" />|
| **Poscondiciones** | *Se espera como resultado que la invitación quede en estado ACEPTADA o RECHAZADA y, si fue aceptada, que el jugador aparezca como miembro del equipo correspondiente.* |

### 2.10 Requerimiento Funcional 10

| Campo | Descripción |
|------|-------------|
| **ID** | RF-10 |
| **Nombre del requerimiento** | Consulta del historial de auditoría |
| **Descripción** | *El sistema debe permitir al administrador consultar el historial de eventos de auditoría con filtros por fecha, usuario y tipo de acción.* |
| **Precondiciones** | *Para que el sistema cumpla con este requerimiento, TECHCUP debe tener previamente:*<br>1. El administrador autenticado.<br>2. El rol ADMINISTRADOR asignado.<br>3. La tabla de auditoría con registros generados por el sistema. |
| **Actor** | *Administrador.* |
| **Flujo principal** | 1. El administrador ingresa al panel administrativo.<br>2. Selecciona la opción "Consola de auditoría".<br>3. El sistema presenta la tabla de eventos con los registros más recientes.<br>4. El administrador define filtros opcionales (fecha, actor o tipo de acción).<br>5. El sistema aplica los filtros y refresca el listado paginado.<br>6. El administrador revisa los eventos. |
| **Diagrama de caso de uso** |<img width="479" height="237" alt="image" src="https://github.com/user-attachments/assets/94b14d6a-7a90-4655-9c78-1a2b191f42d6" />|
| **Poscondiciones** | *Se espera como resultado que el administrador visualice el historial filtrado de los eventos sin alterarlos, ya que los registros son inmutables.* |
