package cl.duoc.viviendas.controller;

import cl.duoc.viviendas.dao.ViviendaJsonDAO;
import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;
import cl.duoc.viviendas.service.ViviendaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainController {
    private static final String TIPO_DEPARTAMENTO = "Departamento";
    private static final String TIPO_CASA = "Casa";
    private static final String TEXTO_SELECCIONE = "Seleccione...";
    private static final String PREFIJO_CODIGO_DEPARTAMENTO = "PROP-D";
    private static final String PREFIJO_CODIGO_CASA = "PROP-C";

    private static final String ESTILO_EXITO = "-fx-text-fill: #1b7a2f; -fx-font-weight: bold;";
    private static final String ESTILO_ERROR = "-fx-text-fill: #c62828; -fx-font-weight: bold;";

    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtSuperficie;
    @FXML private TextField txtHabitaciones;

    @FXML private TitledPane panelDepartamento;
    @FXML private TextField txtPiso;
    @FXML private CheckBox chkGastoComunAlDia;
    @FXML private CheckBox chkEstacionamiento;

    @FXML private TitledPane panelCasa;
    @FXML private CheckBox chkPatio;

    @FXML private TableView<Vivienda> tblViviendas;
    @FXML private TableColumn<Vivienda, String> colTipo;
    @FXML private TableColumn<Vivienda, String> colCodigo;
    @FXML private TableColumn<Vivienda, String> colSuperficie;
    @FXML private TableColumn<Vivienda, String> colHabitaciones;
    @FXML private TableColumn<Vivienda, String> colCosto;

    @FXML private Label lblMensaje;

    private final ObservableList<Vivienda> datosTabla = FXCollections.observableArrayList();
    private final DecimalFormat formatoPesos = crearFormatoPesos();

    private final ViviendaService servicio = new ViviendaService(
            new ViviendaJsonDAO<>(Path.of("departamentos.json"), Departamento.class),
            new ViviendaJsonDAO<>(Path.of("casas.json"), Casa.class));

    @FXML
    private void initialize() {
        configurarSelectorTipo();
        configurarTabla();
        cargarArchivos();
    }

    private void configurarSelectorTipo() {
        cmbTipo.getItems().setAll(TIPO_DEPARTAMENTO, TIPO_CASA);

        cmbTipo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? TEXTO_SELECCIONE : item);
            }
        });
        cmbTipo.valueProperty().addListener((obs, anterior, actual) -> actualizarPanelesPorTipo());
        actualizarPanelesPorTipo();
    }

    private void configurarTabla() {
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));
        colSuperficie.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getSuperficie())));
        colHabitaciones.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getHabitaciones())));
        colCosto.setCellValueFactory(c ->
                new SimpleStringProperty(formatearPesos(c.getValue().calcularCostoArriendo())));

        String alineadoDerecha = "-fx-alignment: CENTER-RIGHT;";
        colSuperficie.setStyle(alineadoDerecha);
        colHabitaciones.setStyle(alineadoDerecha);
        colCosto.setStyle(alineadoDerecha);

        tblViviendas.setItems(datosTabla);
    }

    private void cargarArchivos() {
        try {
            servicio.cargar();
        } catch (IOException e) {
            mostrarError("Error al leer los archivos: " + e.getMessage());
        }
        refrescarTabla();
    }

    private void actualizarPanelesPorTipo() {
        String tipo = cmbTipo.getValue();
        panelDepartamento.setDisable(!TIPO_DEPARTAMENTO.equals(tipo));
        panelCasa.setDisable(!TIPO_CASA.equals(tipo));
    }

    @FXML
    private void onRegistrar() {
        try {
            Vivienda vivienda = construirViviendaDesdeFormulario();
            servicio.registrar(vivienda);
            refrescarTabla();
            limpiarFormulario();
            mostrarExito(vivienda.getCodigo() + " (" + vivienda.getTipo() + ") registrada correctamente.");
        } catch (DatoInvalidoException e) {
            mostrarError(e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarError("Dato no válido: " + e.getMessage());
        } catch (IOException e) {
            mostrarError("Error al guardar los archivos: " + e.getMessage());
        }
    }

    @FXML
    private void onEliminar() {
        Vivienda seleccionada = tblViviendas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarError("Debe seleccionar una vivienda de la tabla para eliminarla.");
            return;
        }
        try {
            servicio.eliminar(seleccionada);
            refrescarTabla();
            mostrarExito(seleccionada.getCodigo() + " (" + seleccionada.getTipo() + ") eliminada correctamente.");
        } catch (IOException e) {
            refrescarTabla();
            mostrarError("Error al guardar los archivos: " + e.getMessage());
        }
    }

    private Vivienda construirViviendaDesdeFormulario() throws DatoInvalidoException {
        String tipo = cmbTipo.getValue();
        if (tipo == null) {
            throw new DatoInvalidoException("Tipo: debe seleccionar un tipo de vivienda.");
        }

        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        if (codigo.isEmpty()) {
            throw new DatoInvalidoException("Código: no puede estar vacío.");
        }
        String prefijo = TIPO_DEPARTAMENTO.equals(tipo) ? PREFIJO_CODIGO_DEPARTAMENTO : PREFIJO_CODIGO_CASA;
        if (!codigo.matches(prefijo + "\\d{2}")) {
            throw new DatoInvalidoException("Código: debe tener el formato " + prefijo + "00 (por ejemplo, "
                    + prefijo + "01).");
        }
        if (servicio.existeCodigo(codigo)) {
            throw new DatoInvalidoException("Código: ya existe una vivienda registrada con el código " + codigo + ".");
        }

        double superficie = leerSuperficie();
        int habitaciones = leerEnteroPositivo(txtHabitaciones, "Habitaciones");

        if (TIPO_DEPARTAMENTO.equals(tipo)) {
            int piso = leerEnteroPositivo(txtPiso, "Piso");
            Departamento departamento = new Departamento(codigo, superficie, habitaciones, piso,
                    chkGastoComunAlDia.isSelected());
            if (chkEstacionamiento.isSelected()) {
                departamento.asignarEstacionamiento();
            }
            return departamento;
        }
        return new Casa(codigo, superficie, habitaciones, chkPatio.isSelected());
    }

    private double leerSuperficie() throws DatoInvalidoException {
        String mensaje = "Superficie: debe ser un número entre 20 y 500.";
        String texto = txtSuperficie.getText() == null ? "" : txtSuperficie.getText().trim().replace(',', '.');
        try {
            double valor = Double.parseDouble(texto);
            if (Double.isNaN(valor) || Double.isInfinite(valor) || valor < 20 || valor > 500) {
                throw new DatoInvalidoException(mensaje);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException(mensaje);
        }
    }

    private int leerEnteroPositivo(TextField campo, String nombre) throws DatoInvalidoException {
        String mensaje = nombre + ": debe ser un número entero mayor que cero.";
        String texto = campo.getText() == null ? "" : campo.getText().trim();
        try {
            int valor = Integer.parseInt(texto);
            if (valor <= 0) {
                throw new DatoInvalidoException(mensaje);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException(mensaje);
        }
    }

    private void refrescarTabla() {
        datosTabla.setAll(servicio.getViviendas());
    }

    private void limpiarFormulario() {
        cmbTipo.setValue(null);
        txtCodigo.clear();
        txtSuperficie.clear();
        txtHabitaciones.clear();
        txtPiso.clear();
        chkGastoComunAlDia.setSelected(false);
        chkEstacionamiento.setSelected(false);
        chkPatio.setSelected(false);
    }

    private void mostrarExito(String mensaje) {
        lblMensaje.setStyle(ESTILO_EXITO);
        lblMensaje.setText(mensaje);
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setStyle(ESTILO_ERROR);
        lblMensaje.setText(mensaje);
    }

    private String formatearPesos(double monto) {
        return "$" + formatoPesos.format(Math.round(monto));
    }

    private static DecimalFormat crearFormatoPesos() {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.of("es", "CL"));
        simbolos.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", simbolos);
    }

    private static class DatoInvalidoException extends Exception {
        DatoInvalidoException(String mensaje) {
            super(mensaje);
        }
    }
}
