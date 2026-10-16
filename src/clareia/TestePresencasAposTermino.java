package clareia;

import clareia.dao.*;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import clareia.service.PresencaService;
import clareia.service.UsuarioService;
import java.io.Console;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestePresencasAposTermino {
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
        PresencaService service = new PresencaService(banco, inscricaoDAO, sessaoDAO);
        try {
            Usuario monitor = null;
            List<Usuario> estudantes = new ArrayList<>();
            for (Usuario usuario : usuarioDAO.listar()) {
                if (monitor == null && usuario.isHabilitadoMonitor()) {
                    monitor = usuario;
                }
                if (!usuario.isHabilitadoMonitor()) {
                    estudantes.add(usuario);
                }
            }
            List<Disciplina> disciplinas = disciplinaDAO.listar();
            if (monitor == null || estudantes.size() < 2 || disciplinas.isEmpty()) {
                System.out.println("O teste precisa de monitor, dois estudantes e uma disciplina.");
                return;
            }
            String identificador = String.valueOf(System.currentTimeMillis());
            UsuarioService usuarioService = new UsuarioService(usuarioDAO);
            Usuario cancelado = usuarioService.cadastrar("Teste inscricao cancelada", "P" + identificador, "presenca" + identificador + "@example.com", false);
            Usuario presente = estudantes.get(0);
            Usuario ausente = estudantes.get(1);
            int[] ids = prepararDados(banco, monitor, presente, ausente, cancelado, disciplinas.get(0));
            int sessaoId = ids[0];
            int inscricaoPresente = ids[1];
            int inscricaoAusente = ids[2];
            int inscricaoCancelada = ids[3];
            System.out.println("Sessao passada criada para teste: " + sessaoId);

            service.registrarPresenca(inscricaoPresente, monitor.getId(), StatusPresenca.PRESENTE);
            service.registrarPresenca(inscricaoAusente, monitor.getId(), StatusPresenca.AUSENTE);

            try {
                service.registrarPresenca(inscricaoCancelada, monitor.getId(), StatusPresenca.PRESENTE);
                throw new AssertionError("FALHA: registrou presenca em inscricao cancelada.");
            } catch (IllegalArgumentException e) {
                System.out.println("Inscricao cancelada rejeitada: " + e.getMessage());
            }

            List<Inscricao> salvas = service.consultarInscritos(sessaoId, monitor.getId());
            if (salvas.size() != 3) {
                throw new AssertionError("FALHA: quantidade de inscricoes incorreta.");
            }
            boolean presenteConfirmado = false;
            boolean ausenteConfirmado = false;
            boolean canceladaConfirmada = false;
            for (Inscricao inscricao : salvas) {
                if (inscricao.getId() == inscricaoPresente) {
                    presenteConfirmado = inscricao.getStatus() == StatusInscricao.ATIVA && inscricao.getPresenca() == StatusPresenca.PRESENTE;
                }
                if (inscricao.getId() == inscricaoAusente) {
                    ausenteConfirmado = inscricao.getStatus() == StatusInscricao.ATIVA && inscricao.getPresenca() == StatusPresenca.AUSENTE;
                }
                if (inscricao.getId() == inscricaoCancelada) {
                    canceladaConfirmada = inscricao.getStatus() == StatusInscricao.CANCELADA && inscricao.getPresenca() == StatusPresenca.PENDENTE;
                }
                System.out.println("Inscricao: " + inscricao.getId() + " | Estudante: " + inscricao.getEstudante().getId() + " | Status: " + inscricao.getStatus() + " | Presenca: " + inscricao.getPresenca());
            }
            if (!presenteConfirmado || !ausenteConfirmado || !canceladaConfirmada) {
                throw new AssertionError("FALHA: os estados gravados nao correspondem ao esperado.");
            }
            conferirHistorico(service, presente.getId(), inscricaoPresente, StatusPresenca.PRESENTE);
            conferirHistorico(service, ausente.getId(), inscricaoAusente, StatusPresenca.AUSENTE);
            System.out.println("Presenca, ausencia e historicos confirmados no banco.");
            System.out.println("Testes concluidos. Sessao preservada: " + sessaoId);
        } catch (IllegalArgumentException e) {
            System.out.println("TESTE INTERROMPIDO: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("TESTE INTERROMPIDO POR ERRO NO BANCO: " + e.getMessage());
        }
    }
    private static int[] prepararDados(ConexaoBanco banco, Usuario monitor, Usuario presente, Usuario ausente, Usuario cancelado, Disciplina disciplina) throws SQLException {
        LocalDate data = LocalDate.now().minusDays(1);
        LocalTime inicio = LocalTime.of(10, 0);
        LocalTime fim = LocalTime.of(11, 0);
        String sql = "INSERT INTO sessoes_monitoria (disciplina_id, monitor_id, modalidade, sala, link_acesso, data, hora_inicio, hora_fim, capacidade, status) VALUES (?, ?, 'PRESENCIAL', 'Sala teste presencas', NULL, ?, ?, ?, 3, 'AGENDADA')";
        try (Connection conexao = banco.abrirConexao()) {
            conexao.setAutoCommit(false);
            try {
                int sessaoId;
                try (PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    comando.setInt(1, disciplina.getId());
                    comando.setInt(2, monitor.getId());
                    comando.setDate(3, java.sql.Date.valueOf(data));
                    comando.setTime(4, java.sql.Time.valueOf(inicio));
                    comando.setTime(5, java.sql.Time.valueOf(fim));
                    comando.executeUpdate();
                    try (ResultSet resultado = comando.getGeneratedKeys()) {
                        if (!resultado.next()) {
                            throw new SQLException("Nao foi possivel recuperar o ID da sessao de teste.");
                        }
                        sessaoId = resultado.getInt(1);
                    }
                }
                try (PreparedStatement comando = conexao.prepareStatement("INSERT INTO itens_pauta (sessao_id, ordem, descricao) VALUES (?, 1, 'Teste de presenca apos o termino')")) {
                    comando.setInt(1, sessaoId);
                    comando.executeUpdate();
                }
                List<ItemPauta> pauta = new ArrayList<>();
                pauta.add(new ItemPauta(0, 1, "Teste de presenca apos o termino"));
                SessaoMonitoria sessao = new SessaoPresencial(sessaoId, disciplina, monitor, pauta, data, inicio, fim, 3, StatusSessao.AGENDADA, "Sala teste presencas");
                LocalDateTime criacao = data.minusDays(1).atTime(9, 0);
                InscricaoDAO dao = new InscricaoDAO();
                int idPresente = dao.inserir(conexao, new Inscricao(0, presente, sessao, criacao, StatusInscricao.ATIVA, StatusPresenca.PENDENTE));
                int idAusente = dao.inserir(conexao, new Inscricao(0, ausente, sessao, criacao, StatusInscricao.ATIVA, StatusPresenca.PENDENTE));
                int idCancelada = dao.inserir(conexao, new Inscricao(0, cancelado, sessao, criacao, StatusInscricao.CANCELADA, StatusPresenca.PENDENTE));
                conexao.commit();
                return new int[] {sessaoId, idPresente, idAusente, idCancelada};
            } catch (SQLException | RuntimeException e) {
                try {
                    conexao.rollback();
                } catch (SQLException erroRollback) {
                    e.addSuppressed(erroRollback);
                }
                throw e;
            }
        }
    }
    private static void conferirHistorico(PresencaService service, int estudanteId, int inscricaoId, StatusPresenca esperada) throws SQLException {
        for (Inscricao inscricao : service.consultarHistorico(estudanteId)) {
            if (inscricao.getId() == inscricaoId) {
                if (inscricao.getPresenca() != esperada) {
                    throw new AssertionError("FALHA: presenca incorreta no historico.");
                }
                return;
            }
        }
        throw new AssertionError("FALHA: inscricao nao encontrada no historico.");
    }
}