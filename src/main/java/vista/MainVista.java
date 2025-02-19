package vista;

import bean.Usuario;
import controlador.ControladorGeneral;
import dao.ConexionBD;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class MainVista extends JFrame {
    private JPanel panelLogin, panelPrincipal;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnCerrarSesion;
    private JTabbedPane tabbedPane;
    private ImageIcon iconoTienda;
    private Usuario usuario;
    private ControladorGeneral controlador;

    public MainVista() {
        setTitle("Tienda Jardinería - Sistema de Gestión - ");
        setIconImage(new ImageIcon("logo_tienda.png").getImage());

        setSize(750, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);

        controlador = new ControladorGeneral();
        mostrarLogin();
    }

    private void mostrarLogin() {
        panelLogin = new JPanel();
        panelLogin.setLayout(new BoxLayout(panelLogin, BoxLayout.Y_AXIS));
        panelLogin.setBackground(new Color(242, 246, 246)); // Color claro de fondo
        panelLogin.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Logo o imagen superior
        ImageIcon icono = new ImageIcon("logo_tienda.png"); // Ajusta la ruta según tu archivo
        Image img = icono.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        JLabel lblLogo = new JLabel(new ImageIcon(img));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("Iniciar Sesión");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(50, 50, 50));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtEmail = crearCampoTexto("Correo electrónico");
        txtPassword = crearCampoPassword("Contraseña");

        btnLogin = new JButton("Iniciar Sesión");
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setBackground(new Color(33, 150, 243)); // Azul moderno
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btnLogin.addActionListener(e -> iniciarSesion());

        panelLogin.add(Box.createVerticalStrut(20));
        panelLogin.add(lblLogo);
        panelLogin.add(Box.createVerticalStrut(10));
        panelLogin.add(lblTitulo);
        panelLogin.add(Box.createVerticalStrut(20));
        panelLogin.add(txtEmail);
        panelLogin.add(Box.createVerticalStrut(10));
        panelLogin.add(txtPassword);
        panelLogin.add(Box.createVerticalStrut(20));
        panelLogin.add(btnLogin);

        add(panelLogin, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JTextField crearCampoTexto(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(250, 30)); // Ancho de 250px, alto de 30px
        field.setMaximumSize(new Dimension(250, 30)); // Evita que se expanda demasiado
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), // Borde fino
                BorderFactory.createEmptyBorder(5, 10, 5, 10) // Relleno interno
        ));

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }

    private JPasswordField crearCampoPassword(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(250, 30));
        field.setMaximumSize(new Dimension(250, 30));
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (new String(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (new String(field.getPassword()).isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        return field;
    }



    private void iniciarSesion() {
        String email = txtEmail.getText();
        String pass = new String(txtPassword.getPassword());

        usuario = controlador.autenticarUsuario(email, pass);
        if (usuario != null) {
            JOptionPane.showMessageDialog(this, "Bienvenido " + usuario.getNombre());
            remove(panelLogin);
            mostrarPrincipal();
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas.");
        }
    }

    private void mostrarPrincipal() {
        panelPrincipal = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        // Evita que las pestañas sean desproporcionadas
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setPreferredSize(new Dimension(140, 130));


        iconoTienda = new ImageIcon("rounded_image.png");
        Image imagenRedimensionada = iconoTienda.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon iconoTienda = new ImageIcon(imagenRedimensionada);



        if (usuario.getRol().equalsIgnoreCase("ADMIN")) {
            tabbedPane.addTab("Usuarios", iconoTienda, new JScrollPane(new GestionVista("Usuario", usuario)), "Gestión de Usuarios");
            tabbedPane.addTab("Clientes", iconoTienda, new JScrollPane(new GestionVista("Cliente", usuario)), "Gestión de Clientes");
            tabbedPane.addTab("Productos", iconoTienda, new JScrollPane(new GestionVista("Producto", usuario)), "Gestión de Productos");
            tabbedPane.addTab("Facturas", iconoTienda, new JScrollPane(new GestionVista("Factura", usuario)), "Gestión de Facturas");
            tabbedPane.addTab("Ventas", iconoTienda, new JScrollPane(new GestionVista("Ventas", usuario)), "Resumen de Ventas"); // ✅ AGREGADO AQUÍ

        } else if (usuario.getRol().equalsIgnoreCase("VENDEDOR")) {
            tabbedPane.addTab("Clientes", iconoTienda, new JScrollPane(new GestionVista("Cliente", usuario)), "Gestión de Clientes");
            tabbedPane.addTab("Facturas", iconoTienda, new JScrollPane(new GestionVista("Factura", usuario)), "Gestión de Facturas");
        } else if (usuario.getRol().equalsIgnoreCase("CLIENTE")) {
            tabbedPane.addTab("Mis Facturas", iconoTienda, new JScrollPane(new GestionVista("Factura", usuario)), "Ver mis facturas");
        }

        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);

// Crear el botón de cerrar sesión con estilos
        btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setPreferredSize(new Dimension(150, 40)); // Tamaño del botón más controlado
        btnCerrarSesion.setBackground(new Color(255, 69, 0)); // Color rojo moderno
        btnCerrarSesion.setForeground(Color.WHITE); // Texto en blanco
        btnCerrarSesion.setFont(new Font("Arial", Font.BOLD, 14));
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        btnCerrarSesion.addActionListener(e -> cerrarSesion());

// Crear un panel centrado para el botón
        JPanel panelCerrarSesion = new JPanel();
        panelCerrarSesion.setLayout(new FlowLayout(FlowLayout.CENTER)); // Centrar el botón
        panelCerrarSesion.setBackground(new Color(245, 245, 245)); // Fondo igual al login
        panelCerrarSesion.add(btnCerrarSesion);

        panelPrincipal.add(panelCerrarSesion, BorderLayout.SOUTH);

        add(panelPrincipal, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void cerrarSesion() {
        ConexionBD.cerrarConexion();
        System.out.println("✅ Sesión cerrada correctamente.");
        remove(panelPrincipal);
        mostrarLogin();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainVista().setVisible(true));
    }
}
