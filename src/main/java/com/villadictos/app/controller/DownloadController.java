package com.villadictos.app.controller;

import com.villadictos.app.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;

@Controller
public class DownloadController {

    private final ReportService reportService;

    public DownloadController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/admin/download/pdf/monthly")
    public void downloadMonthlyReport(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            HttpServletResponse response) throws IOException {

        System.out.println("Processing Monthly Download...");
        LocalDate now = LocalDate.now();
        int currentMonth = month != null ? month : now.getMonthValue();
        int currentYear = year != null ? year : now.getYear();

        byte[] pdfBytes = reportService.generateMonthlyReport(currentMonth, currentYear);
        System.out.println("Monthly PDF generated. Size: " + pdfBytes.length);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"informe_mensual_" + currentYear + "_" + currentMonth + ".pdf\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.
        response.setContentLength(pdfBytes.length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(pdfBytes);
            out.flush();
        }
        System.out.println("Monthly PDF sent.");
    }

    @GetMapping("/admin/download/pdf/annual")
    public void downloadAnnualReport(
            @RequestParam(required = false) Integer year,
            HttpServletResponse response) throws IOException {

        System.out.println("Processing Annual Download...");
        LocalDate now = LocalDate.now();
        int currentYear = year != null ? year : now.getYear();

        byte[] pdfBytes = reportService.generateAnnualReport(currentYear);
        System.out.println("Annual PDF generated. Size: " + pdfBytes.length);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"informe_anual_" + currentYear + ".pdf\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentLength(pdfBytes.length);

        try (OutputStream out = response.getOutputStream()) {
            out.write(pdfBytes);
            out.flush();
        }
        System.out.println("Annual PDF sent.");
    }
}
