package com.fonchys.minimarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    @Column(nullable = false)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @DecimalMin(value = "0.01", message = "El precio de compra debe ser mayor a 0")
    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock;

    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    @Column(name = "stock_minimo")
    private Integer stockMinimo = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", length = 20)
    private UnidadMedida unidadMedida = UnidadMedida.UNIDAD;

    @DecimalMin(value = "0.01", message = "El contenido neto debe ser mayor a 0")
    @Column(name = "contenido_neto", precision = 10, scale = 2)
    private BigDecimal contenidoNeto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "codigo_barras", unique = true)
    private String codigoBarras;

    /** Retorna la ganancia por unidad vendida. Null si no hay precio de compra. */
    @Transient
    public BigDecimal getGanancia() {
        if (precioCompra == null || precio == null) return null;
        return precio.subtract(precioCompra).setScale(2, RoundingMode.HALF_UP);
    }

    /** Retorna el margen de ganancia en porcentaje. Null si no hay precio de compra. */
    @Transient
    public BigDecimal getMargenPorcentaje() {
        if (precioCompra == null || precio == null || precioCompra.compareTo(BigDecimal.ZERO) == 0) return null;
        return precio.subtract(precioCompra)
                .divide(precioCompra, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(1, RoundingMode.HALF_UP);
    }

    /** Descripción de presentación: "500 ml", "1 kg", "Unid." */
    @Transient
    public String getPresentacion() {
        if (contenidoNeto == null || unidadMedida == null || unidadMedida == UnidadMedida.UNIDAD) {
            return unidadMedida != null ? unidadMedida.getSimbolo() : "Unid.";
        }
        String neto = contenidoNeto.stripTrailingZeros().toPlainString();
        return neto + " " + unidadMedida.getSimbolo();
    }
}
