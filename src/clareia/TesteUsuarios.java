package clareia;

import clareia.dao.UsuarioDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.Usuario;
import clareia.service.UsuarioService;
import java.io.Console;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TesteUsuarios {
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
        UsuarioDAO dao = new UsuarioDAO(banco);
        UsuarioService service = new UsuarioService(dao);
        while (true) {
            System.out.println("\n--- USUARIOS ---");
            System.out.println("1 - Cadastrar usuario");
            System.out.println("2 - Consultar usuarios");
            System.out.println("0 - Sair");
            String opcao = console.readLine("Opcao: ");
            if (opcao == null || opcao.trim().equals("0")) {
                return;
            }
            try {
                switch (opcao.trim()) {
                    case "1":
                        String nome = console.readLine("Nome completo: ");
                        String matricula = console.readLine("Matricula: ");
                        String email = console.readLine("E-mail: ");
                        String monitor = console.readLine("Habilitado como monitor? (S/N): ");
                        if (monitor == null) {
                            return;
                        }
                        monitor = monitor.trim();
                        if (!monitor.equalsIgnoreCase("S") && !monitor.equalsIgnoreCase("N")) {
                            System.out.println("Informe S ou N. Cadastro nao realizado.");
                            break;
                        }
                        Usuario cadastrado = service.cadastrar(nome, matricula, email, monitor.equalsIgnoreCase("S"));
                        System.out.println("Usuario cadastrado! ID: " + cadastrado.getId());
                        break;
                    case "2":
                        List<Usuario> usuarios = service.listar();
                        if (usuarios.isEmpty()) {
                            System.out.println("Nenhum usuario cadastrado.");
                        }
                        for (Usuario usuario : usuarios) {
                            System.out.printf("ID: %d | Nome: %s | Matricula: %s | E-mail: %s | Monitor: %s%n", usuario.getId(), usuario.getNomeCompleto(), usuario.getMatricula(), usuario.getEmail(), usuario.isHabilitadoMonitor() ? "Sim" : "Nao");
                        }
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Cadastro rejeitado: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco: " + e.getMessage());
            }
        }
    }
}