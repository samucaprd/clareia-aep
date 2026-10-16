package clareia.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SessaoPresencial extends SessaoMonitoria {
    private final String sala;
    public SessaoPresencial(int id, Disciplina disciplina, Usuario monitor, List<ItemPauta> pauta, LocalDate data, LocalTime horaInicio, LocalTime horaFim, int capacidade, StatusSessao status, String sala) {
        super(id, disciplina, monitor, pauta, data, horaInicio, horaFim, capacidade, status);
        if (sala == null || sala.isBlank()) {
            throw new IllegalArgumentException("A sala deve ser informada.");
        }
        sala = sala.trim();
        if (sala.length() > 150) {
            throw new IllegalArgumentException("A sala deve ter no maximo 150 caracteres.");
        }
        this.sala = sala;
    }
    public String getSala() {
        return sala;
    }
    @Override
    public String obterOrientacaoAcesso() {
        return "Comparecer a sala: " + sala;
    }
}