package find_the_number.dominio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Random;

public record CodigoSecreto(String valor) {
    public CodigoSecreto {
        if (valor == null || !valor.matches("[0-9]{1,10}")
                || valor.chars().distinct().count() != valor.length()) {
            throw new IllegalArgumentException("Ingrese digitos distintos, sin signos ni espacios");
        }
    }

    public static CodigoSecreto generar(int longitud, Random aleatorio) {
        if (longitud < 1 || longitud > 10) {
            throw new IllegalArgumentException("La longitud debe estar entre 1 y 10");
        }
        Objects.requireNonNull(aleatorio);
        var digitos = new ArrayList<Integer>();
        for (int digito = 0; digito < 10; digito++) {
            digitos.add(digito);
        }
        Collections.shuffle(digitos, aleatorio);
        StringBuilder valor = new StringBuilder(longitud);
        for (int posicion = 0; posicion < longitud; posicion++) {
            valor.append(digitos.get(posicion));
        }
        return new CodigoSecreto(valor.toString());
    }

    public ResultadoIntento comparar(String entrada) {
        CodigoSecreto intento = new CodigoSecreto(entrada);
        if (intento.valor.length() != valor.length()) {
            throw new IllegalArgumentException("Ingrese exactamente " + valor.length() + " digitos");
        }
        int famas = 0;
        int toques = 0;
        for (int posicion = 0; posicion < valor.length(); posicion++) {
            char digito = entrada.charAt(posicion);
            if (digito == valor.charAt(posicion)) {
                famas++;
            } else if (valor.indexOf(digito) >= 0) {
                toques++;
            }
        }
        return new ResultadoIntento(entrada, toques, famas);
    }
}