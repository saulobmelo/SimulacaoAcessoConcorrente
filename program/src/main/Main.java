package main;

import servidor.ServidorCoordenador;
import cliente.NoCliente;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final AtomicBoolean executando = new AtomicBoolean(true);
    private static final AtomicInteger clienteAtivo = new AtomicInteger(0);
    private static final int TEMPO_ATIVO_POR_CLIENTE = 15000; // 15 segundos por cliente
    private static final int TOTAL_CLIENTES = 4;

    public static void main(String[] args) {
        System.out.println("=== SISTEMA DISTRIBUÍDO - ACESSO CONCORRENTE ===");
        System.out.println("Disciplina: Sistemas Distribuídos");
        System.out.println("Objetivo: Simular acesso concorrente a recurso crítico");
        System.out.println("Modo: Rotação automática de clientes a cada " + (TEMPO_ATIVO_POR_CLIENTE / 1000) + " segundos\n");

        if (args.length == 0) {
            executarSistemaComRotacao();
        } else if (args[0].equals("servidor")) {
            executarApenasServidor();
        } else if (args[0].equals("cliente") && args.length >= 2) {
            executarApenasCliente(args[1]);
        }
    }

    private static void executarSistemaComRotacao() {
        System.out.println("🚀 Iniciando sistema com rotação de clientes...");

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
            System.out.println("[MAIN] Servidor iniciado. Iniciando clientes com rotação...");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Array para controlar as threads dos clientes
        Thread[] clientThreads = new Thread[TOTAL_CLIENTES];
        AtomicBoolean[] clientesAtivos = new AtomicBoolean[TOTAL_CLIENTES];
        for (int i = 0; i < TOTAL_CLIENTES; i++) {
            clientesAtivos[i] = new AtomicBoolean(false);
        }

        // Iniciar todos os clientes
        String[] nomesClientes = {"Cliente1", "Cliente2", "Cliente3", "Cliente4"};

        for (int i = 0; i < TOTAL_CLIENTES; i++) {
            final int clientIndex = i;
            final String nomeCliente = nomesClientes[i];

            clientThreads[i] = new Thread(() -> {
                try {
                    System.out.println("[MAIN] Iniciando " + nomeCliente + " (modo controlado)");
                    clientesAtivos[clientIndex].set(true);

                    // Versão do cliente que verifica se está ativo
                    executarClienteControlado(nomeCliente, clientesAtivos[clientIndex]);
                } catch (Exception e) {
                    System.err.println("[MAIN] Erro no " + nomeCliente + ": " + e.getMessage());
                }
            });
            clientThreads[i].setDaemon(true);
            clientThreads[i].start();

            try {
                Thread.sleep(1000); // Delay entre inícios
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Thread para controlar a rotação de clientes
        Thread controladorThread = new Thread(() -> {
            int clienteAtual = 0;

            while (executando.get()) {
                try {
                    System.out.println("\n🔄 [CONTROLE] Ativando " + nomesClientes[clienteAtual] +
                            " por " + (TEMPO_ATIVO_POR_CLIENTE / 1000) + " segundos");

                    // Ativar o cliente atual
                    clientesAtivos[clienteAtual].set(true);

                    // Aguardar o tempo determinado
                    Thread.sleep(TEMPO_ATIVO_POR_CLIENTE);

                    // Desativar o cliente atual
                    System.out.println("⏸️  [CONTROLE] Desativando " + nomesClientes[clienteAtual]);
                    clientesAtivos[clienteAtual].set(false);

                    // Aguardar um pouco para garantir que o cliente pare
                    Thread.sleep(2000);

                    // Próximo cliente
                    clienteAtual = (clienteAtual + 1) % TOTAL_CLIENTES;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        controladorThread.setDaemon(true);
        controladorThread.start();

        // Manter o sistema rodando
        try {
            System.out.println("\n[MAIN] Sistema em execução com rotação. Pressione Ctrl+C para finalizar.");
            System.out.println("📋 Cronograma de rotação:");
            System.out.println("   - Cada cliente ativo por " + (TEMPO_ATIVO_POR_CLIENTE / 1000) + " segundos");
            System.out.println("   - Rotaão cíclica: Cliente1 → Cliente2 → Cliente3 → Cliente4 → Cliente1...");

            // Manter a thread principal ativa
            while (executando.get()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executando.set(false);
            System.out.println("[MAIN] Sistema finalizado.");
        }
    }

    private static void executarClienteControlado(String nomeCliente, AtomicBoolean ativo) {
        String HOST = "localhost";
        int PORTA = 5000;
        int relogio = 0;

        try {
            while (executando.get()) {
                if (!ativo.get()) {
                    // Cliente pausado - aguardar
                    Thread.sleep(1000);
                    continue;
                }

                // Cliente ativo - executar ciclo normal
                relogio++;
                System.out.println("[" + nomeCliente + "] ⏰ Solicitando acesso... Relógio=" + relogio);

                try (java.net.Socket socket = new java.net.Socket(HOST, PORTA);
                     java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(socket.getOutputStream());
                     java.io.ObjectInputStream in = new java.io.ObjectInputStream(socket.getInputStream())) {

                    socket.setSoTimeout(10000);
                    out.writeObject(new comum.Mensagem("REQUISICAO", nomeCliente + " quer acessar o recurso", relogio));

                    comum.Mensagem resposta = (comum.Mensagem) in.readObject();
                    System.out.println("[" + nomeCliente + "] 📨 Resposta: " + resposta.getConteudo());

                    if (resposta.getTipo().equals("PERMISSAO")) {
                        acessarRecursoCritico(nomeCliente, relogio);
                        liberarAcesso(out, nomeCliente, relogio);
                    }
                }

                // Pequena pausa entre requisições
                Thread.sleep(2000 + (int) (Math.random() * 2000));
            }
        } catch (Exception e) {
            System.out.println("[" + nomeCliente + "] Erro: " + e.getMessage());
        }
    }

    private static void acessarRecursoCritico(String nomeCliente, int relogio) throws Exception {
        relogio++;
        System.out.println("[" + nomeCliente + "] 🔐 === ACESSANDO RECURSO CRÍTICO ===");
        System.out.println("[" + nomeCliente + "] ⏰ Relógio=" + relogio);

        // Simular trabalho no recurso (mais curto para demonstração)
        int tempoTrabalho = 1000 + (int) (Math.random() * 1000);
        System.out.println("[" + nomeCliente + "] ⏳ Trabalhando no recurso por " + tempoTrabalho + "ms");
        Thread.sleep(tempoTrabalho);

        System.out.println("[" + nomeCliente + "] ✅ === RECURSO LIBERADO ===");
    }

    private static void liberarAcesso(java.io.ObjectOutputStream out, String nomeCliente, int relogio) throws Exception {
        relogio++;
        System.out.println("[" + nomeCliente + "] 🚀 Liberando acesso... Relógio=" + relogio);
        out.writeObject(new comum.Mensagem("LIBERACAO", nomeCliente + " liberou o recurso", relogio));
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
            // Usar a versão original sem controle
            NoCliente.main(new String[]{"localhost", "5000", nomeCliente});
        } catch (Exception e) {
            System.err.println("Erro ao executar cliente: " + e.getMessage());
        }
    }
}