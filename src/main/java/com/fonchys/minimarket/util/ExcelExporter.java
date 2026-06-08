package com.fonchys.minimarket.util;

import com.fonchys.minimarket.model.Venta;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ExcelExporter() {}

    public static ByteArrayOutputStream exportarVentas(List<Venta> ventas) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Reporte de Ventas");

            // Estilo encabezado
            CellStyle headerStyle = wb.createCellStyle();
            Font fontHeader = wb.createFont();
            fontHeader.setBold(true);
            fontHeader.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(fontHeader);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Estilo fila total
            CellStyle totalStyle = wb.createCellStyle();
            Font fontTotal = wb.createFont();
            fontTotal.setBold(true);
            totalStyle.setFont(fontTotal);
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Título del reporte
            Row titulo = sheet.createRow(0);
            Cell celdaTitulo = titulo.createCell(0);
            celdaTitulo.setCellValue("Reporte de Ventas - Fonchys Minimarket");
            CellStyle tituloStyle = wb.createCellStyle();
            Font fontTitulo = wb.createFont();
            fontTitulo.setBold(true);
            fontTitulo.setFontHeightInPoints((short) 14);
            tituloStyle.setFont(fontTitulo);
            celdaTitulo.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Encabezados de columna
            Row header = sheet.createRow(2);
            String[] cols = {"ID", "Fecha", "Cajero", "Items", "Total (S/.)", "Estado"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 3;
            double totalGeneral = 0;
            for (Venta v : ventas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getFecha() != null ? v.getFecha().format(FORMATTER) : "");
                row.createCell(2).setCellValue(v.getUsuario() != null ? v.getUsuario().getNombre() : "N/A");
                row.createCell(3).setCellValue(v.getDetalles().size());
                row.createCell(4).setCellValue(v.getTotal().doubleValue());
                row.createCell(5).setCellValue(v.getEstado().name());
                if (v.getEstado() == Venta.Estado.COMPLETADA) {
                    totalGeneral += v.getTotal().doubleValue();
                }
            }

            // Fila de total
            Row rowTotal = sheet.createRow(rowNum + 1);
            Cell labelTotal = rowTotal.createCell(3);
            labelTotal.setCellValue("TOTAL GENERAL:");
            labelTotal.setCellStyle(totalStyle);
            Cell valorTotal = rowTotal.createCell(4);
            valorTotal.setCellValue(totalGeneral);
            valorTotal.setCellStyle(totalStyle);

            // Autoajustar columnas
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el archivo Excel: " + e.getMessage(), e);
        }
    }
}
