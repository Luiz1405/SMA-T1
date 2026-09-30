public class Evento implements Comparable<Evento> {

    public static final int CHEGADA = 0;
    public static final int SAIDA = 1;

    private final int tipo;
    private final double tempo;
    private final String nomeFila;
    private final String nomeDestino;

    public Evento(int tipo, double tempo, String nomeFila) {
        this(tipo, tempo, nomeFila, null);
    }

    public Evento(int tipo, double tempo, String nomeFila, String nomeDestino) {
        this.tipo = tipo;
        this.tempo = tempo;
        this.nomeFila = nomeFila;
        this.nomeDestino = nomeDestino;
    }

    public int getTipo() {
        return tipo;
    }

    public double getTempo() {
        return tempo;
    }

    public String getNomeFila() {
        return nomeFila;
    }

    public String getNomeDestino() {
        return nomeDestino;
    }

    @Override
    public int compareTo(Evento outro) {
        return Double.compare(this.tempo, outro.tempo);
    }

    @Override
    public String toString() {
        String[] nomes = {"CHEGADA", "SAIDA"};
        return String.format("Evento[%s, %s, t=%.4f]", nomes[tipo], nomeFila, tempo);
    }
}
