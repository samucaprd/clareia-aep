package clareia;

import clareia.model.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TesteSessoes {
    public static void main(String[] args) {
        Usuario monitor = new Usuario(6, "Monitor Teste", "MONITOR001", "monitorteste@email.com", true);
        Usuario estudante = new Usuario(1, "Estudante Teste", "TESTE001", "estudante.teste@example.com", false);
        Disciplina disciplina = new Disciplina(1, "POO", "Programacao orientada a objetos");
        List<ItemPauta> pauta = new ArrayList<>();
        pauta.add(new ItemPauta(0, 1, "Heranca e polimorfismo"));
        LocalDate data = LocalDate.now().plusDays(1);
        LocalTime inicio = LocalTime.of(14, 0);
        LocalTime fim = LocalTime.of(15, 0);
        SessaoMonitoria presencial = new SessaoPresencial(0, disciplina, monitor, pauta, data, inicio, fim, 10, StatusSessao.AGENDADA, "Sala 12");
        SessaoMonitoria remota = new SessaoRemota(0, disciplina, monitor, pauta, data, inicio, fim, 10, StatusSessao.AGENDADA, "https://example.com/monitoria");
        List<SessaoMonitoria> sessoes = new ArrayList<>();
        sessoes.add(presencial);
        sessoes.add(remota);
        System.out.println("--- MODALIDADES E POLIMORFISMO ---");
        for (SessaoMonitoria sessao : sessoes) {
            System.out.println(sessao.obterOrientacaoAcesso());
            System.out.println("Pauta: " + sessao.getPauta().get(0).getDescricao());
        }
        System.out.println("\n--- MONITOR NAO HABILITADO ---");
        try {
            new SessaoPresencial(0, disciplina, estudante, pauta, data, inicio, fim, 10, StatusSessao.AGENDADA, "Sala 12");
            throw new AssertionError("FALHA: aceitou um monitor nao habilitado.");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejeitado corretamente: " + e.getMessage());
        }
        System.out.println("\n--- CAPACIDADE INVALIDA ---");
        try {
            new SessaoPresencial(0, disciplina, monitor, pauta, data, inicio, fim, 0, StatusSessao.AGENDADA, "Sala 12");
            throw new AssertionError("FALHA: aceitou capacidade zero.");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejeitado corretamente: " + e.getMessage());
        }
        System.out.println("\n--- HORARIOS INVALIDOS ---");
        try {
            new SessaoRemota(0, disciplina, monitor, pauta, data, fim, inicio, 10, StatusSessao.AGENDADA, "https://example.com/monitoria");
            throw new AssertionError("FALHA: aceitou horario final anterior ao inicial.");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejeitado corretamente: " + e.getMessage());
        }
        System.out.println("\n--- PAUTA VAZIA ---");
        try {
            new SessaoPresencial(0, disciplina, monitor, new ArrayList<>(), data, inicio, fim, 10, StatusSessao.AGENDADA, "Sala 12");
            throw new AssertionError("FALHA: aceitou uma pauta vazia.");
        } catch (IllegalArgumentException e) {
            System.out.println("Rejeitado corretamente: " + e.getMessage());
        }
        System.out.println("\n--- INDEPENDENCIA DOS ITENS ---");
        pauta.get(0).alterarDescricao("Descricao alterada fora das sessoes");
        if (!presencial.getPauta().get(0).getDescricao().equals("Heranca e polimorfismo") || !remota.getPauta().get(0).getDescricao().equals("Heranca e polimorfismo")) {
            throw new AssertionError("FALHA: a alteracao externa modificou a pauta das sessoes.");
        }
        System.out.println("As sessoes preservaram seus proprios itens.");
        System.out.println("\nTestes concluidos.");
    }
}