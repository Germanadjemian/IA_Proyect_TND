package com.example.ocr;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@RestController
@RequestMapping("/ocr")
@CrossOrigin(origins = "*") // 🔓 permite llamadas desde tu frontend (React, etc.)
public class OcrController {

    @PostMapping(
            value = "/procesar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> procesarImagen(@RequestParam("imagen") MultipartFile imagen) {
        Map<String, String> respuesta = new HashMap<>();

        try {
            // Determinar la extensión según el tipo MIME (a saber que es el mime)
            String contentType = imagen.getContentType();
            String extension = ".png"; // valor por defecto

            if (contentType != null) {
                if (contentType.equalsIgnoreCase("image/jpeg") || contentType.equalsIgnoreCase("image/jpg")) {
                    extension = ".jpg";
                } else if (contentType.equalsIgnoreCase("image/png")) {
                    extension = ".png";
                }
            }

            //  Guardar la imagen temporalmente con la extensión correcta ¡¡IMPORTANTE!!
            File tempFile = File.createTempFile("ocr-", extension);
            imagen.transferTo(tempFile);

            // Configurar Tesseract
            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("src/main/resources/tessdata"); // 📁 ruta a los archivos de entrnamiento por idioma (spa.train).traineddata
            tesseract.setLanguage("spa"); // 🇪🇸 español (usa "eng" si querés inglés)

            // Ejecutar OCR
            String textoDetectado = tesseract.doOCR(tempFile);

            // Borrar el archivo temporal
            tempFile.delete();

            // Devolver respuesta JSON
            respuesta.put("texto", textoDetectado.trim());
            return ResponseEntity.ok(respuesta);

        } catch (IOException | TesseractException e) {
            respuesta.put("error", "Error al procesar la imagen: " + e.getMessage());
            return ResponseEntity.internalServerError().body(respuesta);
        }
    }
}
