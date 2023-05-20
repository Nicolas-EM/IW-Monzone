# Equipo Monzone - Proyecto (Pre-Examen)

## Propuesta


## Diagrama BD
A continuación se listan las tablas que forman la Base de Datos de Monzone, con los atributos que contienen.
- User:
  - id - PK
  - enabled - Para bajas lógicas
  - name - Nombre
  - username - Nombre de usuario, único
  - password - Contraseña cifrada
  - roles - Lista de roles, puede ser {User,Admin}
- Group:
  - id - PK
  - enabled - Para bajas lógicas
  - name - Nombre
  - desc - Descripción
  - num_members - Número de miembros, atributo derivado (count(members))
  - tot_budget - Presupuesto grupal, atributo derivado (suma de los budget de cada miembro)
  - currency - Tipo de moneda, uno de {EUR,USD,GBP}
- Member:
  - user_id - Usuario que es miembro, PK
  - group_id - Grupo al que pertenece, PK
  - enabled - Para bajas lógicas
  - role - Rol del usuario en el grupo, puede ser uno de estos {User,Moderator}
  - budget - Presupuesto del miembro en ese grupo
  - balance - Dinero total que debe/le deben en un momento al miembro
- Debt:
  - group_id - Grupo en el que se da la deuda, PK
  - debtor_id - Usuario que debe dinero, PK
  - debt_owner_id - Usuario al que le deben dinero, PK
  - amount - Cuánto debe debtor a debt_owner
- Type:
  - id - PK
  - name - Nombre identificador de la categoría
- Expense:
  - id - PK
  - enabled - Para bajas lógicas
  - name - Nombre
  - desc - Descripción
  - amount - Cuánto ha costado el gasto
  - type_id - Categoría del gasto
  - date - Fecha en la que se produjo el gasto
  - paid_by_id - Usuario que pagó el gasto
- Participates:
  - expense_id - Gasto, PK
  - group_id - Grupo al que pertenece el gasto
  - user_id - Usuario que participa en el gasto, PK
- Notification:
  - id - PK
  - date_read - Fecha en la que se leyó la notificación
  - date_sent - Fecha en la que se envió la notificación
  - group_id - Grupo implicado en la notificación
  - type - Tipo de notificación (ver clase Notification.java)
  - message - Mensaje que contiene la notificación
  - recipient_id - Usuario destinatario
  - sender_id - Usuario emisor

## Usuarios y Roles
### Usuarios
En primer lugar, con el fichero import.sql se crean en la BD los siguientes usuarios, con sus correspondientes contraseñas:
- Username: a | Name: admin | Password: aa
- Username: b | Name: bonito | Password: aa
- Username: Nico | Name: Nicoooooo | Password: aa
- Username: Tester | Name: Tester | Password: aa
- Username: user4 | Name: User 4 | Password: aa
- Username: user5 | Name: User 5 | Password: aa
- Username: user6 | Name: User 6 | Password: aa
- Username: user7 | Name: User 7 | Password: aa
- Username: user8 | Name: User 8 | Password: aa
- Username: user9 | Name: User 9 | Password: aa
- Username: user10 | Name: User 10 | Password: aa
- Username: user11 | Name: User 11 | Password: aa
- Username: user12 | Name: User 12 | Password: aa
- Username: user13 | Name: User 13 | Password: aa

### Roles
En cuanto a tipos de usuario general, existen dos: USER y ADMIN. El usuario normal (USER) es aquel que puede utilizar la funcionalidad normal de la aplicación, formando parte de
grupos, gestionando gastos y deudas.. El administrador (ADMIN) tiene una funcionalidad extra, que permite visualizar todas las entidades que forman parte de la BD. Puede ver
una lista de todos los usuarios (activos o no) con su información (excepto contraseña, evidentemente), y una lista de grupos (activos o no) con su información, incluyendo los miembros
que forman/formaban parte de él, los gastos y las deudas que contiene.
-> En este caso, los usuarios administradores son a (admin) y Nico (Nicoooooo)

Por otro lado, en el contexto de la aplicación, existen a su vez dos maneras de "pertenecer" a un grupo: siendo usuario normal (USER) o siendo moderador (MODERATOR) del grupo.
El primero puede visualizar todo el contenido del grupo, así como crear/editar/borrar gastos, saldar deudas o editar su presupuesto personal dentro del grupo (también puede salirse
del grupo). El moderador, además de estas funcionalidades, tiene unos privilegios extra sobre el grupo: puede editar la configuración del grupo, invitar a miembros y eliminarlos,
y eliminar el grupo.

## Pruebas
Para la realización de pruebas se ha creado un usuario "Tester" que no pertenece a ningún grupo y en el usuario "b" se han añadido un grupo y un expense de forma permanente.
En cuanto a la implementación, se han elaborado 3 archivos .features para llevar a cabo las pruebas:

- login.feature, contiene escenarios relacionados con el inicio y fin de sesión:
    - Hacer login con distintos usuarios (entre ellos el admin)
    - Hacer logout

- principal.feature, contiene los escenarios básicos de navegación:
    - Entrar a un grupo
    - Entrar a la configuración de un grupo
    - Entrar en un gasto
    - Entrar en el perfil
        
- usage.feature, contiene los escenarios básicos de uso de la aplicación:
    - Crear un nuevo grupo y comprobar que se ha creado correctamente
    - Crear un gasto y comprobar que se ha creado correctamente
    - Invitar a un usuario a unirse a un grupo, unirse y entrar en el grupo

## Comentarios