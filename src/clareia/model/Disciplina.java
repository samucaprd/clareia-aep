package clareia.model;

public class Disciplina {

    private final int id;
    private final String codigo;
    private final String nome;

    public Disciplina(int id, String codigo, String nome) {
        this.id = id;
        this.codigo = codigo;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }
}