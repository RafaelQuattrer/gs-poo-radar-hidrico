package br.com.fiap.radar;

import java.util.List;

public final class PrevisaoMediaMovel extends EstrategiaPrevisao {
    public PrevisaoMediaMovel() {
        super("Média móvel");
    }

    @Override
    public double prever(List<Medicao> historico) {
        super.validarHistorico(historico);
        int fim = historico.size();
        return super.media(historico, fim - 3, fim);
    }
}
