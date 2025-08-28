package comum;

import java.io.Serializable;

public class Mensagem implements Serializable {
    private String tipo;
    private String conteudo;
    private int relogio;

    public Mensagem(String tipo, String conteudo, int relogio) {
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.relogio = relogio;
    }

    public String getTipo() {
        return tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public int getRelogio() {
        return relogio;
    }

    @Override
    public String toString() {
        return "[Tipo=" + tipo + ", Conteúdo=" + conteudo + ", Relógio=" + relogio + "]";
    }
}