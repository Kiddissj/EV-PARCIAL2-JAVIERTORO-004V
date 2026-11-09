package cl.duoc.viviendas.model;

public abstract class Vivienda {
    private String codigo;
    private double superficie;
    private int habitaciones;

    protected Vivienda() {
    }

    public Vivienda(String codigo, double superficie, int habitaciones) {
        setCodigo(codigo);
        setSuperficie(superficie);
        setHabitaciones(habitaciones);
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de propiedad no puede ser nulo ni vacío.");
        }
        this.codigo = codigo.trim();
    }

    public double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(double superficie) {
        if (superficie < 20 || superficie > 500) {
            throw new IllegalArgumentException("La superficie debe estar entre 20 y 500 metros cuadrados.");
        }
        this.superficie = superficie;
    }

    public int getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(int habitaciones) {
        if (habitaciones <= 0) {
            throw new IllegalArgumentException("El número de habitaciones debe ser mayor que cero.");
        }
        this.habitaciones = habitaciones;
    }

    public abstract double calcularCostoArriendo();

    public abstract String getTipo();

    @Override
    public String toString() {
        return "Código: " + codigo + " | Superficie: " + superficie + " m²";
    }
}
