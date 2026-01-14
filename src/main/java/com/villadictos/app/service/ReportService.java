package com.villadictos.app.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.villadictos.app.model.Pago;
import com.villadictos.app.model.Reserva;
import com.villadictos.app.repository.PagoRepository;
import com.villadictos.app.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReservaRepository reservaRepository;
    private final PagoRepository pagoRepository;

    public ReportService(ReservaRepository reservaRepository, PagoRepository pagoRepository) {
        this.reservaRepository = reservaRepository;
        this.pagoRepository = pagoRepository;
    }

    public Map<String, Object> getMonthlyStats(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaInicio() != null && r.getFechaFin() != null)
                .filter(r -> !r.getFechaInicio().isAfter(end) && !r.getFechaFin().isBefore(start))
                .collect(Collectors.toList());

        BigDecimal totalIngresos = reservas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada
                        || r.getEstado() == Reserva.EstadoReserva.completada)
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalReservas = reservas.size();

        BigDecimal ingresosHabitaciones = BigDecimal.ZERO;
        BigDecimal ingresosSalas = BigDecimal.ZERO;
        BigDecimal ingresosServicios = BigDecimal.ZERO;

        for (Reserva r : reservas) {
            if (r.getHabitacion() != null) {
                ingresosHabitaciones = ingresosHabitaciones.add(r.getPrecioTotal());
            } else if (r.getSala() != null) {
                ingresosSalas = ingresosSalas.add(r.getPrecioTotal());
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIngresos", totalIngresos);
        stats.put("totalReservas", totalReservas);
        stats.put("reservas", reservas);
        stats.put("month", Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
        stats.put("year", year);
        stats.put("ingresosHabitaciones", ingresosHabitaciones);
        stats.put("ingresosSalas", ingresosSalas);
        stats.put("ingresosServicios", ingresosServicios);

        return stats;
    }

    @Transactional(readOnly = true)
    public byte[] generateMonthlyReport(int month, int year) throws IOException, DocumentException {
        String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        
        // Obtener datos reales del mes
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaInicio() != null && r.getFechaFin() != null)
                .filter(r -> !r.getFechaInicio().isAfter(end) && !r.getFechaFin().isBefore(start))
                .collect(Collectors.toList());

        BigDecimal totalIngresos = reservas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada
                        || r.getEstado() == Reserva.EstadoReserva.completada)
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ingresosHabitaciones = BigDecimal.ZERO;
        BigDecimal ingresosSalas = BigDecimal.ZERO;

        for (Reserva r : reservas) {
            if ((r.getEstado() == Reserva.EstadoReserva.confirmada || r.getEstado() == Reserva.EstadoReserva.completada)) {
                if (r.getHabitacion() != null) {
                    ingresosHabitaciones = ingresosHabitaciones.add(r.getPrecioTotal());
                } else if (r.getSala() != null) {
                    ingresosSalas = ingresosSalas.add(r.getPrecioTotal());
                }
            }
        }

        // Generar PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(36, 36, 36, 36);
        
        PdfWriter.getInstance(document, baos);
        document.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Paragraph title = new Paragraph("Informe Mensual - Villadictos Hotel", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, new Color(100, 100, 100));
        Paragraph subtitle = new Paragraph("Periodo: " + monthName + " " + year, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Resumen
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(63, 81, 181));
        Paragraph resumenTitle = new Paragraph("Resumen General", sectionFont);
        resumenTitle.setSpacingBefore(10);
        resumenTitle.setSpacingAfter(10);
        document.add(resumenTitle);

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        document.add(new Paragraph("Total Ingresos Confirmados: €" + String.format("%.2f", totalIngresos), normalFont));
        document.add(new Paragraph("Total Reservas: " + reservas.size(), normalFont));
        document.add(new Paragraph("Ingresos por Habitaciones: €" + String.format("%.2f", ingresosHabitaciones), normalFont));
        document.add(new Paragraph("Ingresos por Salas: €" + String.format("%.2f", ingresosSalas), normalFont));
        document.add(new Paragraph(" "));

        // Tabla de detalles
        Paragraph detalleTitle = new Paragraph("Detalle de Reservas", sectionFont);
        detalleTitle.setSpacingBefore(10);
        detalleTitle.setSpacingAfter(10);
        document.add(detalleTitle);
        
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1, 3, 3, 2, 2, 2, 1.5f });

        addTableHeader(table, "ID", "Cliente", "Recurso", "Entrada", "Salida", "Total", "Estado");

        // Datos reales de reservas
        for (Reserva reserva : reservas) {
            String cliente = reserva.getUsuario() != null ? reserva.getUsuario().getEmail() : "N/A";
            String recurso = "";
            if (reserva.getHabitacion() != null) {
                recurso = "Hab. " + reserva.getHabitacion().getNumeroHabitacion();
            } else if (reserva.getSala() != null) {
                recurso = "Sala " + reserva.getSala().getNombre();
            }
            String fechaInicio = reserva.getFechaInicio() != null ? reserva.getFechaInicio().toString() : "N/A";
            String fechaFin = reserva.getFechaFin() != null ? reserva.getFechaFin().toString() : "N/A";
            String total = "€" + String.format("%.2f", reserva.getPrecioTotal());
            String estado = reserva.getEstado() != null ? reserva.getEstado().toString() : "N/A";
            
            addTableRow(table, 
                reserva.getId().toString(), 
                cliente, 
                recurso, 
                fechaInicio, 
                fechaFin, 
                total,
                estado);
        }

        document.add(table);
        
        // Pie de página
        document.add(new Paragraph(" "));
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(150, 150, 150));
        Paragraph footer = new Paragraph("Generado el " + LocalDate.now() + " - Villadictos Hotel Management System", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        document.close();
        
        return baos.toByteArray();
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getMonthlyStatsForPdf(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Reserva> reservas = reservaRepository.findAll().stream()
                .filter(r -> r.getFechaInicio() != null && r.getFechaFin() != null)
                .filter(r -> !r.getFechaInicio().isAfter(end) && !r.getFechaFin().isBefore(start))
                .collect(Collectors.toList());

        BigDecimal totalIngresos = reservas.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada
                        || r.getEstado() == Reserva.EstadoReserva.completada)
                .map(Reserva::getPrecioTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Convertir a DTOs para evitar lazy loading
        List<ReservaDTO> reservaDTOs = reservas.stream()
                .map(r -> {
                    ReservaDTO dto = new ReservaDTO();
                    dto.id = r.getId();
                    dto.cliente = r.getUsuario() != null ? r.getUsuario().getEmail() : "N/A";
                    
                    if (r.getHabitacion() != null) {
                        dto.recurso = "Hab. " + r.getHabitacion().getNumeroHabitacion();
                    } else if (r.getSala() != null) {
                        dto.recurso = "Sala " + r.getSala().getNombre();
                    } else {
                        dto.recurso = "N/A";
                    }
                    
                    dto.fechaInicio = r.getFechaInicio() != null ? r.getFechaInicio().toString() : "-";
                    dto.fechaFin = r.getFechaFin() != null ? r.getFechaFin().toString() : "-";
                    dto.precioTotal = r.getPrecioTotal();
                    return dto;
                })
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalIngresos", totalIngresos);
        stats.put("totalReservas", (long) reservaDTOs.size());
        stats.put("reservas", reservaDTOs);
        stats.put("month", Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "ES")));
        stats.put("year", year);

        return stats;
    }
    
    // DTO interno para evitar lazy loading
    private static class ReservaDTO {
        Long id;
        String cliente;
        String recurso;
        String fechaInicio;
        String fechaFin;
        BigDecimal precioTotal;
    }

    @Transactional(readOnly = true)
    public byte[] generateAnnualReport(int year) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        document.setMargins(36, 36, 36, 36);
        
        PdfWriter.getInstance(document, baos);
        document.open();

        // Título
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Paragraph title = new Paragraph("Informe Anual - Villadictos Hotel", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14, new Color(100, 100, 100));
        Paragraph subtitle = new Paragraph("Año: " + year, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Calcular totales anuales
        BigDecimal totalAnual = BigDecimal.ZERO;
        long totalReservasAnual = 0;

        // Tabla por meses
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(63, 81, 181));
        Paragraph detalleTitle = new Paragraph("Desglose Mensual", sectionFont);
        detalleTitle.setSpacingBefore(10);
        detalleTitle.setSpacingAfter(10);
        document.add(detalleTitle);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2, 2, 2, 2 });

        addTableHeader(table, "Mes", "Nº Reservas", "Ingresos", "Promedio");

        // Datos reales para cada mes
        for (int month = 1; month <= 12; month++) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            List<Reserva> reservasMes = reservaRepository.findAll().stream()
                    .filter(r -> r.getFechaInicio() != null && r.getFechaFin() != null)
                    .filter(r -> !r.getFechaInicio().isAfter(end) && !r.getFechaFin().isBefore(start))
                    .collect(Collectors.toList());

            BigDecimal ingresosMes = reservasMes.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.confirmada
                            || r.getEstado() == Reserva.EstadoReserva.completada)
                    .map(Reserva::getPrecioTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long numReservas = reservasMes.size();
            BigDecimal promedio = numReservas > 0 ? ingresosMes.divide(BigDecimal.valueOf(numReservas), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

            totalAnual = totalAnual.add(ingresosMes);
            totalReservasAnual += numReservas;

            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            addTableRow(table,
                    monthName,
                    String.valueOf(numReservas),
                    "€" + String.format("%.2f", ingresosMes),
                    "€" + String.format("%.2f", promedio));
        }

        document.add(table);

        // Resumen anual
        document.add(new Paragraph(" "));
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(63, 81, 181));
        Paragraph resumenTitle = new Paragraph("Resumen Anual", summaryFont);
        resumenTitle.setSpacingBefore(15);
        resumenTitle.setSpacingAfter(10);
        document.add(resumenTitle);

        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        document.add(new Paragraph("Total Ingresos Anuales: €" + String.format("%.2f", totalAnual), normalFont));
        document.add(new Paragraph("Total Reservas Anuales: " + totalReservasAnual, normalFont));
        BigDecimal promedioMensual = totalReservasAnual > 0 ? totalAnual.divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
        document.add(new Paragraph("Promedio Mensual de Ingresos: €" + String.format("%.2f", promedioMensual), normalFont));

        // Pie de página
        document.add(new Paragraph(" "));
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(150, 150, 150));
        Paragraph footer = new Paragraph("Generado el " + LocalDate.now() + " - Villadictos Hotel Management System", footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        return baos.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(63, 81, 181));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, String... values) {
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, cellFont));
            cell.setPadding(4);
            table.addCell(cell);
        }
    }
}
