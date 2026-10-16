package clareia;

import clareia.dao.InscricaoDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.Inscricao;
import clareia.model.StatusPresenca;
import clareia.service.PresencaService;
import java.io.Console;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TestePresencas {
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
        PresencaService service = new PresencaService(banco, new InscricaoDAO(), new SessaoMonitoriaDAO(banco));
        while (true) {
            System.out.println("\n--- PRESENCAS E HISTORICO ---");
            System.out.println("1 - Consultar inscritos");
            System.out.println("2 - Registrar presenca ou ausencia");
            System.out.println("3 - Consultar historico");
            System.out.println("0 - Sair");
            String opcao = console.readLine("Opcao: ");
            if (opcao == null || opcao.trim().equals("0")) {
                return;
            }
            try {
                switch (opcao.trim()) {
                    case "1":
                        Integer sessaoId = lerId(console, "ID da sessao: ");
                        if (sessaoId == null) {
                            return;
                        }
                        Integer monitorConsulta = lerId(console, "ID do monitor: ");
                        if (monitorConsulta == null) {
                            return;
                        }
                        mostrar(service.consultarInscritos(sessaoId, monitorConsulta));
                        break;
                    case "2":
                        Integer inscricaoId = lerId(console, "ID da inscricao: ");
                        if (inscricaoId == null) {
                            return;
                        }
                        Integer monitorRegistro = lerId(console, "ID do monitor: ");
                        if (monitorRegistro == null) {
                            return;
                        }
                        String resposta = console.readLine("P - Presente | A - Ausente: ");
                        if (resposta == null) {
                            return;
                        }
                        StatusPresenca presenca;
                        if (resposta.trim().equalsIgnoreCase("P")) {
                            presenca = StatusPresenca.PRESENTE;
                        } else if (resposta.trim().equalsIgnoreCase("A")) {
                            presenca = StatusPresenca.AUSENTE;
                        } else {
                            System.out.println("Informe P ou A.");
                            break;
                        }
                        service.registrarPresenca(inscricaoId, monitorRegistro, presenca);
                        System.out.println("Presenca atualizada.");
                        break;
                    case "3":
                        Integer estudanteId = lerId(console, "ID do estudante: ");
                        if (estudanteId == null) {
                            return;
                        }
                        mostrar(service.consultarHistorico(estudanteId));
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
    private static void mostrar(List<Inscricao> inscricoes) {
        if (inscricoes.isEmpty()) {
            System.out.println("Nenhuma inscricao encontrada.");
        }
        for (Inscricao inscricao : inscricoes) {
            System.out.println("\nInscricao: " + inscricao.getId());
            System.out.println("Estudante: " + inscricao.getEstudante().getNomeCompleto());
            System.out.println("Sessao: " + inscricao.getSessao().getId());
            System.out.println("Disciplina: " + inscricao.getSessao().getDisciplina().getNome());
            System.out.println("Data: " + inscricao.getSessao().getData());
            System.out.println("Status da sessao: " + inscricao.getSessao().getStatus());
            System.out.println("Status da inscricao: " + inscricao.getStatus());
            System.out.println("Presenca: " + inscricao.getPresenca());
        }
    }
}