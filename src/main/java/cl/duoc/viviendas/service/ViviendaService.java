package cl.duoc.viviendas.service;

import cl.duoc.viviendas.dao.ViviendaDAO;
import cl.duoc.viviendas.model.Casa;
import cl.duoc.viviendas.model.Departamento;
import cl.duoc.viviendas.model.Vivienda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViviendaService {
    private final ViviendaDAO<Departamento> departamentoDAO;
    private final ViviendaDAO<Casa> casaDAO;
    private final List<Vivienda> viviendas = new ArrayList<>();

    public ViviendaService(ViviendaDAO<Departamento> departamentoDAO, ViviendaDAO<Casa> casaDAO) {
        this.departamentoDAO = departamentoDAO;
        this.casaDAO = casaDAO;
    }

    public void cargar() throws IOException {
        viviendas.clear();
        List<String> errores = new ArrayList<>();
        try {
            viviendas.addAll(departamentoDAO.cargar());
        } catch (IOException e) {
            errores.add("departamentos: " + e.getMessage());
        }
        try {
            viviendas.addAll(casaDAO.cargar());
        } catch (IOException e) {
            errores.add("casas: " + e.getMessage());
        }
        if (!errores.isEmpty()) {
            throw new IOException(String.join(" | ", errores));
        }
    }

    public boolean existeCodigo(String codigo) {
        if (codigo == null) {
            return false;
        }
        String buscado = codigo.trim();
        for (Vivienda v : viviendas) {
            if (v.getCodigo().equalsIgnoreCase(buscado)) {
                return true;
            }
        }
        return false;
    }

    public void registrar(Vivienda vivienda) throws IOException {
        if (existeCodigo(vivienda.getCodigo())) {
            throw new IllegalArgumentException("Ya existe una vivienda con el código " + vivienda.getCodigo() + ".");
        }
        viviendas.add(vivienda);
        try {
            guardar();
        } catch (IOException e) {
            viviendas.remove(vivienda);
            throw e;
        }
    }

    public void eliminar(Vivienda vivienda) throws IOException {
        int posicion = viviendas.indexOf(vivienda);
        if (posicion < 0) {
            return;
        }
        viviendas.remove(posicion);
        try {
            guardar();
        } catch (IOException e) {
            viviendas.add(posicion, vivienda);
            throw e;
        }
    }

    public List<Vivienda> getViviendas() {
        return Collections.unmodifiableList(viviendas);
    }

    private void guardar() throws IOException {
        List<Departamento> departamentos = new ArrayList<>();
        List<Casa> casas = new ArrayList<>();
        for (Vivienda v : viviendas) {
            if (v instanceof Departamento d) {
                departamentos.add(d);
            } else if (v instanceof Casa c) {
                casas.add(c);
            }
        }
        departamentoDAO.guardar(departamentos);
        casaDAO.guardar(casas);
    }
}
