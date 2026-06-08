package com.fonchys.minimarket.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.List;

public final class ValidacionUtil {

    private ValidacionUtil() {}

    /**Limpia y normaliza espacios en un texto (Apache Commons)*/
    public static String limpiarTexto(String texto) {
        if (StringUtils.isBlank(texto)) return "";
        return StringUtils.normalizeSpace(texto.trim());
    }

    /**Verifica si un string representa un número válido (Apache Commons)*/
    public static boolean esNumeroValido(String valor) {
        return StringUtils.isNotBlank(valor) && NumberUtils.isCreatable(valor);
    }

    /** Convierte un string a BigDecimal de forma segura (Apache Commons) */
    public static BigDecimal parsearDecimal(String valor) {
        if (!esNumeroValido(valor)) return BigDecimal.ZERO;
        return new BigDecimal(NumberUtils.createNumber(valor).toString());
    }

    /** Valida que un objeto no sea nulo (Google Guava) */
    public static void validarNoNulo(Object valor, String mensaje) {
        Preconditions.checkNotNull(valor, mensaje);
    }

    /** Valida que un string no sea vacío (Guava + Commons) */
    public static void validarNoVacio(String valor, String mensaje) {
        Preconditions.checkArgument(StringUtils.isNotBlank(valor), mensaje);
    }

    /** Valida que una condición sea verdadera (Google Guava) */
    public static void validarCondicion(boolean condicion, String mensaje) {
        Preconditions.checkArgument(condicion, mensaje);
    }

    /** Retorna lista inmutable de roles disponibles (Google Guava) */
    public static List<String> getRolesDisponibles() {
        return ImmutableList.of("ADMIN", "CAJERO");
    }

    /** Capitaliza el nombre con formato correcto (Apache Commons) */
    public static String capitalizarNombre(String nombre) {
        if (StringUtils.isBlank(nombre)) return "";
        return StringUtils.capitalize(nombre.toLowerCase().trim());
    }

    /** Valida email básico con Apache Commons */
    public static boolean esEmailValido(String email) {
        if (StringUtils.isBlank(email)) return false;
        return email.contains("@") && email.contains(".");
    }
}
