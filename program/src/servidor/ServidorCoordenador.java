package servidor;

import comum.Mensagem;
import comum.RecursoCompartilhado;
import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorCoordenador {
    private static final int PORTA = 5000;
    private static Queue<String> filaRequisicoes = new LinkedList<>();
    private static Map<String, Socket> clientesConectados = new HashMap<>();
    private static RecursoCompartilhado recurso = new RecursoCompartilhado();
    private static boolean executando = true;

    public static void main(String[] args) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executando = false;
            System.out.println("[Servidor] Encerrando servidor...");
            salvarEstadoServidor();
        }));

        // Tentar restaurar estado anterior
        try {
            restaurarEstadoServidor();
            System.out.println("[Servidor] Estado anterior restaurado. Fila: " + filaRequisicoes.size() + " clientes");
        } catch (Exception e) {
            System.out.println("[Servidor] Iniciando com estado novo");
        }

        ServerSocket servidor = new ServerSocket(PORTA);
        System.out.println("[Servidor] Coordenador iniciado na porta " + PORTA);

        while (executando) {
            try {
                Socket cliente = servidor.accept();
                new Thread(() -> tratarCliente(cliente)).start();
            } catch (SocketException e) {
                if (executando) {
                    System.out.println("[Servidor] Socket fechado: " + e.getMessage());
                }
            }
        }
        servidor.close();
    }

    private static void tratarCliente(Socket cliente) {
        String clienteId = cliente.getInetAddress().toString() + ":" + cliente.getPort();

        try (ObjectInputStream in = new ObjectInputStream(cliente.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(cliente.getOutputStream())) {

            Mensagem msg = (Mensagem) in.readObject();
            System.out.println("[Servidor] Recebido de " + clienteId + ": " + msg);

            switch (msg.getTipo()) {
                case "REQUISICAO":
                    filaRequisicoes.add(clienteId);
                    clientesConectados.put(clienteId, cliente);

                    if (filaRequisicoes.peek().equals(clienteId)) {
                        out.writeObject(new Mensagem("PERMISSAO", "Acesso concedido", msg.getRelogio()));
                        System.out.println("[Servidor] Permissão concedida para: " + clienteId);
                    } else {
                        out.writeObject(new Mensagem("AGUARDE", "Você está na posição " + filaRequisicoes.size(), msg.getRelogio()));
                    }
                    break;

                case "LIBERACAO":
                    filaRequisicoes.poll();
                    System.out.println("[Servidor] Cliente liberou recurso: " + clienteId);

                    if (!filaRequisicoes.isEmpty()) {
                        String proximoId = filaRequisicoes.peek();
                        Socket proximo = clientesConectados.get(proximoId);
                        if (proximo != null && !proximo.isClosed()) {
                            ObjectOutputStream outProx = new ObjectOutputStream(proximo.getOutputStream());
                            outProx.writeObject(new Mensagem("PERMISSAO", "Acesso concedido", msg.getRelogio()));
                            System.out.println("[Servidor] Permissão concedida para próximo: " + proximoId);
                        }
                    }
                    break;

                case "HEARTBEAT":
                    out.writeObject(new Mensagem("HEARTBEAT_ACK", "Recebido", msg.getRelogio()));
                    break;
            }

            // Salvar estado periodicamente
            salvarEstadoServidor();

        } catch (Exception e) {
            System.out.println("[Servidor] Erro com cliente " + clienteId + ": " + e.getMessage());
            filaRequisicoes.remove(clienteId);
        } finally {
            try {
                if (!cliente.isClosed()) {
                    cliente.close();
                }
            } catch (IOException e) {
                System.out.println("[Servidor] Erro ao fechar socket: " + e.getMessage());
            }
            clientesConectados.remove(clienteId);
        }
    }

    private static void salvarEstadoServidor() {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream("servidor_state.dat"))) {
            out.writeObject(new ArrayList<>(filaRequisicoes));
            System.out.println("[Servidor] Estado salvo. Fila: " + filaRequisicoes.size() + " clientes");
        } catch (IOException e) {
            System.out.println("[Servidor] Erro ao salvar estado: " + e.getMessage());
        }
    }

    private static void restaurarEstadoServidor() {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream("servidor_state.dat"))) {
            List<String> filaSalva = (List<String>) in.readObject();
            filaRequisicoes = new LinkedList<>(filaSalva);
            System.out.println("[Servidor] Estado restaurado com sucesso");
        } catch (FileNotFoundException e) {
            System.out.println("[Servidor] Nenhum estado anterior encontrado");
        } catch (Exception e) {
            System.out.println("[Servidor] Erro ao restaurar estado: " + e.getMessage());
        }
    }
}