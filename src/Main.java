public class Main {

    public static void main(String[] args) {
        String codigoFonte = "int main() {\n" +
                "    int a = 10;\n" +
                "    int b = 20;\n" +
                "    int c = a + b;\n" +
                "    return c;\n" +
                "}";
        executarTodosAnalisadores(codigoFonte);
    }

    public static void executarAnalisadorSintatico(String codigoFonte) {
        if (AnalisadorSintatico.verificarEstrutura(codigoFonte)) {
            System.out.println("Estrutura sintática correta!");
        } else {
            System.out.println("Erro na estrutura sintática!");
        }
    }

    public static void executarAnalisadorLexico(String codigoFonte) {
        AnalisadorLexico.realizarAnalise(codigoFonte);
    }

    public static void executarAnalisadorSemantico(String codigoFonte) {
        if (AnalisadorSemantico.verificarTipagem(codigoFonte)) {
            System.out.println("Tipagem correta!");
        } else {
            System.out.println("Erro na tipagem!");
        }
    }

    public static void executarTodosAnalisadores(String codigoFonte) {
        System.out.println("-------------------------------");
        System.out.println("Executando análise léxica...");
        executarAnalisadorLexico(codigoFonte);

        System.out.println("-------------------------------");
        System.out.println("Executando análise sintática...");
        executarAnalisadorSintatico(codigoFonte);

        System.out.println("-------------------------------");
        System.out.println("Executando análise semântica...");
        executarAnalisadorSemantico(codigoFonte);
    }
}
