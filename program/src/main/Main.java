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

        if (args.length == 0) {
            executarSistemaCompleto();
        } else if (args[0].equals("servidor")) {
            executarApenasServidor();
        } else if (args[0].equals("cliente") && args.length >= 2) {
            executarApenasCliente(args[1]);
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
            Thread.sleep(2000);
            System.out.println("[MAIN] Servidor iniciado. Iniciando clientes...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

            try {
                Thread.sleep(800); // Delay menor entre clientes
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Manter o sistema rodando
        try {
            System.out.println("\n[MAIN] Sistema em execução. Pressione Ctrl+C para finalizar.");
            clienteExecutor.shutdown();
            clienteExecutor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

    private static void mostrarAjuda() {
        System.out.println("📋 Uso do Sistema:");
        System.out.println("  java Main                    - Executa servidor + 4 clientes");
        System.out.println("  java Main servidor           - Executa apenas o servidor");
        System.out.println("  java Main cliente <nome>     - Executa apenas um cliente");
        System.out.println();
        System.out.println("📚 Exemplos:");
        System.out.println("  java Main servidor");
        System.out.println("  java Main cliente Cliente1");
    }
}