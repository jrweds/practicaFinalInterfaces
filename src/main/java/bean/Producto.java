package bean;

public class Producto {
    private int idProducto;
    private String nombre;
    private double precio;
    private int stock;

    public Producto(int idProducto, String nombre, double precio, int stock) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    public int getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    @Override
    public String toString() {
        return nombre + " - $" + precio; // Muestra "Maceta - $5.99" en lugar de "bean.Producto@xyz"
    }

}
