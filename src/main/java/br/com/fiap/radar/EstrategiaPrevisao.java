package br.com.fiap.radar;

import java.util.List;

public abstract class EstrategiaPrevisao {
    public static final int MINIMO_LEITURAS = 6;

    private final String nome;

    protected EstrategiaPrevisao(String nome) {
        this.nome = nome;
    }

    public String nome() { return nome; }

    public abstract double prever(List<Medicao> historico);

    protected double media(List<Medicao> historico, int inicio, int fim) {
        return historico.subList(inicio, fim).stream()
                .mapToDouble(Medicao::valor).average().orElseThrow();
    }

    protected void validarHistorico(List<Medicao> historico) {
        if (historico.size() < MINIMO_LEITURAS) {
            throw new DadosInvalidosException("São necessárias pelo menos seis leituras do sensor.");
        }
    }
}
