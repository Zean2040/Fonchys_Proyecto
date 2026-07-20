package com.fonchys.minimarket.service;

import com.fonchys.minimarket.model.Producto;
import com.fonchys.minimarket.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tareas de mantenimiento automatizadas que se ejecutan en segundo plano.
 * Responsabilidades: alertas de stock, limpieza de datos, verificacion de salud.
 */
@Service
@RequiredArgsConstructor
public class MantenimientoService {

    private static final Logger logger = LoggerFactory.getLogger(MantenimientoService.class);

    private final ProductoRepository productoRepository;

    /**
     * Verifica productos con stock bajo cada dia a las 8:00 AM.
     * cron: segundo minuto hora dia-mes mes dia-semana
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void alertaStockBajo() {
        logger.info("[MANTENIMIENTO] Verificando stock bajo - {}", LocalDateTime.now());
        List<Producto> productosConStockBajo = productoRepository.findProductosStockBajoUmbralPropio();
        if (productosConStockBajo.isEmpty()) {
            logger.info("[MANTENIMIENTO] Todos los productos tienen stock suficiente.");
        } else {
            logger.warn("[MANTENIMIENTO] ALERTA: {} producto(s) con stock bajo:", productosConStockBajo.size());
            productosConStockBajo.forEach(p ->
                logger.warn("[MANTENIMIENTO]   - {} (id={}) stock={} / minimo={}",
                    p.getNombre(), p.getId(), p.getStock(), p.getStockMinimo())
            );
        }
    }

    /**
     * Registra un heartbeat de salud del sistema cada hora.
     */
    @Scheduled(cron = "0 */2 * * * *")
    @Transactional(readOnly = true)
    public void heartbeatSistema() {
        long totalProductos = productoRepository.count();
        logger.info("[HEALTH] Sistema operativo - productos en BD: {} - {}", totalProductos, LocalDateTime.now());
    }

    /**
     * Tarea de limpieza diaria a las 2:00 AM.
     * En produccion: aqui se lanzaria el script de backup externo.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void limpiezaDiaria() {
        logger.info("[MANTENIMIENTO] Inicio de ventana de mantenimiento nocturno - {}", LocalDateTime.now());
        // La base de datos es respaldada por el script backup_db.bat / backup_db.sh
        // que debe configurarse como tarea del sistema operativo (Task Scheduler / cron)
        logger.info("[MANTENIMIENTO] Ventana de mantenimiento completada.");
    }
}
