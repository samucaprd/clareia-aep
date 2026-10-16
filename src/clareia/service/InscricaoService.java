package clareia.service;

import clareia.dao.InscricaoDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InscricaoService {
    private final ConexaoBanco banco;
    private final InscricaoDAO inscricaoDAO;
    private final SessaoMonitoriaDAO sessaoDAO;
    public InscricaoService(ConexaoBanco banco, InscricaoDAO inscricaoDAO, SessaoMonitoriaDAO sessaoDAO) {
        this.banco = banco;
        this.inscricaoDAO = inscricaoDAO;
        this.sessaoDAO = sessaoDAO;
    }
    public int inscrever(int estudanteId, int sessaoId) throws SQLException {
        if (estudanteId <= 0 || sessaoId <= 0) {
            throw new IllegalArgumentException("Informe IDs validos para estudante e sessao.");
        }
        try (Connection conexao = banco.abrirConexao()) {
            conexao.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexao.setAutoCommit(false);
            try {
                Usuario estudante = inscricaoDAO.bloquearEstudante(conexao, estudanteId);
                if (estudante == null) {
                    throw new IllegalArgumentException("Estudante nao encontrado.");
                }
                if (!inscricaoDAO.bloquearSessao(conexao, sessaoId)) {
                    throw new IllegalArgumentException("Sessao nao encontrada.");
                }
                SessaoMonitoria sessao = sessaoDAO.buscarPorId(conexao, sessaoId);
                if (sessao == null) {
                    throw new IllegalArgumentException("Sessao nao encontrada.");
                }
                LocalDateTime inicio = LocalDateTime.of(sessao.getData(), sessao.getHoraInicio());
                if (!inicio.isAfter(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Somente sessoes futuras podem receber inscricoes.");
                }
                if (sessao.getStatus() != StatusSessao.AGENDADA) {
                    throw new IllegalArgumentException("A sessao nao esta disponivel para inscricoes.");
                }
                if (sessao.getMonitor().getId() == estudanteId) {
                    throw new IllegalArgumentException("O monitor nao pode se inscrever na propria sessao.");
                }
                if (inscricaoDAO.existeInscricao(conexao, estudanteId, sessaoId)) {
                    throw new IllegalArgumentException("O estudante ja possui uma inscricao registrada nessa sessao.");
                }
                if (inscricaoDAO.contarAtivas(conexao, sessaoId) >= sessao.getCapacidade()) {
                    throw new IllegalArgumentException("A sessao esta lotada.");
                }
                if (inscricaoDAO.existeConflito(conexao, estudanteId, sessao)) {
                    throw new IllegalArgumentException("Existe conflito de horario com outra inscricao ativa.");
                }
                Inscricao inscricao = new Inscricao(0, estudante, sessao, LocalDateTime.now(), StatusInscricao.ATIVA, StatusPresenca.PENDENTE);
                int id = inscricaoDAO.inserir(conexao, inscricao);
                conexao.commit();
                return id;
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                throw e;
            }
        }
    }
}