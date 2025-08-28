import servidor.ServidorCoordenador;
import cliente.NoCliente;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            exibirAjuda();
            return;
        }

        String papel = args[0].toLowerCase(); // "servidor" ou "cliente"

        switch (papel) {
            case "servidor":
                iniciarServidor();
                break;

            case "cliente":
                // cliente [host] [porta]
                String host = args.length >= 2 ? args[1] : "localhost";
                String portaStr = args.length >= 3 ? args[2] : "5000";

                int porta;
                try {
                    porta = Integer.parseInt(portaStr);
                } catch (NumberFormatException e) {
                    System.out.println("[Main] Porta inválida: " + portaStr);
                    exibirAjuda();
                    return;
                }

                iniciarCliente(host, porta);
                break;

            default:
                System.out.println("[Main] Papel não reconhecido: " + papel);
                exibirAjuda();
        }
    }

    private static void exibirAjuda() {
        System.out.println("=== Sistema Distribuído de Controle Colaborativo ===");
        System.out.println("Uso:");
        System.out.println("  java Main servidor");
        System.out.println("  java Main cliente [host] [porta]");
        System.out.println();
        System.out.println("Exemplos:");
        System.out.println("  java Main servidor");
        System.out.println("  java Main cliente localhost 5000");
    }

    private static void iniciarServidor() throws Exception {
        System.out.println("[Main] Iniciando Servidor Coordenador...");
        // chama o main do servidor
        ServidorCoordenador.main(new String[]{});
    }

    private static void iniciarCliente(String host, int porta) throws Exception {
        System.out.println("[Main] Iniciando Nó Cliente -> host=" + host + ", porta=" + porta);

        String nomeDoNo = "No" + System.currentTimeMillis();

        NoCliente.main(new String[]{host, String.valueOf(porta), nomeDoNo});
    }
}