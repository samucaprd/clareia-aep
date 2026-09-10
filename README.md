# Clareia

## Requisitos Funcionais
- RF01 - O sistema deve permitir cadastrar e consultar estudantes e monitores, registrando nome completo, número de matrícula e e-mail, além de cadastrar disciplinas com código e nome.
- RF02 - O sistema deve permitir cadastrar, consultar, editar e excluir sessões de monitoria, informando disciplina, monitor responsável, pauta, data, horários, capacidade e modalidade, com sala para sessões presenciais ou link para remotas. A edição será permitida antes do início, e a exclusão somente quando não houver inscrições registradas.
- RF03 - O sistema deve permitir buscar monitorias por disciplina e período, exibindo pauta, monitor responsável, horário, local ou link de acesso e vagas disponíveis.
- RF04 - O sistema deve permitir inscrever estudantes em sessões futuras com vagas disponíveis, impedindo inscrições duplicadas, conflitos de horário entre inscrições ativas e inscrição do monitor na própria sessão.
- RF05 - O sistema deve permitir cancelar inscrições e sessões antes de seu início, preservando o histórico. O cancelamento de uma inscrição deverá liberar a vaga; o cancelamento de uma sessão pelo monitor responsável deverá cancelar todas as inscrições vinculadas.
- RF06 - O sistema deve permitir ao monitor responsável consultar os inscritos e registrar presenças e ausências após o término da sessão, além de permitir ao estudante consultar seu histórico de participação.


## Cronograma de Desenvolvimento

| Sprint / Data | Épico | User Story | Responsável |
|---|---|---|---|
| Sprint 1 — 14/09 a 27/09/2026 | Cadastro de usuários e disciplinas | COMO usuário do sistema, EU QUERO cadastrar e consultar estudantes, monitores e disciplinas, PARA QUE as informações necessárias à organização das monitorias estejam disponíveis e armazenadas no banco de dados. **RF01.** | Samuel |
| Sprint 2 — 28/09 a 04/10/2026 | Gestão e consulta de monitorias | COMO monitor, EU QUERO cadastrar, consultar, editar e excluir sessões conforme as regras estabelecidas, PARA QUE eu possa organizar as monitorias. COMO estudante, EU QUERO buscar sessões por disciplina e período, PARA QUE eu encontre apoio adequado às minhas necessidades. **RF02 e RF03.** | Pedro Henrique |
| Sprint 3 — 05/10 a 18/10/2026 | Inscrições e cancelamentos | COMO estudante, EU QUERO realizar e cancelar inscrições, respeitando vagas e horários, PARA QUE eu possa organizar minha participação. COMO monitor, EU QUERO cancelar sessões quando necessário, PARA QUE as inscrições vinculadas sejam atualizadas e o histórico seja preservado. **RF04 e RF05.** | João Pedro |
| Sprint 4 — 19/10 a 25/10/2026 | Presenças e histórico | COMO monitor, EU QUERO consultar os inscritos e registrar presenças e ausências, PARA QUE eu acompanhe a participação nas sessões. COMO estudante, EU QUERO consultar meu histórico, PARA QUE eu acompanhe as monitorias das quais participei. **RF06.** | Samuel, Pedro Henrique e João Pedro |
