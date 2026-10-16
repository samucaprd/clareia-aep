package clareia.model;

public class ItemPauta {
    private final int id;
    private final int ordem;
    private String descricao;
    public ItemPauta(int id, int ordem, String descricao) {
        if (id < 0) {
            throw new IllegalArgumentException("ID do item invalido.");
        }
        if (ordem <= 0) {
            throw new IllegalArgumentException("A ordem do item deve ser maior que zero.");
        }
        this.id = id;
        this.ordem = ordem;
        alterarDescricao(descricao);
    }
    public int getId() {
        return id;
    }
    public int getOrdem() {
        return ordem;
    }
    public String getDescricao() {
        return descricao;
    }
    public void alterarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descricao do item deve ser informada.");
        }
        this.descricao = descricao.trim();
    }
}