package com.villadictos.app.controller;

import com.villadictos.app.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
public class PdfReportController {

    private final ReportService reportService;

    public PdfReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/pdf/mensual")
    public ResponseEntity<byte[]> descargarInformeMensual(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        try {
            LocalDate now = LocalDate.now();
            int currentMonth = month != null ? month : now.getMonthValue();
            int currentYear = year != null ? year : now.getYear();
            
            byte[] pdfBytes = reportService.generateMonthlyReport(currentMonth, currentYear);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", 
                "attachment; filename=informe_mensual_" + currentYear + "_" + currentMonth + ".pdf");
            headers.setContentLength(pdfBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/pdf/anual")
    public ResponseEntity<byte[]> descargarInformeAnual(
            @RequestParam(required = false) Integer year) {
        
        try {
            LocalDate now = LocalDate.now();
            int currentYear = year != null ? year : now.getYear();
            
            byte[] pdfBytes = reportService.generateAnnualReport(currentYear);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add("Content-Disposition", "attachment; filename=informe_anual_" + currentYear + ".pdf");
            headers.setContentLength(pdfBytes.length);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
