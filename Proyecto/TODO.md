# Equipo Monzone
[Link al repositorio de GitHub](https://github.com/Nicolas-EM/IW-Monzone.git)

# Funcionalidades:
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

# Lo que falta:
## General
- [!!!] Refactorizar transfers para reducir nº de peticiones - BEA
- [!] Validación en cliente en SignUp
- Un reimbursement no debería poder editarlo el usuario
- Arreglar el import.sql
- Hacer el README.md
- Logs
- [!!!] TESTS
- Revisar actualizaciones por websockets en user
- [!!!] Refactorizar ADMIN (ahora tiene una funcionalidad rara)
- [P] Revisar DebtCalculator (o comentarlo, o cambiarlo)
- [P] Refactor excepciones: Enum
- [P] Factor común código comprobaciones existe grupo y pertenece
- [P] Estaría bien guardar transacciones hechas de deudas

## Mejoras vistas
- Hovers en home (sobre las cards y sobre el botón de añadir)
- Añadir orden a los grupos en home
- [!!] Los errores se están mostrando de manera muy cutre (hacerlo con toast notification)
- [!] Ajustar decimales en perfil y en home
- La profile pic no se ajusta a un círculo
- En las cards de grupos (home) debería mostrarse el circulito rojo/verde del balance
- [!] El mark as read debería cambiar el color de la notif
- [!] En group_config y en perfil sale scrollbar vertical, no se ajusta
- En los miembros en group_config o expense, tu usuario debería aparecer como "You"
- [P] Las notifs deben mostrar fecha
- [P] Revisar responsiveness en los gastos (group), la fecha se rompe

## Usabilidad
- [!!] Al adjuntar una foto en un expense no se muestra, sólo cuando se guarda
- [!!] Añadir botón ir atrás en vista expense
- [P] Las listas vacías deberían aparecer como "No tienes X todavía"
- [P] Añadir iconos info en los campos para saber qué son
- [P] Al cambiar foto usuario, cambiar también la del nav
- [P] Las operaciones destructivas deben pedir confirmación

## Seguridad
- [P] Muchos puntos por comprobar bien los accesos a los recursos ("check if X belongs to Y" y similares)
- [P] Puedes llamar a /settle para cancelar todas las deudas de un usuario