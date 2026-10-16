package clareia;

import clareia.dao.DisciplinaDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.dao.UsuarioDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import clareia.service.SessaoMonitoriaService;
import java.io.Console;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class TesteBuscaSessoes {
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
        DisciplinaDAO disciplinaDAO = new DisciplinaDAO(banco);
        SessaoMonitoriaDAO sessaoDAO = new SessaoMonitoriaDAO(banco);
        UsuarioDAO usuarioDAO = new UsuarioDAO(banco);
        SessaoMonitoriaService service = new SessaoMonitoriaService(sessaoDAO, usuarioDAO, disciplinaDAO);
        try {
            System.out.println("--- DISCIPLINAS ---");
            for (Disciplina disciplina : disciplinaDAO.listar()) {
                System.out.println(disciplina.getId() + " - " + disciplina.getNome());
            }
            String entradaId = console.readLine("ID da disciplina: ");
            if (entradaId == null) {
                return;
            }
            String entradaInicio = console.readLine("Data inicial (AAAA-MM-DD): ");
            if (entradaInicio == null) {
                return;
            }
            String entradaFim = console.readLine("Data final (AAAA-MM-DD): ");
            if (entradaFim == null) {
                return;
            }
            int disciplinaId = Integer.parseInt(entradaId.trim());
            LocalDate inicio = LocalDate.parse(entradaInicio.trim());
            LocalDate fim = LocalDate.parse(entradaFim.trim());
            List<SessaoMonitoria> sessoes = service.buscarPorDisciplinaEPeriodo(disciplinaId, inicio, fim);
            if (sessoes.isEmpty()) {
                System.out.println("Nenhuma sessao encontrada nesse filtro.");
            }
            for (SessaoMonitoria sessao : sessoes) {
                System.out.println("\nSessao: " + sessao.getId());
                System.out.println("Disciplina: " + sessao.getDisciplina().getNome());
                System.out.println("Monitor: " + sessao.getMonitor().getNomeCompleto());
                System.out.println("Data: " + sessao.getData());
                System.out.println("Horario: " + sessao.getHoraInicio() + " ate " + sessao.getHoraFim());
                System.out.println("Status: " + sessao.getStatus());
                System.out.println("Vagas disponiveis: " + service.consultarVagasDisponiveis(sessao.getId()));
                System.out.println(sessao.obterOrientacaoAcesso());
                for (ItemPauta item : sessao.getPauta()) {
                    System.out.println(item.getOrdem() + " - " + item.getDescricao());
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Data invalida. Use AAAA-MM-DD.");
        } catch (NumberFormatException e) {
            System.out.println("O ID deve ser um numero inteiro.");
        } catch (IllegalArgumentException e) {
            System.out.println("Consulta rejeitada: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}