package clareia;

import clareia.dao.*;
import clareia.infra.ConexaoBanco;
import clareia.service.CancelamentoService;
import clareia.service.SessaoMonitoriaService;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class TesteCancelamentos {
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
        InscricaoDAO inscricaoDAO = new InscricaoDAO();
        SessaoMonitoriaDAO sessaoDAO = new SessaoMonitoriaDAO(banco);
        CancelamentoService service = new CancelamentoService(banco, inscricaoDAO, sessaoDAO);
        SessaoMonitoriaService sessaoService = new SessaoMonitoriaService(sessaoDAO, new UsuarioDAO(banco), new DisciplinaDAO(banco));
        while (true) {
            System.out.println("\n--- CANCELAMENTOS ---");
            System.out.println("1 - Cancelar inscricao");
            System.out.println("2 - Cancelar sessao");
            System.out.println("3 - Conferir sessao e inscricoes");
            System.out.println("0 - Sair");
            String opcao = console.readLine("Opcao: ");
            if (opcao == null || opcao.trim().equals("0")) {
                return;
            }
            try {
                switch (opcao.trim()) {
                    case "1":
                        Integer inscricaoId = lerId(console, "ID da inscricao: ");
                        if (inscricaoId == null) {
                            return;
                        }
                        Integer estudanteId = lerId(console, "ID do estudante que esta operando: ");
                        if (estudanteId == null) {
                            return;
                        }
                        service.cancelarInscricao(inscricaoId, estudanteId);
                        System.out.println("Inscricao cancelada.");
                        break;
                    case "2":
                        Integer sessaoId = lerId(console, "ID da sessao: ");
                        if (sessaoId == null) {
                            return;
                        }
                        Integer monitorId = lerId(console, "ID do usuario que esta operando: ");
                        if (monitorId == null) {
                            return;
                        }
                        service.cancelarSessao(sessaoId, monitorId);
                        System.out.println("Sessao e inscricoes ativas canceladas.");
                        break;
                    case "3":
                        Integer consultaId = lerId(console, "ID da sessao: ");
                        if (consultaId == null) {
                            return;
                        }
                        System.out.println("Status da sessao: " + sessaoService.buscarPorId(consultaId).getStatus());
                        System.out.println("Vagas disponiveis: " + sessaoService.consultarVagasDisponiveis(consultaId));
                        conferirInscricoes(banco, consultaId);
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Informe um numero inteiro.");
            } catch (IllegalArgumentException e) {
                System.out.println("Operacao rejeitada: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco: " + e.getMessage());
            }
        }
    }
    private static Integer lerId(Console console, String mensagem) {
        String entrada = console.readLine(mensagem);
        if (entrada == null) {
            return null;
        }
        return Integer.parseInt(entrada.trim());
    }
    private static void conferirInscricoes(ConexaoBanco banco, int sessaoId) throws SQLException {
        String sql = "SELECT id, estudante_id, status, presenca FROM inscricoes WHERE sessao_id = ? ORDER BY id";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, sessaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    System.out.println("Inscricao: " + resultado.getInt("id") + " | Estudante: " + resultado.getInt("estudante_id") + " | Status: " + resultado.getString("status") + " | Presenca: " + resultado.getString("presenca"));
                }
            }
        }
    }
}