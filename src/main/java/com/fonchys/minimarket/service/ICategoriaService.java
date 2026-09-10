package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface ICategoriaService {
    List<Categoria> listarTodas();
    Optional<Categoria> buscarPorId(Long id);
}
