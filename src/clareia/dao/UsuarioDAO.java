package clareia.dao;

import clareia.infra.ConexaoBanco;
import clareia.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private final ConexaoBanco banco;

    public UsuarioDAO(ConexaoBanco banco) {
        this.banco = banco;
    }

    public Usuario inserir(Usuario usuario) throws SQLException {
        String sql = """
            INSERT INTO usuarios
                (nome_completo, matricula, email, habilitado_monitor)
            VALUES (?, ?, ?, ?)
            """;

        try (
            Connection conexao = banco.abrirConexao();
            PreparedStatement comando = conexao.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS
            )
        ) {
            comando.setString(1, usuario.getNomeCompleto());
            comando.setString(2, usuario.getMatricula());
            comando.setString(3, usuario.getEmail());
            comando.setBoolean(4, usuario.isHabilitadoMonitor());
            comando.executeUpdate();

            try (ResultSet resultado = comando.getGeneratedKeys()) {
                if (resultado.next()) {
                    return new Usuario(
                        resultado.getInt(1),
                        usuario.getNomeCompleto(),
                        usuario.getMatricula(),
                        usuario.getEmail(),
                        usuario.isHabilitadoMonitor()
                    );
                }
            }

            throw new SQLException("O banco nao retornou o identificador do usuario.");
        }
    }

    public List<Usuario> listar() throws SQLException {
        String sql = """
            SELECT id, nome_completo, matricula, email, habilitado_monitor
            FROM usuarios
            ORDER BY nome_completo, id
            """;

        List<Usuario> usuarios = new ArrayList<>();

        try (
            Connection conexao = banco.abrirConexao();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            while (resultado.next()) {
            Usuario usuario = new Usuario(
                resultado.getInt("id"),
                resultado.getString("nome_completo"),
                resultado.getString("matricula"),
                resultado.getString("email"),
                resultado.getBoolean("habilitado_monitor"));

                usuarios.add(usuario);
            }
        }

        return usuarios;
    }
}