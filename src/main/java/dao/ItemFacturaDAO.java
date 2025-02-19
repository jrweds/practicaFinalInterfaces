package dao;

import bean.ItemFactura;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemFacturaDAO {
    private Connection conexion;

    public ItemFacturaDAO() {
        this.conexion = ConexionBD.getConnection(); // ✅ Usa la conexión compartida
    }
    public boolean guardarItemFactura(int idFactura, ItemFactura item) {
        String sqlInsertItem = "INSERT INTO items_factura (id_factura, id_producto, cantidad, precio) VALUES (?, ?, ?, ?)";
        String sqlActualizarStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ?";
        String sqlVerificarStock = "SELECT stock FROM productos WHERE id_producto = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmtVerificar = conn.prepareStatement(sqlVerificarStock);
             PreparedStatement stmtInsert = conn.prepareStatement(sqlInsertItem);
             PreparedStatement stmtStock = conn.prepareStatement(sqlActualizarStock)) {

            // Verificar si hay stock suficiente antes de registrar la venta
            stmtVerificar.setInt(1, item.getProducto().getIdProducto());
            ResultSet rs = stmtVerificar.executeQuery();

            if (rs.next()) {
                int stockDisponible = rs.getInt("stock");
                if (stockDisponible < item.getCantidad()) {
                    System.out.println("⚠️ No hay suficiente stock para el producto: " + item.getProducto().getNombre());
                    return false;
                }
            } else {
                System.out.println("⚠️ Producto no encontrado en la base de datos.");
                return false;
            }

            // Insertar el ítem en la factura
            stmtInsert.setInt(1, idFactura);
            stmtInsert.setInt(2, item.getProducto().getIdProducto());
            stmtInsert.setInt(3, item.getCantidad());
            stmtInsert.setDouble(4, item.getPrecio());
            stmtInsert.executeUpdate();

            // Actualizar el stock del producto
            stmtStock.setInt(1, item.getCantidad());
            stmtStock.setInt(2, item.getProducto().getIdProducto());
            stmtStock.executeUpdate();

            System.out.println("✅ Stock actualizado para el producto: " + item.getProducto().getNombre());
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}




