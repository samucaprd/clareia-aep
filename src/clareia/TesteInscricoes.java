package clareia;

import clareia.dao.InscricaoDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.infra.ConexaoBanco;
import clareia.service.InscricaoService;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class TesteInscricoes {
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.out.println("Execute este programa pelo terminal.");
            return;
        }
        char[] senha = console.readPassword("Senha do MySQL (root): ");
        if (senha == null) {
            return;
        }
        ConexaoBanco banco = new ConexaoBanco("root", new String(senha));
        Arrays.fill(senha, '\0');
        InscricaoDAO dao = new InscricaoDAO();
        SessaoMonitoriaDAO sessaoDAO = new SessaoMonitoriaDAO(banco);
        InscricaoService service = new InscricaoService(banco, dao, sessaoDAO);
        while (true) {
            System.out.println("\n--- INSCRICOES ---");
            System.out.println("1 - Inscrever estudante");
            System.out.println("2 - Conferir inscricoes no banco");
            System.out.println("0 - Sair");
            String opcao = console.readLine("Opcao: ");
            if (opcao == null || opcao.trim().equals("0")) {
                return;
            }
            try {
                switch (opcao.trim()) {
                    case "1":
                        String entradaEstudante = console.readLine("ID do estudante: ");
                        if (entradaEstudante == null) {
                            return;
                        }
                        String entradaSessao = console.readLine("ID da sessao: ");
                        if (entradaSessao == null) {
                            return;
                        }
                        int estudanteId = Integer.parseInt(entradaEstudante.trim());
                        int sessaoId = Integer.parseInt(entradaSessao.trim());
                        int id = service.inscrever(estudanteId, sessaoId);
                        System.out.println("Inscricao realizada! ID: " + id);
                        break;
                    case "2":
                        conferirInscricoes(banco);
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Os IDs devem ser numeros inteiros.");
            } catch (IllegalArgumentException e) {
                System.out.println("Inscricao rejeitada: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco: " + e.getMessage());
            }
        }
    }
    private static void conferirInscricoes(ConexaoBanco banco) throws SQLException {
        String sql = "SELECT id, estudante_id, sessao_id, status, presenca FROM inscricoes ORDER BY id";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            boolean encontrou = false;
            while (resultado.next()) {
                encontrou = true;
                System.out.println("ID: " + resultado.getInt("id") + " | Estudante: " + resultado.getInt("estudante_id") + " | Sessao: " + resultado.getInt("sessao_id") + " | Status: " + resultado.getString("status") + " | Presenca: " + resultado.getString("presenca"));
            }
            if (!encontrou) {
                System.out.println("Nenhuma inscricao registrada.");
            }
        }
    }
}