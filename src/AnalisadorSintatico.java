
import java.util.*;

public class AnalisadorSintatico {

    public static boolean verificarEstrutura(String codigoFonte) {
        try {
            List<String> tokens = Arrays.asList(codigoFonte.split("\\s+"));
            ListIterator<String> it = tokens.listIterator();
            programa(it);
            return !it.hasNext(); 
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void programa(ListIterator<String> it) {
        while (it.hasNext()) {
            declaracao(it);
        }
    }

    private static void declaracao(ListIterator<String> it) {
        if (it.hasNext()) {
            String token = it.next();
            switch (token) {
                case "int":
                case "float":
                case "double":
                case "char":
                case "boolean":
                    declaracaoVariavelOuFuncao(it, token);
                    break;
                case "struct":
                    declaracaoEstrutura(it);
                    break;
                case "//":
                    comentarioLinha(it);
                    break;
                case "/*":
                    comentarioBloco(it);
                    break;
                case "if":
                case "while":
                case "for":
                case "switch":
                case "return":
                case "break":
                case "continue":
                    estruturaControle(it, token);
                    break;
                default:
                    if (token.endsWith(";")) {
                        // Declaração inválida
                        throw new RuntimeException("Declaração inválida: " + token);
                    }
                    break;
            }
        }
    }

    private static void declaracaoVariavelOuFuncao(ListIterator<String> it, String tipo) {
        if (it.hasNext()) {
            String id = it.next();
            if (id.matches("[a-zA-Z_]\\w*")) {
                if (it.hasNext()) {
                    String nextToken = it.next();
                    if (nextToken.equals(";")) {
                        // Declaração de variável sem atribuição
                        return;
                    } else if (nextToken.equals("=")) {
                        // Declaração de variável com atribuição
                        expressao(it);
                        if (it.hasNext() && it.next().equals(";")) {
                            return;
                        } else {
                            throw new RuntimeException("Falta ponto e vírgula após a atribuição");
                        }
                    } else if (nextToken.equals("(")) {
                        // Declaração de função
                        parametros(it);
                        if (it.next().equals(")")) {
                            bloco(it);
                            return;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Declaração de variável ou função inválida");
    }
    
    private static void declaracaoEstrutura(ListIterator<String> it) {
        if (it.hasNext()) {
            String id = it.next();
            if (id.matches("[a-zA-Z_]\\w*")) {
                if (it.next().equals("{")) {
                    while (it.hasNext()) {
                        String token = it.next();
                        if (token.equals("}")) {
                            return;
                        }
                        declaracao(it);
                    }
                }
            }
        }
        throw new RuntimeException("Declaração de estrutura inválida");
    }

    private static void parametros(ListIterator<String> it) {
        while (it.hasNext()) {
            String tipo = it.next();
            if (!tipo.matches("(int|float|double|char|boolean)")) {
                throw new RuntimeException("Tipo de parâmetro inválido: " + tipo);
            }
            String id = it.next();
            if (!id.matches("[a-zA-Z_]\\w*")) {
                throw new RuntimeException("Nome de parâmetro inválido: " + id);
            }
            if (it.hasNext()) {
                String token = it.next();
                if (token.equals(")")) {
                    it.previous(); // Regressa um passo para não consumir ')'
                    return;
                } else if (!token.equals(",")) {
                    throw new RuntimeException("Erro na declaração de parâmetros");
                }
            }
        }
    }

    private static void bloco(ListIterator<String> it) {
        if (it.next().equals("{")) {
            while (it.hasNext()) {
                String token = it.next();
                if (token.equals("}")) {
                    return;
                }
                it.previous(); // Regressa um passo para não consumir o próximo token
                declaracao(it);
            }
        }
        throw new RuntimeException("Erro na estrutura do bloco");
    }

    private static void comentarioLinha(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.contains("\n")) {
                break;
            }
        }
    }

    private static void comentarioBloco(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("*/")) {
                break;
            }
        }
    }

    private static void expressao(ListIterator<String> it) {
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals(";")) {
                it.previous();
                return;
            }
            if (!token.matches("[a-zA-Z_]\\w*|\\d+|[-+*/%]")) {
                throw new RuntimeException("Expressão inválida: " + token);
            }
        }
    }

    private static void estruturaControle(ListIterator<String> it, String tipo) {
        if (tipo.equals("if") || tipo.equals("while") || tipo.equals("for") || tipo.equals("switch")) {
            if (it.next().equals("(")) {
                expressao(it);
                if (it.next().equals(")")) {
                    bloco(it);
                    if (tipo.equals("if") && it.hasNext() && it.next().equals("else")) {
                        bloco(it);
                    }
                    return;
                }
            }
        } else if (tipo.equals("return")) {
            if (it.hasNext()) {
                expressao(it);
            }
            if (it.next().equals(";")) {
                return;
            }
        } else if (tipo.equals("break") || tipo.equals("continue")) {
            if (it.next().equals(";")) {
                return;
            }
        }
        throw new RuntimeException("Estrutura de controle inválida");
    }
}