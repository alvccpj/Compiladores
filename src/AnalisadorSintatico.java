import java.util.*;

public class AnalisadorSintatico {

    public static boolean verificarEstrutura(String codigoFonte) {
        try {
            List<String> tokens = Arrays.asList(codigoFonte.split("\\s+"));
            ListIterator<String> iterador = tokens.listIterator();
            analisarPrograma(iterador);
            return !iterador.hasNext();
        } catch (Exception e) {
            System.err.println("Erro durante a análise sintática: " + e.getMessage());
            return false;
        }
    }

    private static void analisarPrograma(ListIterator<String> iterador) {
        while (iterador.hasNext()) {
            analisarSentenca(iterador);
        }
    }

    private static void analisarSentenca(ListIterator<String> iterador) {
        if (!iterador.hasNext()) return;

        String token = iterador.next();
        if (isTipoVariavel(token)) {
            analisarVariavelOuFuncao(iterador, token);
        } else if ("struct".equals(token)) {
            analisarEstrutura(iterador);
        } else if (isComentario(token)) {
            ignorarComentario(iterador, token);
        } else if (isControleFluxo(token)) {
            analisarControle(iterador, token);
        } else if ("int".equals(token) && iterador.hasNext() && "main".equals(iterador.next())) {
            iterador.previous();
            iterador.previous();
            analisarFuncaoMain(iterador); // Ajustado para tratar a função main
        } else if (token.endsWith(";")) {
            throw new RuntimeException("Declaração inválida: " + token);
        } else {
            throw new RuntimeException("Token inesperado: " + token);
        }
    }

    private static boolean isTipoVariavel(String token) {
        return Arrays.asList("int", "float", "double", "char", "boolean").contains(token);
    }

    private static boolean isComentario(String token) {
        return "//".equals(token) || "/*".equals(token);
    }

    private static boolean isControleFluxo(String token) {
        return Arrays.asList("if", "while", "for", "switch", "return", "break", "continue").contains(token);
    }

    // Método ajustado para analisar funções e variáveis
    private static void analisarVariavelOuFuncao(ListIterator<String> iterador, String tipo) {
        if (!iterador.hasNext()) {
            throw new RuntimeException("Identificador esperado após o tipo " + tipo);
        }

        String id = iterador.next();
        if (!id.matches("[a-zA-Z_]\\w*")) {
            throw new RuntimeException("Identificador inválido: " + id);
        }

        if (!iterador.hasNext()) {
            throw new RuntimeException("Token inesperado após identificador " + id);
        }

        String proximoToken = iterador.next();
        if (";".equals(proximoToken)) {
            return;  // Declaração de variável sem inicialização
        }

        if ("=".equals(proximoToken)) {
            analisarExpressao(iterador);
            verificarPontoVirgula(iterador);
        } else if ("(".equals(proximoToken)) {
            analisarParametrosFuncao(iterador);
            verificarFechamentoParenteses(iterador);
            analisarBlocoCodigo(iterador);
        } else {
            throw new RuntimeException("Token inesperado: " + proximoToken);
        }
    }

    private static void analisarEstrutura(ListIterator<String> iterador) {
        if (!iterador.hasNext()) {
            throw new RuntimeException("Identificador esperado após 'struct'");
        }

        String id = iterador.next();
        if (!id.matches("[a-zA-Z_]\\w*")) {
            throw new RuntimeException("Identificador de estrutura inválido: " + id);
        }

        if (!iterador.hasNext() || !"{".equals(iterador.next())) {
            throw new RuntimeException("Esperado '{' após identificador da estrutura " + id);
        }

        while (iterador.hasNext()) {
            String token = iterador.next();
            if ("}".equals(token)) {
                return;
            }
            iterador.previous();  // Recuar para não consumir o próximo token
            analisarSentenca(iterador);
        }

        throw new RuntimeException("Esperado '}' para fechar a estrutura");
    }

    private static void analisarParametrosFuncao(ListIterator<String> iterador) {
        while (iterador.hasNext()) {
            String tipoParametro = iterador.next();
            if (!tipoParametro.matches("(int|float|double|char|boolean)")) {
                throw new RuntimeException("Tipo de parâmetro inválido: " + tipoParametro);
            }

            if (!iterador.hasNext()) {
                throw new RuntimeException("Identificador do parâmetro esperado após o tipo " + tipoParametro);
            }

            String idParametro = iterador.next();
            if (!idParametro.matches("[a-zA-Z_]\\w*")) {
                throw new RuntimeException("Nome de parâmetro inválido: " + idParametro);
            }

            if (iterador.hasNext() && ")".equals(iterador.next())) {
                iterador.previous();  // Recuar para não consumir ')'
                break;
            } else if (!",".equals(iterador.previous())) {
                throw new RuntimeException("Erro na declaração de parâmetros, token inesperado: " + iterador.next());
            }
        }
    }

    private static void verificarFechamentoParenteses(ListIterator<String> iterador) {
        if (!iterador.hasNext() || !")".equals(iterador.next())) {
            throw new RuntimeException("Esperado ')' após parâmetros da função");
        }
    }

    private static void analisarBlocoCodigo(ListIterator<String> iterador) {
        if (!iterador.hasNext() || !"{".equals(iterador.next())) {
            throw new RuntimeException("Esperado '{' no início do bloco de código");
        }

        while (iterador.hasNext()) {
            String token = iterador.next();
            if ("}".equals(token)) {
                return;
            }
            iterador.previous();  // Recuar para não consumir o próximo token
            analisarSentenca(iterador);
        }

        throw new RuntimeException("Esperado '}' no fim do bloco de código");
    }

    private static void ignorarComentario(ListIterator<String> iterador, String token) {
        if ("//".equals(token)) {
            ignorarComentarioLinha(iterador);
        } else if ("/*".equals(token)) {
            ignorarComentarioBloco(iterador);
        }
    }

    private static void ignorarComentarioLinha(ListIterator<String> iterador) {
        while (iterador.hasNext()) {
            String token = iterador.next();
            if (token.contains("\n")) {
                break;
            }
        }
    }

    private static void ignorarComentarioBloco(ListIterator<String> iterador) {
        while (iterador.hasNext()) {
            String token = iterador.next();
            if ("*/".equals(token)) {
                break;
            }
        }
    }

    private static void analisarExpressao(ListIterator<String> iterador) {
        while (iterador.hasNext()) {
            String token = iterador.next();
            if (";".equals(token)) {
                iterador.previous();
                return;
            }
            if (!token.matches("[a-zA-Z_]\\w*|\\d+|[-+*/%]")) {
                throw new RuntimeException("Expressão inválida: " + token);
            }
        }
    }

    private static void verificarPontoVirgula(ListIterator<String> iterador) {
        if (!iterador.hasNext() || !";".equals(iterador.next())) {
            throw new RuntimeException("Esperado ';' no final da declaração");
        }
    }

    private static void analisarControle(ListIterator<String> iterador, String tipo) {
        if (Arrays.asList("if", "while", "for", "switch").contains(tipo)) {
            if (!iterador.hasNext() || !"(".equals(iterador.next())) {
                throw new RuntimeException("Esperado '(' após " + tipo);
            }
            analisarExpressao(iterador);
            verificarFechamentoParenteses(iterador);
            analisarBlocoCodigo(iterador);
            if ("if".equals(tipo) && iterador.hasNext() && "else".equals(iterador.next())) {
                analisarBlocoCodigo(iterador);
            }
        } else if ("return".equals(tipo)) {
            if (iterador.hasNext()) {
                analisarExpressao(iterador);
            }
            verificarPontoVirgula(iterador);
        } else if (Arrays.asList("break", "continue").contains(tipo)) {
            verificarPontoVirgula(iterador);
        } else {
            throw new RuntimeException("Estrutura de controle inválida: " + tipo);
        }
    }

    private static void analisarFuncaoMain(ListIterator<String> iterador) {
        if (!iterador.hasNext() || !"int".equals(iterador.next())) {
            throw new RuntimeException("Esperado 'int' antes de 'main'");
        }
        if (!iterador.hasNext() || !"main".equals(iterador.next())) {
            throw new RuntimeException("Esperado 'main' após 'int'");
        }
        if (!iterador.hasNext() || !"(".equals(iterador.next())) {
            throw new RuntimeException("Esperado '(' após 'main'");
        }
        if (!iterador.hasNext() || !")".equals(iterador.next())) {
            throw new RuntimeException("Esperado ')' após parâmetros de 'main'");
        }
        analisarBlocoCodigo(iterador);
    }
}
