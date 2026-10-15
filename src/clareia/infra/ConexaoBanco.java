package clareia.infra;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoBanco {

    private final String usuario;
    private final String senha;

    public ConexaoBanco(String usuario, String senha) {
        this.usuario = usuario;
        this.senha = senha;
    }

    public Connection abrirConexao() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/clareia";

        return DriverManager.getConnection(url, usuario, senha);
    }
}