package servidor;

import comum.Mensagem;
import comum.RecursoCompartilhado;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorCoordenador {
    private static final int PORTA = 5000;
    private static Queue<Socket> filaRequisicoes = new LinkedList<>();
    private static RecursoCompartilhado recurso = new RecursoCompartilhado();

    public static void main(String[] args) throws IOException {
        ServerSocket servidor = new ServerSocket(PORTA);
        System.out.println("[Servidor] Coordenador iniciado na porta " + PORTA);

        while (true) {
            Socket cliente = servidor.accept();
            new Thread(() -> tratarCliente(cliente)).start();
        }
    }

    private static void tratarCliente(Socket cliente) {
        try (ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream())) {

            Mensagem msg = (Mensagem) in.readObject();
            System.out.println("[Servidor] Recebido: " + msg);

            if (msg.getTipo().equals("REQUISICAO")) {
                filaRequisicoes.add(cliente);
                if (filaRequisicoes.peek() == cliente) {
                    out.writeObject(new Mensagem("PERMISSAO", "Acesso concedido", msg.getRelogio()));
                }
            } else if (msg.getTipo().equals("LIBERACAO")) {
                filaRequisicoes.poll();
                if (!filaRequisicoes.isEmpty()) {
                    Socket proximo = filaRequisicoes.peek();
                    ObjectOutputStream outProx = new ObjectOutputStream(proximo.getOutputStream());
                    outProx.writeObject(new Mensagem("PERMISSAO", "Acesso concedido", msg.getRelogio()));
                }
            }
        } catch (Exception e) {
            System.out.println("[Servidor] Erro: " + e.getMessage());
        }
    }
}