package clareia.service;

import clareia.dao.DisciplinaDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.dao.UsuarioDAO;
import clareia.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessaoMonitoriaService {
    private final SessaoMonitoriaDAO sessaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final DisciplinaDAO disciplinaDAO;
    public SessaoMonitoriaService(SessaoMonitoriaDAO sessaoDAO, UsuarioDAO usuarioDAO, DisciplinaDAO disciplinaDAO) {
        this.sessaoDAO = sessaoDAO;
        this.usuarioDAO = usuarioDAO;
        this.disciplinaDAO = disciplinaDAO;
    }
    public int cadastrar(SessaoMonitoria sessao, int usuarioResponsavelId) throws SQLException {
        if (sessao == null) {
            throw new IllegalArgumentException("A sessao deve ser informada.");
        }
        if (sessao.getId() != 0) {
            throw new IllegalArgumentException("Uma nova sessao deve possuir ID zero.");
        }
        if (sessao.getStatus() != StatusSessao.AGENDADA) {
            throw new IllegalArgumentException("Uma nova sessao deve estar agendada.");
        }
        LocalDateTime inicio = LocalDateTime.of(sessao.getData(), sessao.getHoraInicio());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A sessao deve iniciar no futuro.");
        }
        if (usuarioResponsavelId != sessao.getMonitor().getId()) {
            throw new IllegalArgumentException("O cadastro deve ser realizado pelo monitor da sessao.");
        }
        Usuario monitor = null;
        for (Usuario usuario : usuarioDAO.listar()) {
            if (usuario.getId() == usuarioResponsavelId) {
                monitor = usuario;
                break;
            }
        }
        if (monitor == null || !monitor.isHabilitadoMonitor()) {
            throw new IllegalArgumentException("O monitor deve estar cadastrado e habilitado.");
        }
        Disciplina disciplina = null;
        for (Disciplina cadastrada : disciplinaDAO.listar()) {
            if (cadastrada.getId() == sessao.getDisciplina().getId()) {
                disciplina = cadastrada;
                break;
            }
        }
        if (disciplina == null) {
            throw new IllegalArgumentException("A disciplina deve estar cadastrada.");
        }
        SessaoMonitoria validada;
        if (sessao instanceof SessaoPresencial) {
            String sala = ((SessaoPresencial) sessao).getSala();
            validada = new SessaoPresencial(0, disciplina, monitor, sessao.getPauta(), sessao.getData(), sessao.getHoraInicio(), sessao.getHoraFim(), sessao.getCapacidade(), StatusSessao.AGENDADA, sala);
        } else if (sessao instanceof SessaoRemota) {
            String link = ((SessaoRemota) sessao).getLinkAcesso();
            validada = new SessaoRemota(0, disciplina, monitor, sessao.getPauta(), sessao.getData(), sessao.getHoraInicio(), sessao.getHoraFim(), sessao.getCapacidade(), StatusSessao.AGENDADA, link);
        } else {
            throw new IllegalArgumentException("Modalidade de sessao invalida.");
        }
        return sessaoDAO.inserir(validada);
    }
    public List<SessaoMonitoria> listar() throws SQLException {
        return sessaoDAO.listar();
    }
    public SessaoMonitoria buscarPorId(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Informe um ID maior que zero.");
        }
        SessaoMonitoria sessao = sessaoDAO.buscarPorId(id);
        if (sessao == null) {
            throw new IllegalArgumentException("Sessao nao encontrada.");
        }
        return sessao;
    }
    public List<SessaoMonitoria> buscarPorDisciplinaEPeriodo(int disciplinaId, LocalDate inicio, LocalDate fim) throws SQLException {
        if (disciplinaId <= 0) {
            throw new IllegalArgumentException("Informe uma disciplina valida.");
        }
        if (inicio == null || fim == null) {
            throw new IllegalArgumentException("Informe as duas datas do periodo.");
        }
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final nao pode ser anterior a inicial.");
        }
        List<SessaoMonitoria> encontradas = new ArrayList<>();
        for (SessaoMonitoria sessao : sessaoDAO.listar()) {
            boolean mesmaDisciplina = sessao.getDisciplina().getId() == disciplinaId;
            boolean dentroDoPeriodo = !sessao.getData().isBefore(inicio) && !sessao.getData().isAfter(fim);
            if (mesmaDisciplina && dentroDoPeriodo) {
                encontradas.add(sessao);
            }
        }
        return encontradas;
    }
    public int consultarVagasDisponiveis(int sessaoId) throws SQLException {
        SessaoMonitoria sessao = buscarPorId(sessaoId);
        LocalDateTime inicio = LocalDateTime.of(sessao.getData(), sessao.getHoraInicio());
        if (sessao.getStatus() != StatusSessao.AGENDADA || !inicio.isAfter(LocalDateTime.now())) {
            return 0;
        }
        int inscricoesAtivas = sessaoDAO.contarInscricoesAtivas(sessaoId);
        return Math.max(0, sessao.getCapacidade() - inscricoesAtivas);
    }
        public void excluir(int sessaoId, int usuarioResponsavelId) throws SQLException {
        SessaoMonitoria sessao = buscarPorId(sessaoId);
        if (sessao.getMonitor().getId() != usuarioResponsavelId) {
            throw new IllegalArgumentException("Somente o monitor responsavel pode excluir a sessao.");
        }
        LocalDateTime inicio = LocalDateTime.of(sessao.getData(), sessao.getHoraInicio());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A sessao so pode ser excluida antes do inicio.");
        }
        if (!sessaoDAO.excluir(sessaoId, usuarioResponsavelId)) {
            throw new IllegalArgumentException("Exclusao nao realizada. Verifique se existem inscricoes ou se a sessao foi alterada ou iniciada.");
        }
    }
        public void editar(SessaoMonitoria alterada, int usuarioResponsavelId) throws SQLException {
        if (alterada == null) {
            throw new IllegalArgumentException("A sessao deve ser informada.");
        }
        SessaoMonitoria atual = buscarPorId(alterada.getId());
        if (atual.getMonitor().getId() != usuarioResponsavelId) {
            throw new IllegalArgumentException("Somente o monitor responsavel pode editar a sessao.");
        }
        LocalDateTime inicioAtual = LocalDateTime.of(atual.getData(), atual.getHoraInicio());
        if (!inicioAtual.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("A sessao so pode ser editada antes do inicio.");
        }
        LocalDateTime novoInicio = LocalDateTime.of(alterada.getData(), alterada.getHoraInicio());
        if (!novoInicio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("O novo inicio deve estar no futuro.");
        }
        if (alterada.getStatus() != atual.getStatus()) {
            throw new IllegalArgumentException("Use a operacao especifica para cancelar ou concluir a sessao.");
        }
        boolean disciplinaExiste = false;
        for (Disciplina disciplina : disciplinaDAO.listar()) {
            if (disciplina.getId() == alterada.getDisciplina().getId()) {
                disciplinaExiste = true;
                break;
            }
        }
        if (!disciplinaExiste) {
            throw new IllegalArgumentException("A disciplina deve estar cadastrada.");
        }
        boolean monitorValido = false;
        for (Usuario usuario : usuarioDAO.listar()) {
            if (usuario.getId() == alterada.getMonitor().getId() && usuario.isHabilitadoMonitor()) {
                monitorValido = true;
                break;
            }
        }
        if (!monitorValido) {
            throw new IllegalArgumentException("O monitor deve estar cadastrado e habilitado.");
        }
        if (!sessaoDAO.atualizar(alterada, usuarioResponsavelId)) {
            throw new IllegalArgumentException("Edicao nao realizada. Verifique as restricoes de inscricoes, capacidade, horario e responsavel.");
        }
    }
}