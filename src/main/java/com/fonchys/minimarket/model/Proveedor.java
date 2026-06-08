package com.fonchys.minimarket.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "Máximo 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @Size(max = 20, message = "Máximo 20 caracteres")
    @Column(length = 20, unique = true)
    private String ruc;

    @Size(max = 20, message = "Máximo 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Email(message = "Email no válido")
    @Size(max = 100)
    @Column(length = 100)
    private String email;

    @Size(max = 255)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;
}
