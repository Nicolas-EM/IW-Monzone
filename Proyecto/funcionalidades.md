# Funcionalidades:
- Navegación por todas las vistas
- Login-Logout
- Crear, Editar, Eliminar Grupo (Excepto imagen)
- Crear, Editar, Eliminar Gasto (Excepto imagen)
- Calculo balances y método de repago
- Eliminar a usuarios de grupos


# Lo que falta:
## General
- Notificationes de todo tipo (Solo se muestran notificaciones ya existentes, no se actualizan)
- Seguridad al crear una notificación
- Guardar imágenes de Gasto/Grupo
- Cambios de funcionalidades a AJAX (eliminar usuario de grupo, cambiar presupuesto de grupo, cambios dentro del perfil)
- Registro de usuario
- Validación de campos en cliente (Javascript)

## /group/
- Hacer vista de balances

## /group/{id}/config
- Invitar a usuario a grupo + notificar

## /user/config
- Calcular gastos mensuales
- Calcular gastos por categoría
- Arreglar vista de grupos


# Mejoras visuales
## /group/
- Mejora en vista de deudas

## /group/new
- Arreglar CSS para que los botones Save/Cancel sean amarillos

## /user/
- Refactorizar para que las cartas se vean bien si no tienen descripción