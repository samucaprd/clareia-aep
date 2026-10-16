package clareia;

import clareia.dao.*;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import clareia.service.*;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TesteValidacoesFinais {
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
        InscricaoDAO inscricaoDAO = new InscricaoDAO();
        SessaoMonitoriaService sessoes = new SessaoMonitoriaService(sessaoDAO, usuarioDAO, disciplinaDAO);
        InscricaoService inscricoes = new InscricaoService(banco, inscricaoDAO, sessaoDAO);
        CancelamentoService cancelamentos = new CancelamentoService(banco, inscricaoDAO, sessaoDAO);
        PresencaService presencas = new PresencaService(banco, inscricaoDAO, sessaoDAO);
        try {
            Usuario monitor = null;
            Usuario estudante = null;
            for (Usuario usuario : usuarioDAO.listar()) {
                if (monitor == null && usuario.isHabilitadoMonitor()) {
                    monitor = usuario;
                }
                if (estudante == null && !usuario.isHabilitadoMonitor()) {
                    estudante = usuario;
                }
            }
            List<Disciplina> disciplinas = disciplinaDAO.listar();
            if (monitor == null || estudante == null || disciplinas.isEmpty()) {
                System.out.println("Cadastre monitor, estudante e disciplina antes do teste.");
                return;
            }
            Disciplina disciplina = disciplinas.get(0);
            LocalDate ontem = LocalDate.now().minusDays(1);
            List<ItemPauta> pauta = new ArrayList<>();
            pauta.add(new ItemPauta(0, 1, "Teste final de limites de horario"));
            SessaoMonitoria passada = new SessaoPresencial(0, disciplina, monitor, pauta, ontem, LocalTime.of(8, 0), LocalTime.of(9, 0), 2, StatusSessao.AGENDADA, "Sala teste final");

            // Preparacao de uma sessao historica exclusivamente para o teste.
            int sessaoId = sessaoDAO.inserir(passada);
            SessaoMonitoria salva = sessoes.buscarPorId(sessaoId);
            Inscricao inscricao = new Inscricao(0, estudante, salva, ontem.minusDays(1).atTime(10, 0), StatusInscricao.ATIVA, StatusPresenca.PENDENTE);
            int inscricaoId;
            try (Connection conexao = banco.abrirConexao()) {
                inscricaoId = inscricaoDAO.inserir(conexao, inscricao);
            }
            System.out.println("Sessao historica de teste: " + sessaoId);

            try {
                inscricoes.inscrever(estudante.getId(), sessaoId);
                throw new AssertionError("FALHA: aceitou inscricao em sessao passada.");
            } catch (IllegalArgumentException e) {
                System.out.println("Inscricao apos inicio rejeitada: " + e.getMessage());
            }

            SessaoMonitoria alterada = new SessaoPresencial(sessaoId, disciplina, monitor, pauta, ontem, LocalTime.of(8, 0), LocalTime.of(9, 0), 3, StatusSessao.AGENDADA, "Sala alterada");
            try {
                sessoes.editar(alterada, monitor.getId());
                throw new AssertionError("FALHA: editou uma sessao passada.");
            } catch (IllegalArgumentException e) {
                System.out.println("Edicao apos inicio rejeitada: " + e.getMessage());
            }

            try {
                sessoes.excluir(sessaoId, monitor.getId());
                throw new AssertionError("FALHA: excluiu uma sessao passada.");
            } catch (IllegalArgumentException e) {
                System.out.println("Exclusao apos inicio rejeitada: " + e.getMessage());
            }

            try {
                cancelamentos.cancelarInscricao(inscricaoId, estudante.getId());
                throw new AssertionError("FALHA: cancelou inscricao apos o inicio.");
            } catch (IllegalArgumentException e) {
                System.out.println("Cancelamento da inscricao apos inicio rejeitado: " + e.getMessage());
            }

            try {
                cancelamentos.cancelarSessao(sessaoId, monitor.getId());
                throw new AssertionError("FALHA: cancelou sessao apos o inicio.");
            } catch (IllegalArgumentException e) {
                System.out.println("Cancelamento da sessao apos inicio rejeitado: " + e.getMessage());
            }

            try {
                presencas.concluirSessao(sessaoId, monitor.getId());
                throw new AssertionError("FALHA: concluiu sessao com presenca pendente.");
            } catch (IllegalArgumentException e) {
                System.out.println("Conclusao com pendencia rejeitada: " + e.getMessage());
            }

            SessaoMonitoria conferida = sessoes.buscarPorId(sessaoId);
            List<Inscricao> registradas = presencas.consultarInscritos(sessaoId, monitor.getId());
            if (conferida.getStatus() != StatusSessao.AGENDADA || conferida.getCapacidade() != 2) {
                throw new AssertionError("FALHA: as tentativas rejeitadas alteraram a sessao.");
            }
            if (registradas.size() != 1 || registradas.get(0).getStatus() != StatusInscricao.ATIVA || registradas.get(0).getPresenca() != StatusPresenca.PENDENTE) {
                throw new AssertionError("FALHA: as tentativas rejeitadas alteraram a inscricao.");
            }

            presencas.registrarPresenca(inscricaoId, monitor.getId(), StatusPresenca.PRESENTE);
            presencas.concluirSessao(sessaoId, monitor.getId());
            if (sessoes.buscarPorId(sessaoId).getStatus() != StatusSessao.CONCLUIDA) {
                throw new AssertionError("FALHA: a conclusao nao foi persistida.");
            }
            System.out.println("Conclusao permitida apos registrar a presenca.");

            testarRollback(banco, sessaoDAO, disciplina, monitor);
            System.out.println("\nTestes finais deste arquivo concluidos.");
            System.out.println("Sessao historica preservada: " + sessaoId);
        } catch (IllegalArgumentException e) {
            System.out.println("TESTE INTERROMPIDO: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("TESTE INTERROMPIDO POR ERRO NO BANCO: " + e.getMessage());
        }
    }
    private static void testarRollback(ConexaoBanco banco, SessaoMonitoriaDAO dao, Disciplina disciplina, Usuario monitor) throws SQLException {
        String marcador = "ROLLBACK-" + System.currentTimeMillis();
        List<ItemPauta> pauta = new ArrayList<>();
        pauta.add(new ItemPauta(0, 1, marcador));
        SessaoMonitoria defeituosa = new SessaoPautaDuplicada(disciplina, monitor, pauta, marcador);
        try {
            dao.inserir(defeituosa);
            throw new AssertionError("FALHA: a pauta duplicada foi aceita.");
        } catch (SQLException e) {
            if (e.getErrorCode() != 1062) {
                throw e;
            }
            System.out.println("Falha esperada provocada na gravacao do segundo item.");
        }
        String sql = "SELECT (SELECT COUNT(*) FROM sessoes_monitoria WHERE sala = ?) AS sessoes, "
                + "(SELECT COUNT(*) FROM itens_pauta WHERE descricao = ?) AS itens";
        try (Connection conexao = banco.abrirConexao();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, marcador);
            comando.setString(2, marcador);
            try (ResultSet resultado = comando.executeQuery()) {
                resultado.next();
                if (resultado.getInt("sessoes") != 0 || resultado.getInt("itens") != 0) {
                    throw new AssertionError("FALHA: a transacao deixou registros parciais.");
                }
            }
        }
        System.out.println("Rollback confirmado: nenhuma sessao ou item desse teste permaneceu.");
    }
    private static class SessaoPautaDuplicada extends SessaoPresencial {
        public SessaoPautaDuplicada(Disciplina disciplina, Usuario monitor, List<ItemPauta> pauta, String sala) {
            super(0, disciplina, monitor, pauta, LocalDate.now().plusDays(2), LocalTime.of(14, 0), LocalTime.of(15, 0), 2, StatusSessao.AGENDADA, sala);
        }
        @Override
        public List<ItemPauta> getPauta() {
            List<ItemPauta> itens = super.getPauta();
            ItemPauta primeiro = itens.get(0);
            itens.add(new ItemPauta(0, primeiro.getOrdem(), primeiro.getDescricao()));
            return itens;
        }
    }
}