package clareia;

import clareia.infra.ConexaoBanco;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class TesteConexao {

    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.out.println(
                "Execute este teste pelo terminal, usando os comandos indicados."
            );
            return;
        }

        char[] senhaDigitada = console.readPassword(
            "Digite a senha do MySQL para o usuario root: "
        );

        if (senhaDigitada == null) {
            System.out.println("Teste cancelado.");
            return;
        }

        ConexaoBanco banco = new ConexaoBanco(
            "root",
            new String(senhaDigitada)
        );

        Arrays.fill(senhaDigitada, '\0');

        String sql = "SELECT DATABASE() AS banco, VERSION() AS versao";

        try (
            Connection conexao = banco.abrirConexao();
            PreparedStatement comando = conexao.prepareStatement(sql);
            ResultSet resultado = comando.executeQuery()
        ) {
            if (resultado.next()) {
                System.out.println("Conexao realizada com sucesso!");
                System.out.println("Banco: " + resultado.getString("banco"));
                System.out.println("MySQL: " + resultado.getString("versao"));
            }
        } catch (SQLException e) {
            System.out.println("Nao foi possivel conectar ao banco.");
            System.out.println("Detalhes: " + e.getMessage());
        }
    }
}