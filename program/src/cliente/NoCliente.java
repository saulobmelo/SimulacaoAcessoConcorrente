package cliente;

import comum.Mensagem;
import comum.RecursoCompartilhado;
import java.io.*;
import java.net.*;
import java.util.Random;

public class NoCliente {
    private static final String HOST = "localhost";
    private static final int PORTA = 5000;
    private static int relogio = 0;
    private static RecursoCompartilhado recurso = new RecursoCompartilhado();

    public static void main(String[] args) throws Exception {
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

            out.writeObject(new Mensagem("REQUISICAO", "Quero acessar o recurso", relogio));
            Mensagem resposta = (Mensagem) in.readObject();
            if (resposta.getTipo().equals("PERMISSAO")) {
                acessarRecursoCritico();
                liberarAcesso();
            }
        }
    }

    private static void acessarRecursoCritico() throws Exception {
        relogio++;
        System.out.println("[Cliente] Acessando recurso crítico... Relógio=" + relogio);
        recurso.incrementar();
        System.out.println("[Cliente] Valor atual do contador = " + recurso.getValor());
        recurso.salvarCheckpoint("checkpoint_cliente.dat");
    }

    private static void liberarAcesso() throws Exception {
        relogio++;
        try (Socket socket = new Socket(HOST, PORTA);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(new Mensagem("LIBERACAO", "Liberei o recurso", relogio));
        }
    }
}