package cliente;

import comum.Mensagem;
import comum.RecursoCompartilhado;
import java.io.*;
import java.net.*;
import java.util.Random;

public class NoCliente {
    private static String HOST = "localhost";
    private static int PORTA = 5000;
    private static String nomeDoNo = "Cliente";

    private static int relogio = 0;
    private static RecursoCompartilhado recurso = new RecursoCompartilhado();
    private static int tentativasConexao = 0;

    public static void main(String[] args) throws Exception {
        if (args.length >= 1) HOST = args[0];
        if (args.length >= 2) PORTA = Integer.parseInt(args[1]);
        if (args.length >= 3) nomeDoNo = args[2];

        System.out.println("[" + nomeDoNo + "] Iniciando cliente...");

        // Tentar restaurar checkpoint se existir
        try {
            File checkpoint = new File("checkpoint_" + nomeDoNo + ".dat");
            if (checkpoint.exists()) {
                recurso.restaurarCheckpoint("checkpoint_" + nomeDoNo + ".dat");
                System.out.println("[" + nomeDoNo + "] Checkpoint restaurado");
            }
        } catch (Exception e) {
            System.out.println("[" + nomeDoNo + "] Erro ao restaurar checkpoint: " + e.getMessage());
        }

        Random random = new Random();
        while (true) {
            try {
                int delay = 2000 + random.nextInt(3000);
                System.out.println("[" + nomeDoNo + "] Próxima requisição em " + delay + "ms");
                Thread.sleep(delay);

                requisitarAcesso();
                tentativasConexao = 0; // Resetar tentativas após sucesso

            } catch (Exception e) {
                System.out.println("[" + nomeDoNo + "] Erro: " + e.getMessage());
                tentativasConexao++;

                if (tentativasConexao > 3) {
                    System.out.println("[" + nomeDoNo + "] Muitas falhas. Verifique se o servidor está rodando.");
                }

                Thread.sleep(5000); // Espera antes de tentar novamente
            }
        }
    }

    private static void requisitarAcesso() throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] Solicitando acesso... Relógio=" + relogio);

        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Timeout para evitar bloqueio infinito
            socket.setSoTimeout(10000);

            out.writeObject(new Mensagem("REQUISICAO", nomeDoNo + " quer acessar o recurso", relogio));
            Mensagem resposta = (Mensagem) in.readObject();

            System.out.println("[" + nomeDoNo + "] Resposta do servidor: " + resposta.getTipo());

            if (resposta.getTipo().equals("PERMISSAO")) {
                acessarRecursoCritico();
                liberarAcesso();
            } else if (resposta.getTipo().equals("AGUARDE")) {
                System.out.println("[" + nomeDoNo + "] " + resposta.getConteudo());
            }
        }
    }

    private static void acessarRecursoCritico() throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] === ACESSANDO RECURSO CRÍTICO ===");
        System.out.println("[" + nomeDoNo + "] Relógio=" + relogio);

        recurso.incrementar();
        System.out.println("[" + nomeDoNo + "] Valor atual do contador = " + recurso.getValor());

        // Simular trabalho no recurso
        Thread.sleep(1000 + new Random().nextInt(2000));

        // Salvar checkpoint
        recurso.salvarCheckpoint("checkpoint_" + nomeDoNo + ".dat");

        System.out.println("[" + nomeDoNo + "] === RECURSO LIBERADO ===");
    }

    private static void liberarAcesso() throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] Liberando acesso... Relógio=" + relogio);

        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(new Mensagem("LIBERACAO", nomeDoNo + " liberou o recurso", relogio));
        }
    }

    private static void enviarHeartbeat() {
        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(new Mensagem("HEARTBEAT", nomeDoNo + " heartbeat", relogio));
        } catch (Exception e) {
            System.out.println("[" + nomeDoNo + "] Servidor não responde ao heartbeat");
        }
    }
}