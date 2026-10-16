package clareia.service;

import clareia.dao.InscricaoDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.SessaoMonitoria;
import clareia.model.StatusSessao;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class CancelamentoService {
    private final ConexaoBanco banco;
    private final InscricaoDAO inscricaoDAO;
    private final SessaoMonitoriaDAO sessaoDAO;
    public CancelamentoService(ConexaoBanco banco, InscricaoDAO inscricaoDAO, SessaoMonitoriaDAO sessaoDAO) {
        this.banco = banco;
        this.inscricaoDAO = inscricaoDAO;
        this.sessaoDAO = sessaoDAO;
    }
    public void cancelarInscricao(int inscricaoId, int estudanteId) throws SQLException {
        if (inscricaoId <= 0 || estudanteId <= 0) {
            throw new IllegalArgumentException("Informe IDs validos.");
        }
        try (Connection conexao = banco.abrirConexao()) {
            conexao.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexao.setAutoCommit(false);
            try {
                if (inscricaoDAO.bloquearEstudante(conexao, estudanteId) == null) {
                    throw new IllegalArgumentException("Estudante nao encontrado.");
                }
                Integer sessaoId = inscricaoDAO.buscarSessaoDaInscricao(conexao, inscricaoId, estudanteId);
                if (sessaoId == null) {
                    throw new IllegalArgumentException("Inscricao nao encontrada para esse estudante.");
                }
                if (!inscricaoDAO.bloquearSessao(conexao, sessaoId)) {
                    throw new IllegalArgumentException("Sessao nao encontrada.");
                }
                SessaoMonitoria sessao = sessaoDAO.buscarPorId(conexao, sessaoId);
                validarAntesDoInicio(sessao);
                if (sessao.getStatus() != StatusSessao.AGENDADA) {
                    throw new IllegalArgumentException("A sessao nao esta agendada.");
                }
                if (!inscricaoDAO.cancelarInscricao(conexao, inscricaoId, estudanteId)) {
                    throw new IllegalArgumentException("A inscricao nao esta ativa.");
                }
                conexao.commit();
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
    public void cancelarSessao(int sessaoId, int monitorId) throws SQLException {
        if (sessaoId <= 0 || monitorId <= 0) {
            throw new IllegalArgumentException("Informe IDs validos.");
        }
        try (Connection conexao = banco.abrirConexao()) {
            conexao.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexao.setAutoCommit(false);
            try {
                if (!inscricaoDAO.bloquearSessao(conexao, sessaoId)) {
                    throw new IllegalArgumentException("Sessao nao encontrada.");
                }
                SessaoMonitoria sessao = sessaoDAO.buscarPorId(conexao, sessaoId);
                if (sessao == null) {
                    throw new IllegalArgumentException("Sessao nao encontrada.");
                }
                if (sessao.getMonitor().getId() != monitorId) {
                    throw new IllegalArgumentException("Somente o monitor responsavel pode cancelar a sessao.");
                }
                validarAntesDoInicio(sessao);
                sessao.cancelar();
                if (!sessaoDAO.cancelar(conexao, sessaoId, monitorId)) {
                    throw new IllegalArgumentException("A sessao nao pode mais ser cancelada.");
                }
                inscricaoDAO.cancelarAtivasDaSessao(conexao, sessaoId);
                conexao.commit();
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
    private void validarAntesDoInicio(SessaoMonitoria sessao) {
        if (sessao == null) {
            throw new IllegalArgumentException("Sessao nao encontrada.");
        }
        LocalDateTime inicio = LocalDateTime.of(sessao.getData(), sessao.getHoraInicio());
        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("O cancelamento so pode ocorrer antes do inicio.");
        }
    }
}