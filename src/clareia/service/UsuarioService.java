package clareia.service;

import clareia.dao.UsuarioDAO;
import clareia.model.Usuario;
import java.sql.SQLException;
import java.util.List;

public class UsuarioService {
    private final UsuarioDAO usuarioDAO;

    public UsuarioService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }
    public Usuario cadastrar(
            String nomeCompleto,
            String matricula,
            String email,
            boolean habilitadoMonitor) throws SQLException {

        String nomeValidado = validarTexto(nomeCompleto, "Nome completo", 150);
        String matriculaValidada = validarTexto(matricula, "Matricula", 30);
        String emailValidado = validarTexto(email, "E-mail", 150);
        Usuario usuario = new Usuario(
            0,
            nomeValidado,
            matriculaValidada,
            emailValidado,
            habilitadoMonitor
        );

        try {
            return usuarioDAO.inserir(usuario);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new IllegalArgumentException("Ja existe um usuario com essa matricula ou esse e-mail.", e);
            }

            throw e;
        }
    }
    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listar();
    }
    private String validarTexto(
            String valor,
            String campo,
            int tamanhoMaximo) {

        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(
                campo + " deve ser informado."
            );
        }
        String texto = valor.trim();
        if (texto.length() > tamanhoMaximo) {
            throw new IllegalArgumentException(
                campo + " deve ter no maximo "
                + tamanhoMaximo + " caracteres."
            );
        }

        return texto;
    }
}