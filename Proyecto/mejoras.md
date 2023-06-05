# MEJORAS SOBRE MONZONE - Entrega Post-Examen

## Miembros
Los miembros del equipo que han participado en estas mejoras han sido:
- Beatriz Espinar Aragón
- Nicolás Espinosa Mooser

## Mejoras

### Notificaciones
Propuesta del examen: Cuando aparece un mensaje, al hacer click en él abre el panel de notificaciones.
- Añadimos la funcionalidad de bootstrap en el renderToastNofif() de ajax-notifications.js

### Grupos en perfil
Propuesta del examen: En la vista de perfil, los grupos son botones que redirigen a ese grupo.
- En user.js, donde se cargan los grupos de manera automática, añadimos el rol de botón y la referencia del onclick en el renderGroup()

### Test
Propuesta del examen: Añadir un test que comprueba que la mejora anterior funciona, es decir, que un grupo de tu lista de grupos en el perfil redirige a la vista de ese grupo.
- Creamos un nuevo fichero groupButton.feature, donde escribimos el test, este llamará a principal.feature@profile para meterse en el perfil del usuario b (que pertenece al grupo 2). Luego pulsamos en la tarjeta correspondiente a ese grupo y esperamos que redirija a /group/2
- En ExternalRunner.java, indicamos que se ejecute el test creado

### Número gastos en admin
Propuesta del examen: En la vista de administrador, en la tarjeta de cada grupo muestra además del nombre el número de gastos de ese grupo.
- En admin.js, añadimos una petición (GET) para pedir el número de gastos de un grupo y en el renderGoup() añadimos el div donde se mostrará el nº de gastos
- En GroupController.java, añadimos un GetMapping que calcule los gastos que tiene un grupo. Para ello, obtenemos las filas de la relación Participates y utilizamos un Set para no repetir los gastos aunque más de un miembro participe en ellos (igual que se hace en AdminController en el GetMapping index)

### Rol de creador
Propuesta del examen: Indicar quién es el creador del grupo, que siempre será un moderador y no podrá ser eliminado del grupo.
- En Member.java, añadir un nuevo GroupRole: GROUP_CREATOR
- En los controladores, cuando se comprueba si es moderador, añadir "|| role == creator" (tiene los mismos permisos que un moderador)
- Añadir icono corona blanca al lado del miembro que sea el creador
- Al crear un grupo, se establece el rol del miembro a CREATOR en vez de MODERATOR

### Hacer moderador
Propuesta del examen: Permitir a un moderador marcar como moderador a otro miembro del grupo.
- En group_config.js, añadimos botón de corona no-marcada a los usuarios que no sean moderadores (y sólo si el usuario logeado es moderador), y corona marcada (amarilla) a los que sí sean moderadores
- En el mismo js, añadir función que hace la petición POST al servidor
- En GroupController.java, añadir un PostMapping /makeMod que hace moderador al usuario. Para ello, basta hacer las comprobaciones necesarias y cambiar su rol
- Crear tipo de notificación: GROUP_MODERATOR_ADDED
- En el js, si nos llega un websocket GROUP indicando que se ha añadido un moderador, y es el usuario logeado, entonces actualizamos los campos cambiando la variable isGroupAdmin a true

### Quitar moderador
Propuesta nuestra: Permitir a un moderador quitar a otro moderador sus privilegios (devolverlo a miembro estándar), él mismo incluido.
- En group_config.js, la corona marcada es un botón con su correspondiente modal de confirmación
- En el mismo js, añadir función que hace la petición POST al servidor
- En GroupController.java, añadir un PostMapping /removeMod que quita el rol de moderador al usuario. Para ello, basta hacer las comprobaciones necesarias y cambiar su rol
- Crear tipo de notificación: GROUP_MODERATOR_REMOVED
- En el js, si nos llega un websocket GROUP indicando que se ha eliminado un moderador, y es el usuario logeado, entonces actualizamos los campos cambiando la variable isGroupAdmin a false

### Mejoras visuales
- Botones ir atrás en expense y group_config
- Disposición botones en user y group_config (tanto new como edit)
- Arreglo botón logout en nav cuando estás en user (salía rojo)
- En la lista de miembros de un grupo (group_config) tu usuario "You" sale en negrita
- Lista de miembros en group_config con mejor espaciado