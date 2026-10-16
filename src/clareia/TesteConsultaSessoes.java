package clareia;

import clareia.dao.SessaoMonitoriaDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.ItemPauta;
import clareia.model.SessaoMonitoria;
import java.io.Console;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TesteConsultaSessoes {
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
        SessaoMonitoriaDAO dao = new SessaoMonitoriaDAO(banco);
        try {
            List<SessaoMonitoria> sessoes = dao.listar();
            if (sessoes.isEmpty()) {
                System.out.println("Nenhuma sessao cadastrada.");
                return;
            }
            for (SessaoMonitoria sessao : sessoes) {
                System.out.println("\nSessao: " + sessao.getId());
                System.out.println("Classe: " + sessao.getClass().getSimpleName());
                System.out.println("Disciplina: " + sessao.getDisciplina().getNome());
                System.out.println("Monitor: " + sessao.getMonitor().getNomeCompleto());
                System.out.println("Data: " + sessao.getData());
                System.out.println("Horario: " + sessao.getHoraInicio() + " ate " + sessao.getHoraFim());
                System.out.println("Capacidade: " + sessao.getCapacidade());
                System.out.println("Status: " + sessao.getStatus());
                System.out.println(sessao.obterOrientacaoAcesso());
                System.out.println("Pauta:");
                for (ItemPauta item : sessao.getPauta()) {
                    System.out.println(item.getOrdem() + " - " + item.getDescricao());
                }
            }
            int id = sessoes.get(0).getId();
            SessaoMonitoria encontrada = dao.buscarPorId(id);
            if (encontrada == null || encontrada.getId() != id) {
                throw new IllegalStateException("Falha na consulta por ID.");
            }
            System.out.println("\nConsulta por ID confirmada: " + encontrada.getId());
            if (dao.buscarPorId(-1) != null) {
                throw new IllegalStateException("A consulta retornou uma sessao para um ID inexistente.");
            }
            System.out.println("ID inexistente: nenhuma sessao encontrada, como esperado.");
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}