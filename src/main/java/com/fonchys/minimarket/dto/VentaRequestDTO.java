package com.fonchys.minimarket.dto;

import lombok.Data;

import java.util.List;

@Data
public class VentaRequestDTO {

    private List<ItemDTO> items;

    @Data
    public static class ItemDTO {
        private Long productoId;
        private Integer cantidad;
    }
}
