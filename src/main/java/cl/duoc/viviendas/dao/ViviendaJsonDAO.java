package cl.duoc.viviendas.dao;

import cl.duoc.viviendas.model.Vivienda;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViviendaJsonDAO<T extends Vivienda> implements ViviendaDAO<T> {
    private final Path archivo;
    private final Type tipoLista;
    private final Gson gson;

    public ViviendaJsonDAO(Path archivo, Class<T> clase) {
        this.archivo = archivo;
        this.tipoLista = TypeToken.getParameterized(List.class, clase).getType();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public List<T> cargar() throws IOException {
        if (!Files.exists(archivo)) {
            return new ArrayList<>();
        }
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            List<T> leidas = gson.fromJson(lector, tipoLista);
            List<T> resultado = new ArrayList<>();
            if (leidas != null) {
                for (T vivienda : leidas) {
                    if (vivienda != null) {
                        resultado.add(vivienda);
                    }
                }
            }
            return resultado;
        } catch (JsonParseException e) {
            throw new IOException("el contenido de " + archivo.getFileName() + " no es válido.", e);
        }
    }

    @Override
    public void guardar(List<T> viviendas) throws IOException {
        Objects.requireNonNull(viviendas, "La lista de viviendas no puede ser nula.");
        try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            gson.toJson(viviendas, tipoLista, escritor);
        }
    }
}
