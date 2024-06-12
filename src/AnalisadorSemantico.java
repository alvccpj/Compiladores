import java.util.*;

public class AnalisadorSemantico {

    // Método principal que verifica a tipagem do código fonte fornecido
    public static boolean verificarTipagem(String codigoFonte) {
        try {
            List<String> tokens = Arrays.asList(codigoFonte.split("\\s+"));
            ListIterator<String> iterador = tokens.listIterator();

            while (iterador.hasNext()) {
                String token = iterador.next();

                if (token.equals("int") || token.equals("float") || token.equals("double") || token.equals("char")
                        || token.equals("boolean")) {
                    if (!verificarDeclaracaoVariavelOuFuncao(iterador, token)) {
                        return false;
                    }
                } else if (token.equals("struct")) {
                    if (!verificarDeclaracaoEstrutura(iterador)) {
                        return false;
                    }
                } else if (token.equals("//")) {
                    ignorarComentarioLinha(iterador);
                } else if (token.equals("/*")) {
                    ignorarComentarioBloco(iterador);
                } else {
                    if (token.endsWith(";")) {
                        // Lógica para declaração de variável
                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Verifica a declaração de variável ou função
    private static boolean verificarDeclaracaoVariavelOuFuncao(ListIterator<String> it, String tipo) {
        if (it.hasNext()) {
            String id = it.next();
            if (id.matches("[a-zA-Z_]\\w*")) {
                if (it.hasNext()) {
                    String nextToken = it.next();
                    if (nextToken.equals(";")) {
                        return true;
                    } else if (nextToken.equals("=")) {
                        if (!verificarExpressao(it)) {
                            return false;
                        }
                        if (!it.next().equals(";")) {
                            return false;
                        }
                        return true;
                    } else if (nextToken.equals("(")) {
                        if (!verificarParametros(it)) {
                            return false;
                        }
                        if (!it.next().equals(")")) {
                            return false;
                        }
                        if (!verificarBloco(it)) {
                            return false;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Verifica a expressão para assegurar que a tipagem está correta
    private static boolean verificarExpressao(ListIterator<String> it) {
        Stack<String> pilha = new Stack<>();

        while (it.hasNext()) {
            String token = it.next();

            if (isOperador(token)) {
                if (pilha.size() < 2) {
                    return false;
                }
                String tipoOperando2 = pilha.pop();
                String tipoOperando1 = pilha.pop();
                if (!tiposSaoCompativeis(tipoOperando1, tipoOperando2, token)) {
                    return false;
                }
                String resultado = inferirTipoResultado(tipoOperando1, tipoOperando2, token);
                if (resultado == null) {
                    return false;
                }
                pilha.push(resultado);
            } else {
                pilha.push(inferirTipo(token));
            }
        }

        return pilha.size() == 1;
    }

    // Verifica se os tipos são compatíveis para uma operação
    private static boolean tiposSaoCompativeis(String tipo1, String tipo2, String operador) {
        if (operador.equals("+") || operador.equals("-") || operador.equals("*") || operador.equals("/")
                || operador.equals("%")) {
            return tipo1.equals("int") && tipo2.equals("int");
        } else {
            return false;
        }
    }

    // Infere o tipo do resultado de uma operação
    private static String inferirTipoResultado(String tipo1, String tipo2, String operador) {
        if (operador.equals("+") || operador.equals("-") || operador.equals("*") || operador.equals("/")
                || operador.equals("%")) {
            return "int";
        } else {
            return null;
        }
    }

    // Infere o tipo de um token
    private static String inferirTipo(String token) {
        if (token.matches("\\d+")) {
            return "int";
        } else if (token.matches("\\d+\\.\\d+")) {
            return "float";
        } else if (token.equals("true") || token.equals("false")) {
            return "boolean";
        } else {
            return null;
        }
    }

    // Verifica se o token é um operador
    private static boolean isOperador(String token) {
        return token.matches("[+\\-*/%]");
    }

    // Verifica a lista de parâmetros de uma função
    private static boolean verificarParametros(ListIterator<String> it) {
        while (it.hasNext()) {
            String tipoParametro = it.next();
            if (!isTipoBasico(tipoParametro)) {
                return false;
            }
            String idParametro = it.next();
            if (!idParametro.matches("[a-zA-Z_]\\w*")) {
                return false;
            }
            if (it.hasNext()) {
                String token = it.next();
                if (token.equals(")")) {
                    it.previous();
                    return true;
                } else if (!token.equals(",")) {
                    return false;
                }
            }
        }
        return false;
    }

    // Verifica se um tipo é básico (int, float, double, char, boolean)
    private static boolean isTipoBasico(String tipo) {
        return tipo.matches("(int|float|double|char|boolean)");
    }

    // Verifica o bloco de código para assegurar que está bem tipado
    private static boolean verificarBloco(ListIterator<String> it) {
        boolean bemTipado = true;
        int contChaves = 0;

        while (it.hasNext()) {
            String token = it.next();

            if (token.equals("{")) {
                contChaves++;
            } else if (token.equals("}")) {
                contChaves--;

                if (contChaves < 0) {
                    bemTipado = false;
                    break;
                }
            }

        }
        if (contChaves != 0) {
            bemTipado = false;
        }

        return bemTipado;
    }

    // Ignora comentários de linha
    private static void ignorarComentarioLinha(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("\n")) {
                return;
            }
        }
    }

    // Ignora comentários de bloco
    private static void ignorarComentarioBloco(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("*/")) {
                return;
            }
        }
    }

    // Verifica a declaração de uma estrutura
    private static boolean verificarDeclaracaoEstrutura(ListIterator<String> it) {
        if (it.hasNext()) {
            String nomeEstrutura = it.next();
            if (nomeEstrutura.matches("[a-zA-Z_]\\w*")) {
                if (it.hasNext() && it.next().equals("{")) {
                    if (verificarCamposEstrutura(it)) {
                        return it.hasNext() && it.next().equals("}");
                    }
                }
            }
        }
        return false;
    }

    // Verifica os campos dentro de uma estrutura
    private static boolean verificarCamposEstrutura(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (isTipoBasico(token)) {
                String nomeCampo = it.next();
                if (nomeCampo.matches("[a-zA-Z_]\\w*")) {
                    if (it.hasNext() && it.next().equals(";")) {
                        continue;
                    }
                }
            }
            return false;
        }
        return true;
    }
}