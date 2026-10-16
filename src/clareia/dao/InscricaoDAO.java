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
import clareia.model.StatusInscricao;
import clareia.model.StatusPresenca;
import java.util.ArrayList;
import java.util.List;

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
        public Integer buscarSessaoDaInscricao(Connection conexao, int inscricaoId, int estudanteId) throws SQLException {
        String sql = "SELECT sessao_id FROM inscricoes WHERE id = ? AND estudante_id = ?";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, inscricaoId);
            comando.setInt(2, estudanteId);
            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getInt("sessao_id");
                }
                return null;
            }
        }
    }
    public boolean cancelarInscricao(Connection conexao, int inscricaoId, int estudanteId) throws SQLException {
        String sql = "UPDATE inscricoes SET status = 'CANCELADA' WHERE id = ? AND estudante_id = ? AND status = 'ATIVA'";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, inscricaoId);
            comando.setInt(2, estudanteId);
            return comando.executeUpdate() == 1;
        }
    }
    public int cancelarAtivasDaSessao(Connection conexao, int sessaoId) throws SQLException {
        String sql = "UPDATE inscricoes SET status = 'CANCELADA' WHERE sessao_id = ? AND status = 'ATIVA'";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, sessaoId);
            return comando.executeUpdate();
        }
    }
        public List<Inscricao> listarPorSessao(Connection conexao, int sessaoId, SessaoMonitoriaDAO sessaoDAO) throws SQLException {
        return consultar(conexao, "i.sessao_id", sessaoId, sessaoDAO);
    }
    public List<Inscricao> listarPorEstudante(Connection conexao, int estudanteId, SessaoMonitoriaDAO sessaoDAO) throws SQLException {
        return consultar(conexao, "i.estudante_id", estudanteId, sessaoDAO);
    }
    public Inscricao buscarPorId(Connection conexao, int id, SessaoMonitoriaDAO sessaoDAO) throws SQLException {
        List<Inscricao> inscricoes = consultar(conexao, "i.id", id, sessaoDAO);
        if (inscricoes.isEmpty()) {
            return null;
        }
        return inscricoes.get(0);
    }
    private List<Inscricao> consultar(Connection conexao, String coluna, int valor, SessaoMonitoriaDAO sessaoDAO) throws SQLException {
        List<Inscricao> inscricoes = new ArrayList<>();
        String sql = "SELECT i.*, u.nome_completo, u.matricula, u.email, u.habilitado_monitor "
                + "FROM inscricoes i JOIN usuarios u ON u.id = i.estudante_id "
                + "WHERE " + coluna + " = ? ORDER BY i.data_criacao, i.id";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, valor);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Usuario estudante = new Usuario(resultado.getInt("estudante_id"), resultado.getString("nome_completo"), resultado.getString("matricula"), resultado.getString("email"), resultado.getBoolean("habilitado_monitor"));
                    SessaoMonitoria sessao = sessaoDAO.buscarPorId(conexao, resultado.getInt("sessao_id"));
                    Inscricao inscricao = new Inscricao(resultado.getInt("id"), estudante, sessao, resultado.getTimestamp("data_criacao").toLocalDateTime(), StatusInscricao.valueOf(resultado.getString("status")), StatusPresenca.valueOf(resultado.getString("presenca")));
                    inscricoes.add(inscricao);
                }
            }
        }
        return inscricoes;
    }
    public boolean atualizarPresenca(Connection conexao, Inscricao inscricao) throws SQLException {
        String sql = "UPDATE inscricoes SET presenca = ? WHERE id = ? AND status = 'ATIVA'";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, inscricao.getPresenca().name());
            comando.setInt(2, inscricao.getId());
            return comando.executeUpdate() == 1;
        }
    }
}