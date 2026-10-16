package clareia.dao;

import clareia.infra.ConexaoBanco;
import clareia.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class SessaoMonitoriaDAO {
    private final ConexaoBanco banco;
    public SessaoMonitoriaDAO(ConexaoBanco banco) {
        this.banco = banco;
    }
    public int inserir(SessaoMonitoria sessao) throws SQLException {
        String modalidade;
        String sala = null;
        String link = null;
        if (sessao instanceof SessaoPresencial) {
            modalidade = "PRESENCIAL";
            sala = ((SessaoPresencial) sessao).getSala();
        } else if (sessao instanceof SessaoRemota) {
            modalidade = "REMOTA";
            link = ((SessaoRemota) sessao).getLinkAcesso();
        } else {
            throw new IllegalArgumentException("Modalidade de sessao invalida.");
        }
        String sql = "INSERT INTO sessoes_monitoria (disciplina_id, monitor_id, modalidade, sala, link_acesso, data, hora_inicio, hora_fim, capacidade, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlPauta = "INSERT INTO itens_pauta (sessao_id, ordem, descricao) VALUES (?, ?, ?)";
        try (Connection conexao = banco.abrirConexao()) {
            conexao.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    comando.setInt(1, sessao.getDisciplina().getId());
                    comando.setInt(2, sessao.getMonitor().getId());
                    comando.setString(3, modalidade);
                    if (sala == null) {
                        comando.setNull(4, Types.VARCHAR);
                    } else {
                        comando.setString(4, sala);
                    }
                    if (link == null) {
                        comando.setNull(5, Types.VARCHAR);
                    } else {
                        comando.setString(5, link);
                    }
                    comando.setDate(6, java.sql.Date.valueOf(sessao.getData()));
                    comando.setTime(7, java.sql.Time.valueOf(sessao.getHoraInicio()));
                    comando.setTime(8, java.sql.Time.valueOf(sessao.getHoraFim()));
                    comando.setInt(9, sessao.getCapacidade());
                    comando.setString(10, sessao.getStatus().name());
                    comando.executeUpdate();
                    try (ResultSet resultado = comando.getGeneratedKeys()) {
                        if (!resultado.next()) {
                            throw new SQLException("Nao foi possivel recuperar o ID da sessao.");
                        }
                        id = resultado.getInt(1);
                    }
                }
                try (PreparedStatement comando = conexao.prepareStatement(sqlPauta)) {
                    for (ItemPauta item : sessao.getPauta()) {
                        comando.setInt(1, id);
                        comando.setInt(2, item.getOrdem());
                        comando.setString(3, item.getDescricao());
                        comando.executeUpdate();
                    }
                }
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