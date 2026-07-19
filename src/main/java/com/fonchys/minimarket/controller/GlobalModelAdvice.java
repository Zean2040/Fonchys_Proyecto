package com.fonchys.minimarket.controller;

import com.fonchys.minimarket.service.IProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final IProductoService productoService;

    @ModelAttribute("stockBajoCount")
    public int stockBajoCount() {
        return productoService.listarStockBajoIndividual().size();
    }
}
