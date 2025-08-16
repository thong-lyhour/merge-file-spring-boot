package com.example.file;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfMergerService {

    @Value("${app.files.upload-dir:src/main/resources/files}")
    private String uploadDir;

    public String mergeAndSaveFilesToPdf(MultipartFile[] files, String fileName) throws IOException {
        // Create the directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename to avoid conflicts
        String uniqueFileName = generateUniqueFileName(fileName);
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Create PDF using FileOutputStream directly
        try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            for (MultipartFile file : files) {
                String originalFileName = file.getOriginalFilename();
                String fileExtension = getFileExtension(originalFileName).toLowerCase();

                switch (fileExtension) {
                    case "pdf":
                        mergePdfFile(file, pdfDocument);
                        break;
                    case "png":
                    case "jpg":
                    case "jpeg":
                        addImageToPdf(file, document);
                        break;
                    case "txt":
                        addTextFileToPdf(file, document);
                        break;
                    default:
                        System.out.println("Unsupported file type: " + fileExtension);
                }
            }

            document.close();
        }

        return uniqueFileName;
    }

    //after merge file it's response back no save
    public byte[] mergeFilesToPdf(MultipartFile[] files) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(fileName).toLowerCase();

            switch (fileExtension) {
                case "pdf":
                    mergePdfFile(file, pdfDocument);
                    break;
                case "png":
                case "jpg":
                case "jpeg":
                    addImageToPdf(file, document);
                    break;
                case "txt":
                    addTextFileToPdf(file, document);
                    break;
                default:
                    // Handle unsupported file types or skip
                    System.out.println("Unsupported file type: " + fileExtension);
            }
        }

        document.close();
        return outputStream.toByteArray();
    }

    public byte[] getFileFromResources(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir, fileName);
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("File not found: " + fileName);
        }
        return Files.readAllBytes(filePath);
    }

    private String generateUniqueFileName(String baseName) {
        Path filePath = Paths.get(uploadDir, baseName + ".pdf");

        if (!Files.exists(filePath)) {
            return baseName + ".pdf";
        }

        // If file exists, add timestamp to make it unique
        int counter = 1;
        String uniqueName;
        do {
            uniqueName = baseName + "_" + counter + ".pdf";
            filePath = Paths.get(uploadDir, uniqueName);
            counter++;
        } while (Files.exists(filePath));

        return uniqueName;
    }

    private void mergePdfFile(MultipartFile file, PdfDocument targetPdf) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            PdfDocument sourcePdf = new PdfDocument(new PdfReader(inputStream));
            int numberOfPages = sourcePdf.getNumberOfPages();

            for (int i = 1; i <= numberOfPages; i++) {
                PdfPage page = sourcePdf.getPage(i);
                targetPdf.addPage(page.copyTo(targetPdf));
            }

            sourcePdf.close();
        }
    }

    private void addImageToPdf(MultipartFile file, Document document) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            ImageData imageData = ImageDataFactory.create(inputStream.readAllBytes());
            Image image = new Image(imageData);

            // Scale image to fit page if needed
            float pageWidth = document.getPdfDocument().getDefaultPageSize().getWidth() - 72; // 36pt margins on each side
            float pageHeight = document.getPdfDocument().getDefaultPageSize().getHeight() - 72;

            if (image.getImageWidth() > pageWidth || image.getImageHeight() > pageHeight) {
                image.scaleToFit(pageWidth, pageHeight);
            }

            document.add(image);
            document.add(new AreaBreak()); // Add page break after image
        }
    }

    private void addTextFileToPdf(MultipartFile file, Document document) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                document.add(new Paragraph(line));
            }
            document.add(new AreaBreak()); // Add page break after text file
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}