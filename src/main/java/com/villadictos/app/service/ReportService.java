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

    public byte[] generateMonthlyReport(int month, int year) throws IOException, DocumentException {
        Map<String, Object> stats = getMonthlyStats(month, year);
        @SuppressWarnings("unchecked")
        List<Reserva> reservas = (List<Reserva>) stats.get("reservas");
        BigDecimal totalIngresos = (BigDecimal) stats.get("totalIngresos");
        String monthName = (String) stats.get("month");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        PdfWriter.getInstance(document, baos);
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph title = new Paragraph("Informe Mensual - Villadictos Hotel", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Periodo: " + monthName + " " + year);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        // Summary
        document.add(new Paragraph("Resumen General:"));
        document.add(new Paragraph("Total Ingresos Confirmados: " + totalIngresos + "€"));
        document.add(new Paragraph("Total Reservas: " + reservas.size()));
        document.add(new Paragraph(" "));

        // Table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1, 3, 3, 2, 2, 2 });

        addTableHeader(table, "ID", "Cliente", "Recurso", "Entrada", "Salida", "Total");

        for (Reserva r : reservas) {
            String cliente = r.getUsuario() != null ? r.getUsuario().getEmail() : "N/A";
            String recurso = "N/A";
            if (r.getHabitacion() != null) {
                recurso = "Hab. " + r.getHabitacion().getNumeroHabitacion();
            } else if (r.getSala() != null) {
                recurso = "Sala " + r.getSala().getNombre();
            }

            addRows(table,
                    String.valueOf(r.getId()),
                    cliente,
                    recurso,
                    r.getFechaInicio() != null ? r.getFechaInicio().toString() : "-",
                    r.getFechaFin() != null ? r.getFechaFin().toString() : "-",
                    r.getPrecioTotal() + "€");
        }

        document.add(table);
        document.close();

        return baos.toByteArray();
    }

    public byte[] generateAnnualReport(int year) throws IOException, DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph title = new Paragraph("Informe Anual - Villadictos Hotel", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Año: " + year);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(20);
        document.add(subtitle);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 2, 2, 2 });

        addTableHeader(table, "Mes", "Nº Reservas", "Ingresos");

        BigDecimal totalAnnualIncome = BigDecimal.ZERO;
        long totalAnnualReservations = 0;

        for (int i = 1; i <= 12; i++) {
            Map<String, Object> monthlyStats = getMonthlyStats(i, year);
            BigDecimal monthlyIncome = (BigDecimal) monthlyStats.get("totalIngresos");
            long monthlyReservations = (long) monthlyStats.get("totalReservas");

            totalAnnualIncome = totalAnnualIncome.add(monthlyIncome);
            totalAnnualReservations += monthlyReservations;

            String monthName = Month.of(i).getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

            addRows(table,
                    monthName,
                    String.valueOf(monthlyReservations),
                    monthlyIncome + "€");
        }

        document.add(table);

        document.add(new Paragraph(" "));
        Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
        document.add(new Paragraph("Total Anual Ingresos: " + totalAnnualIncome + "€", summaryFont));
        document.add(new Paragraph("Total Anual Reservas: " + totalAnnualReservations, summaryFont));

        document.close();

        return baos.toByteArray();
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(Color.LIGHT_GRAY);
            headerCell.setPhrase(new Phrase(header));
            table.addCell(headerCell);
        }
    }

    private void addRows(PdfPTable table, String... cells) {
        for (String cell : cells) {
            table.addCell(cell);
        }
    }
}
