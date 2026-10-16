package clareia;

import clareia.dao.*;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import clareia.service.*;
import java.io.Console;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TesteRegrasInscricoes {
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
        UsuarioService usuarioService = new UsuarioService(usuarioDAO);
        SessaoMonitoriaService sessaoService = new SessaoMonitoriaService(sessaoDAO, usuarioDAO, disciplinaDAO);
        InscricaoService inscricaoService = new InscricaoService(banco, inscricaoDAO, sessaoDAO);
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
                System.out.println("O teste precisa de monitor, estudante e disciplina cadastrados.");
                return;
            }
            String identificador = String.valueOf(System.currentTimeMillis());
            Usuario segundo = usuarioService.cadastrar("Estudante teste de regras", "T" + identificador, "teste" + identificador + "@example.com", false);
            Disciplina disciplina = disciplinas.get(0);
            LocalDate data = LocalDate.now().plusDays(3);
            LocalTime inicio = LocalTime.of(10, 0);
            LocalTime fim = LocalTime.of(11, 0);
            List<ItemPauta> pauta = new ArrayList<>();
            pauta.add(new ItemPauta(0, 1, "Teste das regras de inscricao"));
            SessaoMonitoria nova = new SessaoPresencial(0, disciplina, monitor, pauta, data, inicio, fim, 1, StatusSessao.AGENDADA, "Sala de testes");
            int sessaoId = sessaoService.cadastrar(nova, monitor.getId());
            int primeiraInscricao = inscricaoService.inscrever(estudante.getId(), sessaoId);
            System.out.println("Sessao criada: " + sessaoId + " | Primeira inscricao: " + primeiraInscricao);

            try {
                inscricaoService.inscrever(segundo.getId(), sessaoId);
                throw new AssertionError("FALHA: aceitou inscricao em sessao lotada.");
            } catch (IllegalArgumentException e) {
                System.out.println("Lotacao rejeitada: " + e.getMessage());
            }
            if (sessaoDAO.contarInscricoesAtivas(sessaoId) != 1) {
                throw new AssertionError("FALHA: quantidade incorreta apos a tentativa em sessao lotada.");
            }

            SessaoMonitoria ampliada = new SessaoPresencial(sessaoId, disciplina, monitor, pauta, data, inicio, fim, 2, StatusSessao.AGENDADA, "Sala de testes");
            sessaoService.editar(ampliada, monitor.getId());
            int segundaInscricao = inscricaoService.inscrever(segundo.getId(), sessaoId);
            if (sessaoService.buscarPorId(sessaoId).getCapacidade() != 2 || sessaoDAO.contarInscricoesAtivas(sessaoId) != 2) {
                throw new AssertionError("FALHA: ampliacao ou segunda inscricao incorreta.");
            }
            System.out.println("Capacidade ampliada para 2 e segunda inscricao realizada: " + segundaInscricao);

            SessaoMonitoria reduzida = new SessaoPresencial(sessaoId, disciplina, monitor, pauta, data, inicio, fim, 1, StatusSessao.AGENDADA, "Sala de testes");
            try {
                sessaoService.editar(reduzida, monitor.getId());
                throw new AssertionError("FALHA: reduziu a capacidade abaixo dos inscritos.");
            } catch (IllegalArgumentException e) {
                System.out.println("Reducao indevida rejeitada: " + e.getMessage());
            }
            if (sessaoService.buscarPorId(sessaoId).getCapacidade() != 2) {
                throw new AssertionError("FALHA: a capacidade foi alterada na tentativa rejeitada.");
            }

            SessaoMonitoria horarioAlterado = new SessaoPresencial(sessaoId, disciplina, monitor, pauta, data, LocalTime.of(12, 0), LocalTime.of(13, 0), 2, StatusSessao.AGENDADA, "Sala de testes");
            try {
                sessaoService.editar(horarioAlterado, monitor.getId());
                throw new AssertionError("FALHA: alterou o horario de uma sessao com inscricoes.");
            } catch (IllegalArgumentException e) {
                System.out.println("Mudanca de horario rejeitada: " + e.getMessage());
            }
            SessaoMonitoria conferida = sessaoService.buscarPorId(sessaoId);
            if (!conferida.getHoraInicio().equals(inicio) || !conferida.getHoraFim().equals(fim)) {
                throw new AssertionError("FALHA: os horarios originais nao foram preservados.");
            }

            SessaoMonitoria modalidadeAlterada = new SessaoRemota(sessaoId, disciplina, monitor, pauta, data, inicio, fim, 2, StatusSessao.AGENDADA, "https://example.com/teste");
            try {
                sessaoService.editar(modalidadeAlterada, monitor.getId());
                throw new AssertionError("FALHA: alterou a modalidade de uma sessao com inscricoes.");
            } catch (IllegalArgumentException e) {
                System.out.println("Mudanca de modalidade rejeitada: " + e.getMessage());
            }
            if (!(sessaoService.buscarPorId(sessaoId) instanceof SessaoPresencial)) {
                throw new AssertionError("FALHA: a modalidade original nao foi preservada.");
            }

            try {
                sessaoService.excluir(sessaoId, monitor.getId());
                throw new AssertionError("FALHA: excluiu uma sessao com inscricoes.");
            } catch (IllegalArgumentException e) {
                System.out.println("Exclusao com inscricoes rejeitada: " + e.getMessage());
            }
            if (sessaoDAO.buscarPorId(sessaoId) == null) {
                throw new AssertionError("FALHA: a sessao foi removida.");
            }

            SessaoMonitoria sobreposta = new SessaoPresencial(0, disciplina, monitor, pauta, data, LocalTime.of(10, 30), LocalTime.of(11, 30), 5, StatusSessao.AGENDADA, "Outra sala");
            int conflitoId = sessaoService.cadastrar(sobreposta, monitor.getId());
            try {
                inscricaoService.inscrever(estudante.getId(), conflitoId);
                throw new AssertionError("FALHA: aceitou inscricao com conflito de horario.");
            } catch (IllegalArgumentException e) {
                System.out.println("Conflito de horario rejeitado: " + e.getMessage());
            }
            if (sessaoDAO.contarInscricoesAtivas(conflitoId) != 0) {
                throw new AssertionError("FALHA: a tentativa com conflito criou uma inscricao.");
            }
            sessaoService.excluir(conflitoId, monitor.getId());

            SessaoMonitoria finalizada = sessaoService.buscarPorId(sessaoId);
            if (finalizada.getPauta().size() != 1 || !finalizada.getPauta().get(0).getDescricao().equals("Teste das regras de inscricao")) {
                throw new AssertionError("FALHA: a pauta nao foi preservada.");
            }
            if (sessaoDAO.contarInscricoesAtivas(sessaoId) != 2 || sessaoService.consultarVagasDisponiveis(sessaoId) != 0) {
                throw new AssertionError("FALHA: quantidade final de inscricoes ou vagas incorreta.");
            }
            System.out.println("\nTestes concluidos.");
            System.out.println("Sessao preservada para testar cancelamentos: " + sessaoId);
            System.out.println("Monitor: " + monitor.getId());
            System.out.println("Estudantes: " + estudante.getId() + " e " + segundo.getId());
            System.out.println("Inscricoes: " + primeiraInscricao + " e " + segundaInscricao);
        } catch (IllegalArgumentException e) {
            System.out.println("TESTE INTERROMPIDO: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("TESTE INTERROMPIDO POR ERRO NO BANCO: " + e.getMessage());
        }
    }
}