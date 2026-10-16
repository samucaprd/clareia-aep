package clareia.service;

import clareia.dao.DisciplinaDAO;
import clareia.model.Disciplina;
import java.sql.SQLException;
import java.util.List;

public class DisciplinaService {
    private final DisciplinaDAO disciplinaDAO;
    public DisciplinaService(DisciplinaDAO disciplinaDAO) {
        this.disciplinaDAO = disciplinaDAO;
    }
    public Disciplina cadastrar(String codigo, String nome) throws SQLException {
        String codigoValidado = validarTexto(codigo, "Codigo", 30);
        String nomeValidado = validarTexto(nome, "Nome", 120);
        Disciplina disciplina = new Disciplina(0, codigoValidado, nomeValidado);
        try {
            return disciplinaDAO.inserir(disciplina);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Ja existe uma disciplina com esse codigo.", e);
            }
            throw e;
        }
    }
    public List<Disciplina> listar() throws SQLException {
        return disciplinaDAO.listar();
    }
    private String validarTexto(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " deve ser informado.");
        }
        String texto = valor.trim();
        if (texto.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(campo + " deve ter no maximo " + tamanhoMaximo + " caracteres.");
        }
        return texto;
    }
}