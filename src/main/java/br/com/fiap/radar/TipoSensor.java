package br.com.fiap.radar;

public enum TipoSensor {
    CHUVA("Chuva", "mm", 300),
    VAZAO("Vazão", "m³/s", 200),
    VOLUME("Volume útil", "%", 100);

    private final String nome;
    private final String unidade;
    private final double maximo;

    TipoSensor(String nome, String unidade, double maximo) {
        this.nome = nome;
        this.unidade = unidade;
        this.maximo = maximo;
    }

    public String nome() { return nome; }
    public String unidade() { return unidade; }
    public double maximo() { return maximo; }

    public void validar(double valor) {
        if (!Double.isFinite(valor) || valor < 0 || valor > maximo) {
            throw new DadosInvalidosException(
                    "%s deve ficar entre 0 e %.0f %s.".formatted(nome, maximo, unidade));
        }
    }
}
