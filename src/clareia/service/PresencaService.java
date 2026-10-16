package clareia.service;

import clareia.dao.InscricaoDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PresencaService {
    private final ConexaoBanco banco;
    private final InscricaoDAO inscricaoDAO;
    private final SessaoMonitoriaDAO sessaoDAO;
    public PresencaService(ConexaoBanco banco, InscricaoDAO inscricaoDAO, SessaoMonitoriaDAO sessaoDAO) {
        this.banco = banco;
        this.inscricaoDAO = inscricaoDAO;
        this.sessaoDAO = sessaoDAO;
    }
    public List<Inscricao> consultarInscritos(int sessaoId, int monitorId) throws SQLException {
        if (sessaoId <= 0 || monitorId <= 0) {
            throw new IllegalArgumentException("Informe IDs validos.");
        }
        try (Connection conexao = banco.abrirConexao()) {
            SessaoMonitoria sessao = sessaoDAO.buscarPorId(conexao, sessaoId);
            if (sessao == null) {
                throw new IllegalArgumentException("Sessao nao encontrada.");
            }
            if (sessao.getMonitor().getId() != monitorId) {
                throw new IllegalArgumentException("Somente o monitor responsavel pode consultar os inscritos.");
            }
            return inscricaoDAO.listarPorSessao(conexao, sessaoId, sessaoDAO);
        }
    }
    public List<Inscricao> consultarHistorico(int estudanteId) throws SQLException {
        if (estudanteId <= 0) {
            throw new IllegalArgumentException("Informe um ID valido.");
        }
        try (Connection conexao = banco.abrirConexao()) {
            return inscricaoDAO.listarPorEstudante(conexao, estudanteId, sessaoDAO);
        }
    }
    public void registrarPresenca(int inscricaoId, int monitorId, StatusPresenca presenca) throws SQLException {
        if (inscricaoId <= 0 || monitorId <= 0) {
            throw new IllegalArgumentException("Informe IDs validos.");
        }
        if (presenca == null || presenca == StatusPresenca.PENDENTE) {
            throw new IllegalArgumentException("Informe PRESENTE ou AUSENTE.");
        }
        try (Connection conexao = banco.abrirConexao()) {
            conexao.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexao.setAutoCommit(false);
            try {
                Inscricao inscricao = inscricaoDAO.buscarPorId(conexao, inscricaoId, sessaoDAO);
                if (inscricao == null) {
                    throw new IllegalArgumentException("Inscricao nao encontrada.");
                }
                int sessaoId = inscricao.getSessao().getId();
                if (!inscricaoDAO.bloquearSessao(conexao, sessaoId)) {
                    throw new IllegalArgumentException("Sessao nao encontrada.");
                }
                inscricao = inscricaoDAO.buscarPorId(conexao, inscricaoId, sessaoDAO);
                if (inscricao == null) {
                    throw new IllegalArgumentException("Inscricao nao encontrada.");
                }
                SessaoMonitoria sessao = inscricao.getSessao();
                if (sessao.getMonitor().getId() != monitorId) {
                    throw new IllegalArgumentException("Somente o monitor responsavel pode registrar presenca.");
                }
                if (sessao.getStatus() == StatusSessao.CANCELADA) {
                    throw new IllegalArgumentException("Sessoes canceladas nao podem receber presenca.");
                }
                if (inscricao.getStatus() != StatusInscricao.ATIVA) {
                    throw new IllegalArgumentException("Somente inscricoes ativas podem receber presenca.");
                }
                LocalDateTime termino = LocalDateTime.of(sessao.getData(), sessao.getHoraFim());
                if (!LocalDateTime.now().isAfter(termino)) {
                    throw new IllegalArgumentException("A presenca so pode ser registrada apos o termino da sessao.");
                }
                inscricao.registrarPresenca(presenca);
                if (!inscricaoDAO.atualizarPresenca(conexao, inscricao)) {
                    throw new IllegalArgumentException("Nao foi possivel atualizar a presenca.");
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
        public void concluirSessao(int sessaoId, int monitorId) throws SQLException {
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
                    throw new IllegalArgumentException("Somente o monitor responsavel pode concluir a sessao.");
                }
                if (sessao.getStatus() != StatusSessao.AGENDADA) {
                    throw new IllegalArgumentException("Somente sessoes agendadas podem ser concluidas.");
                }
                LocalDateTime termino = LocalDateTime.of(sessao.getData(), sessao.getHoraFim());
                if (!LocalDateTime.now().isAfter(termino)) {
                    throw new IllegalArgumentException("A sessao so pode ser concluida apos o termino.");
                }
                List<Inscricao> inscricoes = inscricaoDAO.listarPorSessao(conexao, sessaoId, sessaoDAO);
                for (Inscricao inscricao : inscricoes) {
                    if (inscricao.getStatus() == StatusInscricao.ATIVA && inscricao.getPresenca() == StatusPresenca.PENDENTE) {
                        throw new IllegalArgumentException("Registre a presenca ou ausencia de todos os inscritos ativos.");
                    }
                }
                sessao.concluir();
                if (!sessaoDAO.concluir(conexao, sessaoId, monitorId)) {
                    throw new IllegalArgumentException("Nao foi possivel concluir a sessao. Verifique o horario e as presencas.");
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
}