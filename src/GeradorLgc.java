import java.util.ArrayList;
import java.util.List;

public class GeradorLgc {

    // Congruente linear: X(n+1) = (a * X(n) + c) mod M, com M = 2^48
    private static final long A = 25214903917L;
    private static final long C = 11L;
    private static final long MASCARA = (1L << 48) - 1;
    private static final double M = Math.pow(2, 48);

    private long x;
    private int count = 0;
    private final List<Double> numerosFixos;

    public GeradorLgc(long semente) {
        this.x = semente;
        this.numerosFixos = null;
    }

    public GeradorLgc(List<Double> numerosFixos) {
        this.x = 0;
        this.numerosFixos = new ArrayList<>(numerosFixos);
    }

    /**
     * Retorna o proximo numero no intervalo [0,1) e incrementa o contador.
     */
    public double proximo() {
        if (numerosFixos != null) {
            if (count >= numerosFixos.size()) {
                throw new IllegalStateException("Acabaram os numeros aleatorios informados em rndnumbers.");
            }
            return numerosFixos.get(count++);
        }

        x = (A * x + C) & MASCARA;
        count++;
        return x / M;
    }

    public int getCount() {
        return count;
    }
}
