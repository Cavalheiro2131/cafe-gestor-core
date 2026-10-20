package main;

import app.FabricaDeServicos;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import model.Balaio;
import model.Funcionario;
import model.MovimentacaoEstoque;
import model.PerfilUsuario;
import model.TipoCafe;
import model.TipoMovimentacao;
import model.Usuario;
import repository.jdbc.ConexaoMySql;
import util.Conversor;

/**
 * Ponto de entrada e bateria de testes da camada de negócio.
 *
 * <pre>
 *   Sem argumentos : roda os testes com repositórios em memória (não precisa de banco).
 *   --mysql        : roda exatamente os mesmos testes contra o MySQL (precisa do driver e do banco criado).
 * </pre>
 *
 * Os mesmos testes passam nas duas implementações, o que demonstra a substituição de repositórios
 * (Liskov) e a inversão de dependência (os serviços só conhecem as interfaces).
 */
public class Main {
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 9);
    private static final Clock RELOGIO = Clock.fixed(
            HOJE.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant(), ZoneId.of("America/Sao_Paulo"));

    public static void main(String[] args) {
        boolean usarMySql = args.length > 0 && args[0].equals("--mysql");
        FabricaDeServicos servicos = usarMySql
                ? FabricaDeServicos.comMySql(ConexaoMySql.doAmbiente(), RELOGIO)
                : FabricaDeServicos.emMemoria(RELOGIO);
        System.out.println("CafeGestor - testes da camada de negócio (" + (usarMySql ? "MySQL" : "memória") + ")");

        Verificador v = new Verificador();
        // Sufixo único para os testes não colidirem com dados já existentes no MySQL.
        String sufixo = String.valueOf(System.currentTimeMillis() % 1_000_000);

        testarConversor(v);
        testarAutenticacao(v, servicos, sufixo);
        testarFuncionarios(v, servicos, sufixo);
        testarTiposCafe(v, servicos, sufixo);
        testarEstoque(v, servicos, sufixo);
        testarBalaios(v, servicos, sufixo);

        System.exit(v.resumo());
    }

    private static void testarConversor(Verificador v) {
        v.grupo("Conversor e validações de entrada");
        v.igual("converte inteiro válido", 12, Conversor.paraInteiroPositivo(" 12 ", "quantidade"));
        v.rejeita("rejeita texto no lugar de número", "número inteiro", () -> Conversor.paraInteiroPositivo("abc", "quantidade"));
        v.rejeita("rejeita zero", "maior que zero", () -> Conversor.paraInteiroPositivo("0", "quantidade"));
        v.rejeita("rejeita campo vazio", "número inteiro", () -> Conversor.paraInteiroPositivo("", "quantidade"));
        v.igual("converte data dd/mm/aaaa", LocalDate.of(2026, 10, 9), Conversor.paraData("09/10/2026", "data"));
        v.rejeita("rejeita data inexistente (31/02)", "dd/mm/aaaa", () -> Conversor.paraData("31/02/2026", "data"));
        v.rejeita("rejeita formato errado", "dd/mm/aaaa", () -> Conversor.paraData("2026-10-09", "data"));
    }

    private static void testarAutenticacao(Verificador v, FabricaDeServicos s, String sufixo) {
        v.grupo("Autenticação e usuários");
        String login = "teste" + sufixo;
        Usuario criado = s.autenticacao().cadastrar("Usuário de Teste", login, "senha123", PerfilUsuario.OPERADOR);
        v.verdadeiro("cadastro gera id", criado.getId() > 0);
        v.verdadeiro("senha não é guardada em texto puro", !criado.getSenhaCodificada().contains("senha123"));

        v.igual("login correto autentica", criado.getId(), s.autenticacao().autenticar(login, "senha123").getId());
        v.rejeita("senha errada é recusada", "inválidos", () -> s.autenticacao().autenticar(login, "outra"));
        v.rejeita("usuário inexistente é recusado (mesma mensagem)", "inválidos",
                () -> s.autenticacao().autenticar("nao-existe" + sufixo, "senha123"));
        v.rejeita("login vazio é recusado", "login e a senha", () -> s.autenticacao().autenticar("", "senha123"));
        v.rejeita("login repetido é recusado", "Já existe",
                () -> s.autenticacao().cadastrar("Outro", login, "senha123", PerfilUsuario.ADMIN));
        v.rejeita("senha curta é recusada", "pelo menos 6",
                () -> s.autenticacao().cadastrar("Curto", "curto" + sufixo, "123", PerfilUsuario.ADMIN));

        s.autenticacao().alterarSenha(criado.getId(), "senha123", "nova-senha");
        v.igual("nova senha passa a valer", criado.getId(), s.autenticacao().autenticar(login, "nova-senha").getId());
        v.rejeita("senha antiga deixa de valer", "inválidos", () -> s.autenticacao().autenticar(login, "senha123"));
        v.rejeita("troca exige a senha atual correta", "senha atual",
                () -> s.autenticacao().alterarSenha(criado.getId(), "errada", "qualquer1"));

        v.verdadeiro("OPERADOR não altera dados", !PerfilUsuario.OPERADOR.podeAlterarDados());
        v.verdadeiro("GERENTE altera dados", PerfilUsuario.GERENTE.podeAlterarDados());
        v.verdadeiro("ADMIN altera dados", PerfilUsuario.ADMIN.podeAlterarDados());
    }

    private static void testarFuncionarios(Verificador v, FabricaDeServicos s, String sufixo) {
        v.grupo("Funcionários");
        Funcionario f = s.funcionarios().cadastrar("  Maria " + sufixo + "  ", "Colhedora", "99999-0000");
        v.igual("cadastro remove espaços das pontas", "Maria " + sufixo, f.getNome());
        v.verdadeiro("novo funcionário nasce ativo", f.isAtivo());
        v.rejeita("nome vazio é recusado", "nome", () -> s.funcionarios().cadastrar("  ", "Colhedor", "1"));
        v.rejeita("função vazia é recusada", "função", () -> s.funcionarios().cadastrar("Ana", "", "1"));

        s.funcionarios().editar(f.getId(), "Maria " + sufixo, "Encarregada", "88888-0000");
        v.igual("edição altera a função", "Encarregada", s.funcionarios().buscar(f.getId()).getFuncao());
        v.rejeita("editar inexistente é recusado", "não encontrado",
                () -> s.funcionarios().editar(999_999, "X", "Y", "Z"));

        s.funcionarios().inativar(f.getId());
        v.verdadeiro("inativar muda o status", !s.funcionarios().buscar(f.getId()).isAtivo());
        v.verdadeiro("inativo some da lista de ativos",
                s.funcionarios().listarAtivos().stream().noneMatch(x -> x.getId() == f.getId()));
        s.funcionarios().reativar(f.getId());
        v.verdadeiro("reativar volta a aparecer entre os ativos",
                s.funcionarios().listarAtivos().stream().anyMatch(x -> x.getId() == f.getId()));
    }

    private static void testarTiposCafe(Verificador v, FabricaDeServicos s, String sufixo) {
        v.grupo("Tipos de café");
        TipoCafe t = s.tiposCafe().cadastrar("Arábica " + sufixo, "Bourbon", null);
        v.verdadeiro("cadastro gera id", t.getId() > 0);
        v.igual("variedade é guardada", "Bourbon", s.tiposCafe().buscar(t.getId()).getVariedade());
        v.verdadeiro("variedade e observações são opcionais",
                s.tiposCafe().cadastrar("Conilon " + sufixo, "", "").getVariedade() == null);
        v.rejeita("nome vazio é recusado", "nome", () -> s.tiposCafe().cadastrar("", "x", "y"));
        v.rejeita("buscar inexistente é recusado", "não encontrado", () -> s.tiposCafe().buscar(999_999));
    }

    private static void testarEstoque(Verificador v, FabricaDeServicos s, String sufixo) {
        v.grupo("Estoque (entrada, saída e histórico)");
        TipoCafe t = s.tiposCafe().cadastrar("Estoque " + sufixo, "Catuaí", null);
        int id = t.getId();

        v.igual("saldo inicial é zero", 0, s.estoque().consultarSaldo(id));
        s.estoque().registrarEntrada(id, 100, "Colheita da safra");
        v.igual("entrada soma ao saldo", 100, s.estoque().consultarSaldo(id));
        s.estoque().registrarSaida(id, 30, "Venda");
        v.igual("saída subtrai do saldo", 70, s.estoque().consultarSaldo(id));
        s.estoque().registrarEntrada(id, 5, null);
        v.igual("nova entrada reaproveita o mesmo registro de estoque", 75, s.estoque().consultarSaldo(id));

        v.rejeita("saída maior que o saldo é recusada", "Estoque insuficiente", () -> s.estoque().registrarSaida(id, 76, "Venda"));
        v.igual("saldo não muda após saída recusada", 75, s.estoque().consultarSaldo(id));
        v.rejeita("saída exige destino", "destino", () -> s.estoque().registrarSaida(id, 1, " "));
        v.rejeita("quantidade zero é recusada", "maior que zero", () -> s.estoque().registrarEntrada(id, 0, null));
        v.rejeita("quantidade negativa é recusada", "maior que zero", () -> s.estoque().registrarSaida(id, -3, "Venda"));
        v.rejeita("tipo de café inexistente é recusado", "não encontrado", () -> s.estoque().registrarEntrada(999_999, 1, null));

        TipoCafe semEstoque = s.tiposCafe().cadastrar("Sem estoque " + sufixo, null, null);
        v.rejeita("saída sem nenhuma entrada é recusada", "Estoque insuficiente",
                () -> s.estoque().registrarSaida(semEstoque.getId(), 1, "Venda"));

        java.util.List<MovimentacaoEstoque> historico = s.estoque().historico(id);
        v.igual("histórico guarda só as 3 operações aceitas", 3, historico.size());
        v.igual("primeira movimentação é a entrada de 100", TipoMovimentacao.ENTRADA, historico.get(0).getTipo());
        v.igual("segunda movimentação é a saída de 30", 30, historico.get(1).getQuantidade());
        v.igual("destino da saída é registrado", "Venda", historico.get(1).getDestino());
        v.igual("data vem do relógio injetado", HOJE, historico.get(0).getData());
        v.verdadeiro("saldo aparece na listagem geral",
                s.estoque().listarSaldos().stream().anyMatch(e -> e.getTipoCafe().getId() == id && e.getQuantidadeSacas() == 75));
    }

    private static void testarBalaios(Verificador v, FabricaDeServicos s, String sufixo) {
        v.grupo("Balaios");
        Funcionario f = s.funcionarios().cadastrar("Colhedor " + sufixo, "Colhedor", "77777-0000");
        TipoCafe t = s.tiposCafe().cadastrar("Balaio " + sufixo, "Mundo Novo", null);

        Balaio b = s.balaios().registrar(f.getId(), t.getId(), 20, HOJE);
        v.verdadeiro("registro gera id", b.getId() > 0);
        s.balaios().registrar(f.getId(), t.getId(), 15, HOJE.minusDays(1));
        v.igual("total do funcionário soma os registros", 35, s.balaios().totalDoFuncionario(f.getId()));
        v.verdadeiro("registro aparece na listagem",
                s.balaios().listar().stream().anyMatch(x -> x.getId() == b.getId()
                        && x.getFuncionario().getId() == f.getId() && x.getTipoCafe().getId() == t.getId()));

        v.rejeita("quantidade zero é recusada", "maior que zero", () -> s.balaios().registrar(f.getId(), t.getId(), 0, HOJE));
        v.rejeita("data futura é recusada", "futuro", () -> s.balaios().registrar(f.getId(), t.getId(), 5, HOJE.plusDays(1)));
        v.rejeita("data ausente é recusada", "data", () -> s.balaios().registrar(f.getId(), t.getId(), 5, null));
        v.rejeita("funcionário inexistente é recusado", "Funcionário não encontrado",
                () -> s.balaios().registrar(999_999, t.getId(), 5, HOJE));
        v.rejeita("tipo de café inexistente é recusado", "Tipo de café não encontrado",
                () -> s.balaios().registrar(f.getId(), 999_999, 5, HOJE));

        s.funcionarios().inativar(f.getId());
        v.rejeita("funcionário inativo não registra balaio", "inativo", () -> s.balaios().registrar(f.getId(), t.getId(), 5, HOJE));
        v.igual("total não muda após registros recusados", 35, s.balaios().totalDoFuncionario(f.getId()));
    }
}
