package br.com.fiap.radar;

import java.util.Map;
import java.util.function.Supplier;

public final class FabricaPrevisao {
    private final Map<String, Supplier<EstrategiaPrevisao>> estrategias = Map.of(
            "1", PrevisaoMediaMovel::new,
            "2", PrevisaoComparacao::new);

    public EstrategiaPrevisao criar(String codigo) {
        Supplier<EstrategiaPrevisao> criador = estrategias.get(codigo);
        if (criador == null) {
            throw new DadosInvalidosException("Escolha 1 para média móvel ou 2 para comparação.");
        }
        return criador.get();
    }
}
