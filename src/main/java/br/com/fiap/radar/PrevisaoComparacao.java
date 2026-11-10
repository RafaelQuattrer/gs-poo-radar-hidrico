package br.com.fiap.radar;

import java.util.List;

public final class PrevisaoComparacao extends EstrategiaPrevisao {
    public PrevisaoComparacao() {
        super("Comparação de períodos");
    }

    @Override
    public double prever(List<Medicao> historico) {
        super.validarHistorico(historico);
        int fim = historico.size();
        double anterior = super.media(historico, fim - 6, fim - 3);
        double recente = super.media(historico, fim - 3, fim);
        double estimativa = recente + (recente - anterior);
        return Math.clamp(estimativa, 0, historico.getLast().tipo().maximo());
    }
}
