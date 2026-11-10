package br.com.fiap.radar;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class AlertaDAO {
    private final Banco banco;

    public AlertaDAO(Banco banco) {
        this.banco = banco;
    }

    public void salvar(Alerta alerta) {
        String sql = """
                INSERT INTO alertas (sensor_id, tipo, nivel, previsto, limiar, estrategia, emitido_em)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, alerta.sensorId());
            comando.setString(2, alerta.tipo().name());
            comando.setString(3, alerta.nivel().name());
            comando.setDouble(4, alerta.previsto());
            comando.setDouble(5, alerta.limiar());
            comando.setString(6, alerta.estrategia());
            comando.setObject(7, alerta.emitidoEm().atOffset(ZoneOffset.UTC));
            comando.executeUpdate();
        } catch (SQLException causa) {
            throw new FonteIndisponivelException("Não foi possível salvar o alerta.", causa);
        }
    }

    public List<Alerta> recentes(int limite) {
        String sql = """
                SELECT sensor_id, tipo, nivel, previsto, limiar, estrategia, emitido_em
                FROM alertas ORDER BY id DESC LIMIT ?
                """;
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, limite);
            List<Alerta> alertas = new ArrayList<>();
            try (var linhas = comando.executeQuery()) {
                while (linhas.next()) {
                    alertas.add(new Alerta(linhas.getString("sensor_id"),
                            TipoSensor.valueOf(linhas.getString("tipo")),
                            NivelAlerta.valueOf(linhas.getString("nivel")),
                            linhas.getDouble("previsto"), linhas.getDouble("limiar"),
                            linhas.getString("estrategia"),
                            linhas.getObject("emitido_em", OffsetDateTime.class).toInstant()));
                }
            }
            return List.copyOf(alertas);
        } catch (SQLException causa) {
            throw new FonteIndisponivelException("Não foi possível consultar os alertas.", causa);
        }
    }
}
