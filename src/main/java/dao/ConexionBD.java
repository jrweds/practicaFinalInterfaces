package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static Connection conexion;

    public static Connection getConnection() {
        try {
            if (conexion == null || conexion.isClosed()) { // 🔥 Verifica si está cerrada antes de reutilizarla
                conexion = DriverManager.getConnection("jdbc:mysql://localhost/tienda_jardineria?useSSL=false", "root", "root");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conexion;
    }


    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                conexion = null;
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
