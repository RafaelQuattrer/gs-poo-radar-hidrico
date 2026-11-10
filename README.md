# Radar Hídrico

Global Solution 2026.2 · Object-Oriented Programming · Turma 2CCPG

## Problema e público

O Radar Hídrico simula o acompanhamento de chuva, vazão e volume útil no Sistema Cantareira. O público beneficiado são os moradores que dependem do abastecimento. Quem opera o sistema é a equipe de monitoramento, que pode usar os alertas para decidir se precisa investigar uma tendência de baixa disponibilidade.

## Como executar

Requisitos: **JDK 21** e **Maven 3.9**. Na pasta do projeto, execute:

```sh
mvn clean package
java -jar target/radar-hidrico.jar
```

O Maven executa os testes durante a compilação e cria um JAR que já inclui o H2. Para executar apenas os testes, use `mvn test`.

1. Selecione **1** e informe **6** rodadas para gerar histórico dos três sensores.
2. Selecione **2** e teste as duas estratégias. Compare a previsão e o limiar apresentado no alerta.
3. Selecione **3** para consultar medições por sensor ou alertas emitidos. Saia com **0**, abra o programa novamente a partir da mesma pasta e consulte o histórico.
4. Selecione **4** para alterar limiares. O código da simulação é `POO-2026`. Para trocá-lo, defina a variável de ambiente `RADAR_CODIGO_ACESSO` antes de iniciar. Use **5** para conferir sensores e limiares.

O banco é criado automaticamente em `dados/radar-hidrico.mv.db`. Medições e alertas sobrevivem ao reinício. Os limiares personalizados valem na sessão atual. Cada rodada registra medições simuladas e cada execução de previsão registra os alertas que disparar.

## Decisões técnicas

- **Domínio.** Os sensores de chuva, vazão e volume implementam a interface `Sensor` e herdam a base territorial. `DadosTerritorio` valida a ordem dos limiares e protege a alteração por código de acesso.
- **Previsão.** A média móvel usa as três últimas leituras. A comparação usa as médias de dois blocos consecutivos de três leituras e projeta a diferença sobre a média mais recente. As duas estratégias trabalham sobre o histórico do H2 e requerem seis leituras.
- **Factory.** `FabricaPrevisao` concentra a escolha e a criação das estratégias em um mapa de opções. O serviço recebe uma `EstrategiaPrevisao` e chama `prever()` sem consultar o tipo concreto.
- **Persistência.** JDBC com `PreparedStatement` e `try-with-resources` mantém consultas e gravações separadas da regra de negócio. H2 em arquivo permite demonstrar a persistência local sem instalar um servidor.
- **Validação.** `DadosInvalidosException` informa entradas incorretas. `FonteIndisponivelException` informa falhas do sensor ou do armazenamento. O menu apresenta uma mensagem para cada caso.

## Reflexão crítica

O sistema se abstém de prever até reunir seis leituras válidas por sensor. Esse mínimo permite comparar duas janelas completas. Ele é uma condição de cálculo da simulação. Uma operação real precisaria verificar também se os registros são recentes, se há lacunas e se as medições correspondem a intervalos comparáveis.

Chuva, vazão e volume útil descrevem partes diferentes do sistema. Três pontos simulados não mostram o que ocorre em cada reservatório ou bairro, nem permitem estimar o efeito de captação, demanda ou manutenção. Os limiares servem para exercitar a regra de alertas e precisariam de calibração com séries históricas e revisão por especialistas antes de qualquer uso em campo.

Cada alerta apresenta o sensor, a previsão, o limiar e a estratégia. A equipe de monitoramento pode conferir a origem dos dados e verificar se vale intensificar o acompanhamento. Uma decisão sobre o abastecimento depende de análise humana e de informações operacionais adicionais.

## Identificação e organização

**Rafael Quattrer** · entrega individual nesta versão. Domínio, persistência, interface de console, testes e documentação estão reunidos no mesmo projeto.

## Uso de IA

Usei o ChatGPT para apoiar a organização dos requisitos, a implementação inicial e a documentação. A verificação do código incluiu testes JUnit, mocks dos DAOs, reabertura do banco em arquivo e execução do menu. As previsões e alertas são demonstrados apenas com dados simulados.

