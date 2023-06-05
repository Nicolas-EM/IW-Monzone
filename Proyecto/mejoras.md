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
