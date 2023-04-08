# Funcionalidades:
- Navegación por todas las vistas
- Login-Logout
- Crear, Editar, Eliminar Grupo (Excepto imagen)
- Crear, Editar, Eliminar Gasto (Excepto imagen)
- Calculo balances y método de repago
- Eliminar a usuarios de grupos
- Notificationes de gastos e invitaciones
- Cuando se añade un gasto al grupo, se muestra en /group/ por WebSocket


# Lo que falta:
## General
- Guardar imágenes de Gasto/Grupo
- Registro de usuario
- Error en login
- Crear logs info en todo lo que se pueda
- Gestionar errores de AJAX (e.j. fallo al aceptar invitación)
- Validación de campos en cliente (Javascript)
- Al aceptar invitación, que te pida budget

## /group/
- Hacer vista de balances

## /user/config
- Calcular gastos mensuales (ALBERTO)
- Calcular gastos por categoría (ALBERTO)
- Arreglar vista de grupos

## AJAX + Websockets
- Eliminar usuario de grupo
  - Al eliminar, por websocket (/topic/group/{groupId}) se borre en todas las pantallas
- Cambiar presupuesto de grupo
- Cambios dentro del perfil
- Marcar notificaciones como leidas
- Eliminar invitación
- Aceptar invitación
  - A usuario que aceptó --> Mostrar grupo si está en /user/
  - A otros usuarios del grupo --> Actualizar /group/id/config si están en esa vista


# Mejoras visuales
## NAV
- Botones en panel de notificaciones
- Botones de invitación
  
## /group/
- Mejora en vista de deudas

## /user/
- Refactorizar para que las cartas se vean bien si no tienen descripción