package clareia;

import clareia.dao.DisciplinaDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.Disciplina;
import clareia.service.DisciplinaService;
import java.io.Console;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TesteDisciplinas {
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
        DisciplinaDAO dao = new DisciplinaDAO(banco);
        DisciplinaService service = new DisciplinaService(dao);
        while (true) {
            System.out.println("\n--- DISCIPLINAS ---");
            System.out.println("1 - Cadastrar disciplina");
            System.out.println("2 - Consultar disciplinas");
            System.out.println("0 - Sair");
            String opcao = console.readLine("Opcao: ");
            if (opcao == null || opcao.trim().equals("0")) {
                return;
            }
            try {
                switch (opcao.trim()) {
                    case "1":
                        String codigo = console.readLine("Codigo: ");
                        if (codigo == null) {
                            return;
                        }
                        String nome = console.readLine("Nome: ");
                        if (nome == null) {
                            return;
                        }
                        Disciplina cadastrada = service.cadastrar(codigo, nome);
                        System.out.println("Disciplina cadastrada! ID: " + cadastrada.getId());
                        break;
                    case "2":
                        List<Disciplina> disciplinas = service.listar();
                        if (disciplinas.isEmpty()) {
                            System.out.println("Nenhuma disciplina cadastrada.");
                        }
                        for (Disciplina disciplina : disciplinas) {
                            System.out.printf("ID: %d | Codigo: %s | Nome: %s%n", disciplina.getId(), disciplina.getCodigo(), disciplina.getNome());
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