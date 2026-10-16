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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TesteEdicaoSessao {
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
        UsuarioDAO usuarioDAO = new UsuarioDAO(banco);
        DisciplinaDAO disciplinaDAO = new DisciplinaDAO(banco);
        SessaoMonitoriaDAO sessaoDAO = new SessaoMonitoriaDAO(banco);
        SessaoMonitoriaService service = new SessaoMonitoriaService(sessaoDAO, usuarioDAO, disciplinaDAO);
        try {
            Usuario monitor = null;
            Usuario outroUsuario = null;
            List<Usuario> usuarios = usuarioDAO.listar();
            for (Usuario usuario : usuarios) {
                if (usuario.isHabilitadoMonitor()) {
                    monitor = usuario;
                    break;
                }
            }
            if (monitor == null) {
                System.out.println("Cadastre um monitor habilitado.");
                return;
            }
            for (Usuario usuario : usuarios) {
                if (usuario.getId() != monitor.getId()) {
                    outroUsuario = usuario;
                    break;
                }
            }
            List<Disciplina> disciplinas = disciplinaDAO.listar();
            if (outroUsuario == null || disciplinas.isEmpty()) {
                System.out.println("Cadastre outro usuario e uma disciplina.");
                return;
            }
            Disciplina disciplina = disciplinas.get(0);
            LocalDate data = LocalDate.now().plusDays(2);
            LocalTime inicio = LocalTime.of(10, 0);
            LocalTime fim = LocalTime.of(11, 0);
            List<ItemPauta> pauta = new ArrayList<>();
            pauta.add(new ItemPauta(0, 1, "Pauta original"));
            SessaoMonitoria original = new SessaoPresencial(0, disciplina, monitor, pauta, data, inicio, fim, 5, StatusSessao.AGENDADA, "Sala de teste");
            int id = service.cadastrar(original, monitor.getId());
            System.out.println("Sessao de teste criada. ID: " + id);
            List<ItemPauta> novaPauta = new ArrayList<>();
            novaPauta.add(new ItemPauta(0, 1, "Pauta atualizada"));
            novaPauta.add(new ItemPauta(0, 2, "Exercicios"));
            SessaoMonitoria alterada = new SessaoRemota(id, disciplina, monitor, novaPauta, data, inicio, fim, 8, StatusSessao.AGENDADA, "https://example.com/aula-editada");
            try {
                service.editar(alterada, outroUsuario.getId());
                throw new AssertionError("FALHA: outro usuario conseguiu editar.");
            } catch (IllegalArgumentException e) {
                System.out.println("Outro responsavel rejeitado: " + e.getMessage());
            }
            SessaoMonitoria antes = service.buscarPorId(id);
            if (!(antes instanceof SessaoPresencial) || antes.getCapacidade() != 5) {
                throw new AssertionError("FALHA: a tentativa rejeitada alterou a sessao.");
            }
            service.editar(alterada, monitor.getId());
            SessaoMonitoria salva = service.buscarPorId(id);
            if (!(salva instanceof SessaoRemota) || salva.getCapacidade() != 8 || salva.getPauta().size() != 2) {
                throw new AssertionError("FALHA: modalidade, capacidade ou pauta incorreta.");
            }
            if (!salva.getPauta().get(0).getDescricao().equals("Pauta atualizada") || !salva.getPauta().get(1).getDescricao().equals("Exercicios")) {
                throw new AssertionError("FALHA: descricoes da pauta incorretas.");
            }
            if (!((SessaoRemota) salva).getLinkAcesso().equals("https://example.com/aula-editada")) {
                throw new AssertionError("FALHA: link incorreto.");
            }
            System.out.println("Edicao confirmada no mesmo ID: " + salva.getId());
            System.out.println("Classe: " + salva.getClass().getSimpleName());
            System.out.println("Capacidade: " + salva.getCapacidade());
            System.out.println("Itens de pauta: " + salva.getPauta().size());
            System.out.println(salva.obterOrientacaoAcesso());
            service.excluir(id, monitor.getId());
            if (sessaoDAO.buscarPorId(id) != null) {
                throw new AssertionError("FALHA: a sessao de teste nao foi removida.");
            }
            System.out.println("Sessao de teste removida. Testes concluidos.");
        } catch (IllegalArgumentException e) {
            System.out.println("Operacao rejeitada: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
}