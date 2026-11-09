package cl.duoc.viviendas.model;

public class Casa extends Vivienda {
    private static final double COSTO_BASE = 250000;
    private static final double RECARGO_PATIO = 0.10;

    private boolean tienePatio;

    public Casa() {
    }

    public Casa(String codigo, double superficie, int habitaciones, boolean tienePatio) {
        super(codigo, superficie, habitaciones);
        setTienePatio(tienePatio);
    }

    public boolean isTienePatio() {
        return tienePatio;
    }

    public void setTienePatio(boolean tienePatio) {
        this.tienePatio = tienePatio;
    }

    @Override
    public double calcularCostoArriendo() {
        if (tienePatio) {
            return COSTO_BASE + COSTO_BASE * RECARGO_PATIO;
        }
        return COSTO_BASE;
    }

    @Override
    public String getTipo() {
        return "Casa";
    }
}
