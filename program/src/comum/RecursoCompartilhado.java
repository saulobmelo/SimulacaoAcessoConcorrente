package comum;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RecursoCompartilhado implements Serializable {
    private AtomicInteger contador = new AtomicInteger(0);

    public synchronized void incrementar() {
        contador.incrementAndGet();
    }

    public synchronized int getValor() {
        return contador.get();
    }

    // Checkpoint
    public synchronized void salvarCheckpoint(String arquivo) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(arquivo))) {
            out.writeObject(contador.get());
        }
    }

    // Rollback
    public synchronized void restaurarCheckpoint(String arquivo) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(arquivo))) {
            int valor = (Integer) in.readObject();
            contador.set(valor);
        }
    }
}