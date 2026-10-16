package clareia.dao;

import clareia.infra.ConexaoBanco;
import clareia.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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

        public List<SessaoMonitoria> listar() throws SQLException {
        return consultar(null);
    }
    public SessaoMonitoria buscarPorId(int id) throws SQLException {
        List<SessaoMonitoria> sessoes = consultar(id);
        if (sessoes.isEmpty()) {
            return null;
        }
        return sessoes.get(0);
    }
    private List<SessaoMonitoria> consultar(Integer id) throws SQLException {
        List<SessaoMonitoria> sessoes = new ArrayList<>();
        String sql = "SELECT s.*, d.codigo AS disciplina_codigo, d.nome AS disciplina_nome, "
                + "u.nome_completo, u.matricula, u.email, u.habilitado_monitor "
                + "FROM sessoes_monitoria s "
                + "JOIN disciplinas d ON d.id = s.disciplina_id "
                + "JOIN usuarios u ON u.id = s.monitor_id";
        if (id != null) {
            sql += " WHERE s.id = ?";
        }
        sql += " ORDER BY s.data, s.hora_inicio, s.id";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            if (id != null) {
                comando.setInt(1, id);
            }
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    sessoes.add(montarSessao(conexao, resultado));
                }
            }
        }
        return sessoes;
    }
    private List<ItemPauta> buscarPauta(Connection conexao, int sessaoId) throws SQLException {
        List<ItemPauta> pauta = new ArrayList<>();
        String sql = "SELECT id, ordem, descricao FROM itens_pauta WHERE sessao_id = ? ORDER BY ordem";
        try (PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, sessaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    pauta.add(new ItemPauta(resultado.getInt("id"), resultado.getInt("ordem"), resultado.getString("descricao")));
                }
            }
        }
        return pauta;
    }
    private SessaoMonitoria montarSessao(Connection conexao, ResultSet resultado) throws SQLException {
        int id = resultado.getInt("id");
        Disciplina disciplina = new Disciplina(resultado.getInt("disciplina_id"), resultado.getString("disciplina_codigo"), resultado.getString("disciplina_nome"));
        Usuario monitor = new Usuario(resultado.getInt("monitor_id"), resultado.getString("nome_completo"), resultado.getString("matricula"), resultado.getString("email"), resultado.getBoolean("habilitado_monitor"));
        List<ItemPauta> pauta = buscarPauta(conexao, id);
        String modalidade = resultado.getString("modalidade");
        try {
            StatusSessao status = StatusSessao.valueOf(resultado.getString("status"));
            if ("PRESENCIAL".equals(modalidade)) {
                return new SessaoPresencial(id, disciplina, monitor, pauta, resultado.getDate("data").toLocalDate(), resultado.getTime("hora_inicio").toLocalTime(), resultado.getTime("hora_fim").toLocalTime(), resultado.getInt("capacidade"), status, resultado.getString("sala"));
            }
            if ("REMOTA".equals(modalidade)) {
                return new SessaoRemota(id, disciplina, monitor, pauta, resultado.getDate("data").toLocalDate(), resultado.getTime("hora_inicio").toLocalTime(), resultado.getTime("hora_fim").toLocalTime(), resultado.getInt("capacidade"), status, resultado.getString("link_acesso"));
            }
            throw new SQLException("Modalidade desconhecida na sessao " + id);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Dados invalidos na sessao " + id + ": " + e.getMessage(), e);
        }
    }
}
