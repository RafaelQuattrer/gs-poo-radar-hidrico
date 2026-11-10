package br.com.fiap.radar;

import java.time.Instant;

public record Alerta(String sensorId, TipoSensor tipo, NivelAlerta nivel,
                     double previsto, double limiar, String estrategia, Instant emitidoEm) {
    public String descrever() {
        return "%s | %s (%s): %.2f %s, no ou abaixo do limiar de %.2f %s. Estratégia: %s."
                .formatted(nivel.rotulo(), sensorId, tipo.nome(), previsto,
                        tipo.unidade(), limiar, tipo.unidade(), estrategia);
    }
}
