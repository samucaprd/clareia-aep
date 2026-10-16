package clareia.model;

import java.time.LocalDateTime;

public class Inscricao {
    private final int id;
    private final Usuario estudante;
    private final SessaoMonitoria sessao;
    private final LocalDateTime criadaEm;
    private StatusInscricao status;
    private StatusPresenca presenca;
    public Inscricao(int id, Usuario estudante, SessaoMonitoria sessao, LocalDateTime criadaEm, StatusInscricao status, StatusPresenca presenca) {
        if (id < 0) {
            throw new IllegalArgumentException("ID da inscricao invalido.");
        }
        if (estudante == null || sessao == null) {
            throw new IllegalArgumentException("O estudante e a sessao devem ser informados.");
        }
        if (criadaEm == null) {
            throw new IllegalArgumentException("A data de criacao deve ser informada.");
        }
        if (status == null || presenca == null) {
            throw new IllegalArgumentException("O status da inscricao e da presenca devem ser informados.");
        }
        this.id = id;
        this.estudante = estudante;
        this.sessao = sessao;
        this.criadaEm = criadaEm;
        this.status = status;
        this.presenca = presenca;
    }
    public void cancelar() {
        if (status != StatusInscricao.ATIVA) {
            throw new IllegalArgumentException("Somente inscricoes ativas podem ser canceladas.");
        }
        status = StatusInscricao.CANCELADA;
    }
    public void registrarPresenca(StatusPresenca presenca) {
        if (status != StatusInscricao.ATIVA) {
            throw new IllegalArgumentException("Somente inscricoes ativas podem receber presenca.");
        }
        if (sessao.getStatus() == StatusSessao.CANCELADA) {
            throw new IllegalArgumentException("Sessoes canceladas nao podem receber presenca.");
        }
        if (presenca == null || presenca == StatusPresenca.PENDENTE) {
            throw new IllegalArgumentException("Informe PRESENTE ou AUSENTE.");
        }
        this.presenca = presenca;
    }
    public int getId() {
        return id;
    }
    public Usuario getEstudante() {
        return estudante;
    }
    public SessaoMonitoria getSessao() {
        return sessao;
    }
    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
    public StatusInscricao getStatus() {
        return status;
    }
    public StatusPresenca getPresenca() {
        return presenca;
    }
}