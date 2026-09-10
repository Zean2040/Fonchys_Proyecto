package com.fonchys.minimarket.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VentaRequestDTO {

    @NotEmpty(message = "La venta debe tener al menos un producto")
    @Valid
    private List<ItemDTO> items;

    private String clienteNombre;
    private String clienteDni;

    @Data
    public static class ItemDTO {

        @NotNull(message = "El producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser al menos 1")
        private Integer cantidad;
    }
}
