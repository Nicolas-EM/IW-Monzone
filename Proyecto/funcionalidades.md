# Funcionalidades:
- Navegación por todas las vistas
- Login-Logout
- Crear, Editar, Eliminar Grupo (Excepto imagen)
- Crear, Editar, Eliminar Gasto (Excepto imagen)
- Calculo balances y método de repago
- Eliminar a usuarios de grupos
- Notificationes de gastos e invitaciones


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
- Calcular gastos mensuales
- Calcular gastos por categoría
- Arreglar vista de grupos

## AJAX
- Eliminar usuario de grupo
- Cambiar presupuesto de grupo
- Cambios dentro del perfil
- Marcar notificaciones como leidas
- Eliminar invitación
- Aceptar invitación
  - A usuario que aceptó --> Mostrar grupo si está en /user/
  - A otros usuarios del grupo --> Actualizar /group/id/config si están en esa vista
- Cuando se añade un gasto al grupo, que se actualice /group/id sin tener que refrescar


# Mejoras visuales
## NAV
- Botones en panel de notificaciones
- Botones de invitación
  
## /group/
- Mejora en vista de deudas

## /group/new
- Arreglar CSS para que los botones Save/Cancel sean amarillos

## /user/
- Refactorizar para que las cartas se vean bien si no tienen descripción