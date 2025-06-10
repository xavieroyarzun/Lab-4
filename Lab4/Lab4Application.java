import java.util.*;
import java.util.stream.Collectors;

class Paciente {
    private String nombre;
    private String apellido;
    private String id;
    private int categoria;
    private long tiempoLlegada;
    private String estado;
    private String area;
    private Stack<String> historialCambios;

    public Paciente(String nombre, String apellido, String id, int categoria, long tiempoLlegada) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.id = id;
        this.categoria = categoria;
        this.tiempoLlegada = tiempoLlegada;
        this.estado = "en_espera";
        this.historialCambios = new Stack<>();
        asignarArea();
    }
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    private void asignarArea() {
        if (categoria == 1 || categoria == 2) {
            this.area = "urgencia adulto";
        } else if (categoria == 3 || categoria == 4) {
            this.area = "SAPU";
        } else {
            this.area = "infantil";
        }
    }

    public long tiempoEsperaActual() {
        long ahora = System.currentTimeMillis() / 1000;
        return (ahora - tiempoLlegada) / 60;
    }

    public void registrarCambio(String descripcion) {
        historialCambios.push(descripcion);
    }

    public String obtenerUltimoCambio() {
        return historialCambios.isEmpty() ? null : historialCambios.pop();
    }

    public String getId() {
        return id;
    }

    public int getCategoria() {
        return categoria;
    }

    public long getTiempoLlegada() {
        return tiempoLlegada;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getArea() {
        return area;
    }

    public String toString() {
        return "Paciente: nombre: " + nombre + ", apellido: " + apellido +
                ", id: " + id + ", categoria: " + categoria +
                ", tiempoLlegada: " + tiempoLlegada +
                ", estado: " + estado + ", area: " + area;
    }
}

class AreaAtencion {
    private String nombre;
    private PriorityQueue<Paciente> pacientesHeap;
    private int capacidadMaxima;

    public AreaAtencion(String nombre, int capacidadMaxima) {
        this.nombre = nombre;
        this.capacidadMaxima = capacidadMaxima;
        this.pacientesHeap = new PriorityQueue<>((p1, p2) -> {
            if (p1.getCategoria() != p2.getCategoria()) {
                return Integer.compare(p1.getCategoria(), p2.getCategoria());
            } else {
                return Long.compare(p1.getTiempoLlegada(), p2.getTiempoLlegada());
            }
        });
    }

    public void ingresarPaciente(Paciente p) {
        if (pacientesHeap.size() < capacidadMaxima) {
            pacientesHeap.add(p);
        }
    }

    public Paciente atenderPaciente() {
        return pacientesHeap.poll();
    }

    public boolean estaSaturada() {
        return pacientesHeap.size() >= capacidadMaxima;
    }

    public List<Paciente> obtenerPacientesPorHeapSort() {
        List<Paciente> pacientes = new ArrayList<>(pacientesHeap);
        Collections.sort(pacientes, (p1, p2) -> {
            if (p1.getCategoria() != p2.getCategoria()) {
                return Integer.compare(p1.getCategoria(), p2.getCategoria());
            } else {
                return Long.compare(p1.getTiempoLlegada(), p2.getTiempoLlegada());
            }
        });
        return pacientes;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidadPacientes() {
        return pacientesHeap.size();
    }
}

class Hospital {
    private Map<String, Paciente> pacientesTotales;
    private PriorityQueue<Paciente> colaAtencion;
    private Map<String, AreaAtencion> areasAtencion;
    private List<Paciente> pacientesAtendidos;

    public Hospital() {
        this.pacientesTotales = new HashMap<>();
        this.colaAtencion = new PriorityQueue<>((p1, p2) -> {
            if (p1.getCategoria() != p2.getCategoria()) {
                return Integer.compare(p1.getCategoria(), p2.getCategoria());
            } else {
                return Long.compare(p1.getTiempoLlegada(), p2.getTiempoLlegada());
            }
        });
        this.areasAtencion = new HashMap<>();
        this.areasAtencion.put("SAPU", new AreaAtencion("SAPU", 200));
        this.areasAtencion.put("urgencia adulto", new AreaAtencion("urgencia adulto", 150));
        this.areasAtencion.put("infantil", new AreaAtencion("infantil", 100));
        this.pacientesAtendidos = new ArrayList<>();
    }

    public void registrarPaciente(Paciente p) {
        pacientesTotales.put(p.getId(), p);
        colaAtencion.add(p);
    }

    public void reassignarCategoria(String id, int nuevaCategoria) {
        Paciente p = pacientesTotales.get(id);
        if (p != null) {
            p.registrarCambio("Cambio de categoría de " + p.getCategoria() + " a " + nuevaCategoria);
            p = new Paciente(p.getNombre(), p.getApellido(), p.getId(), nuevaCategoria, p.getTiempoLlegada());
            pacientesTotales.put(id, p);
            colaAtencion.removeIf(patient -> patient.getId().equals(id));
            colaAtencion.add(p);
        }
    }

    public Paciente atenderSiguiente() {
        Paciente p = colaAtencion.poll();
        if (p != null) {
            p.setEstado("en_atencion");
            AreaAtencion area = areasAtencion.get(p.getArea());
            area.ingresarPaciente(p);
            pacientesAtendidos.add(p);
            return p;
        }
        return null;
    }

    public List<Paciente> obtenerPacientesPorCategoria(int categoria) {
        return colaAtencion.stream()
                .filter(p -> p.getCategoria() == categoria)
                .collect(Collectors.toList());
    }

    public AreaAtencion obtenerArea(String nombre) {
        return areasAtencion.get(nombre);
    }

    public List<Paciente> getPacientesAtendidos() {
        return pacientesAtendidos;
    }
}

class GeneradorPacientes {
    private static final String[] NOMBRES = {"Juan", "Maria", "Pedro", "Ana", "Luis", "Laura", "Carlos", "Sofia", "David", "Javiera", "Nicolas", "Esteban", "Millaray", "Martin", "Balatro"};
    private static final String[] APELLIDOS = {"Garcia", "Rodriguez", "Gonzalez", "Fernandez", "Lopez", "Martinez", "Sanchez", "Perez", "Fuentes", "Castro", "Balatrez", "Cuevas", "Cifuentes"};
    private static int idCounter = 1;

    public static List<Paciente> generarPacientes(int cantidad, long timestampInicio) {
        List<Paciente> pacientes = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < cantidad; i++) {
            String nombre = NOMBRES[random.nextInt(NOMBRES.length)];
            String apellido = APELLIDOS[random.nextInt(APELLIDOS.length)];
            String id = "id-" + (idCounter++);
            int categoria = generarCategoria(random);
            long tiempoLlegada = timestampInicio + (i * 600);

            pacientes.add(new Paciente(nombre, apellido, id, categoria, tiempoLlegada));
        }

        return pacientes;
    }

    private static int generarCategoria(Random random) {
        int prob = random.nextInt(100);
        if (prob < 10) return 1;
        else if (prob < 25) return 2;
        else if (prob < 43) return 3;
        else if (prob < 70) return 4;
        else return 5;
    }
}

class SimuladorUrgencia {
    private Hospital hospital;
    private List<Paciente> pacientesGenerados;
    private int pacientesAtendidos;
    private Map<Integer, Integer> pacientesPorCategoria;
    private Map<Integer, Long> tiempoEsperaAcumulado;
    private List<Paciente> pacientesExcedidos;

    public SimuladorUrgencia() {
        this.hospital = new Hospital();
        this.pacientesGenerados = new ArrayList<>();
        this.pacientesAtendidos = 0;
        this.pacientesPorCategoria = new HashMap<>();
        this.tiempoEsperaAcumulado = new HashMap<>();
        this.pacientesExcedidos = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            pacientesPorCategoria.put(i, 0);
            tiempoEsperaAcumulado.put(i, 0L);
        }
    }

    public void simular(int pacientesPorDia) {
        long timestampInicio = System.currentTimeMillis() / 1000;
        pacientesGenerados = GeneradorPacientes.generarPacientes(pacientesPorDia, timestampInicio);

        int minutosSimulacion = 24 * 60;
        int pacientesIngresados = 0;
        int pacientesEnCola = 0;

        for (int minuto = 0; minuto < minutosSimulacion; minuto++) {
            if (minuto % 10 == 0 && pacientesIngresados < pacientesGenerados.size()) {
                Paciente p = pacientesGenerados.get(pacientesIngresados);
                hospital.registrarPaciente(p);
                pacientesIngresados++;
                pacientesEnCola++;
            }

            if (minuto % 15 == 0 && pacientesEnCola > 0) {
                atenderPaciente();
                pacientesEnCola--;
            }

            if (pacientesIngresados % 3 == 0 && pacientesEnCola >= 2) {
                atenderPaciente();
                atenderPaciente();
                pacientesEnCola -= 2;
            }

        }
    }

    private void atenderPaciente() {
        Paciente p = hospital.atenderSiguiente();
        if (p != null) {
            pacientesAtendidos++;
            pacientesPorCategoria.put(p.getCategoria(), pacientesPorCategoria.get(p.getCategoria()) + 1);
            long tiempoEspera = p.tiempoEsperaActual();
            tiempoEsperaAcumulado.put(p.getCategoria(), tiempoEsperaAcumulado.get(p.getCategoria()) + tiempoEspera);

            if (excedioTiempoMaximo(p, tiempoEspera)) {
                pacientesExcedidos.add(p);
            }
        }
    }

    private boolean excedioTiempoMaximo(Paciente p, long tiempoEspera) {
        switch (p.getCategoria()) {
            case 1: return tiempoEspera > 0;
            case 2: return tiempoEspera > 30;
            case 3: return tiempoEspera > 90;
            case 4: return tiempoEspera > 180;
            default: return false;
        }
    }


    public void mostrarEstadisticas() {
        System.out.println("Total de pacientes atendidos: " + pacientesAtendidos);

        System.out.println("\nPacientes atendidos por categoría:");
        for (int i = 1; i <= 5; i++) {
            System.out.println("C" + i + ": " + pacientesPorCategoria.get(i));
        }

        System.out.println("\nTiempo promedio de espera por categoría:");
        for (int i = 1; i <= 5; i++) {
            int cantidad = pacientesPorCategoria.get(i);
            double promedio = cantidad > 0 ? (double) tiempoEsperaAcumulado.get(i) / cantidad : 0;
            System.out.printf("C%d: %.2f minutos\n", i, promedio);
        }

        System.out.println("\nPacientes que excedieron el tiempo máximo de espera: " + pacientesExcedidos.size());
    }
}

public class Main {
    public static void main(String[] args) {
        Hospital hospital = new Hospital();

        long ahora = System.currentTimeMillis() / 1000;
        Paciente p1 = new Paciente("Juan", "Perez", "id-001", 1, ahora - 300);
        Paciente p2 = new Paciente("Maria", "Gomez", "id-002", 3, ahora - 600);
        Paciente p3 = new Paciente("Pedro", "Lopez", "id-003", 5, ahora - 900);

        hospital.registrarPaciente(p1);
        hospital.registrarPaciente(p2);
        hospital.registrarPaciente(p3);

        System.out.println("Atendiendo pacientes:");
        System.out.println(hospital.atenderSiguiente());
        System.out.println(hospital.atenderSiguiente());

        hospital.reassignarCategoria("id-003", 2);
        System.out.println("\nDespués de reasignar categoría:");
        System.out.println(hospital.atenderSiguiente());

        System.out.println("\nSimulación de 24 horas");
        SimuladorUrgencia simulador = new SimuladorUrgencia();
        simulador.simular(144);
        simulador.mostrarEstadisticas();
    }
}