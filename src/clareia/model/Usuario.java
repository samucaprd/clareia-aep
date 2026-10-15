package clareia.model;

public class Usuario {

    private final int id;
    private final String nomeCompleto;
    private final String matricula;
    private final String email;
    private final boolean habilitadoMonitor;

    public Usuario(
            int id,
            String nomeCompleto,
            String matricula,
            String email,
            boolean habilitadoMonitor) {

        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.matricula = matricula;
        this.email = email;
        this.habilitadoMonitor = habilitadoMonitor;
    }

    public int getId() {
        return id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getEmail() {
        return email;
    }

    public boolean isHabilitadoMonitor() {
        return habilitadoMonitor;
    }
}