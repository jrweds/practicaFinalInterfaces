package bean;

import java.util.Date;

public class Pago {
    private int idPago;
    private int idFactura;
    private double monto;
    private String metodoPago;
    private Date fecha;

    public Pago(int idPago, int idFactura, double monto, String metodoPago, Date fecha) {
        this.idPago = idPago;
        this.idFactura = idFactura;
        this.monto = monto;
        this.metodoPago = metodoPago;
        this.fecha = fecha;
    }

    // Getters y Setters
    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(int idFactura) {
        this.idFactura = idFactura;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    @Override
    public String toString() {
        return "Pago{" +
                "idPago=" + idPago +
                ", idFactura=" + idFactura +
                ", monto=" + monto +
                ", metodoPago='" + metodoPago + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
