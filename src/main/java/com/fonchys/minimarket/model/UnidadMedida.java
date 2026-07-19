package com.fonchys.minimarket.model;

public enum UnidadMedida {

    UNIDAD("Unid."),
    KILOGRAMO("kg"),
    GRAMO("g"),
    LITRO("L"),
    MILILITRO("ml"),
    PAQUETE("Paq."),
    CAJA("Caja"),
    DOCENA("Doc.");

    private final String simbolo;

    UnidadMedida(String simbolo) {
        this.simbolo = simbolo;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
