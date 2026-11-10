package br.com.fiap.radar;

import java.util.function.DoubleSupplier;
import java.util.random.RandomGenerator;

public abstract class SensorSimulado extends ComponenteTerritorial implements Sensor {
    private final TipoSensor tipo;
    private final DoubleSupplier gerador;

    private SensorSimulado(String id, String regiao, TipoSensor tipo, DoubleSupplier gerador) {
        super(id, tipo.nome(), regiao);
        this.tipo = tipo;
        this.gerador = gerador;
    }

    @Override
    public TipoSensor tipo() { return tipo; }

    @Override
    public double lerValor() {
        double valor;
        try {
            valor = gerador.getAsDouble();
        } catch (RuntimeException causa) {
            atualizarStatus(Status.INDISPONIVEL);
            throw new FonteIndisponivelException("Falha no sensor " + id() + ".", causa);
        }
        tipo.validar(valor);
        atualizarStatus(Status.OPERACIONAL);
        return valor;
    }

    @Override
    public Status verificarStatus() { return status(); }

    @Override
    public Status verificarFuncionamento() { return verificarStatus(); }

    public static final class Chuva extends SensorSimulado {
        public Chuva(String regiao, RandomGenerator random) {
            super("CH-01", regiao, TipoSensor.CHUVA, () -> random.nextDouble(0, 25));
        }
    }

    public static final class Vazao extends SensorSimulado {
        public Vazao(String regiao, RandomGenerator random) {
            super("VZ-01", regiao, TipoSensor.VAZAO, () -> random.nextDouble(8, 42));
        }
    }

    public static final class Volume extends SensorSimulado {
        public Volume(String regiao, RandomGenerator random) {
            super("VL-01", regiao, TipoSensor.VOLUME, () -> random.nextDouble(20, 72));
        }
    }
}
