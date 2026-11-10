package br.com.fiap.radar;

public interface Sensor {
    String id();
    TipoSensor tipo();
    double lerValor();
    ComponenteTerritorial.Status verificarFuncionamento();
}
