package controlador;

import bean.*;
import dao.*;

import javax.swing.*;
import java.util.Date;
import java.util.List;

public class ControladorGeneral {
    private UsuarioDAO usuarioDAO;
    private ProductoDAO productoDAO;
    private FacturaDAO facturaDAO;
    private ItemFacturaDAO itemFacturaDAO;
    private PagoDAO pagoDAO;
    private VentaDAO ventaDAO;

    public ControladorGeneral() {
        usuarioDAO = new UsuarioDAO();
        productoDAO = new ProductoDAO();
        facturaDAO = new FacturaDAO();
        itemFacturaDAO = new ItemFacturaDAO();
        pagoDAO = new PagoDAO();
        ventaDAO = new VentaDAO();

    }


    // ✅ Obtener historial de ventas
    public List<Factura> obtenerHistorialVentas() {
        return ventaDAO.obtenerHistorialVentas();
    }

    // ✅ Filtrar facturas según los parámetros
    public List<Factura> filtrarFacturas(String fechaInicio, String fechaFin, String cliente, String estado) {
        java.sql.Date fecha = null;
        if (fechaInicio != null && !fechaInicio.isEmpty()) {
            try {
                fecha = java.sql.Date.valueOf(fechaInicio);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(null, "Formato de fecha incorrecto. Use YYYY-MM-DD.");
                return null;
            }
        }
        return ventaDAO.filtrarFacturas(fecha, cliente, estado);
    }

    // ✅ Marcar una factura como pagada
    public boolean marcarFacturaComoPagada(int idFactura) {
        return ventaDAO.marcarFacturaComoPagada(idFactura);
    }

    // ✅ Exportar ventas a Excel
    public boolean exportarVentasExcel(String rutaArchivo) {
        return ventaDAO.exportarVentasExcel(rutaArchivo);
    }

    // ✅ Exportar ventas a PDF
    public boolean exportarVentasPDF(String rutaArchivo) {
        return ventaDAO.exportarVentasPDF(rutaArchivo);
    }

    // ✅ Mostrar gráfico de ventas
    public void mostrarGraficoVentas() {
        ventaDAO.mostrarGraficoVentas();
    }





    // ✅ Gestión de Usuarios y Clientes (Unificados)
    public Usuario autenticarUsuario(String email, String pass) {
        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(email);
        if (usuario != null && usuario.getContraseña().equals(pass)) {
            return usuario;
        }
        JOptionPane.showMessageDialog(null, "Credenciales incorrectas.");
        return null;
    }

    public boolean registrarUsuario(String nombre, String apellido, String email, String pass, String rol, String telefono) {
        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || pass.isEmpty() || rol.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.");
            return false;
        }

        Usuario nuevoUsuario = new Usuario(0, nombre, apellido, email, pass, rol, telefono);
        return usuarioDAO.guardarUsuario(nuevoUsuario);
    }

    public boolean eliminarUsuario(int idUsuario) {
        return usuarioDAO.eliminarUsuario(idUsuario);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioDAO.obtenerTodosLosUsuarios();
    }

    // ✅ Gestión de Clientes (ahora dentro de `UsuarioDAO`)
    public boolean registrarCliente(String nombre, String apellido, String email, String pass, String telefono) {
        return registrarUsuario(nombre, apellido, email, pass, "CLIENTE", telefono);
    }

    public boolean actualizarCliente(int id, String nombre, String apellido, String email, String pass, String telefono) {
        Usuario clienteActualizado = new Usuario(id, nombre, apellido, email, pass, "CLIENTE", telefono);
        return usuarioDAO.actualizarUsuario(clienteActualizado);
    }

    public boolean eliminarCliente(int idCliente) {
        return usuarioDAO.eliminarUsuario(idCliente);
    }

    public List<Usuario> listarClientes() {
        List<Usuario> clientes = usuarioDAO.obtenerTodosLosClientes();
        System.out.println("Clientes obtenidos desde la BD: " + clientes.size()); // 🔥 Debug
        return clientes;
    }

    // ✅ Gestión de Productos
    public boolean registrarProducto(String nombre, double precio, int stock) {
        if (nombre.isEmpty() || precio <= 0 || stock < 0) {
            JOptionPane.showMessageDialog(null, "Datos inválidos. Verifique el nombre, precio y stock.");
            return false;
        }

        Producto nuevoProducto = new Producto(0, nombre, precio, stock);
        return productoDAO.guardarProducto(nuevoProducto);
    }

    public boolean actualizarStock(int idProducto, int nuevoStock) {
        if (nuevoStock < 0) {
            JOptionPane.showMessageDialog(null, "El stock no puede ser negativo.");
            return false;
        }
        return productoDAO.actualizarStock(idProducto, nuevoStock);
    }

    public List<Producto> listarProductos() {
        return productoDAO.obtenerTodosLosProductos();
    }

    public boolean eliminarProducto(int idProducto) {
        return productoDAO.eliminarProducto(idProducto);
    }

    // ✅ Gestión de Facturas
    public boolean crearFactura(Usuario cliente, List<ItemFactura> items) {
        if (cliente == null || items.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Datos insuficientes para generar la factura.");
            return false;
        }

        double total = items.stream().mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();

        // ✅ Verificar que hay suficiente stock antes de proceder
        for (ItemFactura item : items) {
            Producto producto = item.getProducto();
            int stockActual = productoDAO.obtenerStock(producto.getIdProducto());
            if (stockActual < item.getCantidad()) {
                JOptionPane.showMessageDialog(null, "No hay suficiente stock para " + producto.getNombre() + ". Stock actual: " + stockActual);
                return false; // ❌ No se puede vender si no hay stock suficiente
            }
        }


        Factura nuevaFactura = new Factura(0, cliente, new Date(), total, "PENDIENTE",items);

        System.out.println("Intentando guardar factura: Cliente = " + cliente.getNombre() + ", Total = " + total);

        boolean facturaCreada = facturaDAO.guardarFactura(nuevaFactura);

        if (facturaCreada) {
            System.out.println("✅ Factura creada con ID: " + nuevaFactura.getIdFactura());

            // ✅ Actualizar el stock de los productos
            for (ItemFactura item : items) {
                productoDAO.reducirStock(item.getProducto().getIdProducto(), item.getCantidad());
                System.out.println("Stock actualizado para producto: " + item.getProducto().getNombre());
            }
        } else {
            System.out.println("⚠️ Error al guardar la factura.");
        }
        return facturaCreada;
    }

    public List<Factura> listarFacturas() {
        return facturaDAO.obtenerTodasLasFacturas();
    }

    public List<Factura> listarFacturasPorCliente(int idCliente) {
        return facturaDAO.obtenerFacturasPorCliente(idCliente);
    }

    public boolean eliminarFactura(int idFactura) {
        return facturaDAO.eliminarFactura(idFactura);
    }

    public Factura obtenerFacturaPorId(int idFactura) {
        return facturaDAO.obtenerFacturaPorId(idFactura);
    }

    public boolean registrarPago(int idFactura, double monto, String metodoPago) {
        if (monto <= 0 || metodoPago == null || metodoPago.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Monto inválido o método de pago vacío.");
            return false;
        }

        boolean registrado = pagoDAO.registrarPago(idFactura, monto, metodoPago);

        if (registrado) {
            JOptionPane.showMessageDialog(null, "Pago registrado exitosamente.");
            facturaDAO.actualizarEstadoFactura(idFactura); // ✅ Actualiza estado de la factura
        } else {
            JOptionPane.showMessageDialog(null, "Error al registrar el pago.");
        }
        return registrado;
    }


    // ✅ Obtener pagos de una factura específica
    public List<Pago> obtenerPagosPorFactura(int idFactura) {
        return pagoDAO.obtenerPagosPorFactura(idFactura);
    }

}


