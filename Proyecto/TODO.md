# Equipo Monzone
[Link al repositorio de GitHub](https://github.com/Nicolas-EM/IW-Monzone.git)

# FUNCIONALIDADES
- Navegación por todas las vistas
- Login-Logout
- Registro
- Todas las funcionalidades principales
  - CRUD Grupo
  - CRUD Gasto
  - Cálculo deudas
  - Pago de deudas
  - Invitar miembros
  - Eliminar miembros, salir del grupo
- Notificaciones de gastos y grupos
- Notificaciones de invitaciones
- Actualizaciones por websockets en:
  - home
  - group (tanto expenses como balances)
  - expense
  - user

_______

# MEJORAS IMPLEMENTADAS PRE-EXAMEN
[P] - indica que se ha pedido por el profesor

## General
- Refactorizar transfers para reducir nº de peticiones
- [P] Factor común código comprobaciones existe grupo y pertenece
- Revisar actualizaciones por websockets en user
- [P] Revisar DebtCalculator (o comentarlo, o cambiarlo)
- [P] Refactor excepciones: Enum

## Errores [!!!]
- USER: Cuando se añaden grupos no se elimina el mensaje de lista vacía
- USER: El budget personal no sale
- No se comprueba que la fecha sea anterior a la actual en expense (servidor)
- No se comprueba que name y desc sea < x caracteres (servidor)
- USER: Cuando te eliminan de un grupo desaparece de tu lista, pero hay error en AJAX

## Mejoras vistas
- [!!] Los errores se están mostrando de manera muy cutre (hacerlo con toast notification)
- [P] Arreglar responsiveness en los gastos (group), la fecha se rompe
- La profile pic no se ajusta a un círculo
- En las cards de grupos (home) debería mostrarse el circulito rojo/verde del balance
- [P] Las notifs deben mostrar fecha
- Los botones de ir atrás podrían ser flechas en vez de "Back"
- [!] Ajustar decimales en perfil y en home
- En los miembros en group_config o expense, tu usuario debería aparecer como "You"
- Al crear expense no sale la foto por defecto
- [!] En group_config y en perfil sale scrollbar vertical, no se ajusta

## Usabilidad
- [P] Las operaciones destructivas deben pedir confirmación
- [!!] Al adjuntar una foto en un expense no se muestra, sólo cuando se guarda
- [!!] Añadir botón ir atrás en vista expense
- Añadir botón ir atrás en vista group_config
- [P] Al cambiar foto usuario, cambiar también la del nav
- [P] Las listas vacías deberían aparecer como "No tienes X todavía"
- [P] Añadir iconos info en los campos para saber qué son
- Debería salir msj éxito al guardar un grupo/gasto

## Seguridad
- [P] Puedes llamar a /settle para cancelar todas las deudas de un usuario

_______

# LO QUE FALTA

## General
- [!] Validación en cliente en SignUp
- Hacer el README.md
- Logs
- [!!!] TESTS
- [!!!] Refactorizar ADMIN (ahora tiene una funcionalidad rara)

## Errores [!!!]
- GROUP_CONFIG: Cuando vuelves a unirse a un grupo un usuario después de salirse/sacarle, se añade dos veces en la lista de miembros
  (al hacer F5 ya sale normal)
- Seguir testeando manualmente...

## Mejoras vistas
- Hovers en home (sobre las cards y sobre el botón de añadir)
- Añadir orden a los grupos en home
- [!] El mark as read debería cambiar el color de la notif