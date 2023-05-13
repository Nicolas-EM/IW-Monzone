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
- [!!!] Refactorizar transfers para reducir nº de peticiones
- [!] Validación en cliente en SignUp
- Un reimbursement no debería poder editarlo el usuario
- Arreglar el import.sql
- Hacer el README.md
- Logs
- TESTS
- Revisar actualizaciones por websockets en user
- [!!!] Refactorizar ADMIN (ahora tiene una funcionalidad rara)

## Mejoras vistas
- Hovers en home (sobre las cards y sobre el botón de añadir)
- Añadir orden a los grupos en home
- [!!] Los errores se están mostrando de manera muy cutre (hacerlo con toast notification)
- [!!] Al adjuntar una foto en un expense no se muestra, sólo cuando se guarda
- [!] Ajustar decimales en perfil y en home
- La profile pic no se ajusta a un círculo
- En las cards de grupos (home) debería mostrarse el circulito rojo/verde del balance
- [!] El mark as read debería cambiar el color de la notif
- [!!] Añadir botón ir atrás en vista expense
- [!] En group_config y en perfil sale scrollbar vertical, no se ajusta
- En los miembros en group_config o expense, tu usuario debería aparecer como "You"