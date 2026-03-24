package br.com.desafio.cartoes.exception;

public class ClienteInvalidoException extends RuntimeException {
    
    private final String codigoErro;
    private final int statusCode;
    
    public ClienteInvalidoException(String mensagem, String codigoErro, int statusCode) {
        super(mensagem);
        this.codigoErro = codigoErro;
        this.statusCode = statusCode;
    }
    
    public String getCodigoErro() {
        return codigoErro;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
