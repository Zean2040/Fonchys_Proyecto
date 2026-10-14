# Probar la API con Postman

La aplicación debe estar encendida en `http://localhost:8080`. Para iniciarla
desde la carpeta `demo`, ejecuta `./mvnw.cmd spring-boot:run` y espera a que termine
el arranque. `GET /actuator/health` debe responder `200` con `{"status":"UP"}`.

1. Importa `Fonchys_API.postman_collection.json` en Postman y abre la colección
   **Fonchys Minimarket - API REST**. Usa sus peticiones, que ya incluyen los scripts.
2. Para descartar una sesión antigua, abre **Cookies** y elimina las cookies de
   `localhost` una vez, antes del paso 1a. En Settings de las peticiones,
   **Disable cookie jar** debe estar desactivado. Deja que Postman gestione las
   cookies; elimina cualquier encabezado `Cookie` que hayas escrito manualmente.
3. Envía **1a. GET /login (obtener CSRF)**. Devuelve HTML y guarda automáticamente
   el token del formulario en `fonchysCsrfToken`. Sus dos pruebas deben pasar.
4. Envía **1b. POST /login (iniciar sesion admin)**. Debe devolver `302` con
   `Location` terminado en `/dashboard`. Sus dos pruebas deben pasar. La colección
   desactiva las redirecciones automáticas en estas peticiones.
5. Envía **2. GET /api/productos (200)**. Debe devolver `200`, tipo
   `application/json` y un arreglo de productos. Sus tres pruebas deben pasar.

Las variables `loginEmail` y `loginPassword` contienen las credenciales de
demostración del administrador. Ajusta sus valores si cambiaste la contraseña.
La prueba del paso 1b espera el destino del rol ADMIN.

Conserva las cookies entre estos pasos y utiliza siempre el mismo `baseUrl`.
Después de reiniciar la aplicación o para repetir el login, vuelve a empezar por
1a. La configuración permite una sesión por usuario: iniciar sesión con la misma
cuenta en otro cliente puede invalidar la anterior.

Si 1b vuelve a `/login`, repite desde 1a con las cookies limpias. Si va a
`/login?error=true`, revisa las credenciales y que el usuario esté activo. Un `302`
por sí solo no confirma el login: también debe coincidir el destino.

Para comprobar solamente el login y la lectura de JSON, ejecuta los pasos 1a, 1b
y 2. Las peticiones siguientes incluyen operaciones que modifican productos y
registran ventas de prueba.
