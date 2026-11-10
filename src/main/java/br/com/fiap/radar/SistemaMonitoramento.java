package br.com.fiap.radar;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

public final class SistemaMonitoramento {
    private final Scanner entrada;
    private final PrintStream saida;
    private final DadosTerritorio territorio;
    private final List<Sensor> sensores;
    private final MedicaoDAO medicoes;
    private final AlertaDAO alertas;
    private final ServicoMonitoramento monitoramento;
    private final FabricaPrevisao fabrica = new FabricaPrevisao();

    private SistemaMonitoramento(Scanner entrada, PrintStream saida, DadosTerritorio territorio,
                                 Banco banco) {
        this.entrada = entrada;
        this.saida = saida;
        this.territorio = territorio;
        var random = new Random();
        sensores = List.of(new SensorSimulado.Chuva(territorio.regiao(), random),
                new SensorSimulado.Vazao(territorio.regiao(), random),
                new SensorSimulado.Volume(territorio.regiao(), random));
        medicoes = new MedicaoDAO(banco);
        alertas = new AlertaDAO(banco);
        monitoramento = new ServicoMonitoramento(territorio, medicoes, alertas, Clock.systemUTC());
    }

    public static void main(String[] args) {
        var saida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        try (var entrada = new Scanner(System.in, StandardCharsets.UTF_8)) {
            String codigo = System.getenv().getOrDefault("RADAR_CODIGO_ACESSO", "POO-2026");
            var territorio = new DadosTerritorio("Sistema Cantareira", codigo);
            var banco = new Banco("jdbc:h2:./dados/radar-hidrico");
            banco.inicializar();
            new SistemaMonitoramento(entrada, saida, territorio, banco).executar();
        } catch (DadosInvalidosException | FonteIndisponivelException erro) {
            saida.println("Falha na inicialização: " + erro.getMessage());
        }
    }

    private void executar() {
        saida.println("Radar Hídrico | " + territorio.regiao() + " | simulação acadêmica");
        while (true) {
            saida.println("""
                    
                    1. Verificar sensores e coletar leituras
                    2. Rodar previsões e gerar alertas
                    3. Consultar histórico
                    4. Gerenciar limiares do território
                    5. Exibir status completo
                    0. Sair
                    """);
            saida.print("Opção: ");
            try {
                switch (entrada.nextLine().strip()) {
                    case "1" -> coletar();
                    case "2" -> prever();
                    case "3" -> historico();
                    case "4" -> gerenciar();
                    case "5" -> status();
                    case "0" -> { return; }
                    default -> saida.println("Opção inválida.");
                }
            } catch (DadosInvalidosException | FonteIndisponivelException erro) {
                saida.println(erro.getMessage());
            } catch (NoSuchElementException fim) {
                saida.println("Entrada encerrada.");
                return;
            }
        }
    }

    private void coletar() {
        int rodadas = inteiro("Quantidade de rodadas (1 a 20): ");
        if (rodadas < 1 || rodadas > 20) {
            throw new DadosInvalidosException("Informe de 1 a 20 rodadas.");
        }
        for (int i = 1; i <= rodadas; i++) {
            saida.println("Rodada " + i);
            for (Sensor sensor : sensores) {
                try {
                    var medicao = monitoramento.coletar(sensor);
                    saida.printf("  %s | %s: %.2f %s | %s%n", sensor.id(), sensor.tipo().nome(),
                            medicao.valor(), medicao.tipo().unidade(), sensor.verificarFuncionamento());
                } catch (DadosInvalidosException | FonteIndisponivelException erro) {
                    saida.printf("  %s | %s%n", sensor.id(), erro.getMessage());
                }
            }
        }
    }

    private void prever() {
        EstrategiaPrevisao estrategia = fabrica.criar(ler("1. Média móvel | 2. Comparação: "));
        for (Sensor sensor : sensores) {
            try {
                var resultado = monitoramento.prever(sensor, estrategia);
                saida.printf("%s | próxima leitura simulada: %.2f %s%n",
                        sensor.id(), resultado.valor(), sensor.tipo().unidade());
                saida.println(resultado.alerta().map(Alerta::descrever)
                        .orElse("Sem alerta para os limiares atuais."));
            } catch (DadosInvalidosException | FonteIndisponivelException erro) {
                saida.printf("%s | Previsão indisponível. %s%n", sensor.id(), erro.getMessage());
            }
        }
        saida.println("Alertas apoiam a avaliação humana.");
    }

    private void historico() {
        switch (ler("1. Medições por sensor | 2. Alertas: ")) {
            case "1" -> {
                Sensor sensor = escolherSensor();
                List<Medicao> historico = medicoes.recentes(sensor.id(), 20);
                if (historico.isEmpty()) {
                    saida.println("Nenhuma medição para este sensor.");
                }
                historico.forEach(m -> saida.printf("%s | %s | %.2f %s%n",
                        m.coletadaEm(), m.sensorId(), m.valor(), m.tipo().unidade()));
            }
            case "2" -> {
                List<Alerta> historico = alertas.recentes(20);
                if (historico.isEmpty()) {
                    saida.println("Nenhum alerta emitido.");
                }
                historico.forEach(a -> saida.println(a.emitidoEm() + " | " + a.descrever()));
            }
            default -> throw new DadosInvalidosException("Selecione 1 ou 2.");
        }
    }

    private void gerenciar() {
        Sensor sensor = escolherSensor();
        String codigo = ler("Código de acesso: ");
        double atencao = decimal("Limiar de atenção: ");
        double alerta = decimal("Limiar de alerta: ");
        double critico = decimal("Limiar crítico: ");
        territorio.alterarLimiares(codigo, sensor.tipo(),
                new DadosTerritorio.Limiares(atencao, alerta, critico));
        saida.println("Limiares atualizados para " + sensor.id() + ".");
    }

    private void status() {
        saida.println(territorio.regiao() + " | leituras simuladas");
        for (Sensor sensor : sensores) {
            var limiares = territorio.limiares(sensor.tipo());
            saida.printf("%s | %s | %s | atenção ≤ %.2f, alerta ≤ %.2f, crítico ≤ %.2f %s%n",
                    sensor.id(), sensor.tipo().nome(), sensor.verificarFuncionamento(),
                    limiares.atencao(), limiares.alerta(), limiares.critico(), sensor.tipo().unidade());
        }
    }

    private Sensor escolherSensor() {
        for (int i = 0; i < sensores.size(); i++) {
            Sensor sensor = sensores.get(i);
            saida.printf("%d. %s (%s)%n", i + 1, sensor.tipo().nome(), sensor.id());
        }
        int opcao = inteiro("Sensor: ");
        if (opcao < 1 || opcao > sensores.size()) {
            throw new DadosInvalidosException("Sensor inválido.");
        }
        return sensores.get(opcao - 1);
    }

    private int inteiro(String pergunta) {
        try {
            return Integer.parseInt(ler(pergunta));
        } catch (NumberFormatException erro) {
            throw new DadosInvalidosException("Informe um número inteiro.");
        }
    }

    private double decimal(String pergunta) {
        try {
            return Double.parseDouble(ler(pergunta).replace(',', '.'));
        } catch (NumberFormatException erro) {
            throw new DadosInvalidosException("Informe um valor numérico.");
        }
    }

    private String ler(String pergunta) {
        saida.print(pergunta);
        return entrada.nextLine().strip();
    }
}
