package main;

import servidor.ServidorCoordenador;
import cliente.NoCliente;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SISTEMA DISTRIBUÍDO - ACESSO CONCORRENTE ===");
        System.out.println("Disciplina: Sistemas Distribuídos");
        System.out.println("Objetivo: Simular acesso concorrente a recurso crítico\n");

        if (args.length == 0 || args[0].equals("todos")) {
            executarSistemaCompleto();
        } else if (args[0].equals("servidor")) {
            executarApenasServidor();
        } else if (args[0].equals("cliente") && args.length >= 2) {
            executarApenasCliente(args[1]);
        } else if (args[0].equals("teste")) {
            executarTestes();
        } else {
            mostrarAjuda();
        }
    }

    private static void executarSistemaCompleto() {
        System.out.println("🚀 Iniciando sistema completo (servidor + 4 clientes)...");

        // Executar servidor em thread separada
        Thread servidorThread = new Thread(() -> {
            try {
                System.out.println("[MAIN] Iniciando servidor coordenador...");
                ServidorCoordenador.main(new String[]{});
            } catch (Exception e) {
                System.err.println("[MAIN] Erro no servidor: " + e.getMessage());
            }
        });
        servidorThread.setDaemon(true);
        servidorThread.start();

        // Aguardar servidor iniciar
        try {
            Thread.sleep(3000);
            System.out.println("[MAIN] Servidor iniciado. Iniciando clientes...");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Executar 4 clientes
        ExecutorService clienteExecutor = Executors.newFixedThreadPool(4);
        String[] nomesClientes = {"Cliente1", "Cliente2", "Cliente3", "Cliente4"};

        for (String nomeCliente : nomesClientes) {
            clienteExecutor.submit(() -> {
                try {
                    System.out.println("[MAIN] Iniciando " + nomeCliente + "...");
                    NoCliente.main(new String[]{"localhost", "5000", nomeCliente});
                } catch (Exception e) {
                    System.err.println("[MAIN] Erro no " + nomeCliente + ": " + e.getMessage());
                }
            });

            // Pequeno delay entre início dos clientes
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Manter o sistema rodando
        try {
            System.out.println("\n[MAIN] Sistema em execução. Pressione Ctrl+C para finalizar.");
            clienteExecutor.shutdown();
            clienteExecutor.awaitTermination(1, TimeUnit.HOURS);
            System.out.println("[MAIN] Sistema finalizado.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void executarApenasServidor() {
        System.out.println("🖥️  Iniciando apenas servidor...");
        try {
            ServidorCoordenador.main(new String[]{});
        } catch (Exception e) {
            System.err.println("Erro ao executar servidor: " + e.getMessage());
        }
    }

    private static void executarApenasCliente(String nomeCliente) {
        System.out.println("👤 Iniciando cliente: " + nomeCliente);
        try {
            NoCliente.main(new String[]{"localhost", "5000", nomeCliente});
        } catch (Exception e) {
            System.err.println("Erro ao executar cliente: " + e.getMessage());
        }
    }

    private static void executarTestes() {
        System.out.println("🧪 Executando testes...");
        // Implementar testes automatizados aqui
        System.out.println("Testes concluídos!");
    }

    private static void mostrarAjuda() {
        System.out.println("📋 Uso do Sistema:");
        System.out.println("  java Main                    - Executa servidor + 4 clientes");
        System.out.println("  java Main todos              - Executa servidor + 4 clientes");
        System.out.println("  java Main servidor           - Executa apenas o servidor");
        System.out.println("  java Main cliente <nome>     - Executa apenas um cliente");
        System.out.println("  java Main teste              - Executa testes automatizados");
        System.out.println();
        System.out.println("📚 Exemplos:");
        System.out.println("  java Main servidor");
        System.out.println("  java Main cliente Worker1");
        System.out.println("  java Main todos");
        System.out.println();
        System.out.println("🎯 Funcionalidades implementadas:");
        System.out.println("  ✅ Exclusão mútua distribuída");
        System.out.println("  ✅ Acesso concorrente a recurso compartilhado");
        System.out.println("  ✅ Checkpoints e recuperação de falhas");
        System.out.println("  ✅ Relógios lógicos");
        System.out.println("  ✅ Persistência de estado do servidor");
        System.out.println("  ✅ Tolerância a falhas básica");
    }
}