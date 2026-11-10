package br.com.fiap.radar;

import java.time.Instant;
import java.util.Objects;

public record Medicao(String sensorId, TipoSensor tipo, double valor, Instant coletadaEm) {
    public Medicao {
        if (sensorId == null || sensorId.isBlank()) {
            throw new DadosInvalidosException("Informe o identificador do sensor.");
        }
        Objects.requireNonNull(tipo).validar(valor);
        Objects.requireNonNull(coletadaEm);
    }
}
