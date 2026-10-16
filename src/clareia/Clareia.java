package clareia;

import clareia.dao.*;
import clareia.infra.ConexaoBanco;
import clareia.model.*;
import clareia.service.*;
import java.io.Console;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Clareia {
    private final Console console;
    private final UsuarioService usuarioService;
    private final DisciplinaService disciplinaService;
    private final SessaoMonitoriaService sessaoService;
    private final InscricaoService inscricaoService;
    private final CancelamentoService cancelamentoService;
    private final PresencaService presencaService;
    private Usuario usuarioAtual;

    public Clareia(Console console, ConexaoBanco banco) {
        this.console = console;
        UsuarioDAO usuarioDAO = new UsuarioDAO(banco);
        DisciplinaDAO disciplinaDAO = new DisciplinaDAO(banco);
        SessaoMonitoriaDAO sessaoDAO = new SessaoMonitoriaDAO(banco);
        InscricaoDAO inscricaoDAO = new InscricaoDAO();
        usuarioService = new UsuarioService(usuarioDAO);
        disciplinaService = new DisciplinaService(disciplinaDAO);
        sessaoService = new SessaoMonitoriaService(sessaoDAO, usuarioDAO, disciplinaDAO);
        inscricaoService = new InscricaoService(banco, inscricaoDAO, sessaoDAO);
        cancelamentoService = new CancelamentoService(banco, inscricaoDAO, sessaoDAO);
        presencaService = new PresencaService(banco, inscricaoDAO, sessaoDAO);
    }
    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.out.println("Execute o Clareia pelo terminal.");
            return;
        }
        char[] senha = console.readPassword("Senha do MySQL (root): ");
        if (senha == null) {
            return;
        }
        ConexaoBanco banco = new ConexaoBanco("root", new String(senha));
        Arrays.fill(senha, '\0');
        try (Connection conexao = banco.abrirConexao()) {
            if (!conexao.isValid(5)) {
                System.out.println("Nao foi possivel validar a conexao.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Nao foi possivel conectar ao banco: " + e.getMessage());
            return;
        }
        Clareia aplicativo = new Clareia(console, banco);
        aplicativo.executar();
    }
    public void executar() {
        while (true) {
            mostrarMenu();
            try {
                String opcao = ler("Opcao: ");
                switch (opcao) {
                    case "1":
                        cadastrarUsuario();
                        break;
                    case "2":
                        listarUsuarios();
                        break;
                    case "3":
                        cadastrarDisciplina();
                        break;
                    case "4":
                        listarDisciplinas();
                        break;
                    case "5":
                        selecionarUsuario();
                        break;
                    case "6":
                        cadastrarSessao();
                        break;
                    case "7":
                        mostrarSessoes(sessaoService.listar());
                        break;
                    case "8":
                        buscarSessoes();
                        break;
                    case "9":
                        editarSessao();
                        break;
                    case "10":
                        excluirSessao();
                        break;
                    case "11":
                        inscrever();
                        break;
                    case "12":
                        consultarHistorico();
                        break;
                    case "13":
                        cancelarInscricao();
                        break;
                    case "14":
                        cancelarSessao();
                        break;
                    case "15":
                        consultarInscritos();
                        break;
                    case "16":
                        registrarPresenca();
                        break;
                    case "0":
                        System.out.println("Clareia encerrado.");
                        return;
                    default:
                        System.out.println("Opcao invalida.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("Data ou horario invalido. Use AAAA-MM-DD e HH:MM.");
            } catch (NumberFormatException e) {
                System.out.println("Informe um numero inteiro valido.");
            } catch (IllegalArgumentException e) {
                System.out.println("Operacao rejeitada: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Erro no banco: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                return;
            }
        }
    }
    private void mostrarMenu() {
        System.out.println("\n--- CLAREIA ---");
        if (usuarioAtual == null) {
            System.out.println("Nenhum usuario selecionado.");
        } else {
            System.out.println("Usuario: " + usuarioAtual.getNomeCompleto() + " | ID: " + usuarioAtual.getId());
        }
        System.out.println("1 - Cadastrar usuario");
        System.out.println("2 - Consultar usuarios");
        System.out.println("3 - Cadastrar disciplina");
        System.out.println("4 - Consultar disciplinas");
        System.out.println("5 - Selecionar ou trocar usuario");
        System.out.println("6 - Cadastrar sessao");
        System.out.println("7 - Consultar todas as sessoes");
        System.out.println("8 - Buscar sessoes por disciplina e periodo");
        System.out.println("9 - Editar sessao");
        System.out.println("10 - Excluir sessao");
        System.out.println("11 - Inscrever-se em uma sessao");
        System.out.println("12 - Consultar meu historico");
        System.out.println("13 - Cancelar minha inscricao");
        System.out.println("14 - Cancelar sessao");
        System.out.println("15 - Consultar inscritos de uma sessao");
        System.out.println("16 - Registrar presenca ou ausencia");
        System.out.println("0 - Sair");
    }
    private String ler(String mensagem) {
        String entrada = console.readLine(mensagem);
        if (entrada == null) {
            throw new IllegalStateException("Entrada encerrada. Clareia finalizado.");
        }
        return entrada.trim();
    }
    private int lerInteiro(String mensagem) {
        return Integer.parseInt(ler(mensagem));
    }
    private boolean confirmar(String mensagem) {
        String resposta = ler(mensagem + " (S/N): ");
        if (resposta.equalsIgnoreCase("S")) {
            return true;
        }
        if (resposta.equalsIgnoreCase("N")) {
            return false;
        }
        throw new IllegalArgumentException("Informe S ou N.");
    }
    private Usuario exigirUsuario() {
        if (usuarioAtual == null) {
            throw new IllegalArgumentException("Selecione um usuario pela opcao 5.");
        }
        return usuarioAtual;
    }
    private Usuario exigirMonitor() {
        Usuario usuario = exigirUsuario();
        if (!usuario.isHabilitadoMonitor()) {
            throw new IllegalArgumentException("O usuario selecionado nao esta habilitado como monitor.");
        }
        return usuario;
    }
    private void cadastrarUsuario() throws SQLException {
        String nome = ler("Nome completo: ");
        String matricula = ler("Matricula: ");
        String email = ler("E-mail: ");
        boolean monitor = confirmar("Habilitado como monitor?");
        Usuario usuario = usuarioService.cadastrar(nome, matricula, email, monitor);
        System.out.println("Usuario cadastrado! ID: " + usuario.getId());
    }
    private void listarUsuarios() throws SQLException {
        List<Usuario> usuarios = usuarioService.listar();
        if (usuarios.isEmpty()) {
            System.out.println("Nenhum usuario cadastrado.");
        }
        for (Usuario usuario : usuarios) {
            System.out.println("ID: " + usuario.getId() + " | Nome: " + usuario.getNomeCompleto() + " | Matricula: " + usuario.getMatricula() + " | E-mail: " + usuario.getEmail() + " | Monitor: " + (usuario.isHabilitadoMonitor() ? "Sim" : "Nao"));
        }
    }
    private void selecionarUsuario() throws SQLException {
        listarUsuarios();
        int id = lerInteiro("ID do usuario: ");
        for (Usuario usuario : usuarioService.listar()) {
            if (usuario.getId() == id) {
                usuarioAtual = usuario;
                System.out.println("Usuario selecionado: " + usuario.getNomeCompleto());
                return;
            }
        }
        throw new IllegalArgumentException("Usuario nao encontrado. A selecao anterior foi mantida.");
    }
    private void cadastrarDisciplina() throws SQLException {
        String codigo = ler("Codigo: ");
        String nome = ler("Nome: ");
        Disciplina disciplina = disciplinaService.cadastrar(codigo, nome);
        System.out.println("Disciplina cadastrada! ID: " + disciplina.getId());
    }
    private void listarDisciplinas() throws SQLException {
        List<Disciplina> disciplinas = disciplinaService.listar();
        if (disciplinas.isEmpty()) {
            System.out.println("Nenhuma disciplina cadastrada.");
        }
        for (Disciplina disciplina : disciplinas) {
            System.out.println("ID: " + disciplina.getId() + " | Codigo: " + disciplina.getCodigo() + " | Nome: " + disciplina.getNome());
        }
    }
    private Disciplina escolherDisciplina() throws SQLException {
        listarDisciplinas();
        int id = lerInteiro("ID da disciplina: ");
        for (Disciplina disciplina : disciplinaService.listar()) {
            if (disciplina.getId() == id) {
                return disciplina;
            }
        }
        throw new IllegalArgumentException("Disciplina nao encontrada.");
    }
    private Usuario escolherMonitor() throws SQLException {
        List<Usuario> usuarios = usuarioService.listar();
        for (Usuario usuario : usuarios) {
            if (usuario.isHabilitadoMonitor()) {
                System.out.println("ID: " + usuario.getId() + " | Monitor: " + usuario.getNomeCompleto());
            }
        }
        int id = lerInteiro("ID do monitor responsavel: ");
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id && usuario.isHabilitadoMonitor()) {
                return usuario;
            }
        }
        throw new IllegalArgumentException("Monitor habilitado nao encontrado.");
    }
    private SessaoMonitoria lerDadosSessao(int id, StatusSessao status, Usuario monitor) throws SQLException {
        Disciplina disciplina = escolherDisciplina();
        LocalDate data = LocalDate.parse(ler("Data (AAAA-MM-DD): "));
        LocalTime inicio = LocalTime.parse(ler("Inicio (HH:MM): "));
        LocalTime fim = LocalTime.parse(ler("Termino (HH:MM): "));
        int capacidade = lerInteiro("Capacidade: ");
        int quantidade = lerInteiro("Quantidade de assuntos na pauta: ");
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Informe pelo menos um assunto.");
        }
        List<ItemPauta> pauta = new ArrayList<>();
        for (int ordem = 1; ordem <= quantidade; ordem++) {
            pauta.add(new ItemPauta(0, ordem, ler("Assunto " + ordem + ": ")));
        }
        String modalidade = ler("Modalidade: 1 - Presencial | 2 - Remota: ");
        if (modalidade.equals("1")) {
            String sala = ler("Sala: ");
            return new SessaoPresencial(id, disciplina, monitor, pauta, data, inicio, fim, capacidade, status, sala);
        }
        if (modalidade.equals("2")) {
            String link = ler("Link de acesso: ");
            return new SessaoRemota(id, disciplina, monitor, pauta, data, inicio, fim, capacidade, status, link);
        }
        throw new IllegalArgumentException("Modalidade invalida.");
    }
    private void cadastrarSessao() throws SQLException {
        Usuario monitor = exigirMonitor();
        SessaoMonitoria sessao = lerDadosSessao(0, StatusSessao.AGENDADA, monitor);
        int id = sessaoService.cadastrar(sessao, monitor.getId());
        System.out.println("Sessao cadastrada! ID: " + id);
    }
    private void mostrarSessao(SessaoMonitoria sessao) throws SQLException {
        System.out.println("\nSessao: " + sessao.getId() + " | Status: " + sessao.getStatus());
        System.out.println("Disciplina: " + sessao.getDisciplina().getNome() + " | ID: " + sessao.getDisciplina().getId());
        System.out.println("Monitor: " + sessao.getMonitor().getNomeCompleto() + " | ID: " + sessao.getMonitor().getId());
        System.out.println("Data: " + sessao.getData() + " | Horario: " + sessao.getHoraInicio() + " ate " + sessao.getHoraFim());
        System.out.println("Capacidade: " + sessao.getCapacidade() + " | Vagas disponiveis: " + sessaoService.consultarVagasDisponiveis(sessao.getId()));
        System.out.println(sessao.obterOrientacaoAcesso());
        System.out.println("Pauta:");
        for (ItemPauta item : sessao.getPauta()) {
            System.out.println(item.getOrdem() + " - " + item.getDescricao());
        }
    }
    private void mostrarSessoes(List<SessaoMonitoria> sessoes) throws SQLException {
        if (sessoes.isEmpty()) {
            System.out.println("Nenhuma sessao encontrada.");
        }
        for (SessaoMonitoria sessao : sessoes) {
            mostrarSessao(sessao);
        }
    }
    private void buscarSessoes() throws SQLException {
        Disciplina disciplina = escolherDisciplina();
        LocalDate inicio = LocalDate.parse(ler("Data inicial (AAAA-MM-DD): "));
        LocalDate fim = LocalDate.parse(ler("Data final (AAAA-MM-DD): "));
        mostrarSessoes(sessaoService.buscarPorDisciplinaEPeriodo(disciplina.getId(), inicio, fim));
    }
    private void editarSessao() throws SQLException {
        Usuario responsavel = exigirMonitor();
        int id = lerInteiro("ID da sessao: ");
        SessaoMonitoria atual = sessaoService.buscarPorId(id);
        if (atual.getMonitor().getId() != responsavel.getId()) {
            throw new IllegalArgumentException("Voce nao e o monitor responsavel.");
        }
        mostrarSessao(atual);
        System.out.println("Informe os dados completos da sessao. Repita os valores que deseja manter.");
        System.out.println("Com inscricoes registradas, mantenha disciplina, monitor, data, horarios e modalidade.");
        Usuario monitor = escolherMonitor();
        SessaoMonitoria alterada = lerDadosSessao(id, atual.getStatus(), monitor);
        if (!confirmar("Salvar alteracoes?")) {
            System.out.println("Alteracoes descartadas.");
            return;
        }
        sessaoService.editar(alterada, responsavel.getId());
        System.out.println("Sessao atualizada.");
    }
    private void excluirSessao() throws SQLException {
        Usuario monitor = exigirMonitor();
        int id = lerInteiro("ID da sessao: ");
        SessaoMonitoria sessao = sessaoService.buscarPorId(id);
        mostrarSessao(sessao);
        if (confirmar("Excluir definitivamente esta sessao e sua pauta?")) {
            sessaoService.excluir(id, monitor.getId());
            System.out.println("Sessao excluida.");
        }
    }
    private void inscrever() throws SQLException {
        Usuario estudante = exigirUsuario();
        int sessaoId = lerInteiro("ID da sessao: ");
        int id = inscricaoService.inscrever(estudante.getId(), sessaoId);
        System.out.println("Inscricao realizada! ID: " + id);
    }
    private void consultarHistorico() throws SQLException {
        Usuario estudante = exigirUsuario();
        mostrarInscricoes(presencaService.consultarHistorico(estudante.getId()));
    }
    private void cancelarInscricao() throws SQLException {
        Usuario estudante = exigirUsuario();
        int id = lerInteiro("ID da sua inscricao: ");
        if (confirmar("Cancelar esta inscricao?")) {
            cancelamentoService.cancelarInscricao(id, estudante.getId());
            System.out.println("Inscricao cancelada.");
        }
    }
    private void cancelarSessao() throws SQLException {
        Usuario monitor = exigirMonitor();
        int id = lerInteiro("ID da sessao: ");
        if (confirmar("Cancelar a sessao e suas inscricoes ativas?")) {
            cancelamentoService.cancelarSessao(id, monitor.getId());
            System.out.println("Sessao e inscricoes ativas canceladas.");
        }
    }
    private void consultarInscritos() throws SQLException {
        Usuario monitor = exigirMonitor();
        int id = lerInteiro("ID da sessao: ");
        mostrarInscricoes(presencaService.consultarInscritos(id, monitor.getId()));
    }
    private void registrarPresenca() throws SQLException {
        Usuario monitor = exigirMonitor();
        int id = lerInteiro("ID da inscricao: ");
        String resposta = ler("P - Presente | A - Ausente: ");
        StatusPresenca presenca;
        if (resposta.equalsIgnoreCase("P")) {
            presenca = StatusPresenca.PRESENTE;
        } else if (resposta.equalsIgnoreCase("A")) {
            presenca = StatusPresenca.AUSENTE;
        } else {
            throw new IllegalArgumentException("Informe P ou A.");
        }
        presencaService.registrarPresenca(id, monitor.getId(), presenca);
        System.out.println("Presenca atualizada.");
    }
    private void mostrarInscricoes(List<Inscricao> inscricoes) {
        if (inscricoes.isEmpty()) {
            System.out.println("Nenhuma inscricao encontrada.");
        }
        for (Inscricao inscricao : inscricoes) {
            SessaoMonitoria sessao = inscricao.getSessao();
            System.out.println("\nInscricao: " + inscricao.getId() + " | Status: " + inscricao.getStatus());
            System.out.println("Estudante: " + inscricao.getEstudante().getNomeCompleto());
            System.out.println("Sessao: " + sessao.getId() + " | Status da sessao: " + sessao.getStatus());
            System.out.println("Disciplina: " + sessao.getDisciplina().getNome());
            System.out.println("Data: " + sessao.getData() + " | Horario: " + sessao.getHoraInicio() + " ate " + sessao.getHoraFim());
            System.out.println("Presenca: " + inscricao.getPresenca());
        }
    }
}