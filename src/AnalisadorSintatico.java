import java.util.*;

public class AnalisadorSintatico {

    public static boolean verificarEstrutura(String codigoFonte) {
        List<String> tokens = Arrays.asList(codigoFonte.split("\\s+"));
        ListIterator<String> it = tokens.listIterator();
        boolean ocorreuExcecao = false;

        if (it != null) {
            while (it.hasNext()) {
                analisarToken(it);
            }
        } else {
            ocorreuExcecao = true;
        }

        if (ocorreuExcecao) {
            System.err.println("Ocorreu uma exceção durante a análise da estrutura.");
        }

        return !ocorreuExcecao;
    }

    private static void analisarToken(ListIterator<String> it) {
        if (!it.hasNext())
            return;
        String token = it.next();

        if (token.equals("int") || token.equals("float") || token.equals("double") ||
                token.equals("char") || token.equals("boolean")) {
            analisarDeclaracao(it, token);
        } else if (token.equals("struct")) {
            analisarEstrutura(it);
        } else if (token.equals("//")) {
            ignorarComentarioLinha(it);
        } else if (token.equals("/*")) {
            ignorarComentarioBloco(it);
        } else if (token.equals("if") || token.equals("while") || token.equals("for") ||
                token.equals("switch") || token.equals("return") || token.equals("break") ||
                token.equals("continue")) {
            analisarControle(it, token);
        } else {
            if (!token.endsWith(";")) {
                throw new RuntimeException("Declaração inválida: " + token);
            }
        }
    }

    private static void analisarDeclaracao(ListIterator<String> it, String tipo) {
        if (!it.hasNext())
            return;
        String id = it.next();
        if (!id.matches("[a-zA-Z_]\\w*")) {
            throw new RuntimeException("Declaração de variável ou função inválida");
        }
        if (it.hasNext()) {
            String proxToken = it.next();
            if (proxToken.equals("=")) {
                analisarExpressao(it);
                if (!it.next().equals(";")) {
                    throw new RuntimeException("Falta ponto e vírgula após a atribuição");
                }
            } else if (proxToken.equals("(")) {
                analisarParametros(it);
                if (!it.next().equals(")")) {
                    throw new RuntimeException("Falta fechar parênteses após os parâmetros");
                }
                analisarBloco(it);
            } else if (!proxToken.equals(";")) {
                throw new RuntimeException("Declaração inválida");
            }
        }
    }

    private static void analisarEstrutura(ListIterator<String> it) {
        // Implementação da análise de estrutura
        if (!it.hasNext())
            return;
        String id = it.next();
        if (!id.matches("[a-zA-Z_]\\w*")) {
            throw new RuntimeException("Declaração de estrutura inválida");
        }
        if (!it.hasNext() || !it.next().equals("{")) {
            throw new RuntimeException("Falta abrir chaves após a declaração da estrutura");
        }
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("}")) {
                return;
            }
            analisarDeclaracao(it, token);
        }
        throw new RuntimeException("Falta fechar chaves após a declaração da estrutura");
    }

    private static void analisarParametros(ListIterator<String> it) {
        // Implementação da análise de parâmetros
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
                    it.previous();
                    return;
                } else if (!token.equals(",")) {
                    throw new RuntimeException("Erro na declaração de parâmetros");
                }
            }
        }
        throw new RuntimeException("Falta fechar parênteses após os parâmetros");
    }

    private static void analisarBloco(ListIterator<String> it) {
        // Implementação da análise de bloco
        if (!it.hasNext() || !it.next().equals("{")) {
            throw new RuntimeException("Falta abrir chaves no bloco");
        }
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("}")) {
                return;
            }
            it.previous();
            analisarDeclaracao(it, token);
        }
        throw new RuntimeException("Falta fechar chaves no bloco");
    }

    private static void ignorarComentarioLinha(ListIterator<String> it) {
        // Implementação da análise de comentário de linha
        while (it.hasNext()) {
            String token = it.next();
            if (token.contains("\n")) {
                return;
            }
        }
    }

    private static void ignorarComentarioBloco(ListIterator<String> it) {
        // Implementação da análise de comentário de bloco
        while (it.hasNext()) {
            String token = it.next();
            if (token.equals("*/")) {
                return;
            }
        }
        throw new RuntimeException("Falta fechar comentário de bloco");
    }

    private static void analisarExpressao(ListIterator<String> it) {
        // Implementação da análise de expressão
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

    private static void analisarControle(ListIterator<String> it, String tipo) {
        // Implementação da análise de estruturas de controle
        if (tipo.equals("if") || tipo.equals("while") || tipo.equals("for") || tipo.equals("switch")) {
            if (!it.hasNext() || !it.next().equals("(")) {
                throw new RuntimeException("Falta abrir parênteses na estrutura de controle");
            }
            analisarExpressao(it);
            if (!it.next().equals(")")) {
                throw new RuntimeException("Falta fechar parênteses na estrutura de controle");
            }
            analisarBloco(it);
            if (tipo.equals("if") && it.hasNext() && it.next().equals("else")) {
                analisarBloco(it);
            }
            return;
        } else if (tipo.equals("return")) {
            if (it.hasNext()) {
                analisarExpressao(it);
            }
            if (!it.next().equals(";")) {
                throw new RuntimeException("Falta ponto e vírgula no retorno");
            }
            return;
        } else if (tipo.equals("break") || tipo.equals("continue")) {
            if (!it.next().equals(";")) {
                throw new RuntimeException("Falta ponto e vírgula no " + tipo);
            }
            return;
        }
        throw new RuntimeException("Estrutura de controle inválida");
    }
}
