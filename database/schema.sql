CREATE DATABASE IF NOT EXISTS clareia
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE clareia;

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_completo VARCHAR(150) NOT NULL,
    matricula VARCHAR(30) NOT NULL,
    email VARCHAR(150) NOT NULL,
    habilitado_monitor BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uk_usuarios_matricula UNIQUE (matricula),
    CONSTRAINT uk_usuarios_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE disciplinas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL,
    nome VARCHAR(120) NOT NULL,
    CONSTRAINT uk_disciplinas_codigo UNIQUE (codigo)
) ENGINE = InnoDB;

CREATE TABLE sessoes_monitoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    disciplina_id INT NOT NULL,
    monitor_id INT NOT NULL,
    modalidade VARCHAR(20) NOT NULL,
    sala VARCHAR(150) NULL,
    link_acesso VARCHAR(500) NULL,
    data DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    capacidade INT NOT NULL,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_sessoes_disciplina
        FOREIGN KEY (disciplina_id)
        REFERENCES disciplinas (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_sessoes_monitor
        FOREIGN KEY (monitor_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_sessoes_capacidade
        CHECK (capacidade > 0),

    CONSTRAINT ck_sessoes_horarios
        CHECK (hora_fim > hora_inicio),

    CONSTRAINT ck_sessoes_status
        CHECK (status IN ('AGENDADA', 'CANCELADA', 'CONCLUIDA')),

    CONSTRAINT ck_sessoes_modalidade
        CHECK (
            (
                modalidade = 'PRESENCIAL'
                AND sala IS NOT NULL
                AND link_acesso IS NULL)
            OR
            (
                modalidade = 'REMOTA'
                AND link_acesso IS NOT NULL
                AND sala IS NULL)
        )
) ENGINE = InnoDB;

CREATE TABLE inscricoes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sessao_id INT NOT NULL,
    estudante_id INT NOT NULL,
    data_criacao DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    presenca VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',

    CONSTRAINT uk_inscricoes_sessao_estudante
        UNIQUE (sessao_id, estudante_id),

    CONSTRAINT fk_inscricoes_sessao
        FOREIGN KEY (sessao_id)
        REFERENCES sessoes_monitoria (id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_inscricoes_estudante
        FOREIGN KEY (estudante_id)
        REFERENCES usuarios (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_inscricoes_status
        CHECK (status IN ('ATIVA', 'CANCELADA')),

    CONSTRAINT ck_inscricoes_presenca
        CHECK (presenca IN ('PENDENTE', 'PRESENTE', 'AUSENTE'))
) ENGINE = InnoDB;

CREATE TABLE itens_pauta (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sessao_id INT NOT NULL,
    ordem INT NOT NULL,
    descricao TEXT NOT NULL,

    CONSTRAINT uk_itens_pauta_sessao_ordem
        UNIQUE (sessao_id, ordem),

    CONSTRAINT fk_itens_pauta_sessao
        FOREIGN KEY (sessao_id)
        REFERENCES sessoes_monitoria (id)
        ON DELETE CASCADE,

    CONSTRAINT ck_itens_pauta_ordem
        CHECK (ordem > 0)
) ENGINE = InnoDB;