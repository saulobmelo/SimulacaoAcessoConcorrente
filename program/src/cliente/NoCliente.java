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
        // args[0] = host, args[1] = porta, args[2] = nomeDoNo
        if (args.length >= 1) HOST = args[0];
        if (args.length >= 2) PORTA = Integer.parseInt(args[1]);
        if (args.length >= 3) nomeDoNo = args[2];

        Random random = new Random();
        while (true) {
            Thread.sleep(2000 + random.nextInt(3000));
            requisitarAcesso();
        }
    }

    private static void requisitarAcesso() throws Exception {
        relogio++;
        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new Mensagem("REQUISICAO", nomeDoNo + " quer acessar o recurso", relogio));
            Mensagem resposta = (Mensagem) in.readObject();
            if (resposta.getTipo().equals("PERMISSAO")) {
                acessarRecursoCritico();
                liberarAcesso();
            }
        }
    }

    private static void acessarRecursoCritico() throws Exception {
        relogio++;
        System.out.println("[" + nomeDoNo + "] Acessando recurso crítico... Relógio=" + relogio);
        recurso.incrementar();
        System.out.println("[" + nomeDoNo + "] Valor atual do contador = " + recurso.getValor());
        recurso.salvarCheckpoint("checkpoint_" + nomeDoNo + ".dat");
    }

    private static void liberarAcesso() throws Exception {
        relogio++;
        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(new Mensagem("LIBERACAO", nomeDoNo + " liberou o recurso", relogio));
        }
    }
}