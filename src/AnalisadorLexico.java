import java.util.*;
import java.util.regex.*;

public class AnalisadorLexico {

    // Lista dos tipos de tokens que serão identificados
    private static final List<String> tipos = Arrays.asList(
            "COMENTARIO",
            "NUM_DEC",
            "NUM_INT",
            "PALAVRA_RESERVADA",
            "OPERADOR",
            "SIMBOLO_ESPECIAL",
            "ID",
            "TEXTO");

    // Expressões regulares para identificar cada tipo de token
    private static final List<String> expressoes = Arrays.asList(
            "//.*",                                             // Comentário
            "\\b\\d+\\.\\d+\\b",                                 // Número decimal
            "\\b\\d+\\b",                                       // Número inteiro
            "\\b(int|float|char|boolean|void|if|else|for|while|scanf|println|main|return)\\b", // Palavras reservadas
            "(\\+|\\-|\\*|\\/|\\%|==|=|!=|>=|<=|>|<|&&|\\|\\|)",  // Operadores
            "(\\(|\\)|\\[|\\]|\\{|\\}|,|;)",                      // Símbolos especiais
            "\\b[a-zA-Z_]\\w*\\b",                               // Identificadores (variáveis, funções, etc.)
            "\".*?\"");                                          // Texto entre aspas

    // Método para identificar os tokens no código fonte
    static List<Token> identificarTokens(String codigoFonte) {
        List<Token> tokens = new ArrayList<>();
        Set<Integer> posicoes = new HashSet<>(); // Conjunto para controlar as posições já analisadas

        for (int i = 0; i < expressoes.size(); i++) {
            String tipo = tipos.get(i);
            String expressao = expressoes.get(i);
            Matcher matcher = Pattern.compile(expressao).matcher(codigoFonte);

            while (matcher.find()) {
                if (!posicoes.contains(matcher.start())) { // Verifica se a posição já foi analisada
                    tokens.add(new Token(tipo, matcher.group(), matcher.start()));

                    // Marca as posições ocupadas pelo token na fonte
                    for (int j = matcher.start(); j < matcher.end(); j++) {
                        posicoes.add(j);
                    }
                }
            }
        }

        tokens.sort(Comparator.comparingInt(Token::getPosicao)); // Ordena os tokens por posição
        return tokens;
    }

    // Método principal que realiza a análise léxica
    public static boolean realizarAnalise(String codigoFonte) {
        List<Token> tokens = identificarTokens(codigoFonte);
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("(%s), '%s'%n", token.getTipo(), token.getValor());
        }
        return false;
    }

    // Método de entrada para executar o analisador léxico
    public static void executarAnalisadorLexico() {
        String codigoFonte = "int main() {\n" +
                "    int a = 10;\n" +
                "    int b = 20;\n" +
                "    int c = a + b;\n" +
                "    return c;\n" +
                "}";

        realizarAnalise(codigoFonte);
    }

    // Classe interna para representar um Token
    static class Token {
        private final String tipo;
        private final String valor;
        private final int posicao;

        public Token(String tipo, String valor, int posicao) {
            this.tipo = tipo;
            this.valor = valor;
            this.posicao = posicao;
        }

        public String getTipo() {
            return tipo;
        }

        public String getValor() {
            return valor;
        }

        public int getPosicao() {
            return posicao;
        }
    }
}
