package br.com.fiap.radar;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class MedicaoDAO {
    private final Banco banco;

    public MedicaoDAO(Banco banco) {
        this.banco = banco;
    }

    public void salvar(Medicao medicao) {
        String sql = "INSERT INTO medicoes (sensor_id, tipo, valor, coletada_em) VALUES (?, ?, ?, ?)";
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, medicao.sensorId());
            comando.setString(2, medicao.tipo().name());
            comando.setDouble(3, medicao.valor());
            comando.setObject(4, medicao.coletadaEm().atOffset(ZoneOffset.UTC));
            comando.executeUpdate();
        } catch (SQLException causa) {
            throw new FonteIndisponivelException("Não foi possível salvar a medição.", causa);
        }
    }

    public List<Medicao> recentes(String sensorId, int limite) {
        String sql = """
                SELECT sensor_id, tipo, valor, coletada_em
                FROM medicoes WHERE sensor_id = ? ORDER BY id DESC LIMIT ?
                """;
        try (var conexao = banco.abrir(); var comando = conexao.prepareStatement(sql)) {
            comando.setString(1, sensorId);
            comando.setInt(2, limite);
            List<Medicao> medicoes = new ArrayList<>();
            try (var linhas = comando.executeQuery()) {
                while (linhas.next()) {
                    medicoes.add(new Medicao(linhas.getString("sensor_id"),
                            TipoSensor.valueOf(linhas.getString("tipo")), linhas.getDouble("valor"),
                            linhas.getObject("coletada_em", OffsetDateTime.class).toInstant()));
                }
            }
            return List.copyOf(medicoes.reversed());
        } catch (SQLException causa) {
            throw new FonteIndisponivelException("Não foi possível consultar as medições.", causa);
        }
    }
}
