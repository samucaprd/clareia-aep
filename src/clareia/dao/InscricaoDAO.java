package clareia.dao;

import clareia.model.Inscricao;
import clareia.model.SessaoMonitoria;
import clareia.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class InscricaoDAO {
    public Usuario bloquearEstudante(Connection conexao, int estudanteId) throws SQLException {
        String sql = "SELECT id, nome_completo, matricula, email, habilitado_monitor FROM usuarios WHERE id = ? FOR UPDATE";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, estudanteId);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return new Usuario(resultado.getInt("id"), resultado.getString("nome_completo"), resultado.getString("matricula"), resultado.getString("email"), resultado.getBoolean("habilitado_monitor"));
                }
                return null;
            }
        }
    }
    public boolean bloquearSessao(Connection conexao, int sessaoId) throws SQLException {
        try (PreparedStatement comando = conexao.prepareStatement("SELECT id FROM sessoes_monitoria WHERE id = ? FOR UPDATE")) {
            comando.setInt(1, sessaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }
    public boolean existeInscricao(Connection conexao, int estudanteId, int sessaoId) throws SQLException {
        String sql = "SELECT id FROM inscricoes WHERE estudante_id = ? AND sessao_id = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, estudanteId);
            comando.setInt(2, sessaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }
    public int contarAtivas(Connection conexao, int sessaoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscricoes WHERE sessao_id = ? AND status = 'ATIVA'";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, sessaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                return resultado.getInt(1);
            }
        }
    }
    public boolean existeConflito(Connection conexao, int estudanteId, SessaoMonitoria sessao) throws SQLException {
        String sql = "SELECT i.id FROM inscricoes i JOIN sessoes_monitoria s ON s.id = i.sessao_id "
                + "WHERE i.estudante_id = ? AND i.status = 'ATIVA' AND s.data = ? "
                + "AND s.hora_inicio < ? AND s.hora_fim > ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, estudanteId);
            comando.setDate(2, java.sql.Date.valueOf(sessao.getData()));
            comando.setTime(3, java.sql.Time.valueOf(sessao.getHoraFim()));
            comando.setTime(4, java.sql.Time.valueOf(sessao.getHoraInicio()));
            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        }
    }
    public int inserir(Connection conexao, Inscricao inscricao) throws SQLException {
        String sql = "INSERT INTO inscricoes (sessao_id, estudante_id, data_criacao, status, presenca) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setInt(1, inscricao.getSessao().getId());
            comando.setInt(2, inscricao.getEstudante().getId());
            comando.setTimestamp(3, Timestamp.valueOf(inscricao.getCriadaEm()));
            comando.setString(4, inscricao.getStatus().name());
            comando.setString(5, inscricao.getPresenca().name());
            comando.executeUpdate();
            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
            }
            throw new SQLException("Nao foi possivel recuperar o ID da inscricao.");
        }
    }
}