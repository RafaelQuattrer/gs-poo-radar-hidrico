package br.com.fiap.radar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonitoramentoTest {
    private static final Instant INSTANTE = Instant.parse("2026-11-10T12:00:00Z");

    @Test
    void rejeitaMedicaoNegativa() {
        assertThrows(DadosInvalidosException.class,
                () -> new Medicao("CH-01", TipoSensor.CHUVA, -1, INSTANTE));
    }

    @Test
    void rejeitaAlteracaoComCodigoInvalido() {
        var territorio = new DadosTerritorio("Cantareira", "correto");
        var original = territorio.limiares(TipoSensor.VOLUME);
        var novos = new DadosTerritorio.Limiares(50, 40, 20);

        assertThrows(DadosInvalidosException.class,
                () -> territorio.alterarLimiares("incorreto", TipoSensor.VOLUME, novos));
        assertEquals(original, territorio.limiares(TipoSensor.VOLUME));
    }

    @Test
    void atualizaLimiaresValidosComCodigoCorreto() {
        var territorio = new DadosTerritorio("Cantareira", "correto");
        var novos = new DadosTerritorio.Limiares(50, 40, 20);

        territorio.alterarLimiares("correto", TipoSensor.VOLUME, novos);

        assertEquals(novos, territorio.limiares(TipoSensor.VOLUME));
    }

    @Test
    void mediaMovelUsaSomenteAsTresLeiturasMaisRecentes() {
        var estrategia = new PrevisaoMediaMovel();
        var historico = chuva(40, 40, 40, 3, 6, 9);

        double resultado = estrategia.prever(historico);

        assertEquals(6, resultado, 0.0001);
    }

    @Test
    void comparacaoProjetaATendenciaDosDoisPeriodos() {
        var estrategia = new PrevisaoComparacao();
        var historico = chuva(11, 12, 13, 8, 9, 10);

        double resultado = estrategia.prever(historico);

        assertEquals(6, resultado, 0.0001);
    }

    @Test
    void exigeHistoricoMinimoNasDuasEstrategias() {
        var historico = chuva(1, 2, 3, 4, 5);
        var fabrica = new FabricaPrevisao();

        assertThrows(DadosInvalidosException.class, () -> fabrica.criar("1").prever(historico));
        assertThrows(DadosInvalidosException.class, () -> fabrica.criar("2").prever(historico));
    }

    @Test
    void geraEPersisteAlertaCriticoComLimiteCorreto() {
        var medicoes = mock(MedicaoDAO.class);
        var alertas = mock(AlertaDAO.class);
        var sensor = mock(Sensor.class);
        when(sensor.id()).thenReturn("CH-01");
        when(sensor.tipo()).thenReturn(TipoSensor.CHUVA);
        when(medicoes.recentes("CH-01", 6)).thenReturn(chuva(2, 2, 2, 2, 2, 2));
        var servico = new ServicoMonitoramento(new DadosTerritorio("Cantareira", "demo"),
                medicoes, alertas, Clock.fixed(INSTANTE, ZoneOffset.UTC));

        var resultado = servico.prever(sensor, new PrevisaoMediaMovel());

        var alerta = resultado.alerta().orElseThrow();
        assertEquals(NivelAlerta.CRITICO, alerta.nivel());
        assertEquals(2, alerta.previsto(), 0.0001);
        assertEquals(3, alerta.limiar(), 0.0001);
        assertEquals("CH-01", alerta.sensorId());
        verify(alertas).salvar(alerta);
    }

    @Test
    void dadosInsuficientesNaoGeramAlerta() {
        var medicoes = mock(MedicaoDAO.class);
        var alertas = mock(AlertaDAO.class);
        var sensor = mock(Sensor.class);
        when(sensor.id()).thenReturn("CH-01");
        when(medicoes.recentes("CH-01", 6)).thenReturn(chuva(2, 2, 2, 2, 2));
        var servico = new ServicoMonitoramento(new DadosTerritorio("Cantareira", "demo"),
                medicoes, alertas, Clock.fixed(INSTANTE, ZoneOffset.UTC));

        assertThrows(DadosInvalidosException.class,
                () -> servico.prever(sensor, new PrevisaoMediaMovel()));
        verifyNoInteractions(alertas);
    }

    @Test
    void bancoEmArquivoPreservaMedicaoEAlertaAoReabrir(@TempDir Path pasta) {
        String url = "jdbc:h2:" + pasta.resolve("historico").toAbsolutePath();
        var primeiroBanco = new Banco(url);
        primeiroBanco.inicializar();
        var medicao = chuva(2).getFirst();
        var alerta = new Alerta("CH-01", TipoSensor.CHUVA, NivelAlerta.CRITICO,
                2, 3, "Média móvel", INSTANTE);
        new MedicaoDAO(primeiroBanco).salvar(medicao);
        new AlertaDAO(primeiroBanco).salvar(alerta);

        var bancoReaberto = new Banco(url);
        bancoReaberto.inicializar();

        assertEquals(List.of(medicao), new MedicaoDAO(bancoReaberto).recentes("CH-01", 20));
        assertEquals(List.of(alerta), new AlertaDAO(bancoReaberto).recentes(20));
    }

    private static List<Medicao> chuva(double... valores) {
        return Arrays.stream(valores)
                .mapToObj(valor -> new Medicao("CH-01", TipoSensor.CHUVA, valor, INSTANTE))
                .toList();
    }
}
