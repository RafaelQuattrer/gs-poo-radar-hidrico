package br.com.fiap.radar;

public class FonteIndisponivelException extends RuntimeException {
    public FonteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public FonteIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
