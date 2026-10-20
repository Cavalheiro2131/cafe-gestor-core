# CafeGestorTests

Projeto de testes unitários (JUnit 5) do **CafeGestorCore**. É um projeto Maven, que o NetBeans abre direto.
Ele compila o código de `../src` (o núcleo) junto com os testes de `src/test/java`, então não há cópia de código.

## Como executar

**No NetBeans:** File > Open Project > pasta `CafeGestorTests`. Clique com o botão direito no projeto > **Test** (ou Alt+F6).
Na primeira vez o NetBeans baixa o JUnit pela internet. O resultado aparece na janela *Test Results*.

**Pelo terminal** (com Maven instalado), dentro da pasta `CafeGestorTests`:

```
mvn test
```

## O que é testado

Nenhum teste acessa banco de dados: os serviços rodam com repositórios em memória.

| Classe de teste | Alvo | Requisito |
|---|---|---|
| `model.EstoqueTest` | `Estoque.adicionar/retirar` | saldo nunca negativo |
| `model.TipoMovimentacaoTest` | `TipoMovimentacao.aplicar` | entrada soma, saída subtrai |
| `model.PerfilUsuarioTest` | `PerfilUsuario.podeAlterarDados` | permissões por perfil |
| `service.CalculadoraProducaoTest` | `CalculadoraProducao` | cálculo de sacas, sobra e média |
| `util.ConversorTest` | `Conversor` | conversão de número e data digitados |
| `util.ValidacoesTest` | `Validacoes` | campos obrigatórios e tamanhos |
| `security.CodificadorPbkdf2Test` | `CodificadorPbkdf2` | senha nunca em texto puro |
| `service.EstoqueServiceTest` | `EstoqueService` | entrada, saída e histórico |
| `service.BalaioServiceTest` | `BalaioService` | registro de balaio e total |
| `service.AutenticacaoServiceTest` | `AutenticacaoService` | login, cadastro e troca de senha |
| `service.FuncionarioServiceTest` | `FuncionarioService` | cadastro, edição e inativação |

O plano de testes completo (automatizados e manuais) está no documento de plano de testes entregue com o projeto.
