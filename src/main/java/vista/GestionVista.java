package vista;

import bean.*;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import controlador.ControladorGeneral;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class GestionVista extends JPanel {
    private JTable tablaDatos;
    private JButton btnAgregar, btnEliminar, btnActualizar, btnVerDetalles,btnDescargarPDF,btnRegistrarPago;
    private DefaultTableModel modelo;
    private ControladorGeneral controlador;
    private String tipo;
    private Usuario usuario;
    private JPanel panelBotones;
    // ✅ Etiqueta para mostrar el total de ventas




    // Elementos de la pestaña Ventas
    private JTable tablaVentas;
    private DefaultTableModel modeloVentas;
    private JComboBox<String> comboClientes, comboEstado;
    private JButton btnFiltrar, btnExportarExcel, btnExportarPDF, btnGenerarGrafico, btnMarcarPagado;
    private JTextField txtFecha;
    private JLabel lblTotalVentas;


    public GestionVista(String tipo, Usuario usuario) {
        this.tipo = tipo;
        this.usuario = usuario;
        setLayout(new BorderLayout());
        controlador = new ControladorGeneral();

        if (tipo.equals("Ventas")) {
            configurarVistaVentas();
        } else {
            configurarTabla();
            configurarEstilos();
            cargarDatos();

            panelBotones = new JPanel();
            panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
            panelBotones.setBackground(new Color(224, 234, 241)); // Color de fondo gris claro

            //   panelBotones.setBackground(Color.decode("#E3E3E3"));
            panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            btnAgregar = new JButton("Agregar " + tipo);
            btnEliminar = new JButton("Eliminar " + tipo);
            btnActualizar = new JButton("Actualizar Datos");
            btnVerDetalles = new JButton("Ver Detalles");
            btnDescargarPDF = new JButton("Descargar Factura PDF");
            btnRegistrarPago = new JButton("Registrar Pago"); // 🔥 Nuevo botón


            estilizarBoton(btnAgregar, "#007ACC", Color.WHITE);
            estilizarBoton(btnEliminar, "#D9534F", Color.WHITE);
            estilizarBoton(btnActualizar, "#5CB85C", Color.WHITE);
            estilizarBoton(btnVerDetalles, "#5BC0DE", Color.WHITE);
            estilizarBoton(btnDescargarPDF, "#F0AD4E", Color.WHITE);
            estilizarBoton(btnRegistrarPago, "#FF5733", Color.WHITE); // 🔥 Color para el botón de pagos


            // 🔹 Controlar la visibilidad de los botones según el rol del usuario
            if (!tipo.equals("Factura") || (usuario != null && !usuario.getRol().equals("CLIENTE"))) {
                panelBotones.add(btnAgregar);
                panelBotones.add(Box.createVerticalStrut(10));
                panelBotones.add(btnEliminar);
            }

            if (tipo.equals("Producto")) {
                panelBotones.add(Box.createVerticalStrut(10));
                panelBotones.add(btnActualizar);
            }

            // 🔹 Restricción para los CLIENTES en la pestaña Facturas:
            if (tipo.equals("Factura")) {
                panelBotones.add(Box.createVerticalStrut(10));
                panelBotones.add(btnVerDetalles);
                panelBotones.add(Box.createVerticalStrut(10));
                panelBotones.add(btnDescargarPDF);

                // 🔹 Solo admins y vendedores pueden registrar pagos
                if (usuario != null && (usuario.getRol().equals("ADMIN") || usuario.getRol().equals("VENDEDOR"))) {
                    panelBotones.add(Box.createVerticalStrut(10));
                    panelBotones.add(btnRegistrarPago);
                }

                // ❌ Si el usuario es CLIENTE, NO puede agregar ni eliminar facturas.
                if (usuario.getRol().equals("CLIENTE")) {
                    btnAgregar.setVisible(false);
                    btnEliminar.setVisible(false);
                }
            }

            btnAgregar.addActionListener(e -> agregarDato());
            btnEliminar.addActionListener(e -> eliminarDato());
            btnActualizar.addActionListener(e -> actualizarDato());
            btnVerDetalles.addActionListener(e -> verDetalleFactura());
            btnDescargarPDF.addActionListener(e -> descargarFacturaPDF());
            btnRegistrarPago.addActionListener(e -> registrarPago()); // 🔥 Acción para registrar pago

            add(new JScrollPane(tablaDatos), BorderLayout.CENTER);
            add(panelBotones, BorderLayout.WEST);
        }
    }


    //////////////////VENTAS////////////////////////////////////////////////////////////////


    private void configurarVistaVentas() {
        setLayout(new BorderLayout());
        setBackground(Color.decode("#F5F5F5")); // Fondo claro

        // ✅ Modelo de la tabla con estilos
        modeloVentas = new DefaultTableModel(new String[]{"ID", "Cliente", "Fecha", "Total", "Estado"}, 0);
        tablaVentas = new JTable(modeloVentas);
        tablaVentas.setBackground(Color.WHITE);
        tablaVentas.setGridColor(Color.LIGHT_GRAY);
        tablaVentas.setRowHeight(25);
        tablaVentas.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaVentas.getTableHeader().setBackground(Color.decode("#007ACC"));
        tablaVentas.getTableHeader().setForeground(Color.WHITE);
        tablaVentas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));


        // ✅ Etiqueta para el total de ventas
        lblTotalVentas = new JLabel("💰 Total de Ventas: $0.00");
        lblTotalVentas.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotalVentas.setForeground(Color.decode("#007ACC"));
        lblTotalVentas.setHorizontalAlignment(SwingConstants.CENTER);


        cargarVentas();

        // ✅ Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBackground(Color.decode("#E3E3E3"));
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtFecha = new JTextField(10);
        txtFecha.setToolTipText("Formato: YYYY-MM-DD");

        comboClientes = new JComboBox<>();
        comboClientes.addItem("Todos");
        for (Usuario cliente : controlador.listarClientes()) {
            comboClientes.addItem(cliente.getNombre());
        }

        comboEstado = new JComboBox<>(new String[]{"Todos", "Pendiente", "Pagado", "Parcial"});

        btnFiltrar = new JButton("Filtrar");
        estilizarBoton(btnFiltrar, "#007ACC", Color.WHITE);
        btnFiltrar.addActionListener(e -> filtrarVentas());


        panelFiltros.add(new JLabel("👤 Cliente:"));
        panelFiltros.add(comboClientes);
        panelFiltros.add(new JLabel("📌 Estado:"));
        panelFiltros.add(comboEstado);
        panelFiltros.add(btnFiltrar);

        // ✅ Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new BoxLayout(panelBotones, BoxLayout.Y_AXIS));
        panelBotones.setBackground(new Color(224, 234, 241)); // Color de fondo gris claro
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnExportarExcel = new JButton("📊 Exportar Excel");
        btnExportarPDF = new JButton("📄 Exportar PDF");
        btnGenerarGrafico = new JButton("📈 Ver Estadísticas");
        btnMarcarPagado = new JButton("✅ Marcar como Pagado");

        estilizarBoton(btnExportarExcel, "#5CB85C", Color.WHITE);
        estilizarBoton(btnExportarPDF, "#F0AD4E", Color.WHITE);
        estilizarBoton(btnGenerarGrafico, "#5BC0DE", Color.WHITE);
        estilizarBoton(btnMarcarPagado, "#D9534F", Color.WHITE);

        btnExportarExcel.addActionListener(e -> exportarExcel());
        btnExportarPDF.addActionListener(e -> exportarPDF());
        btnGenerarGrafico.addActionListener(e -> mostrarGraficoVentas());
        btnMarcarPagado.addActionListener(e -> marcarComoPagado());

        panelBotones.add(btnExportarExcel);
        panelBotones.add(Box.createVerticalStrut(10));
        panelBotones.add(btnExportarPDF);
        panelBotones.add(Box.createVerticalStrut(10));
        panelBotones.add(btnGenerarGrafico);
        panelBotones.add(Box.createVerticalStrut(10));
        panelBotones.add(btnMarcarPagado);



        // ✅ Panel para la tabla y el total
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.add(new JScrollPane(tablaVentas), BorderLayout.CENTER);
        panelTabla.add(lblTotalVentas, BorderLayout.SOUTH);

        // ✅ Añadir los elementos al layout principal
        add(panelFiltros, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.WEST);
    }


    private void cargarVentas() {
        modeloVentas.setRowCount(0);
        List<Factura> facturas = controlador.obtenerHistorialVentas();

        double totalVentas = 0;

        for (Factura factura : facturas) {
            modeloVentas.addRow(new Object[]{
                    factura.getIdFactura(),
                    factura.getCliente() != null ? factura.getCliente().getNombre() : "Desconocido",
                    factura.getFecha(),
                    factura.getTotal(),
                    factura.getEstado()
            });

            totalVentas += factura.getTotal();
        }

        // ✅ Actualizar el total en la etiqueta
        lblTotalVentas.setText(String.format("💰 Total de Ventas: $%.2f", totalVentas));
    }


    private void filtrarVentas() {
        String fechaStr = txtFecha.getText().trim();
        String clienteSeleccionado = (String) comboClientes.getSelectedItem();
        String estadoSeleccionado = (String) comboEstado.getSelectedItem();

        // Si el usuario selecciona "Todos" en los filtros, enviamos `null`
        if ("Todos".equals(clienteSeleccionado)) {
            clienteSeleccionado = null;
        }
        if ("Todos".equals(estadoSeleccionado)) {
            estadoSeleccionado = null;
        }
        if (fechaStr.isEmpty()) {
            fechaStr = null;
        }

        // Limpiar la tabla antes de actualizarla
        modeloVentas.setRowCount(0);

        // Obtener la lista filtrada de facturas
        List<Factura> facturas = controlador.filtrarFacturas(fechaStr, null, clienteSeleccionado, estadoSeleccionado);

        // Verificar si la lista de facturas es nula o vacía
        if (facturas == null || facturas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se encontraron ventas con estos filtros.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return; // Salimos para evitar mostrar datos incorrectos
        }

        double totalFiltrado = 0;

        // Agregar las facturas filtradas al modelo de la tabla
        for (Factura factura : facturas) {
            modeloVentas.addRow(new Object[]{
                    factura.getIdFactura(),
                    factura.getCliente() != null ? factura.getCliente().getNombre() : "Desconocido",
                    factura.getFecha(),
                    factura.getTotal(),
                    factura.getEstado()
            });

            totalFiltrado += factura.getTotal();
        }

        // ✅ Actualizar el total de ventas filtradas
        lblTotalVentas.setText(String.format("💰 Total de Ventas: $%.2f", totalFiltrado));
    }





    private void marcarComoPagado() {
        int filaSeleccionada = tablaVentas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura.");
            return;
        }

        int idFactura = (int) modeloVentas.getValueAt(filaSeleccionada, 0);
        if (controlador.marcarFacturaComoPagada(idFactura)) {
            JOptionPane.showMessageDialog(this, "✅ Factura marcada como pagada.");
            cargarVentas(); // ✅ Recargar la tabla
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al actualizar factura.");
        }
    }

    private void exportarExcel() {
        String ruta = "ventas.xlsx"; // ✅ Agregar ruta
        if (controlador.exportarVentasExcel(ruta)) {
            JOptionPane.showMessageDialog(this, "✅ Ventas exportadas a Excel: " + ruta);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al exportar a Excel.");
        }
    }

    private void exportarPDF() {
        String ruta = "reporte_ventas.pdf"; // ✅ Agregar ruta
        if (controlador.exportarVentasPDF(ruta)) {
            JOptionPane.showMessageDialog(this, "✅ Ventas exportadas a PDF: " + ruta);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Error al exportar a PDF.");
        }
    }

    private void mostrarGraficoVentas() {
        controlador.mostrarGraficoVentas();
    }




    ////////////////////////////////////////////////////////////////////////////////////////

    private void configurarEstilos() {
        setBackground(Color.decode("#F5F5F5")); // Fondo claro
        tablaDatos.setBackground(Color.WHITE);
        tablaDatos.setGridColor(Color.LIGHT_GRAY);
        tablaDatos.setRowHeight(25);
        tablaDatos.setFont(new Font("Arial", Font.PLAIN, 14));
        tablaDatos.getTableHeader().setBackground(Color.decode("#007ACC"));
        tablaDatos.getTableHeader().setForeground(Color.WHITE);
        tablaDatos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    }

    private void estilizarBoton(JButton boton, String colorFondo, Color colorTexto) {
        boton.setBackground(Color.decode(colorFondo));
        boton.setForeground(colorTexto);
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    private void actualizarDato() {
        if (!tipo.equals("Producto")) return;
        int fila = tablaDatos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto para actualizar el stock.");
            return;
        }
        int idProducto = (int) modelo.getValueAt(fila, 0);
        String nuevoStockStr = JOptionPane.showInputDialog(this, "Ingrese el nuevo stock:");
        try {
            int nuevoStock = Integer.parseInt(nuevoStockStr);
            if (nuevoStock < 0) {
                JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.");
                return;
            }
            if (controlador.actualizarStock(idProducto, nuevoStock)) {
                JOptionPane.showMessageDialog(this, "Stock actualizado con éxito.");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar stock.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido.");
        }
    }

    private void configurarTabla() {
        switch (tipo) {
            case "Usuario":
                modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Apellido", "Email", "Rol"}, 0);
                break;
            case "Cliente":
                modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Apellido", "Email", "Teléfono"}, 0);
                break;
            case "Producto":
                modelo = new DefaultTableModel(new String[]{"ID", "Nombre", "Precio", "Stock"}, 0);
                break;
            case "Factura":
                modelo = new DefaultTableModel(new String[]{"ID", "Cliente", "Fecha", "Total", "Estado"}, 0);
                break;

            default:
                throw new IllegalArgumentException("Tipo no válido.");
        }
        tablaDatos = new JTable(modelo);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        switch (tipo) {
            case "Usuario":
                for (Usuario u : controlador.listarUsuarios()) {
                    modelo.addRow(new Object[]{u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol()});
                }
                break;
            case "Cliente":
                List<Usuario> clientes = controlador.listarClientes();
                System.out.println("Clientes obtenidos: " + clientes.size()); // Debugging: Imprime cuántos clientes hay en la lista
                for (Usuario c : clientes) {
                    System.out.println("Cliente: " + c.getNombre()); // Debugging: Imprime cada cliente en la consola
                    modelo.addRow(new Object[]{c.getId(), c.getNombre(), c.getApellido(), c.getEmail(), c.getTelefono()});
                }
                break;

            case "Producto":
                for (Producto p : controlador.listarProductos()) {
                    modelo.addRow(new Object[]{p.getIdProducto(), p.getNombre(), p.getPrecio(), p.getStock()});
                }
                break;
            case "Factura":
                List<Factura> facturas = usuario != null && usuario.getRol().equals("CLIENTE") ?
                        controlador.listarFacturasPorCliente(usuario.getId()) : controlador.listarFacturas();
                for (Factura f : facturas) {
                    String nombreCliente = (f.getCliente() != null) ? f.getCliente().getNombre() : "Desconocido";
                    modelo.addRow(new Object[]{f.getIdFactura(), nombreCliente, f.getFecha(), f.getTotal(), f.getEstado()});
                }
                break;

        }
    }

    private void agregarDato() {
        switch (tipo) {
            case "Usuario":
                agregarUsuario();
                break;
            case "Cliente":
                agregarCliente();
                break;
            case "Producto":
                agregarProducto();
                break;
            case "Factura":
                agregarFactura();
                break;
        }
        cargarDatos();
    }



    private void agregarUsuario() {
        // Crear panel contenedor con GridLayout
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        // Crear etiquetas y campos de texto
        panel.add(new JLabel("Nombre:"));
        JTextField nombreField = new JTextField();
        panel.add(nombreField);

        panel.add(new JLabel("Apellido:"));
        JTextField apellidoField = new JTextField();
        panel.add(apellidoField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Contraseña:"));
        JPasswordField passField = new JPasswordField();
        panel.add(passField);

        // Si el usuario es ADMIN, permitir seleccionar el rol
        String rol;
        if (usuario.getRol().equals("ADMIN")) {
            String[] opciones = {"ADMIN", "VENDEDOR", "CLIENTE"};
            rol = (String) JOptionPane.showInputDialog(
                    null, "Seleccione el rol:", "Seleccionar Rol",
                    JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (rol == null) {
                JOptionPane.showMessageDialog(null, "No se creó el usuario.");
                return;
            }
        } else {
            rol = "CLIENTE"; // Si el usuario no es ADMIN, solo puede crear CLIENTES
        }

        // Si el rol es CLIENTE, pedir el teléfono
        JTextField telefonoField = null;
        if (rol.equals("CLIENTE")) {
            panel.add(new JLabel("Teléfono:"));
            telefonoField = new JTextField();
            panel.add(telefonoField);
        }

        // Mostrar JOptionPane con el panel
        int result = JOptionPane.showConfirmDialog(null, panel, "Agregar Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Verificar si el usuario presionó "OK"
        if (result == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String telefono = (telefonoField != null) ? telefonoField.getText().trim() : null;

            // Validaciones
            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || pass.isEmpty() || (rol.equals("CLIENTE") && telefono.isEmpty())) {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Llamar al método para registrar el usuario
            boolean registrado = controlador.registrarUsuario(nombre, apellido, email, pass, rol, telefono);

            if (registrado) {
                JOptionPane.showMessageDialog(null, "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos(); // Recargar la tabla
            } else {
                JOptionPane.showMessageDialog(null, "Error al registrar el usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void agregarCliente() {
        // Crear panel contenedor con GridLayout
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));

        // Crear etiquetas y campos de texto
        panel.add(new JLabel("Nombre:"));
        JTextField nombreField = new JTextField();
        panel.add(nombreField);

        panel.add(new JLabel("Apellido:"));
        JTextField apellidoField = new JTextField();
        panel.add(apellidoField);

        panel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Contraseña:"));
        JPasswordField passField = new JPasswordField();
        panel.add(passField);

        panel.add(new JLabel("Teléfono:"));
        JTextField telefonoField = new JTextField();
        panel.add(telefonoField);

        // Mostrar JOptionPane con el panel
        int result = JOptionPane.showConfirmDialog(null, panel, "Agregar Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Verificar si el usuario presionó "OK"
        if (result == JOptionPane.OK_OPTION) {
            String nombre = nombreField.getText().trim();
            String apellido = apellidoField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String telefono = telefonoField.getText().trim();

            // Validar que los campos no estén vacíos
            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() || pass.isEmpty() || telefono.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Llamar al método para registrar el cliente
            boolean registrado = controlador.registrarUsuario(nombre, apellido, email, pass, "CLIENTE", telefono);

            if (registrado) {
                JOptionPane.showMessageDialog(null, "Cliente creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos(); // Recargar la tabla
            } else {
                JOptionPane.showMessageDialog(null, "Error al registrar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void agregarProducto() {
        // Crear panel contenedor
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        // Crear etiquetas y campos de texto
        panel.add(new JLabel("Nombre:"));
        JTextField nombreField = new JTextField();
        panel.add(nombreField);

        panel.add(new JLabel("Precio:"));
        JTextField precioField = new JTextField();
        panel.add(precioField);

        panel.add(new JLabel("Stock:"));
        JTextField stockField = new JTextField();
        panel.add(stockField);

        // Mostrar JOptionPane con el panel
        int result = JOptionPane.showConfirmDialog(null, panel, "Agregar Producto", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Verificar si el usuario presionó "OK"
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nombreField.getText();
                double precio = Double.parseDouble(precioField.getText());
                int stock = Integer.parseInt(stockField.getText());

                // Validar que los campos no estén vacíos
                if (nombre.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "El nombre no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Llamar al método para registrar el producto
                controlador.registrarProducto(nombre, precio, stock);
                JOptionPane.showMessageDialog(null, "Producto agregado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Ingrese valores válidos para Precio y Stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void registrarPago() {
        int fila = tablaDatos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura para registrar un pago.");
            return;
        }

        int idFactura = (int) modelo.getValueAt(fila, 0);
        Factura factura = controlador.obtenerFacturaPorId(idFactura);

        if (factura == null) {
            JOptionPane.showMessageDialog(this, "No se encontró la factura seleccionada.");
            return;
        }

        double totalFactura = factura.getTotal();
        double pagosRegistrados = controlador.obtenerPagosPorFactura(idFactura)
                .stream()
                .mapToDouble(Pago::getMonto)
                .sum();
        double saldoRestante = totalFactura - pagosRegistrados;

        // 🔹 Verificar si la factura ya está pagada
        if (saldoRestante <= 0) {
            JOptionPane.showMessageDialog(this, "Esta factura ya está completamente pagada.");
            return;
        }

        // 🔹 Solicitar el monto del pago
        String montoStr = JOptionPane.showInputDialog(this,
                String.format("Ingrese el monto del pago (Máximo: %.2f):", saldoRestante));

        try {
            double monto = Double.parseDouble(montoStr);

            // 🔹 Redondear el monto a dos decimales
            monto = Math.round(monto * 100.0) / 100.0;

            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto debe ser mayor a 0.");
                return;
            }

            if (monto > saldoRestante) {
                JOptionPane.showMessageDialog(this,
                        String.format("El monto excede el saldo pendiente. Máximo permitido: %.2f", saldoRestante));
                return;
            }

            // 🔹 Seleccionar el método de pago
            String[] opciones = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"};
            String metodoPago = (String) JOptionPane.showInputDialog(
                    this, "Seleccione el método de pago:", "Método de Pago",
                    JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

            if (metodoPago == null) return;

            // 🔹 Registrar el pago
            boolean pagoRegistrado = controlador.registrarPago(idFactura, monto, metodoPago);

            if (pagoRegistrado) {
                JOptionPane.showMessageDialog(this, "Pago registrado exitosamente.");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el pago.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido.");
        }
    }



    private void agregarFactura() {
        // Obtener lista de clientes (usuarios con rol "CLIENTE")
        List<Usuario> clientes = new ArrayList<>();
        for (Usuario u : controlador.listarUsuarios()) {
            if ("CLIENTE".equalsIgnoreCase(u.getRol())) {
                clientes.add(u);
            }
        }

        // Verificar si hay clientes disponibles
        if (clientes.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay clientes registrados.");
            return;
        }

        // Mostrar clientes con solo ID, Nombre y Apellido
        String[] opcionesClientes = new String[clientes.size()];
        for (int i = 0; i < clientes.size(); i++) {
            Usuario u = clientes.get(i);
            opcionesClientes[i] = "ID: " + u.getId() + ", " + u.getNombre() + " " + u.getApellido();
        }

        // Seleccionar un cliente
        String clienteSeleccionadoStr = (String) JOptionPane.showInputDialog(this,
                "Seleccione un cliente", "Agregar Factura", JOptionPane.QUESTION_MESSAGE,
                null, opcionesClientes, opcionesClientes[0]);

        if (clienteSeleccionadoStr == null) return;

        // Buscar el objeto Usuario correspondiente
        Usuario clienteSeleccionado = null;
        for (Usuario u : clientes) {
            if (clienteSeleccionadoStr.contains("ID: " + u.getId())) {
                clienteSeleccionado = u;
                break;
            }
        }

        // Selección de productos con posibilidad de edición o eliminación
        List<ItemFactura> items = new ArrayList<>();
        List<Producto> productos = controlador.listarProductos();

        while (true) {
            // Crear panel para mostrar los productos seleccionados con opción de eliminación
            JPanel panel = new JPanel(new BorderLayout(10, 10));

            // Mostrar los productos seleccionados en un JTextArea
            JTextArea listaProductos = new JTextArea(10, 30);
            listaProductos.setEditable(false);
            StringBuilder sb = new StringBuilder("Productos seleccionados:\n");
            for (int i = 0; i < items.size(); i++) {
                ItemFactura item = items.get(i);
                sb.append(i + 1).append(". ")
                        .append(item.getProducto().getNombre())
                        .append(" | Cantidad: ").append(item.getCantidad())
                        .append(" | Precio: ").append(item.getProducto().getPrecio()).append("\n");
            }
            listaProductos.setText(sb.toString());
            panel.add(new JScrollPane(listaProductos), BorderLayout.CENTER);

            // Agregar selección de productos
            panel.add(new JLabel("Seleccione un producto o elimine uno existente"), BorderLayout.NORTH);

            String[] opciones = new String[productos.size() + items.size()];
            int index = 0;

            // Agregar productos disponibles
            for (Producto p : productos) {
                opciones[index++] = "Agregar: " + p.getNombre();
            }

            // Agregar productos seleccionados con opción de eliminación
            for (int i = 0; i < items.size(); i++) {
                opciones[index++] = "Eliminar: " + items.get(i).getProducto().getNombre();
            }

            String seleccion = (String) JOptionPane.showInputDialog(this,
                    panel, "Administrar Productos en la Factura", JOptionPane.QUESTION_MESSAGE,
                    null, opciones, opciones[0]);

            if (seleccion == null) break; // Si el usuario cierra el cuadro, termina la selección

            // Si selecciona agregar un producto
            if (seleccion.startsWith("Agregar: ")) {
                String nombreProducto = seleccion.replace("Agregar: ", "");
                Producto productoSeleccionado = null;
                for (Producto p : productos) {
                    if (p.getNombre().equals(nombreProducto)) {
                        productoSeleccionado = p;
                        break;
                    }
                }

                if (productoSeleccionado == null) continue;

                // Pedir cantidad y permitir modificación
                String cantidadStr = JOptionPane.showInputDialog(this,
                        "Ingrese la cantidad de " + productoSeleccionado.getNombre() + ":");

                try {
                    int cantidad = Integer.parseInt(cantidadStr);
                    if (cantidad > 0) {
                        // Verificar si el producto ya está en la lista para modificar la cantidad
                        boolean productoExiste = false;
                        for (ItemFactura item : items) {
                            if (item.getProducto().getIdProducto() == productoSeleccionado.getIdProducto()) {
                                item.setCantidad(cantidad);
                                productoExiste = true;
                                break;
                            }
                        }
                        if (!productoExiste) {
                            items.add(new ItemFactura(0, productoSeleccionado, cantidad, productoSeleccionado.getPrecio()));
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que 0.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número válido.");
                }

                // Si selecciona eliminar un producto
            } else if (seleccion.startsWith("Eliminar: ")) {
                String nombreProducto = seleccion.replace("Eliminar: ", "");
                items.removeIf(item -> item.getProducto().getNombre().equals(nombreProducto));
            }
        }

        // Verificar si hay productos en la factura antes de proceder
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto.");
            return;
        }

        // **Mostrar resumen final antes de crear la factura**
        StringBuilder resumen = new StringBuilder("Factura para: " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido() + "\n\n");
        for (ItemFactura item : items) {
            resumen.append(item.getProducto().getNombre())
                    .append(" | Cantidad: ").append(item.getCantidad())
                    .append(" | Precio: ").append(item.getProducto().getPrecio()).append("\n");
        }

        int confirmar = JOptionPane.showConfirmDialog(this,
                new JScrollPane(new JTextArea(resumen.toString(), 10, 30)),
                "Confirmar Factura", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // **Solo crear la factura si el usuario presiona OK**
        if (confirmar == JOptionPane.OK_OPTION) {
            if (controlador.crearFactura(clienteSeleccionado, items)) {
                JOptionPane.showMessageDialog(this, "Factura creada con éxito.");
                cargarDatos();
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear la factura.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Factura cancelada.");
        }
    }


    private void eliminarDato() {
        int fila = tablaDatos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un " + tipo.toLowerCase() + " para eliminar.");
            return;
        }

        int id = (int) modelo.getValueAt(fila, 0);
        boolean eliminado = false;

        switch (tipo) {
            case "Usuario":
                eliminado = controlador.eliminarUsuario(id);
                break;
            case "Cliente":
                eliminado = controlador.eliminarCliente(id);
                break;
            case "Producto":
                eliminado = controlador.eliminarProducto(id); // 🔥 Corregido
                break;
            case "Factura":
                eliminado = controlador.eliminarFactura(id);
                break;
        }

        if (eliminado) {
            JOptionPane.showMessageDialog(this, tipo + " eliminado con éxito.");
            cargarDatos();
        } else {
            JOptionPane.showMessageDialog(this, "Error al eliminar " + tipo.toLowerCase() + ".");
        }
    }
    private void verDetalleFactura() {
        int fila = tablaDatos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura para ver detalles.");
            return;
        }

        int idFactura = (int) modelo.getValueAt(fila, 0);
        Factura factura = controlador.obtenerFacturaPorId(idFactura);

        if (factura == null) {
            JOptionPane.showMessageDialog(this, "No se encontraron detalles para esta factura.");
            return;
        }

        List<ItemFactura> items = factura.getItems();
        List<Pago> pagos = controlador.obtenerPagosPorFactura(idFactura);

        if (items == null || items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Esta factura no tiene productos asociados.");
            return;
        }

        // Construimos los detalles
        StringBuilder detalles = new StringBuilder("Factura ID: " + factura.getIdFactura() +
                "\nCliente: " + factura.getCliente().getNombre() +
                "\nFecha: " + factura.getFecha() +
                "\nTotal: $" + factura.getTotal() + "\n\nItems:\n");

        for (ItemFactura item : items) {
            detalles.append("- ").append(item.getProducto().getNombre())
                    .append(" x").append(item.getCantidad())
                    .append(" ($").append(item.getPrecio()).append(" c/u)\n");
        }


        // 🔹 Agregar pagos registrados a la factura
        if (!pagos.isEmpty()) {
            detalles.append("\n🔹 Pagos Realizados:\n");
            for (Pago pago : pagos) {
                detalles.append("- $").append(pago.getMonto()).append(" | ").append(pago.getMetodoPago())
                        .append(" | Fecha: ").append(pago.getFecha()).append("\n");
            }
        } else {
            detalles.append("\n🔹 No hay pagos registrados para esta factura.\n");
        }
        JOptionPane.showMessageDialog(this, detalles.toString(), "Detalles de Factura", JOptionPane.INFORMATION_MESSAGE);
        generarFacturaPDF(factura);

    }
    public static void generarFacturaPDF(Factura factura) {
        String destino = "factura_" + factura.getIdFactura() + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(new FileOutputStream(new File(destino)));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // **Encabezado - Datos de la Empresa**
            document.add(new Paragraph("🌿 TIENDA DE JARDINERÍA 🌿")
                    .setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Calle Primavera 123, Madrid, España")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Tel: +34 600 123 456 - Email: info@tiendajardineria.com")
                    .setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("NIF: B12345678\n\n").setTextAlignment(TextAlignment.CENTER));

            // **Título de la Factura**
            document.add(new Paragraph("FACTURA #" + factura.getIdFactura())
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.LEFT));

            // **Datos del Cliente**
            document.add(new Paragraph("Cliente: " + factura.getCliente().getNombre())
                    .setBold());
            document.add(new Paragraph("Fecha: " + factura.getFecha()));
            document.add(new Paragraph("Total: $" + factura.getTotal() + "\n\n"));

            // **Tabla con los productos de la factura**
            Table tabla = new Table(new float[]{4, 1, 2, 2});
            tabla.addCell(new Cell().add(new Paragraph("Producto")).setBold());
            tabla.addCell(new Cell().add(new Paragraph("Cantidad")).setBold());
            tabla.addCell(new Cell().add(new Paragraph("Precio Unitario")).setBold());
            tabla.addCell(new Cell().add(new Paragraph("Total")).setBold());

            List<ItemFactura> items = factura.getItems();
            for (ItemFactura item : items) {
                tabla.addCell(new Cell().add(new Paragraph(item.getProducto().getNombre())));
                tabla.addCell(new Cell().add(new Paragraph(String.valueOf(item.getCantidad()))));
                tabla.addCell(new Cell().add(new Paragraph("$" + item.getPrecio())));
                tabla.addCell(new Cell().add(new Paragraph("$" + (item.getCantidad() * item.getPrecio()))));
            }

            document.add(tabla);
            document.add(new Paragraph("\nGracias por su compra 🎍").setTextAlignment(TextAlignment.CENTER));

            document.close();
            System.out.println("✅ Factura PDF generada correctamente: " + destino);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void descargarFacturaPDF() {
        int fila = tablaDatos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura para descargar.");
            return;
        }

        int idFactura = (int) modelo.getValueAt(fila, 0);
        String origen = "factura_" + idFactura + ".pdf";
        File archivoFactura = new File(origen);

        if (!archivoFactura.exists()) {
            JOptionPane.showMessageDialog(this, "No se encontró el archivo de la factura.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Factura PDF");
        fileChooser.setSelectedFile(new File("factura_" + idFactura + ".pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File destino = fileChooser.getSelectedFile();
            try {
                Files.copy(archivoFactura.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Factura guardada en: " + destino.getAbsolutePath());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar la factura.");
                e.printStackTrace();
            }
        }

    }
}

