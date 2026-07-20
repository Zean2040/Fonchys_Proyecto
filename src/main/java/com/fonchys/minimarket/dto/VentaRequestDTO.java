package com.fonchys.minimarket.dto;

import lombok.Data;

import java.util.List;

@Data
public class VentaRequestDTO {

    private List<ItemDTO> items;
    private String clienteNombre;
    private String clienteDni;

    @Data
    public static class ItemDTO {
        private Long productoId;
        private Integer cantidad;
    }
}
