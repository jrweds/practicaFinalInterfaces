package dao;

import bean.Factura;
import bean.ItemFactura;
import bean.Producto;
import bean.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FacturaDAO {
    private Connection conexion;

    public FacturaDAO() {
        this.conexion = ConexionBD.getConnection(); // ✅ Reutiliza la conexión compartida
    }

    public List<Factura> obtenerTodasLasFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT f.id_factura, f.fecha, f.total, f.estado, u.id_usuario, u.nombre " +
                "FROM facturas f " +
                "JOIN usuarios u ON f.id_usuario = u.id_usuario"; // ✅ Incluye estado

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        "", "", "", "CLIENTE"
                );

                Factura factura = new Factura(
                        rs.getInt("id_factura"),
                        usuario,
                        rs.getTimestamp("fecha"),
                        rs.getDouble("total"),
                        rs.getString("estado"), // ✅ Estado incluido
                        new ArrayList<>()
                );

                facturas.add(factura);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return facturas;
    }


    private boolean guardarItemFactura(int idFactura, ItemFactura item) {
        String sql = "INSERT INTO items_factura (id_factura, id_producto, cantidad, precio) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            stmt.setInt(2, item.getProducto().getIdProducto());
            stmt.setInt(3, item.getCantidad());
            stmt.setDouble(4, item.getPrecio());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                System.out.println("✅ Producto añadido a la factura: " + item.getProducto().getNombre() + " x" + item.getCantidad());
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean guardarFactura(Factura factura) {
        String sqlFactura = "INSERT INTO facturas (id_usuario, fecha, total) VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmtFactura = conn.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {

            stmtFactura.setInt(1, factura.getCliente().getId()); // Cliente ahora es un usuario
            stmtFactura.setTimestamp(2, new Timestamp(factura.getFecha().getTime()));
            stmtFactura.setDouble(3, factura.getTotal());

            int filas = stmtFactura.executeUpdate();
            if (filas > 0) {
                ResultSet rs = stmtFactura.getGeneratedKeys();
                if (rs.next()) {
                    int idFactura = rs.getInt(1);
                    factura.setIdFactura(idFactura);
                    System.out.println("✅ Factura creada con ID: " + idFactura);

                    // Guardar los productos en items_factura
                    for (ItemFactura item : factura.getItems()) {
                        guardarItemFactura(idFactura, item);
                    }


                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<Factura> obtenerFacturasPorCliente(int idUsuario) {
        List<Factura> facturas = new ArrayList<>();
        String sql = "SELECT f.id_factura, f.fecha, f.total, u.id_usuario, u.nombre " +
                "FROM facturas f " +
                "JOIN usuarios u ON f.id_usuario = u.id_usuario " +
                "WHERE f.id_usuario = ?"; // ✅ Corregido

        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre"),
                        "", "", "", "CLIENTE" // ✅ Cliente ahora es un Usuario
                );

                facturas.add(new Factura(
                        rs.getInt("id_factura"),
                        usuario,
                        rs.getTimestamp("fecha"),
                        rs.getDouble("total"),
                        null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return facturas;
    }


    public boolean eliminarFactura(int idFactura) {
        String sqlEliminarItems = "DELETE FROM items_factura WHERE id_factura = ?";
        String sqlEliminarFactura = "DELETE FROM facturas WHERE id_factura = ?";

        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmtItems = conn.prepareStatement(sqlEliminarItems)) {
                stmtItems.setInt(1, idFactura);
                int itemsEliminados = stmtItems.executeUpdate();
                System.out.println("✅ Ítems eliminados de la factura: " + itemsEliminados);
            }

            try (PreparedStatement stmtFactura = conn.prepareStatement(sqlEliminarFactura)) {
                stmtFactura.setInt(1, idFactura);
                int facturaEliminada = stmtFactura.executeUpdate();

                if (facturaEliminada > 0) {
                    conn.commit();
                    System.out.println("✅ Factura eliminada correctamente. ID: " + idFactura);
                    return true;
                } else {
                    conn.rollback();
                    System.out.println("⚠️ No se encontró la factura con ID: " + idFactura);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Error al eliminar la factura.");
        }
        return false;
    }

    public Factura obtenerFacturaPorId(int idFactura) {
        String sqlFactura = "SELECT f.id_factura, f.fecha, f.total, f.estado, u.id_usuario, u.nombre, u.apellido, u.email, u.rol " +
                "FROM facturas f " +
                "JOIN usuarios u ON f.id_usuario = u.id_usuario " +
                "WHERE f.id_factura = ?";


        String sqlItems = "SELECT i.id_item, i.id_producto, p.nombre, i.cantidad, i.precio " +
                "FROM items_factura i " +
                "JOIN productos p ON i.id_producto = p.id_producto " +
                "WHERE i.id_factura = ?";

        Factura factura = null;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmtFactura = conn.prepareStatement(sqlFactura);
             PreparedStatement stmtItems = conn.prepareStatement(sqlItems)) {

            stmtFactura.setInt(1, idFactura);
            ResultSet rsFactura = stmtFactura.executeQuery();

            if (rsFactura.next()) {
                Usuario usuario = new Usuario(
                        rsFactura.getInt("id_usuario"),
                        rsFactura.getString("nombre"),
                        rsFactura.getString("apellido"),
                        rsFactura.getString("email"),
                        "",
                        rsFactura.getString("rol")
                );

                factura = new Factura(
                        rsFactura.getInt("id_factura"),
                        usuario,
                        rsFactura.getTimestamp("fecha"),
                        rsFactura.getDouble("total"),
                        rsFactura.getString("estado"), // ✅ Asegurar que se pase el estado
                        new ArrayList<>() // ✅ Para evitar que la lista sea nula
                );


                System.out.println("🔍 Factura encontrada: " + factura.getIdFactura());
            }

            stmtItems.setInt(1, idFactura);
            ResultSet rsItems = stmtItems.executeQuery();

            if (factura != null) {
                while (rsItems.next()) {
                    Producto producto = new Producto(
                            rsItems.getInt("id_producto"),
                            rsItems.getString("nombre"),
                            0,
                            0
                    );

                    ItemFactura item = new ItemFactura(
                            rsItems.getInt("id_item"),
                            producto,
                            rsItems.getInt("cantidad"),
                            rsItems.getDouble("precio")
                    );

                    factura.getItems().add(item);
                    System.out.println("✅ Producto añadido: " + producto.getNombre() + " x" + item.getCantidad());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return factura;
    }


    public void actualizarEstadoFactura(int idFactura) {
        double totalPagado = 0;
        double totalFactura = 0;

        // ✅ 1️⃣ Obtener el total pagado por la factura
        String sqlTotalPagado = "SELECT SUM(monto) AS total_pagado FROM pagos WHERE id_factura = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlTotalPagado)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalPagado = rs.getDouble("total_pagado");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // ✅ 2️⃣ Obtener el total de la factura
        String sqlTotalFactura = "SELECT total FROM facturas WHERE id_factura = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlTotalFactura)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalFactura = rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // ✅ 3️⃣ Determinar el nuevo estado de la factura
        String nuevoEstado = (totalPagado == 0) ? "PENDIENTE" :
                (totalPagado >= totalFactura) ? "PAGADO" : "PARCIAL";

        // ✅ 4️⃣ Actualizar el estado de la factura
        String updateSql = "UPDATE facturas SET estado = ? WHERE id_factura = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmtUpdate = conn.prepareStatement(updateSql)) {

            stmtUpdate.setString(1, nuevoEstado);
            stmtUpdate.setInt(2, idFactura);
            stmtUpdate.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double obtenerTotalFacturaSeguro(int idFactura) {
        String sql = "SELECT total FROM facturas WHERE id_factura = ?";
        double total = 0;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }



}
