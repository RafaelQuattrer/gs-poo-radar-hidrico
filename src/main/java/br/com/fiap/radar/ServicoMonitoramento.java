package br.com.fiap.radar;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

public final class ServicoMonitoramento {
    public record Resultado(double valor, Optional<Alerta> alerta) {}

    private final DadosTerritorio territorio;
    private final MedicaoDAO medicoes;
    private final AlertaDAO alertas;
    private final Clock relogio;

    public ServicoMonitoramento(DadosTerritorio territorio, MedicaoDAO medicoes,
                                 AlertaDAO alertas, Clock relogio) {
        this.territorio = territorio;
        this.medicoes = medicoes;
        this.alertas = alertas;
        this.relogio = relogio;
    }

    public Medicao coletar(Sensor sensor) {
        if (sensor.verificarFuncionamento() == ComponenteTerritorial.Status.INDISPONIVEL) {
            throw new FonteIndisponivelException("Sensor indisponível: " + sensor.id() + ".");
        }
        Medicao medicao = new Medicao(sensor.id(), sensor.tipo(), sensor.lerValor(), relogio.instant());
        medicoes.salvar(medicao);
        return medicao;
    }

    public Resultado prever(Sensor sensor, EstrategiaPrevisao estrategia) {
        List<Medicao> historico = medicoes.recentes(sensor.id(), EstrategiaPrevisao.MINIMO_LEITURAS);
        double valor = estrategia.prever(historico);
        TipoSensor tipo = sensor.tipo();
        tipo.validar(valor);
        var limiares = territorio.limiares(tipo);

        if (valor <= limiares.critico()) {
            return emitir(sensor, estrategia, valor, NivelAlerta.CRITICO, limiares.critico());
        }
        if (valor <= limiares.alerta()) {
            return emitir(sensor, estrategia, valor, NivelAlerta.ALERTA, limiares.alerta());
        }
        if (valor <= limiares.atencao()) {
            return emitir(sensor, estrategia, valor, NivelAlerta.ATENCAO, limiares.atencao());
        }
        return new Resultado(valor, Optional.empty());
    }

    private Resultado emitir(Sensor sensor, EstrategiaPrevisao estrategia, double valor,
                             NivelAlerta nivel, double limiar) {
        Alerta alerta = new Alerta(sensor.id(), sensor.tipo(), nivel, valor, limiar,
                estrategia.nome(), relogio.instant());
        alertas.salvar(alerta);
        return new Resultado(valor, Optional.of(alerta));
    }
}
