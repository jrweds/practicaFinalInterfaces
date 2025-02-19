package dao;

import bean.Factura;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAO {

    // Obtener todas las facturas para el historial de ventas
    public List<Factura> obtenerHistorialVentas() {
        List<Factura> ventas = new ArrayList<>();
        String sql = "SELECT id_factura, fecha, total, estado FROM facturas";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ventas.add(new Factura(
                        rs.getInt("id_factura"),
                        null,
                        rs.getTimestamp("fecha"),
                        rs.getDouble("total"),
                        rs.getString("estado"),
                        new ArrayList<>()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ventas;
    }

    // Filtrar facturas por fecha, cliente o estado
    public List<Factura> filtrarFacturas(Date fecha, String cliente, String estado) {
        List<Factura> ventas = new ArrayList<>();
        String sql = "SELECT f.id_factura, f.fecha, f.total, f.estado, u.nombre " +
                "FROM facturas f JOIN usuarios u ON f.id_usuario = u.id_usuario " +
                "WHERE (? IS NULL OR f.fecha = ?) " +
                "AND (? IS NULL OR u.nombre LIKE ?) " +
                "AND (? IS NULL OR f.estado = ?)";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, fecha);
            stmt.setDate(2, fecha);
            stmt.setString(3, cliente);
            stmt.setString(4, "%" + cliente + "%");
            stmt.setString(5, estado);
            stmt.setString(6, estado);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventas.add(new Factura(
                        rs.getInt("id_factura"),
                        null,
                        rs.getTimestamp("fecha"),
                        rs.getDouble("total"),
                        rs.getString("estado"),
                        new ArrayList<>()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ventas;
    }

    // Marcar factura como pagada
    public boolean marcarFacturaComoPagada(int idFactura) {
        String sql = "UPDATE facturas SET estado = 'PAGADO' WHERE id_factura = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idFactura);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Exportar ventas a Excel
    public boolean exportarVentasExcel(String rutaArchivo) {
        String sql = "SELECT id_factura, fecha, total, estado FROM facturas";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Ventas");

            Row headerRow = sheet.createRow(0);
            String[] columnas = {"ID Factura", "Fecha", "Total", "Estado"};

            for (int i = 0; i < columnas.length; i++) {
                headerRow.createCell(i).setCellValue(columnas[i]);
            }

            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getInt("id_factura"));
                row.createCell(1).setCellValue(rs.getString("fecha"));
                row.createCell(2).setCellValue(rs.getDouble("total"));
                row.createCell(3).setCellValue(rs.getString("estado"));
            }

            FileOutputStream fileOut = new FileOutputStream(new File(rutaArchivo));
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Generar gráfico de ventas
    public void mostrarGraficoVentas() {
        String sql = "SELECT fecha, SUM(total) AS total_ventas FROM facturas GROUP BY fecha";

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dataset.addValue(rs.getDouble("total_ventas"), "Total Vendido", rs.getString("fecha"));
            }

            JFreeChart chart = ChartFactory.createLineChart(
                    "Tendencia de Ventas",
                    "Fecha",
                    "Total Vendido",
                    dataset
            );

            JFrame frame = new JFrame("Gráfico de Ventas");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new ChartPanel(chart));
            frame.pack();
            frame.setVisible(true);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Exportar ventas a PDF
    public boolean exportarVentasPDF(String rutaArchivo) {
        String sql = "SELECT id_factura, fecha, total, estado FROM facturas";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            PdfWriter writer = new PdfWriter(new FileOutputStream(new File(rutaArchivo)));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("📊 Reporte de Ventas 📊")
                    .setBold().setFontSize(18).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Fecha de generación: " + new Timestamp(System.currentTimeMillis()))
                    .setTextAlignment(TextAlignment.RIGHT));

            Table table = new Table(new float[]{2, 3, 2, 2});
            table.addCell(new Cell().add(new Paragraph("ID Factura").setBold()));
            table.addCell(new Cell().add(new Paragraph("Fecha").setBold()));
            table.addCell(new Cell().add(new Paragraph("Total").setBold()));
            table.addCell(new Cell().add(new Paragraph("Estado").setBold()));

            while (rs.next()) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(rs.getInt("id_factura")))));
                table.addCell(new Cell().add(new Paragraph(rs.getString("fecha"))));
                table.addCell(new Cell().add(new Paragraph("$" + rs.getDouble("total"))));
                table.addCell(new Cell().add(new Paragraph(rs.getString("estado"))));
            }

            document.add(table);
            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
