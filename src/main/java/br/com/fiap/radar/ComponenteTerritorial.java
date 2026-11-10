package br.com.fiap.radar;

public abstract class ComponenteTerritorial {
    public enum Status { OPERACIONAL, INDISPONIVEL }

    private final String id;
    private final String nome;
    private final String regiao;
    private Status status = Status.OPERACIONAL;

    protected ComponenteTerritorial(String id, String nome, String regiao) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
    }

    public String id() { return id; }
    public String nome() { return nome; }
    public String regiao() { return regiao; }
    public Status status() { return status; }

    protected void atualizarStatus(Status status) {
        this.status = status;
    }

    public abstract Status verificarStatus();
}
