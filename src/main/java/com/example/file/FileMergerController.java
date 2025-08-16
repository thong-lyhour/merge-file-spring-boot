package com.example.file;

import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileMergerController {

    @Autowired
    private PdfMergerService pdfMergerService;

    @PostMapping("/merge-to-pdf")
    public ResponseEntity<byte[]> mergeFilesToPdf(@RequestParam("files") MultipartFile[] files) {
        try {
            byte[] mergedPdf = pdfMergerService.mergeFilesToPdf(files);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("merged-document.pdf").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(mergedPdf);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/merge-to-pdf/save")
    public ResponseEntity<Map<String, String>> mergeFilesToPdf(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "fileName", required = false) String fileName) {
        try {
            // Generate filename if not provided
            if (StringUtils.isBlank(fileName)) {
                fileName = "merged-document-" + System.currentTimeMillis();
            }

            // Remove .pdf extension if provided (we'll add it)
            if (fileName.endsWith(".pdf")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }

            String savedFileName = pdfMergerService.mergeAndSaveFilesToPdf(files, fileName);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Files merged successfully");
            response.put("fileName", savedFileName);
            response.put("filePath", "resources/files/" + savedFileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to merge files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        try {
            byte[] fileContent = pdfMergerService.getFileFromResources(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename(fileName).build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}