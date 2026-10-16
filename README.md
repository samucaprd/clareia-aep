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

## Sobre o projeto

O Clareia é um sistema de gerenciamento de monitorias acadêmicas desenvolvido para a AEP do 4º semestre de Engenharia de Software da UniCesumar.

O projeto está relacionado ao ODS 4 — Educação de Qualidade, facilitando o acesso às monitorias e a organização de sessões, inscrições e presenças.

### Integrantes

- Samuel Freitas Emeriquis Moreira
- Pedro Henrique Piciani Pazinato
- João Pedro Ramirez

## Tecnologias e organização

- Java 21.
- MySQL 8.0.
- JDBC com MySQL Connector/J, disponível na pasta `lib`.
- Interface pelo terminal.
- Arquitetura em camadas, separando apresentação, regras de negócio e acesso aos dados.

### Estrutura do projeto

| Caminho | Conteúdo |
|---|---|
| `src/clareia/Clareia.java` | Menu principal da aplicação |
| `src/clareia/model` | Entidades e enumerações |
| `src/clareia/service` | Regras de negócio e coordenação das operações |
| `src/clareia/dao` | Consultas e operações no banco |
| `src/clareia/infra` | Configuração da conexão JDBC |
| `src/clareia/Teste*.java` | Programas de teste |
| `database/schema.sql` | Criação do banco e das tabelas |
| `docs` | Documentação do planejamento |
| `lib` | Driver JDBC |
| `bin` | Classes compiladas localmente, ignoradas pelo Git |

## Programação Orientada a Objetos

- **Abstração:** `SessaoMonitoria` é uma classe abstrata.
- **Herança:** `SessaoPresencial` e `SessaoRemota` estendem `SessaoMonitoria`.
- **Polimorfismo:** cada modalidade implementa `obterOrientacaoAcesso()` com `@Override`.
- **Composição:** a sessão mantém seus próprios itens de pauta.
- **Encapsulamento:** os atributos são privados e acessados por métodos.

## Preparação do ambiente

As instruções abaixo utilizam Windows e PowerShell.

### Pré-requisitos

- JDK 21 instalado, com `java` e `javac` disponíveis no terminal.
- MySQL Server 8.0 instalado e em execução.
- MySQL Workbench para executar o script inicial.
- Git, caso o projeto seja obtido por clonagem.

O ambiente utilizado nos testes foi Windows 11, JDK 21 e MySQL 8.0.46. O projeto não utiliza Maven.

Confira o Java:

```powershell
java -version
javac -version
```

### Obter o projeto

```powershell
git clone https://github.com/samucaprd/clareia-aep.git
cd clareia-aep
```

Se o repositório já estiver no computador, abra o terminal na pasta existente.

### Preparar o banco de dados

1. Abra o MySQL Workbench e conecte-se ao servidor local.
2. Abra o arquivo `database/schema.sql`.
3. Execute o script completo na primeira instalação.
4. Atualize a lista de schemas e confira o banco `clareia`.

O script cria as tabelas, os relacionamentos e as restrições. Ele não inclui os dados inseridos durante os testes de desenvolvimento.

Não execute novamente o script completo sobre uma instalação já preparada: as tabelas já existentes produzirão erros. Para retomar o uso da aplicação, basta manter o servidor MySQL em execução.

### Configuração da conexão

A configuração atual utiliza:

- Servidor: `localhost`
- Porta: `3306`
- Banco: `clareia`
- Usuário: `root`

A senha é solicitada ao iniciar o programa e não fica gravada no código.

A URL está em `src/clareia/infra/ConexaoBanco.java`. Caso o ambiente utilize outro servidor ou porta, ajuste essa URL. Para utilizar outro usuário de banco no aplicativo principal, ajuste o valor `"root"` na criação de `ConexaoBanco` em `Clareia.java`.

## Compilar e executar

Execute os comandos na pasta principal do repositório.

Crie a pasta de compilação:

```powershell
New-Item -ItemType Directory -Force bin
```

Compile a aplicação:

```powershell
javac -encoding UTF-8 -cp "lib/*" -d bin src/clareia/infra/*.java src/clareia/model/*.java src/clareia/dao/*.java src/clareia/service/*.java src/clareia/Clareia.java
```

Execute:

```powershell
java -cp "bin;lib/*" clareia.Clareia
```

Informe a senha do MySQL. Os caracteres não aparecem durante a digitação.

Execute pelo terminal, pois a aplicação utiliza `System.console()`.

## Utilização

Em um banco recém-criado:

1. Cadastre um estudante e um usuário habilitado como monitor.
2. Cadastre uma disciplina.
3. Selecione o monitor pela opção `5`.
4. Cadastre uma sessão futura, com pauta e sala ou link.
5. Selecione o estudante pela opção `5`.
6. Busque a sessão e realize a inscrição.
7. Consulte o histórico do estudante.
8. Selecione novamente o monitor para consultar os inscritos.

Os IDs são gerados pelo banco. Utilize os números exibidos na sua instalação.

### Menu principal

| Opção | Operação |
|---|---|
| 1 e 2 | Cadastrar e consultar usuários |
| 3 e 4 | Cadastrar e consultar disciplinas |
| 5 | Selecionar ou trocar o usuário |
| 6 e 7 | Cadastrar e consultar sessões |
| 8 | Buscar sessões por disciplina e período |
| 9 e 10 | Editar e excluir sessões |
| 11 e 12 | Inscrever-se e consultar o próprio histórico |
| 13 e 14 | Cancelar inscrição ou sessão |
| 15 e 16 | Consultar inscritos e registrar presença ou ausência |
| 17 | Concluir sessão |
| 0 | Encerrar |

A seleção de usuário identifica quem está operando o sistema. Não há autenticação por senha de usuário. A senha solicitada na inicialização pertence à conexão com o MySQL.

### Comportamentos importantes

- Os filtros de período incluem as datas inicial e final.
- Sessões consecutivas, em que uma termina no horário de início da outra, não representam conflito.
- Cancelamentos preservam os registros.
- Uma inscrição cancelada não pode ser recriada para o mesmo estudante e sessão.
- Inscrições canceladas também impedem a exclusão da sessão.
- O registro de presença exige que a sessão já tenha terminado.
- A conclusão é realizada pelo monitor responsável após o término e exige que todas as inscrições ativas tenham presença ou ausência registrada.
- Inscrições canceladas não impedem a conclusão.
- A conclusão da sessão não altera as inscrições ativas para canceladas.

## Testes

Os programas `Teste*.java` verificam funcionalidades específicas. Alguns criam registros reais e outros alteram dados de teste. Leia o arquivo antes de executá-lo e utilize um banco destinado ao desenvolvimento.

Para compilar também os programas de teste:

```powershell
javac -encoding UTF-8 -cp "lib/*" -d bin src/clareia/infra/*.java src/clareia/model/*.java src/clareia/dao/*.java src/clareia/service/*.java src/clareia/*.java
```

Exemplo de execução:

```powershell
java -cp "bin;lib/*" clareia.TesteSessoes
```

Durante o desenvolvimento foram executados cenários de:

- Cadastro, consulta e rejeição de matrículas, e-mails e códigos de disciplina duplicados.
- Recuperação de registros após reiniciar a aplicação.
- Herança, polimorfismo e validações dos modelos de sessão.
- Cadastro, consulta, edição e exclusão de sessões.
- Busca por disciplina e período.
- Inscrições, lotação, duplicidade, conflito de horários e impedimento de inscrição do próprio monitor.
- Restrições de edição e exclusão com inscrições registradas.
- Cancelamentos, liberação de vagas e preservação do histórico.
- Presença, ausência e restrições de responsável, horário e estado da inscrição.
- Conclusão de sessão e bloqueio por presença pendente.
- Rollback de sessão e pauta após falha provocada na gravação.
- Navegação integrada pelo menu principal.

Esses resultados correspondem aos cenários executados no ambiente de desenvolvimento. Ainda não foram realizados testes simultâneos de concorrência nem confirmada a instalação em outro computador.

## Solução de problemas

- **`javac` não reconhecido:** confira a instalação do JDK e a configuração do `PATH`.
- **Classe principal não encontrada:** salve os arquivos e execute novamente a compilação a partir da pasta do projeto.
- **`Access denied`:** confira o usuário e a senha do MySQL. Reinicie o aplicativo para informar outra senha.
- **Falha de comunicação:** verifique se o servidor MySQL está em execução e se a porta está correta.
- **Banco ou tabela inexistente:** confira a execução inicial de `database/schema.sql`.
- **Driver JDBC não encontrado:** confira se o arquivo do Connector/J está em `lib` e se o comando inclui `"bin;lib/*"`.