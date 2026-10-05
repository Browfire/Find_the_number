package find_the_number.dominio;

public record ResultadoIntento(String numero, int toques, int famas) {
    public ResultadoIntento {
        new CodigoSecreto(numero);
        if (toques < 0 || famas < 0 || toques + famas > numero.length()) {
            throw new IllegalArgumentException("Pistas fuera de rango");
        }
    }

    public boolean acierto() {
        return famas == numero.length();
    }
}