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

    public static void main(String[] args) throws Exception {
        if (args.length >= 1) HOST = args[0];
        if (args.length >= 2) PORTA = Integer.parseInt(args[1]);
        if (args.length >= 3) nomeDoNo = args[2];

        System.out.println("[" + nomeDoNo + "] Iniciando cliente...");

        // Limpar checkpoint anterior para demonstração
        File checkpoint = new File("checkpoint_" + nomeDoNo + ".dat");
        if (checkpoint.exists()) checkpoint.delete();

        Random random = new Random();

        while (true) {
            try {
                int delay = 3000 + random.nextInt(2000);
                System.out.println("[" + nomeDoNo + "] Aguardando " + delay + "ms antes da próxima requisição");
                Thread.sleep(delay);

                requisitarAcesso();

            } catch (Exception e) {
                System.out.println("[" + nomeDoNo + "] Erro: " + e.getMessage());
                Thread.sleep(5000);
            }
        }
    }

    private static void requisitarAcesso() throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] ⏰ Solicitando acesso... Relógio=" + relogio);

        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            socket.setSoTimeout(30000); // Timeout de 30 segundos

            // Enviar requisição
            out.writeObject(new Mensagem("REQUISICAO", nomeDoNo + " quer acessar o recurso", relogio));

            // Aguardar resposta
            Mensagem resposta = (Mensagem) in.readObject();
            System.out.println("[" + nomeDoNo + "] 📨 Resposta: " + resposta.getConteudo());

            if (resposta.getTipo().equals("PERMISSAO")) {
                acessarRecursoCritico();
                liberarAcesso(out);
            } else if (resposta.getTipo().equals("AGUARDE")) {
                // Manter conexão aberta aguardando permissão
                System.out.println("[" + nomeDoNo + "] 🕒 " + resposta.getConteudo());

                // Aguardar pela permissão
                Mensagem permissao = (Mensagem) in.readObject();
                if (permissao.getTipo().equals("PERMISSAO")) {
                    acessarRecursoCritico();
                    liberarAcesso(out);
                }
            }
        }
    }

    private static void acessarRecursoCritico() throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] 🔐 === ACESSANDO RECURSO CRÍTICO ===");
        System.out.println("[" + nomeDoNo + "] ⏰ Relógio=" + relogio);

        recurso.incrementar();
        int valorAtual = recurso.getValor();
        System.out.println("[" + nomeDoNo + "] 📊 Valor atual do contador = " + valorAtual);

        // Simular trabalho no recurso (1-3 segundos)
        int tempoTrabalho = 1000 + new Random().nextInt(2000);
        System.out.println("[" + nomeDoNo + "] ⏳ Trabalhando no recurso por " + tempoTrabalho + "ms");
        Thread.sleep(tempoTrabalho);

        // Salvar checkpoint
        recurso.salvarCheckpoint("checkpoint_" + nomeDoNo + ".dat");

        System.out.println("[" + nomeDoNo + "] ✅ === RECURSO LIBERADO ===");
    }

    private static void liberarAcesso(ObjectOutputStream out) throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] 🚀 Liberando acesso... Relógio=" + relogio);
        out.writeObject(new Mensagem("LIBERACAO", nomeDoNo + " liberou o recurso", relogio));
    }
}