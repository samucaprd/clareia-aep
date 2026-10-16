package clareia;

import clareia.dao.DisciplinaDAO;
import clareia.dao.SessaoMonitoriaDAO;
import clareia.dao.UsuarioDAO;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
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

public class TesteCadastroSessoes {
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
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO(banco);
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(banco);
            SessaoMonitoriaDAO sessaoDAO = new SessaoMonitoriaDAO(banco);
            Usuario monitor = null;
            for (Usuario usuario : usuarioDAO.listar()) {
                if (usuario.isHabilitadoMonitor()) {
                    monitor = usuario;
                    break;
                }
            }
            if (monitor == null) {
                System.out.println("Cadastre um monitor habilitado antes de executar o teste.");
                return;
            }
            List<Disciplina> disciplinas = disciplinaDAO.listar();
            if (disciplinas.isEmpty()) {
                System.out.println("Cadastre uma disciplina antes de executar o teste.");
                return;
            }
            Disciplina disciplina = disciplinas.get(0);
            List<ItemPauta> pauta = new ArrayList<>();
            pauta.add(new ItemPauta(0, 1, "Revisao de heranca"));
            pauta.add(new ItemPauta(0, 2, "Exercicios de polimorfismo"));
            LocalDate data = LocalDate.now().plusDays(1);
            SessaoMonitoria presencial = new SessaoPresencial(0, disciplina, monitor, pauta, data, LocalTime.of(14, 0), LocalTime.of(15, 0), 10, StatusSessao.AGENDADA, "Sala 12");
            SessaoMonitoria remota = new SessaoRemota(0, disciplina, monitor, pauta, data, LocalTime.of(16, 0), LocalTime.of(17, 0), 10, StatusSessao.AGENDADA, "https://example.com/monitoria");
            int idPresencial = sessaoDAO.inserir(presencial);
            System.out.println("Sessao presencial cadastrada! ID: " + idPresencial);
            int idRemota = sessaoDAO.inserir(remota);
            System.out.println("Sessao remota cadastrada! ID: " + idRemota);
            conferirGravacao(banco, idPresencial, "PRESENCIAL");
            conferirGravacao(banco, idRemota, "REMOTA");
            System.out.println("Gravacao e consulta das duas sessoes confirmadas.");
        } catch (IllegalArgumentException e) {
            System.out.println("Dados rejeitados: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Erro no banco: " + e.getMessage());
        }
    }
    private static void conferirGravacao(ConexaoBanco banco, int id, String modalidadeEsperada) throws SQLException {
        String sql = "SELECT modalidade, sala, link_acesso, (SELECT COUNT(*) FROM itens_pauta WHERE sessao_id = s.id) AS total_itens FROM sessoes_monitoria s WHERE s.id = ?";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet resultado = comando.executeQuery()) {
                if (!resultado.next()) {
                    throw new IllegalStateException("A sessao " + id + " nao foi encontrada no banco.");
                }
                String modalidade = resultado.getString("modalidade");
                String sala = resultado.getString("sala");
                String link = resultado.getString("link_acesso");
                int totalItens = resultado.getInt("total_itens");
                if (!modalidadeEsperada.equals(modalidade) || totalItens != 2) {
                    throw new IllegalStateException("Modalidade ou quantidade de itens incorreta na sessao " + id);
                }
                if (modalidade.equals("PRESENCIAL") && (!"Sala 12".equals(sala) || link != null)) {
                    throw new IllegalStateException("Dados da sessao presencial incorretos.");
                }
                if (modalidade.equals("REMOTA") && (!"https://example.com/monitoria".equals(link) || sala != null)) {
                    throw new IllegalStateException("Dados da sessao remota incorretos.");
                }
                System.out.println("ID: " + id + " | Modalidade: " + modalidade + " | Itens salvos: " + totalItens);
            }
        }
    }
}