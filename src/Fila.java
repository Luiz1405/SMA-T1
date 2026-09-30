import java.util.HashMap;
import java.util.Map;

public class Fila {

    private final String nomeFila;
    private final int servers;
    private final int capacity;
    private final double minArrival;
    private final double maxArrival;
    private final double minService;
    private final double maxService;
    private int customers = 0;
    private int loss = 0;
    private final Map<Integer, Double> timesMap = new HashMap<>();

    public Fila(String nomeFila, int servers, int capacity,
                double minArrival, double maxArrival,
                double minService, double maxService) {
        this.nomeFila = nomeFila;
        this.servers = servers;
        this.capacity = capacity;
        this.minArrival = minArrival;
        this.maxArrival = maxArrival;
        this.minService = minService;
        this.maxService = maxService;
    }

    public String getNomeFila() {
        return nomeFila;
    }

    public int status() {
        return customers;
    }

    public int capacity() {
        return capacity;
    }

    public int servers() {
        return servers;
    }

    public int loss() {
        return loss;
    }

    public double minArrival() {
        return minArrival;
    }

    public double maxArrival() {
        return maxArrival;
    }

    public double minService() {
        return minService;
    }

    public double maxService() {
        return maxService;
    }

    public void contaPerda() {
        loss++;
    }

    public void in() {
        customers++;
    }

    public void out() {
        customers--;
    }

    public Map<Integer, Double> getTimesMap() {
        return timesMap;
    }

    public void acumulaTempo(double delta) {
        timesMap.merge(customers, delta, Double::sum);
    }
}
