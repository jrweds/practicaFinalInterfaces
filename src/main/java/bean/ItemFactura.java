package bean;

public class ItemFactura {
    private int idItem;
    private Producto producto;
    private int cantidad;
    private double precio;

    public ItemFactura(int idItem, Producto producto, int cantidad, double precio) {
        this.idItem = idItem;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public int getIdItem() { return idItem; }
    public Producto getProducto() { return producto; }
    public int getCantidad() { return cantidad; }
    public double getPrecio() { return precio; }

    public void setCantidad(int cantidad) {
    }
}
