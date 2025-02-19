package bean;

public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String contraseña;
    private String rol;
    private String telefono; // Nuevo campo para clientes

    public Usuario(int id, String nombre, String apellido, String email, String contraseña, String rol, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contraseña = contraseña;
        this.rol = rol;
        this.telefono = telefono; // Puede ser NULL para admin/vendedor
    }

    public Usuario(int id, String nombre, String apellido, String email, String contraseña, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contraseña = contraseña;
        this.rol = rol;
        this.telefono = null; // No todos los usuarios tienen teléfono
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }

    public String getContraseña() {
        return contraseña;
    }

    public String getRol() {
        return rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String toString() {
        return "ID: " + id + ", Nombre: " + nombre + ", Apellido: " + apellido + ", Email: " + email +
                ", Rol: " + rol + (telefono != null ? ", Teléfono: " + telefono : "");
    }
}