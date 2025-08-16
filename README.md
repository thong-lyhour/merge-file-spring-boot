# PDF Merger API

A Spring Boot REST API that merges multiple files of different formats (PDF, images, text files) into a single PDF document. The merged PDF is automatically saved to the application's resources directory.

## Features

- **Multiple File Format Support**: PDF, PNG, JPG, JPEG, TXT
- **Intelligent Merging**: 
  - PDFs are merged page by page
  - Images are automatically scaled to fit pages
  - Text files are converted to PDF paragraphs
- **File Storage**: Automatically saves merged PDFs to `resources/files/` directory
- **Unique Naming**: Prevents filename conflicts with automatic unique name generation
- **Download Support**: Provides endpoint to download saved PDFs
- **Configurable**: Customizable upload directory and file size limits

## Prerequisites

- Java 11 or higher
- Gradle 6.0 or higher
- Spring Boot 2.7+

## Dependencies

The following dependencies are required in your `build.gradle`:

```gradle
dependencies {
    // Spring Boot Starter Web
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // iText PDF for PDF manipulation
    implementation 'com.itextpdf:itext-core:9.1.0'
    
    // Apache PDFBox for additional PDF operations
    implementation 'org.apache.pdfbox:pdfbox:3.0.3'
    
    // Spring Boot Test (for testing)
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## Configuration

Add the following configuration to your `application.properties`:

```properties
# File upload configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB

# Custom upload directory (optional)
app.files.upload-dir=src/main/resources/files
```

## Installation & Setup

1. **Clone the repository** (or add the code to your existing Spring Boot project)

2. **Add dependencies** to your `build.gradle`

3. **Create the files directory**:
   ```bash
   mkdir -p src/main/resources/files
   ```

4. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

The API will be available at `http://localhost:8080`

## Build & Package

To build the project:
```bash
./gradlew build
```

To create a JAR file:
```bash
./gradlew bootJar
```

To run the JAR:
```bash
java -jar build/libs/your-app-name-0.0.1-SNAPSHOT.jar
```

## API Endpoints

### 1. Merge Files to PDF

**Endpoint**: `POST /api/files/merge-to-pdf/save`

**Description**: Merges multiple files into a single PDF and saves it to the resources directory.

**Parameters**:
- `files` (required): Array of files to merge
- `fileName` (optional): Custom filename for the merged PDF (without .pdf extension)

**Request Example**:
```bash
curl -X POST "http://localhost:8080/api/files/merge-to-pdf?fileName=my-document" \
  -F "files=@document1.pdf" \
  -F "files=@image1.png" \
  -F "files=@notes.txt"
```

**Response**:
```json
{
  "message": "Files merged successfully",
  "fileName": "my-document.pdf",
  "filePath": "resources/files/my-document.pdf"
}
```

### 2. Download Saved PDF

**Endpoint**: `GET /api/files/download/{fileName}`

**Description**: Downloads a previously merged PDF file.

**Request Example**:
```bash
curl -X GET "http://localhost:8080/api/files/download/my-document.pdf" \
  --output downloaded-file.pdf
```

## Supported File Formats

| Format | Extension | Description |
|--------|-----------|-------------|
| PDF | `.pdf` | Existing PDF pages are merged directly |
| Images | `.png`, `.jpg`, `.jpeg` | Images are scaled to fit PDF pages |
| Text | `.txt` | Text content is converted to PDF paragraphs |

## Usage Examples

### Example 1: Basic File Merging
```bash
curl -X POST "http://localhost:8080/api/files/merge-to-pdf/save" \
  -F "files=@report.pdf" \
  -F "files=@chart.png" \
  -F "files=@summary.txt"
```

### Example 2: Custom Filename
```bash
curl -X POST "http://localhost:8080/api/files/merge-to-pdf/save?fileName=quarterly-report" \
  -F "files=@q1.pdf" \
  -F "files=@q2.pdf" \
  -F "files=@analysis.png"
```

### Example 3: Using Postman
1. Set method to `POST`
2. URL: `http://localhost:8080/api/files/merge-to-pdf/save`
3. Body: `form-data`
4. Add key `files` with multiple file values
5. Optionally add `fileName` parameter

## File Storage

- **Location**: `src/main/resources/files/` (configurable)
- **Naming**: If no filename is provided, auto-generates: `merged-document-{timestamp}.pdf`
- **Conflict Resolution**: Appends counter to filename if file already exists
- **Directory Creation**: Automatically creates the storage directory if it doesn't exist

## Error Handling

The API handles various error scenarios:

- **Unsupported file formats**: Skips unsupported files and continues processing
- **File conflicts**: Automatically generates unique filenames
- **Large files**: Configurable size limits prevent memory issues
- **Missing files**: Returns appropriate HTTP status codes

## Extending the API

### Adding New File Formats

To support additional file formats, modify the switch statement in `PdfMergerService`:

```java
switch (fileExtension) {
    case "pdf":
        mergePdfFile(file, pdfDocument);
        break;
    case "docx":
        addWordDocumentToPdf(file, document);
        break;
    // Add more cases here
}
```

### Custom Processing Options

You can extend the API to support:
- Page numbering
- Bookmarks for each merged file
- Watermarks
- Custom page layouts
- Metadata preservation

## Performance Considerations

- **Memory Usage**: Large files are processed in streams to minimize memory consumption
- **File Size Limits**: Configure appropriate limits based on your server capacity
- **Concurrent Requests**: The service is stateless and supports concurrent file processing

## Troubleshooting

### Common Issues

1. **OutOfMemoryError**: Increase JVM heap size or reduce file size limits
2. **File not found**: Ensure the `resources/files` directory exists and is writable
3. **Unsupported format**: Check that file extensions match supported formats
4. **Permission denied**: Verify write permissions on the storage directory

### Logs

Enable debug logging to troubleshoot issues:
```properties
logging.level.com.yourpackage.PdfMergerService=DEBUG
```

## License

This project is open source and available under the [MIT License](LICENSE).
