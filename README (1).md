# Sistema de Ventas y Facturación - Tienda de Jardinería

Este es un sistema de gestión de ventas y facturación diseñado para una tienda de jardinería. La aplicación permite registrar clientes, gestionar productos, realizar ventas a través de facturas y administrar pagos.

## Funcionalidades Principales

- Gestión de Usuarios: Permite crear, modificar y eliminar usuarios con diferentes roles: Administrador, Vendedor y Cliente.
- Gestión de Productos: Los productos pueden ser registrados, editados y eliminados con control de stock.
- Facturación: Se pueden generar facturas asociadas a clientes, con detalles de los productos comprados.
- Pagos: Se permite registrar pagos (totales o parciales) y actualizar el estado de la factura automáticamente.
- Descuentos: Se pueden aplicar cupones de descuento a las facturas.
- Reportes y Exportación: Generación de reportes de ventas y exportación de datos a formatos como PDF y Excel.

---

## Estructura del Proyecto

### 1. Modelo (`bean`)
Contiene las clases que representan las entidades del sistema:
- `Usuario.java`: Representa a los usuarios (Clientes, Vendedores y Administradores).
- `Producto.java`: Define los productos con sus atributos como precio y stock.
- `Factura.java`: Representa una factura con sus productos y estado de pago.
- `ItemFactura.java`: Detalla los productos dentro de una factura.
- `Pago.java`: Registra los pagos realizados en cada factura.

### 2. Acceso a Datos (`dao`)
Contiene las clases encargadas de la comunicación con la base de datos:
- `UsuarioDAO.java`: Gestiona la persistencia de usuarios en la base de datos.
- `ProductoDAO.java`: Realiza operaciones sobre los productos.
- `FacturaDAO.java`: Maneja la creación, eliminación y consulta de facturas, asegurando que las facturas sin cliente sigan visibles en la interfaz.
- `PagoDAO.java`: Registra y consulta pagos asociados a facturas.

### 3. Controladores (`controlador`)
Contiene la lógica de negocio que comunica la base de datos con la interfaz:
- `ControladorUsuarios.java`: Gestiona las operaciones relacionadas con los usuarios.
- `ControladorProductos.java`: Controla la gestión de productos.
- `ControladorFacturas.java`: Maneja la creación y modificación de facturas, implementando `LEFT JOIN` para mostrar facturas de clientes eliminados.
- `ControladorPagos.java`: Gestiona los pagos y actualización de estado de las facturas.

### 4. Interfaz Gráfica (`vista`)
Contiene las pantallas del sistema:
- `MainVista.java`: Interfaz principal con el menú de navegación.
- `GestionUsuariosVista.java`: Permite administrar usuarios.
- `GestionProductosVista.java`: Gestiona los productos de la tienda.
- `GestionFacturasVista.java`: Muestra y permite crear/editar facturas, asegurando que se muestren aquellas con clientes eliminados.
- `GestionPagosVista.java`: Permite registrar pagos y actualizar estados de facturas.

---

## Explicación de los Apartados Claves

### Gestión de Facturación
Las facturas pueden ser creadas y asociadas a clientes. En caso de eliminar un cliente, las facturas permanecerán en el sistema con el cliente asignado como "Desconocido".

### Pagos y Estados de Facturas
Cuando se registra un pago, se calcula automáticamente el total pagado y el estado de la factura cambia a `PENDIENTE`, `PARCIAL` o `PAGADO`.

### Cupones de Descuento
Se pueden aplicar cupones a las facturas para otorgar descuentos porcentuales o fijos.

### Reportes y Exportación
El sistema permite exportar reportes de facturación en formato PDF y Excel, facilitando la gestión y análisis de ventas.

---

## Tecnologías Utilizadas
- Java (Swing): Para la interfaz gráfica.
- MySQL: Base de datos relacional.
- JDBC: Para la conexión con MySQL.
- Apache POI: Para la generación de archivos Excel.
- iTextPDF: Para la generación de reportes en PDF.

---

## Instalación y Ejecución
1. Configurar la base de datos:
   - Crear la base de datos ejecutando el script SQL incluido en el proyecto.
   - Modificar `ConexionBD.java` con las credenciales de la base de datos.

2. Compilar y ejecutar el proyecto:
   ```sh
   javac -cp ".;lib/*" MainVista.java
   java -cp ".;lib/*" MainVista
   ```

3. Iniciar sesión:
   - Usuario por defecto: `admin@email.com`
   - Contraseña: `admin123`

---

## Próximas Mejoras
- Soporte para múltiples idiomas.
- Implementación de copias de seguridad automáticas.
- Notificaciones de stock bajo para productos.

---

Listo para gestionar tu tienda de jardinería de manera eficiente.
