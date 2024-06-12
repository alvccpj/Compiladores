import java.util.*;
import java.util.regex.*;

public class AnalisadorLexico {

    private static final List<String> tipos = Arrays.asList(
            "COMENTARIO",
            "NUM_DEC",
            "NUM_INT",
            "PALAVRA_RESERVADA",
            "OPERADOR",
            "SIMBOLO_ESPECIAL",
            "ID",
            "TEXTO");

    private static final List<String> expressoes = Arrays.asList(
            "//.*",
            "\\b\\d+\\.\\d+\\b",
            "\\b\\d+\\b",
            "\\b(int|float|char|boolean|void|if|else|for|while|scanf|println|main|return)\\b",
            "(\\+|\\-|\\*|\\/|\\%|==|=|!=|>=|<=|>|<|&&|\\|\\|)",
            "(\\(|\\)|\\[|\\]|\\{|\\}|,|;)",
            "\\b[a-zA-Z_]\\w*\\b",
            "\".*?\"");

    static List<Token> identificarTokens(String codigoFonte) {
        List<Token> tokens = new ArrayList<>();
        Set<Integer> posicoes = new HashSet<>();

        for (int i = 0; i < expressoes.size(); i++) {
            String tipo = tipos.get(i);
            String expressao = expressoes.get(i);
            Matcher matcher = Pattern.compile(expressao).matcher(codigoFonte);

            while (matcher.find()) {
                if (!posicoes.contains(matcher.start())) {
                    tokens.add(new Token(tipo, matcher.group(), matcher.start()));

                    for (int j = matcher.start(); j < matcher.end(); j++) {
                        posicoes.add(j);
                    }
                }
            }
        }

        tokens.sort(Comparator.comparingInt(Token::getPosicao));
        return tokens;
    }

    public static boolean realizarAnalise(String codigoFonte) {
        List<Token> tokens = identificarTokens(codigoFonte);
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            System.out.printf("(%s), '%s'%n", token.getTipo(), token.getValor());
        }
        return false;
    }

    public static void executarAnalisadorLexico() {
        String codigoFonte = "int main() {\n" +
                "    int a = 10;\n" +
                "    int b = 20;\n" +
                "    int c = a + b;\n" +
                "    return c;\n" +
                "}";

        realizarAnalise(codigoFonte);
    }

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
