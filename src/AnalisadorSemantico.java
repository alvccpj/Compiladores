import java.util.*;

public class AnalisadorSemantico {

    // Método principal que realiza a verificação de tipos do código fornecido
    public static boolean validarTipos(String codigo) {
        try {
            List<String> elementos = Arrays.asList(codigo.split("\\s+"));
            ListIterator<String> iterador = elementos.listIterator();

            while (iterador.hasNext()) {
                String elemento = iterador.next();

                if (isTipo(elemento)) {
                    if (!validarDeclaracaoVarOuFunc(iterador, elemento)) {
                        return false;
                    }
                } else if (elemento.equals("struct")) {
                    if (!validarDeclaracaoStruct(iterador)) {
                        return false;
                    }
                } else if (elemento.equals("//")) {
                    pularComentarioLinha(iterador);
                } else if (elemento.equals("/*")) {
                    pularComentarioBloco(iterador);
                } else {
                    if (elemento.endsWith(";")) {
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

    // Método para verificar a declaração de variáveis ou funções
    private static boolean validarDeclaracaoVarOuFunc(ListIterator<String> it, String tipo) {
        if (it.hasNext()) {
            String identificador = it.next();
            if (identificador.matches("[a-zA-Z_]\\w*")) {
                if (it.hasNext()) {
                    String proximoToken = it.next();
                    if (proximoToken.equals(";")) {
                        return true;
                    } else if (proximoToken.equals("=")) {
                        if (!validarExpressao(it)) {
                            return false;
                        }
                        if (!it.next().equals(";")) {
                            return false;
                        }
                        return true;
                    } else if (proximoToken.equals("(")) {
                        if (!validarParametros(it)) {
                            return false;
                        }
                        if (!it.next().equals(")")) {
                            return false;
                        }
                        if (!validarBloco(it)) {
                            return false;
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Método para validar expressões
    private static boolean validarExpressao(ListIterator<String> it) {
        Stack<String> pilha = new Stack<>();

        while (it.hasNext()) {
            String token = it.next();

            if (isOperador(token)) {
                if (pilha.size() < 2) {
                    return false;
                }
                String operando2 = pilha.pop();
                String operando1 = pilha.pop();
                if (!tiposCompativeis(operando1, operando2, token)) {
                    return false;
                }
                String resultado = inferirTipoResultado(operando1, operando2, token);
                if (resultado == null) {
                    return false;
                }
                pilha.push(resultado);
            } else {
                pilha.push(determinarTipo(token));
            }
        }

        return pilha.size() == 1;
    }

    // Verifica se os tipos são compatíveis
    private static boolean tiposCompativeis(String tipo1, String tipo2, String operador) {
        if (operador.matches("[+\\-*/%]")) {
            return tipo1.equals("int") && tipo2.equals("int");
        } else {
            return false;
        }
    }

    // Inferir o tipo de resultado de uma operação
    private static String inferirTipoResultado(String tipo1, String tipo2, String operador) {
        if (operador.matches("[+\\-*/%]")) {
            return "int";
        } else {
            return null;
        }
    }

    // Determina o tipo de um token
    private static String determinarTipo(String token) {
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
    private static boolean validarParametros(ListIterator<String> it) {
        while (it.hasNext()) {
            String tipoParametro = it.next();
            if (!isTipo(tipoParametro)) {
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

    // Verifica se um tipo é válido
    private static boolean isTipo(String tipo) {
        return tipo.matches("(int|float|double|char|boolean)");
    }

    // Verifica se o bloco de código é válido
    private static boolean validarBloco(ListIterator<String> it) {
        boolean bemTipado = true;
        int contadorChaves = 0;

        while (it.hasNext()) {
            String token = it.next();

            if (token.equals("{")) {
                contadorChaves++;
            } else if (token.equals("}")) {
                contadorChaves--;

                if (contadorChaves < 0) {
                    bemTipado = false;
                    break;
                }
            }
        }
        if (contadorChaves != 0) {
            bemTipado = false;
        }

        return bemTipado;
    }

    // Ignora comentários de linha
    private static void pularComentarioLinha(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("\n")) {
                return;
            }
        }
    }

    // Ignora comentários de bloco
    private static void pularComentarioBloco(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("*/")) {
                return;
            }
        }
    }

    // Verifica a declaração de uma estrutura
    private static boolean validarDeclaracaoStruct(ListIterator<String> it) {
        if (it.hasNext()) {
            String nomeStruct = it.next();
            if (nomeStruct.matches("[a-zA-Z_]\\w*")) {
                if (it.hasNext() && it.next().equals("{")) {
                    if (validarCamposStruct(it)) {
                        return it.hasNext() && it.next().equals("}");
                    }
                }
            }
        }
        return false;
    }

    // Verifica os campos dentro de uma estrutura
    private static boolean validarCamposStruct(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (isTipo(token)) {
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
