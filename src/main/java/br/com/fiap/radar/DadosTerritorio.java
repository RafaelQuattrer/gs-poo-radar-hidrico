package br.com.fiap.radar;

import java.util.EnumMap;
import java.util.Map;

public final class DadosTerritorio {
    public record Limiares(double atencao, double alerta, double critico) {
        public Limiares {
            if (!Double.isFinite(atencao) || !Double.isFinite(alerta) || !Double.isFinite(critico)
                    || critico < 0 || critico >= alerta || alerta >= atencao) {
                throw new DadosInvalidosException("Use 0 ≤ crítico < alerta < atenção.");
            }
        }
    }

    private final String regiao;
    private final String codigoAcesso;
    private final Map<TipoSensor, Limiares> limiares = new EnumMap<>(Map.of(
            TipoSensor.CHUVA, new Limiares(15, 8, 3),
            TipoSensor.VAZAO, new Limiares(30, 22, 14),
            TipoSensor.VOLUME, new Limiares(55, 42, 30)));

    public DadosTerritorio(String regiao, String codigoAcesso) {
        if (codigoAcesso.isBlank()) {
            throw new DadosInvalidosException("O código de acesso precisa ser preenchido.");
        }
        this.regiao = regiao;
        this.codigoAcesso = codigoAcesso;
    }

    public String regiao() { return regiao; }
    public Limiares limiares(TipoSensor tipo) { return limiares.get(tipo); }

    public void alterarLimiares(String codigo, TipoSensor tipo, Limiares novos) {
        if (!codigoAcesso.equals(codigo)) {
            throw new DadosInvalidosException("Código de acesso inválido.");
        }
        tipo.validar(novos.atencao());
        limiares.put(tipo, novos);
    }
}
