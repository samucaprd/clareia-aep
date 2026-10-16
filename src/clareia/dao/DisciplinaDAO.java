package clareia.dao;

import clareia.infra.ConexaoBanco;
import clareia.model.Disciplina;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {
    private final ConexaoBanco banco;
    public DisciplinaDAO(ConexaoBanco banco) {
        this.banco = banco;
    }
    public Disciplina inserir(Disciplina disciplina) throws SQLException {
        String sql = "INSERT INTO disciplinas (codigo, nome) VALUES (?, ?)";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, disciplina.getCodigo());
            comando.setString(2, disciplina.getNome());
            comando.executeUpdate();
            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return new Disciplina(resultado.getInt(1), disciplina.getCodigo(), disciplina.getNome());
                }
            }
            throw new SQLException("Nao foi possivel recuperar o ID da disciplina.");
        }
    }
    public List<Disciplina> listar() throws SQLException {
        List<Disciplina> disciplinas = new ArrayList<>();
        String sql = "SELECT id, codigo, nome FROM disciplinas ORDER BY nome, id";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                Disciplina disciplina = new Disciplina(resultado.getInt("id"), resultado.getString("codigo"), resultado.getString("nome"));
                disciplinas.add(disciplina);
            }
        }
        return disciplinas;
    }
}