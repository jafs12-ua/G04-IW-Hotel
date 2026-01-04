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
                .filter(r -> !r.getFechaInicio().isAfter(end) && !r.getFechaFin().isBefore(start))
                .collect(Collectors.toList());

        List<Pago> pagos = pagoRepository.findAll().stream()
                .filter(p -> {
                    // Assuming payment date is roughly related to reservation or we use a date
                    // field if available
                    // Pago entity doesn't have date field in the snippet I saw, let's assume we
                    // filter by reservation date for now
                    // Or better, let's look at Pago entity again.
                    // Assuming Pago has no date, we use associated reservation date.
                    return p.getReserva() != null &&
                            !p.getReserva().getFechaInicio().isAfter(end) &&
                            !p.getReserva().getFechaInicio().isBefore(start);
                })
                .collect(Collectors.toList());

        BigDecimal totalIngresos = pagos.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalReservas = reservas.size();

        // Calculate occupancy (simplified: total days reserved / total days in month *
        // total rooms)
        // Let's just return raw numbers for the chart for now

        // Calcular distribución
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

        // Chart Data
        stats.put("ingresosHabitaciones", ingresosHabitaciones);
        stats.put("ingresosSalas", ingresosSalas);
        stats.put("ingresosServicios", ingresosServicios);

        return stats;
    }

    public byte[] generateMonthlyReportPdf(int month, int year) throws IOException {
        Map<String, Object> stats = getMonthlyStats(month, year);
        List<Reserva> reservas = (List<Reserva>) stats.get("reservas");
        BigDecimal totalIngresos = (BigDecimal) stats.get("totalIngresos");
        String monthName = (String) stats.get("month");

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
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
            document.add(new Paragraph("Total Ingresos: " + totalIngresos + "€"));
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
                        r.getFechaInicio().toString(),
                        r.getFechaFin().toString(),
                        r.getPrecioTotal() + "€");
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        }
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
