package clareia.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public abstract class SessaoMonitoria {
    private final int id;
    private final Disciplina disciplina;
    private final Usuario monitor;
    private final List<ItemPauta> pauta;
    private final LocalDate data;
    private final LocalTime horaInicio;
    private final LocalTime horaFim;
    private final int capacidade;
    private StatusSessao status;
    protected SessaoMonitoria(int id, Disciplina disciplina, Usuario monitor, List<ItemPauta> pauta, LocalDate data, LocalTime horaInicio, LocalTime horaFim, int capacidade, StatusSessao status) {
        if (id < 0) {
            throw new IllegalArgumentException("ID da sessao invalido.");
        }
        if (disciplina == null) {
            throw new IllegalArgumentException("A disciplina deve ser informada.");
        }
        if (monitor == null || !monitor.isHabilitadoMonitor()) {
            throw new IllegalArgumentException("A sessao deve possuir um monitor habilitado.");
        }
        if (pauta == null || pauta.isEmpty()) {
            throw new IllegalArgumentException("A sessao deve possuir pelo menos um item de pauta.");
        }
        if (data == null || horaInicio == null || horaFim == null) {
            throw new IllegalArgumentException("A data e os horarios devem ser informados.");
        }
        if (!horaFim.isAfter(horaInicio)) {
            throw new IllegalArgumentException("O horario final deve ser posterior ao inicial.");
        }
        if (capacidade <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        }
        if (status == null) {
            throw new IllegalArgumentException("O status deve ser informado.");
        }
        this.id = id;
        this.disciplina = disciplina;
        this.monitor = monitor;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.capacidade = capacidade;
        this.status = status;
        this.pauta = new ArrayList<>();
        for (ItemPauta item : pauta) {
            adicionarItem(item);
        }
    }
    public abstract String obterOrientacaoAcesso();
    public void adicionarItem(ItemPauta item) {
        if (item == null) {
            throw new IllegalArgumentException("O item de pauta deve ser informado.");
        }
        for (ItemPauta existente : pauta) {
            if (existente.getOrdem() == item.getOrdem()) {
                throw new IllegalArgumentException("Ja existe um item com essa ordem na pauta.");
            }
        }
        pauta.add(new ItemPauta(item.getId(), item.getOrdem(), item.getDescricao()));
    }
    public void cancelar() {
        if (status != StatusSessao.AGENDADA) {
            throw new IllegalArgumentException("Somente sessoes agendadas podem ser canceladas.");
        }
        status = StatusSessao.CANCELADA;
    }
    public void concluir() {
        if (status != StatusSessao.AGENDADA) {
            throw new IllegalArgumentException("Somente sessoes agendadas podem ser concluidas.");
        }
        status = StatusSessao.CONCLUIDA;
    }
    public int getId() {
        return id;
    }
    public Disciplina getDisciplina() {
        return disciplina;
    }
    public Usuario getMonitor() {
        return monitor;
    }
    public List<ItemPauta> getPauta() {
        List<ItemPauta> copia = new ArrayList<>();
        for (ItemPauta item : pauta) {
            copia.add(new ItemPauta(item.getId(), item.getOrdem(), item.getDescricao()));
        }
        return copia;
    }
    public LocalDate getData() {
        return data;
    }
    public LocalTime getHoraInicio() {
        return horaInicio;
    }
    public LocalTime getHoraFim() {
        return horaFim;
    }
    public int getCapacidade() {
        return capacidade;
    }
    public StatusSessao getStatus() {
        return status;
    }
}