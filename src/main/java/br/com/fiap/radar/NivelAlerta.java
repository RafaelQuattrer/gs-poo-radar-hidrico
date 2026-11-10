package br.com.fiap.radar;

public enum NivelAlerta {
    ATENCAO("ATENÇÃO"), ALERTA("ALERTA"), CRITICO("CRÍTICO");

    private final String rotulo;

    NivelAlerta(String rotulo) { this.rotulo = rotulo; }

    public String rotulo() { return rotulo; }
}
