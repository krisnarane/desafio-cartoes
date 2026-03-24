package br.com.desafio.cartoes.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClienteValidator {
    
    /**
     * Valida formato do CPF (básico - apenas formato)
     */
    public boolean isValidCPFFormat(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            return false;
        }
        // Remove pontuação
        String cpfNumerico = cpf.replaceAll("\\D", "");
        return cpfNumerico.length() == 11;
    }
    
    /**
     * Valida se é uma UF válida
     */
    public boolean isValidUF(String uf) {
        if (uf == null || uf.isEmpty()) {
            return false;
        }
        String[] ufsValidas = {
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA",
            "MT", "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN",
            "RS", "RO", "RR", "SC", "SP", "SE", "TO"
        };
        for (String ufValida : ufsValidas) {
            if (ufValida.equals(uf)) {
                return true;
            }
        }
        return false;
    }
}
