package app;

import java.time.Clock;
import repository.BalaioRepository;
import repository.EstoqueRepository;
import repository.FuncionarioRepository;
import repository.MovimentacaoRepository;
import repository.TipoCafeRepository;
import repository.UsuarioRepository;
import repository.jdbc.FonteDeConexao;
import repository.jdbc.JdbcBalaioRepository;
import repository.jdbc.JdbcEstoqueRepository;
import repository.jdbc.JdbcFuncionarioRepository;
import repository.jdbc.JdbcMovimentacaoRepository;
import repository.jdbc.JdbcTipoCafeRepository;
import repository.jdbc.JdbcUsuarioRepository;
import repository.memory.MemoriaBalaioRepository;
import repository.memory.MemoriaEstoqueRepository;
import repository.memory.MemoriaFuncionarioRepository;
import repository.memory.MemoriaMovimentacaoRepository;
import repository.memory.MemoriaTipoCafeRepository;
import repository.memory.MemoriaUsuarioRepository;
import security.CodificadorPbkdf2;
import service.AutenticacaoService;
import service.BalaioService;
import service.EstoqueService;
import service.FuncionarioService;
import service.TipoCafeService;

/**
 * Raiz de composição: o único lugar que sabe qual implementação de repositório usar e que liga
 * as dependências (injeção por construtor). A tela Swing, um Servlet ou o main() só pedem os serviços prontos.
 */
public final class FabricaDeServicos {
    private final AutenticacaoService autenticacao;
    private final FuncionarioService funcionarios;
    private final TipoCafeService tiposCafe;
    private final BalaioService balaios;
    private final EstoqueService estoque;

    private FabricaDeServicos(UsuarioRepository usuarios, FuncionarioRepository funcionarios,
                              TipoCafeRepository tipos, BalaioRepository balaios,
                              EstoqueRepository estoques, MovimentacaoRepository movimentacoes, Clock relogio) {
        this.autenticacao = new AutenticacaoService(usuarios, new CodificadorPbkdf2());
        this.funcionarios = new FuncionarioService(funcionarios);
        this.tiposCafe = new TipoCafeService(tipos);
        this.balaios = new BalaioService(balaios, funcionarios, tipos, relogio);
        this.estoque = new EstoqueService(estoques, movimentacoes, tipos, relogio);
    }

    public static FabricaDeServicos emMemoria(Clock relogio) {
        return new FabricaDeServicos(new MemoriaUsuarioRepository(), new MemoriaFuncionarioRepository(),
                new MemoriaTipoCafeRepository(), new MemoriaBalaioRepository(),
                new MemoriaEstoqueRepository(), new MemoriaMovimentacaoRepository(), relogio);
    }

    public static FabricaDeServicos comMySql(FonteDeConexao fonte, Clock relogio) {
        return new FabricaDeServicos(new JdbcUsuarioRepository(fonte), new JdbcFuncionarioRepository(fonte),
                new JdbcTipoCafeRepository(fonte), new JdbcBalaioRepository(fonte),
                new JdbcEstoqueRepository(fonte), new JdbcMovimentacaoRepository(fonte), relogio);
    }

    public AutenticacaoService autenticacao() { return autenticacao; }
    public FuncionarioService funcionarios() { return funcionarios; }
    public TipoCafeService tiposCafe() { return tiposCafe; }
    public BalaioService balaios() { return balaios; }
    public EstoqueService estoque() { return estoque; }
}
