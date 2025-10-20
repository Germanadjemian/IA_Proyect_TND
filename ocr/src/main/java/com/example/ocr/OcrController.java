package com.example.ocr;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/ocr")
@CrossOrigin(origins = "*")
public class OcrController {

    private final GeminiService geminiService;

    public OcrController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping(value = "/procesar", consumes = "multipart/form-data")
    public ResponseEntity<NutritionResponse> procesarImagen(@RequestParam("imagen") MultipartFile imagen) {
        try {
            // 1. Procesar OCR
            String textoExtraido = procesarOCR(imagen);
            
            // 2. Gemini hace TODO el análisis
            NutritionResponse respuesta = geminiService.analizarConGemini(textoExtraido);
            
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            // En caso de error, devolver respuesta básica
            NutritionResponse errorResponse = new NutritionResponse();
            errorResponse.setTextoExtraido("Error: " + e.getMessage());
            errorResponse.setConclusion("Error al procesar la imagen. Intenta con otra.");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private String procesarOCR(MultipartFile imagen) throws IOException, Exception {
        String contentType = imagen.getContentType();
        String extension = ".png";

        if (contentType != null) {
            if (contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg")) {
                extension = ".jpg";
            } else if (contentType.equalsIgnoreCase("image/png")) {
                extension = ".png";
            }
        }

        File tempFile = File.createTempFile("ocr-", extension);
        imagen.transferTo(tempFile);

        net.sourceforge.tess4j.ITesseract tesseract = new net.sourceforge.tess4j.Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("spa");

        String textoDetectado = tesseract.doOCR(tempFile);
        tempFile.delete();

        return textoDetectado.trim();
    }
}