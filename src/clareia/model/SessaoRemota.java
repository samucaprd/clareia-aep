package clareia.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SessaoRemota extends SessaoMonitoria {
    private final String linkAcesso;
    public SessaoRemota(int id, Disciplina disciplina, Usuario monitor, List<ItemPauta> pauta, LocalDate data, LocalTime horaInicio, LocalTime horaFim, int capacidade, StatusSessao status, String linkAcesso) {
        super(id, disciplina, monitor, pauta, data, horaInicio, horaFim, capacidade, status);
        if (linkAcesso == null || linkAcesso.isBlank()) {
            throw new IllegalArgumentException("O link de acesso deve ser informado.");
        }
        linkAcesso = linkAcesso.trim();
        if (linkAcesso.length() > 500) {
            throw new IllegalArgumentException("O link deve ter no maximo 500 caracteres.");
        }
        this.linkAcesso = linkAcesso;
    }
    public String getLinkAcesso() {
        return linkAcesso;
    }
    @Override
    public String obterOrientacaoAcesso() {
        return "Acessar a monitoria pelo link: " + linkAcesso;
    }
}