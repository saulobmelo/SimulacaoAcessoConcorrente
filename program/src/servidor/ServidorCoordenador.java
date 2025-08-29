package servidor;

import comum.Mensagem;
import comum.RecursoCompartilhado;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorCoordenador {
    private static final int PORTA = 5000;
    private static Queue<ClientHandler> filaRequisicoes = new LinkedList<>();
    private static RecursoCompartilhado recurso = new RecursoCompartilhado();
    private static boolean executando = true;

    public static void main(String[] args) throws IOException {
        limparEstadoAnterior();

        ServerSocket servidor = new ServerSocket(PORTA);
        System.out.println("[Servidor] Coordenador iniciado na porta " + PORTA);
        System.out.println("[Servidor] Estado inicial: fila vazia");

        while (executando) {
            try {
                Socket cliente = servidor.accept();
                new Thread(new ClientHandler(cliente)).start();
            } catch (SocketException e) {
                if (executando) {
                    System.out.println("[Servidor] Socket fechado: " + e.getMessage());
                }
            }
        }
        servidor.close();
    }

    private static void limparEstadoAnterior() {
        File stateFile = new File("servidor_state.dat");
        if (stateFile.exists()) {
            stateFile.delete();
            System.out.println("[Servidor] Estado anterior removido");
        }
    }

    // Classe interna para lidar com cada cliente
    static class ClientHandler implements Runnable {
        private Socket socket;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = socket.getInetAddress() + ":" + socket.getPort();
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                while (!socket.isClosed()) {
                    Mensagem msg = (Mensagem) in.readObject();
                    System.out.println("[Servidor] Recebido de " + clientId + ": " + msg);

                    switch (msg.getTipo()) {
                        case "REQUISICAO":
                            synchronized (filaRequisicoes) {
                                filaRequisicoes.add(this);
                                System.out.println("[Servidor] Fila atual: " + filaRequisicoes.size() + " clientes");

                                if (filaRequisicoes.peek() == this) {
                                    out.writeObject(new Mensagem("PERMISSAO", "Acesso concedido", msg.getRelogio()));
                                    System.out.println("[Servidor] Permissão concedida para: " + clientId);
                                } else {
                                    out.writeObject(new Mensagem("AGUARDE", "Você está na posição " + filaRequisicoes.size(), msg.getRelogio()));
                                }
                            }
                            break;

                        case "LIBERACAO":
                            synchronized (filaRequisicoes) {
                                ClientHandler liberado = filaRequisicoes.poll();
                                System.out.println("[Servidor] Cliente liberou recurso: " + clientId);

                                if (!filaRequisicoes.isEmpty()) {
                                    ClientHandler proximo = filaRequisicoes.peek();
                                    proximo.out.writeObject(new Mensagem("PERMISSAO", "Acesso concedido", msg.getRelogio()));
                                    System.out.println("[Servidor] Permissão concedida para próximo: " + proximo.clientId);
                                }
                            }
                            break;

                        case "HEARTBEAT":
                            out.writeObject(new Mensagem("HEARTBEAT_ACK", "Recebido", msg.getRelogio()));
                            break;
                    }
                }
            } catch (EOFException e) {
                System.out.println("[Servidor] Cliente desconectado: " + clientId);
            } catch (Exception e) {
                System.out.println("[Servidor] Erro com cliente " + clientId + ": " + e.getMessage());
            } finally {
                removerDaFila();
                fecharConexao();
            }
        }

        private void removerDaFila() {
            synchronized (filaRequisicoes) {
                filaRequisicoes.remove(this);
            }
        }

        private void fecharConexao() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException e) {
                System.out.println("[Servidor] Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}