package dao;

import bean.Pago;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    // ✅ Registrar un pago en una factura
    public boolean registrarPago(int idFactura, double monto, String metodoPago) {
        String sqlInsertPago = "INSERT INTO pagos (id_factura, monto, metodo_pago) VALUES (?, ?, ?)";
        String sqlActualizarEstado = "UPDATE facturas SET estado = ? WHERE id_factura = ?";

        try (Connection conn = ConexionBD.getConnection()) {
            conn.setAutoCommit(false); // 🔥 Habilitamos transacción

            try (PreparedStatement stmtPago = conn.prepareStatement(sqlInsertPago)) {
                stmtPago.setInt(1, idFactura);
                stmtPago.setDouble(2, monto);
                stmtPago.setString(3, metodoPago);
                int filasAfectadas = stmtPago.executeUpdate();

                if (filasAfectadas > 0) {
                    // ✅ Obtener la suma total de pagos registrados
                    double totalPagado = obtenerTotalPagado(idFactura, conn);
                    double totalFactura = obtenerTotalFactura(idFactura, conn);

                    String nuevoEstado;
                    if (totalPagado >= totalFactura) {
                        nuevoEstado = "PAGADO";
                    } else if (totalPagado > 0) {
                        nuevoEstado = "PARCIAL";
                    } else {
                        nuevoEstado = "PENDIENTE";
                    }

                    // ✅ Actualizar el estado de la factura
                    try (PreparedStatement stmtEstado = conn.prepareStatement(sqlActualizarEstado)) {
                        stmtEstado.setString(1, nuevoEstado);
                        stmtEstado.setInt(2, idFactura);
                        stmtEstado.executeUpdate();
                    }

                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 🔹 Obtener el total pagado de una factura
    private double obtenerTotalPagado(int idFactura, Connection conn) throws SQLException {
        String sql = "SELECT SUM(monto) AS totalPagado FROM pagos WHERE id_factura = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("totalPagado");
            }
        }
        return 0;
    }

    // 🔹 Obtener el total de la factura
    private double obtenerTotalFactura(int idFactura, Connection conn) throws SQLException {
        String sql = "SELECT total FROM facturas WHERE id_factura = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0;
    }



    // ✅ Obtener los pagos de una factura
    public List<Pago> obtenerPagosPorFactura(int idFactura) {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT id_pago, monto, metodo_pago, fecha FROM pagos WHERE id_factura = ?";

        try (Connection conn = ConexionBD.getConnection(); // ✅ Obtiene una nueva conexión
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                pagos.add(new Pago(
                        rs.getInt("id_pago"),
                        idFactura,
                        rs.getDouble("monto"),
                        rs.getString("metodo_pago"),
                        rs.getTimestamp("fecha")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pagos;
    }
}
