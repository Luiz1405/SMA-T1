import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Simulador {
    static Map<String, Fila> filas;
    static PriorityQueue<Evento> escalonador;
    static GeradorLgc gerador;
    static ModeloSimulacao modelo;
    static double tempoAtual;
    static int maxAleatorios;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java -cp src Simulador <arquivo.yml>");
            return;
        }

        modelo = LeitorModeloYaml.ler(args[0]);

        if (modelo.usaSementes()) {
            for (Long semente : modelo.getSementes()) {
                executarSimulacao(new GeradorLgc(semente), modelo.getRndnumbersPerSeed(),
                        "Semente: " + semente);
            }
        } else if (!modelo.getRndnumbers().isEmpty()) {
            executarSimulacao(new GeradorLgc(modelo.getRndnumbers()), modelo.getRndnumbers().size(),
                    "Numeros aleatorios do arquivo");
        } else {
            throw new IllegalArgumentException("O arquivo YAML precisa declarar seeds ou rndnumbers.");
        }
    }

    static void executarSimulacao(GeradorLgc geradorDaRodada, int limiteAleatorios, String titulo) {
        filas = new LinkedHashMap<>();
        escalonador = new PriorityQueue<>();
        gerador = geradorDaRodada;
        maxAleatorios = limiteAleatorios;
        tempoAtual = 0.0;

        carregarFilas();
        agendarChegadasIniciais();

        while (!escalonador.isEmpty()) {
            Evento ev = escalonador.poll();

            acumulaTempo(ev.getTempo() - tempoAtual);
            tempoAtual = ev.getTempo();

            try {
                if (ev.getTipo() == Evento.CHEGADA) {
                    chegada(ev);
                } else {
                    saida(ev);
                }
            } catch (FimDosAleatorios e) {
                break;
            }
        }

        imprimirResultados(titulo);
    }

    static void carregarFilas() {
        for (ModeloSimulacao.FilaConfig config : modelo.getFilas().values()) {
            filas.put(config.nome, new Fila(
                    config.nome,
                    config.servers,
                    config.capacity,
                    config.minArrival,
                    config.maxArrival,
                    config.minService,
                    config.maxService));
        }
    }

    static void agendarChegadasIniciais() {
        for (Map.Entry<String, Double> chegada : modelo.getChegadas().entrySet()) {
            escalonador.add(new Evento(Evento.CHEGADA, chegada.getValue(), chegada.getKey()));
        }
    }

    static double proximoAleatorio() {
        if (gerador.getCount() >= maxAleatorios) {
            throw new FimDosAleatorios();
        }
        return gerador.proximo();
    }

    static double intervalo(double min, double max) {
        return min + proximoAleatorio() * (max - min);
    }

    static void acumulaTempo(double delta) {
        if (delta <= 0) {
            return;
        }
        for (Fila f : filas.values()) {
            f.acumulaTempo(delta);
        }
    }

    static Fila buscarFila(String nome) {
        Fila fila = filas.get(nome);
        if (fila == null) {
            throw new IllegalArgumentException("Fila inexistente: " + nome);
        }
        return fila;
    }

    static void chegada(Evento ev) {
        Fila fila = buscarFila(ev.getNomeFila());
        entrarNaFila(fila);
        agendarProximaChegadaExterna(fila);
    }

    static void agendarProximaChegadaExterna(Fila fila) {
        if (!modelo.temChegadaExterna(fila.getNomeFila())) {
            return;
        }
        double proximaChegada = tempoAtual + intervalo(fila.minArrival(), fila.maxArrival());
        escalonador.add(new Evento(Evento.CHEGADA, proximaChegada, fila.getNomeFila()));
    }

    static void entrarNaFila(Fila fila) {
        if (fila.status() < fila.capacity()) {
            fila.in();
            if (fila.status() <= fila.servers()) {
                agendarFimServico(fila);
            }
        } else {
            fila.contaPerda();
        }
    }

    // O destino e sorteado no inicio do atendimento e viaja junto com o evento de saida
    static void agendarFimServico(Fila fila) {
        String nomeDestino = sortearProximoDestino(fila.getNomeFila());
        double fimServico = tempoAtual + intervalo(fila.minService(), fila.maxService());
        escalonador.add(new Evento(Evento.SAIDA, fimServico, fila.getNomeFila(), nomeDestino));
    }

    // Retorna null quando o cliente sai do sistema (probabilidade restante ate 1)
    static String sortearProximoDestino(String origem) {
        List<ModeloSimulacao.Rota> rotas = modelo.getRotas(origem);
        if (rotas.isEmpty()) {
            return null;
        }
        if (rotas.size() == 1 && rotas.get(0).probability >= 1.0) {
            return rotas.get(0).target;
        }

        double rnd = proximoAleatorio();
        for (ModeloSimulacao.Rota rota : rotas) {
            if (rnd <= rota.probability) {
                return rota.target;
            }
            rnd -= rota.probability;
        }
        return null;
    }

    static void saida(Evento ev) {
        Fila origem = buscarFila(ev.getNomeFila());
        origem.out();

        if (origem.status() >= origem.servers()) {
            agendarFimServico(origem);
        }

        if (ev.getNomeDestino() != null) {
            entrarNaFila(buscarFila(ev.getNomeDestino()));
        }
    }

    static void imprimirResultados(String titulo) {
        System.out.println("========================================");
        System.out.println(titulo);
        System.out.println("Tempo global da simulacao: " + tempoAtual);
        System.out.println("Aleatorios utilizados: " + gerador.getCount());

        for (Map.Entry<String, Fila> entrada : filas.entrySet()) {
            imprimirFila(entrada.getKey(), entrada.getValue());
        }
    }

    static void imprimirFila(String nome, Fila fila) {
        Map<Integer, Double> times = fila.getTimesMap();
        double totalTempoFila = 0;
        for (double t : times.values()) {
            totalTempoFila += t;
        }

        System.out.println("Fila: " + nome);
        System.out.printf("%-10s %-20s %-15s%n", "Estado", "Tempo", "Probabilidade");

        List<Integer> estados = new ArrayList<>(times.keySet());
        Collections.sort(estados);

        for (Integer estado : estados) {
            double tempo = times.get(estado);
            double probPercentual = totalTempoFila > 0 ? (tempo / totalTempoFila) * 100.0 : 0.0;
            System.out.printf("%-10d %-20.4f %14.2f%%%n", estado, tempo, probPercentual);
        }
        System.out.println("Perdas: " + fila.loss());
    }

    private static class FimDosAleatorios extends RuntimeException {
    }
}
