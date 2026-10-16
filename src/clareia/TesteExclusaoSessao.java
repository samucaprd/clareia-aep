package clareia;

import clareia.dao.DisciplinaDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.dao.UsuarioDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import clareia.service.SessaoMonitoriaService;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TesteExclusaoSessao {
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
                System.out.println("O teste precisa de outro usuario e de uma disciplina cadastrados.");
                return;
            }
            List<ItemPauta> pauta = new ArrayList<>();
            pauta.add(new ItemPauta(0, 1, "Pauta do teste de exclusao"));
            SessaoMonitoria sessao = new SessaoPresencial(0, disciplinas.get(0), monitor, pauta, LocalDate.now().plusDays(2), LocalTime.of(10, 0), LocalTime.of(11, 0), 5, StatusSessao.AGENDADA, "Sala de teste");
            int id = service.cadastrar(sessao, monitor.getId());
            System.out.println("Cadastro pelo Service confirmado. ID: " + id);
            try {
                service.excluir(id, outroUsuario.getId());
                throw new AssertionError("FALHA: outro usuario conseguiu excluir a sessao.");
            } catch (IllegalArgumentException e) {
                System.out.println("Outro responsavel rejeitado: " + e.getMessage());
            }
            if (sessaoDAO.buscarPorId(id) == null) {
                throw new AssertionError("FALHA: a tentativa rejeitada removeu a sessao.");
            }
            service.excluir(id, monitor.getId());
            if (sessaoDAO.buscarPorId(id) != null) {
                throw new AssertionError("FALHA: a sessao continua no banco.");
            }
            conferirPautaRemovida(banco, id);
            System.out.println("Sessao e pauta excluidas corretamente.");
            try {
                service.excluir(id, monitor.getId());
                throw new AssertionError("FALHA: aceitou excluir uma sessao inexistente.");
            } catch (IllegalArgumentException e) {
                System.out.println("Sessao inexistente rejeitada: " + e.getMessage());
            }
            System.out.println("Testes concluidos.");
        } catch (IllegalArgumentException e) {
            System.out.println("Operacao rejeitada: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
    private static void conferirPautaRemovida(ConexaoBanco banco, int sessaoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM itens_pauta WHERE sessao_id = ?";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, sessaoId);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                if (resultado.getInt(1) != 0) {
                    throw new AssertionError("FALHA: os itens da sessao continuam no banco.");
                }
            }
        }
    }
}