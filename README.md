# CafeGestorCore

Núcleo de regras de negócio do **CafeGestor** (gestão de colheita e estoque de café), extraído do sistema desktop
(Java Swing) para ser reutilizado pela versão **web** do Projeto Integrador. Não há nenhum código de interface aqui:
telas Swing, Servlets ou controllers só precisam chamar os serviços.

## Estrutura

```
src/
  model/        entidades e enums (Funcionario, TipoCafe, Balaio, Estoque, MovimentacaoEstoque, Usuario...)
  exception/    RegraNegocioException (regra violada) e RepositorioException (falha técnica)
  util/         Validacoes e Conversor (texto digitado -> int/data)
  repository/   interfaces dos repositórios (padrão Repository)
    memory/     implementações em memória (usadas nos testes do main)
    jdbc/       implementações MySQL via JDBC
  security/     CodificadorSenha (interface) e CodificadorPbkdf2
  service/      casos de uso: Autenticacao, Funcionario, TipoCafe, Balaio, Estoque; CalculadoraProducao (cálculos puros)
  app/          FabricaDeServicos (raiz de composição / injeção de dependências)
  main/         Main (testes) e Verificador
sql/            schema.sql e dados_iniciais.sql
docs/           diagrama de classes original
CafeGestorTests/  projeto Maven com os testes JUnit 5 (compila ../src junto com os testes)
```

## Como executar os testes

**Modo padrão (sem banco):** abra a pasta no NetBeans (File > Open Project) e rode o projeto (F6).
`main.Main` executa 62 verificações com repositórios em memória e termina com código 0 se tudo passar.

**Contra o MySQL (opcional):**
1. Rode `sql/schema.sql` no MySQL (e, se quiser, `sql/dados_iniciais.sql`).
2. Adicione o `mysql-connector-j-8.x.jar` em Properties > Libraries (veja `lib/LEIAME.txt`).
3. Informe a conexão por variáveis de ambiente ou propriedades da JVM (nada de senha no código):
   `CAFE_DB_URL` (padrão `jdbc:mysql://localhost:3306/cafe_gestor?...`), `CAFE_DB_USER` (padrão `root`), `CAFE_DB_PASS`.
4. Em Properties > Run > Arguments, informe `--mysql`.

Os mesmos testes rodam nos dois modos.

## Testes JUnit

O projeto `CafeGestorTests/` tem os testes unitários (JUnit 5), sem acesso a banco de dados.
No NetBeans: abra a pasta `CafeGestorTests` e execute **Test** (Alt+F6), ou rode `mvn test` dentro dela.
Detalhes em `CafeGestorTests/README.md`.

## Usando no Swing ou na Web

```java
FabricaDeServicos servicos = FabricaDeServicos.comMySql(ConexaoMySql.doAmbiente(), Clock.systemDefaultZone());

// Antes (handler do botão Salvar): validação + JOptionPane misturados.
// Agora: a tela só converte, delega e exibe o resultado.
try {
    servicos.funcionarios().cadastrar(txtNome.getText(), txtFuncao.getText(), txtContato.getText());
} catch (RegraNegocioException e) {
    JOptionPane.showMessageDialog(this, e.getMessage());   // na web: devolver a mensagem na resposta
}
```

## Git / GitHub

```
cd CafeGestorCore
git init
git add .
git commit -m "Projeto base: separação das regras de negócio da interface Swing"
git branch -M main
git remote add origin https://github.com/<usuario>/<repositorio>.git
git push -u origin main
```

## Credenciais iniciais (apenas em `dados_iniciais.sql`)

Usuário `admin`, senha `admin123`. A senha é gravada com PBKDF2; troque-a após o primeiro acesso.
