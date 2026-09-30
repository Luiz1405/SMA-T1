# Simulador de Filas

**Disciplina:** Simulação e Métodos Analíticos
**Atividade:** T1 — Rede de Filas (M8)

## Integrantes do Grupo

| Nome |
|------|
| Luiz Augusto de Souza Kubaszewski |
| Gabriel Cristiano Ramos Barcelos |
| João Ayache |
| Marco Laimer |

## Modelo

| Fila | Tipo | Chegadas | Atendimento |
|------|------|----------|-------------|
| Q1 | G/G/1 | 2..4 (primeira em t=2.0) | 1..2 |
| Q2 | G/G/2/5 | — | 4..6 |
| Q3 | G/G/2/10 | — | 5..15 |

Roteamento: Q1→Q2 (0.8), Q1→Q3 (0.2), Q2→Q1 (0.3), Q2→Q3 (0.5), Q3→Q2 (0.7). A probabilidade restante de cada fila corresponde à saída do sistema.

A simulação usa 100.000 números pseudoaleatórios (semente 1).

## Como Rodar

Compile:

```bash
javac -d bin src/*.java
```

Execute informando o arquivo YAML:

```bash
java -cp bin Simulador model.yml
```

O arquivo `.yml` deve conter o modelo da simulação e os números aleatórios, usando `seeds` com `rndnumbersPerSeed` ou usando `rndnumbers`.
