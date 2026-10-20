package main;

import exception.RegraNegocioException;
import java.util.Objects;

/** Mini framework de verificação para os testes do main() (sem depender de JUnit). */
final class Verificador {
    private int total;
    private int falhas;

    void grupo(String titulo) {
        System.out.println();
        System.out.println("== " + titulo);
    }

    void verdadeiro(String descricao, boolean condicao) {
        total++;
        if (condicao) {
            System.out.println("  [OK]    " + descricao);
        } else {
            falhas++;
            System.out.println("  [FALHA] " + descricao);
        }
    }

    void igual(String descricao, Object esperado, Object atual) {
        total++;
        if (Objects.equals(esperado, atual)) {
            System.out.println("  [OK]    " + descricao);
        } else {
            falhas++;
            System.out.println("  [FALHA] " + descricao + " (esperado: " + esperado + ", atual: " + atual + ")");
        }
    }

    /** Espera uma RegraNegocioException cuja mensagem contenha o trecho informado. */
    void rejeita(String descricao, String trechoDaMensagem, Runnable acao) {
        total++;
        try {
            acao.run();
            falhas++;
            System.out.println("  [FALHA] " + descricao + " (nenhuma exceção lançada)");
        } catch (RegraNegocioException e) {
            if (e.getMessage().contains(trechoDaMensagem)) {
                System.out.println("  [OK]    " + descricao + " -> \"" + e.getMessage() + "\"");
            } else {
                falhas++;
                System.out.println("  [FALHA] " + descricao + " (mensagem inesperada: " + e.getMessage() + ")");
            }
        }
    }

    /** Imprime o resumo e devolve o código de saída do programa. */
    int resumo() {
        System.out.println();
        System.out.println("Total: " + total + " verificações, " + (total - falhas) + " OK, " + falhas + " falha(s).");
        return falhas == 0 ? 0 : 1;
    }
}
