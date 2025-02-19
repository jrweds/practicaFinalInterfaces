package bean;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Factura {
    private int idFactura;
    private Usuario cliente; // Ahora usamos Usuario en lugar de Cliente
    private Date fecha;
    private double total;
    private String estado; // ✅ Nuevo atributo
    private List<ItemFactura> items;

    public Factura(int idFactura, Usuario cliente, Date fecha, double total, String estado, List<ItemFactura> items) {
        this.idFactura = idFactura;
        this.cliente = cliente;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
        this.items = items;
    }


    public Factura(int idFactura, Usuario cliente, Date fecha, double total, String estado) {
        this.idFactura = idFactura;
        this.cliente = cliente;
        this.fecha = fecha;
        this.total = total;
        this.estado = estado;
    }


    // Getter y Setter para ID
    public int getIdFactura() {
        return idFactura;
    }

    public String getEstado() {
        return estado;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    // Getter y Setter para Cliente (ahora Usuario)
    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    // Getter y Setter para Fecha
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    // Getter y Setter para Total
    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    // Getter y Setter para Items
    public List<ItemFactura> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<ItemFactura> items) {
        this.items = items;
    }
}
