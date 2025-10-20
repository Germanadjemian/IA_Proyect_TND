package com.example.ocr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;

    public GeminiService() {
        this.restTemplate = new RestTemplate();
    }

    public NutritionResponse analizarConGemini(String textoNutricional) {
        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return crearRespuestaPorDefecto(textoNutricional);
        }

        try {
            String prompt = "Eres un experto en nutrición. Analiza esta información nutricional extraída por OCR " +
                    "(puede tener errores de reconocimiento) y devuélveme SOLO un JSON con esta estructura exacta:\n" +
                    "{\n" +
                    "  \"desglose\": [\n" +
                    "    {\"nombre\": \"Calorías\", \"valor\": \"X\", \"unidad\": \"kcal\"},\n" +
                    "    {\"nombre\": \"Proteínas\", \"valor\": \"X\", \"unidad\": \"g\"},\n" +
                    "    {\"nombre\": \"Grasas totales\", \"valor\": \"X\", \"unidad\": \"g\"},\n" +
                    "    {\"nombre\": \"Grasas saturadas\", \"valor\": \"X\", \"unidad\": \"g\"},\n" +
                    "    {\"nombre\": \"Carbohidratos\", \"valor\": \"X\", \"unidad\": \"g\"},\n" +
                    "    {\"nombre\": \"Azúcares\", \"valor\": \"X\", \"unidad\": \"g\"},\n" +
                    "    {\"nombre\": \"Fibra\", \"valor\": \"X\", \"unidad\": \"g\"},\n" +
                    "    {\"nombre\": \"Sodio\", \"valor\": \"X\", \"unidad\": \"mg\"}\n" +
                    "  ],\n" +
                    "  \"observacionesBuenas\": [\"obs1\", \"obs2\", \"obs3\"],\n" +
                    "  \"observacionesMalas\": [\"obs1\", \"obs2\", \"obs3\"],\n" +
                    "  \"conclusion\": \"texto de conclusión aquí\"\n" +
                    "}\n\n" +
                    "INSTRUCCIONES CRÍTICAS:\n" +
                    "1. El texto OCR tiene errores comunes: '128' significa '12g', '68' significa '6g', '28' significa '2g', '358' significa '3.5g'\n" +
                    "2. Corrige automáticamente estos errores de OCR\n" +
                    "3. Para valores no encontrados, usa \"0\"\n" +
                    "4. Las observaciones deben ser específicas y útiles para el consumidor\n" +
                    "5. Responde ÚNICAMENTE con el JSON, sin texto adicional\n\n" +
                    "TEXTO OCR (con errores):\n" + textoNutricional;

            // Crear el request body para Gemini
            Map<String, Object> requestBody = new HashMap<>();
            
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            
            content.put("parts", Collections.singletonList(part));
            
            requestBody.put("contents", Collections.singletonList(content));
            requestBody.put("generationConfig", Map.of(
                "temperature", 0.1,
                "maxOutputTokens", 4096,
                "topP", 0.8,
                "topK", 40
            ));

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Gemini API uses query parameter for API key
            String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("URL de Gemini: " + urlWithKey);

            // Hacer la petición a Gemini
            ResponseEntity<Map> response = restTemplate.exchange(
                    urlWithKey,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return procesarRespuestaGemini(response.getBody(), textoNutricional);

        } catch (Exception e) {
            System.err.println("Error con Gemini API: " + e.getMessage());
            e.printStackTrace();
            return crearRespuestaPorDefecto(textoNutricional);
        }
    }

    private NutritionResponse procesarRespuestaGemini(Map<String, Object> response, String textoOriginal) {
    try {
        // IMPRIMIR RESPUESTA COMPLETA PARA DEBUG
        System.out.println("=== RESPUESTA COMPLETA DE GEMINI ===");
        System.out.println("Response: " + response);
        System.out.println("Response class: " + response.getClass().getName());
        System.out.println("Response keys: " + response.keySet());
        System.out.println("================================");

        // Verificar si hay candidates
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            System.err.println("ERROR: No candidates in response");
            return crearRespuestaPorDefecto(textoOriginal);
        }
        
        // Imprimir información del primer candidate
        Map<String, Object> candidate = candidates.get(0);
        System.out.println("Candidate: " + candidate);
        System.out.println("Candidate keys: " + candidate.keySet());
        
        // Verificar finishReason
        String finishReason = (String) candidate.get("finishReason");
        System.out.println("Finish Reason: " + finishReason);
        
        if ("MAX_TOKENS".equals(finishReason)) {
            System.err.println("ERROR: Gemini excedió el límite de tokens");
        }
        
        // Obtener el contenido
        Map<String, Object> content = (Map<String, Object>) candidate.get("content");
        if (content == null) {
            System.err.println("ERROR: Content is null");
            return crearRespuestaPorDefecto(textoOriginal);
        }
        
        System.out.println("Content: " + content);
        System.out.println("Content keys: " + content.keySet());
        
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            System.err.println("ERROR: Parts is null or empty");
            return crearRespuestaPorDefecto(textoOriginal);
        }
        
        String contentText = (String) parts.get(0).get("text");
        
        System.out.println("=== TEXTO EXTRAÍDO DE GEMINI ===");
        System.out.println(contentText);
        System.out.println("================================");
        
        return parsearRespuestaCompleta(contentText, textoOriginal);
        
    } catch (Exception e) {
        System.err.println("Error procesando respuesta de Gemini: " + e.getMessage());
        e.printStackTrace();
        return crearRespuestaPorDefecto(textoOriginal);
    }
}

    // Los demás métodos permanecen igual...
    private NutritionResponse parsearRespuestaCompleta(String jsonContent, String textoOriginal) {
        try {
            List<Nutriente> desglose = new ArrayList<>();
            List<String> observacionesBuenas = new ArrayList<>();
            List<String> observacionesMalas = new ArrayList<>();
            String conclusion = "Conclusión no disponible";
            
            // Limpiar el contenido - quitar markdown si existe
            jsonContent = jsonContent.replace("```json", "").replace("```", "").trim();
            
            // Parsear desglose
            int desgloseStart = jsonContent.indexOf("\"desglose\":") + 10;
            int desgloseEnd = jsonContent.indexOf("]", desgloseStart) + 1;
            if (desgloseStart > 10 && desgloseEnd > desgloseStart) {
                String desgloseStr = jsonContent.substring(desgloseStart, desgloseEnd);
                desglose = parsearDesglose(desgloseStr);
            }
            
            // Parsear observaciones buenas
            observacionesBuenas = extraerLista(jsonContent, "observacionesBuenas");
            
            // Parsear observaciones malas
            observacionesMalas = extraerLista(jsonContent, "observacionesMalas");
            
            // Parsear conclusión
            conclusion = extraerValorSimple(jsonContent, "conclusion");
            
            return new NutritionResponse(textoOriginal, desglose, observacionesBuenas, observacionesMalas, conclusion);
            
        } catch (Exception e) {
            System.err.println("Error parseando respuesta completa: " + e.getMessage());
            return crearRespuestaPorDefecto(textoOriginal);
        }
    }

    private List<Nutriente> parsearDesglose(String desgloseJson) {
        List<Nutriente> nutrientes = new ArrayList<>();
        try {
            // Buscar cada objeto de nutriente
            int startIndex = 0;
            while ((startIndex = desgloseJson.indexOf("{", startIndex)) != -1) {
                int endIndex = desgloseJson.indexOf("}", startIndex) + 1;
                if (endIndex > startIndex) {
                    String nutrienteJson = desgloseJson.substring(startIndex, endIndex);
                    Nutriente nutriente = parsearNutriente(nutrienteJson);
                    if (nutriente != null) {
                        nutrientes.add(nutriente);
                    }
                    startIndex = endIndex;
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parseando desglose: " + e.getMessage());
        }
        
        // Si no se pudo parsear, crear desglose básico
        if (nutrientes.isEmpty()) {
            nutrientes.add(new Nutriente("Calorías", "0", "kcal"));
            nutrientes.add(new Nutriente("Proteínas", "0", "g"));
            nutrientes.add(new Nutriente("Grasas totales", "0", "g"));
        }
        
        return nutrientes;
    }

    private Nutriente parsearNutriente(String nutrienteJson) {
        try {
            String nombre = extraerValorDeObjeto(nutrienteJson, "nombre");
            String valor = extraerValorDeObjeto(nutrienteJson, "valor");
            String unidad = extraerValorDeObjeto(nutrienteJson, "unidad");
            
            if (nombre != null && valor != null && unidad != null) {
                return new Nutriente(nombre, valor, unidad);
            }
        } catch (Exception e) {
            System.err.println("Error parseando nutriente: " + e.getMessage());
        }
        return null;
    }

    private String extraerValorDeObjeto(String json, String clave) {
        try {
            String busqueda = "\"" + clave + "\":\"";
            int start = json.indexOf(busqueda);
            if (start == -1) {
                busqueda = "\"" + clave + "\": \"";
                start = json.indexOf(busqueda);
            }
            if (start != -1) {
                start += busqueda.length();
                int end = json.indexOf("\"", start);
                if (end > start) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Ignorar error
        }
        return null;
    }

    private List<String> extraerLista(String json, String clave) {
        List<String> lista = new ArrayList<>();
        try {
            int start = json.indexOf("\"" + clave + "\":") + clave.length() + 3;
            int end = json.indexOf("]", start);
            if (end > start) {
                String contenido = json.substring(start, end);
                // Dividir por comas que estén fuera de comillas
                String[] items = contenido.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String item : items) {
                    String limpio = item.trim().replace("\"", "");
                    if (!limpio.isEmpty() && !limpio.equals("[")) {
                        lista.add(limpio);
                    }
                }
            }
        } catch (Exception e) {
            // Valores por defecto si falla
            if (clave.equals("observacionesBuenas")) {
                lista.add("Análisis completado correctamente");
            } else {
                lista.add("Revisar valores manualmente");
            }
        }
        return lista;
    }

    private String extraerValorSimple(String json, String clave) {
        try {
            String busqueda = "\"" + clave + "\":\"";
            int start = json.indexOf(busqueda);
            if (start == -1) {
                busqueda = "\"" + clave + "\": \"";
                start = json.indexOf(busqueda);
            }
            if (start != -1) {
                start += busqueda.length();
                int end = json.indexOf("\"", start);
                if (end > start) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            // Ignorar error
        }
        return "Análisis nutricional completado. Revisa los valores detallados.";
    }

    private NutritionResponse crearRespuestaPorDefecto(String textoOriginal) {
        // Análisis local mejorado como fallback
        List<Nutriente> desglose = extraerDesgloseLocal(textoOriginal);
        List<String> buenas = generarObservacionesBuenas(desglose);
        List<String> malas = generarObservacionesMalas(desglose);
        String conclusion = generarConclusionLocal(desglose);
        
        return new NutritionResponse(textoOriginal, desglose, buenas, malas, conclusion);
    }

    // Métodos de análisis local (como respaldo) - permanecen iguales
    private List<Nutriente> extraerDesgloseLocal(String texto) {
        List<Nutriente> nutrientes = new ArrayList<>();
        String[] lineas = texto.split("\n");
        
        Map<String, String> correccionesOCR = Map.of(
            "128", "12", "68", "6", "28", "2", "358", "3.5",
            "1008", "100", "42864", "428"
        );
        
        for (String linea : lineas) {
            String lineaOriginal = linea.trim();
            String lineaLower = lineaOriginal.toLowerCase();
            
            // Aplicar correcciones OCR
            for (Map.Entry<String, String> correccion : correccionesOCR.entrySet()) {
                if (lineaOriginal.contains(correccion.getKey())) {
                    lineaLower = lineaLower.replace(correccion.getKey().toLowerCase(), correccion.getValue());
                }
            }
            
            // Detección de nutrientes
            if (lineaLower.contains("prote") && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Proteínas", extraerNumero(lineaLower), "g"));
            }
            if ((lineaLower.contains("grasas totales") || lineaLower.contains("grasa total")) && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Grasas totales", extraerNumero(lineaLower), "g"));
            }
            if (lineaLower.contains("grasas saturadas") && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Grasas saturadas", extraerNumero(lineaLower), "g"));
            }
            if (lineaLower.contains("grasas trans") && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Grasas trans", extraerNumero(lineaLower), "mg"));
            }
            if ((lineaLower.contains("calor") || lineaLower.contains("energ") || lineaLower.contains("kcal")) && extraerNumero(lineaLower) != null) {
                String valor = extraerNumero(lineaLower);
                nutrientes.add(new Nutriente("Calorías", valor, "kcal"));
            }
            if ((lineaLower.contains("carbohidratos") || lineaLower.contains("hidratos")) && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Carbohidratos", extraerNumero(lineaLower), "g"));
            }
            if (lineaLower.contains("azúcar") && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Azúcares", extraerNumero(lineaLower), "g"));
            }
            if (lineaLower.contains("fibra") && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Fibra", extraerNumero(lineaLower), "g"));
            }
            if (lineaLower.contains("sodio") && extraerNumero(lineaLower) != null) {
                nutrientes.add(new Nutriente("Sodio", extraerNumero(lineaLower), "mg"));
            }
        }
        
        // Eliminar duplicados
        Set<String> nombres = new HashSet<>();
        List<Nutriente> nutrientesUnicos = new ArrayList<>();
        for (Nutriente n : nutrientes) {
            if (nombres.add(n.getNombre())) {
                nutrientesUnicos.add(n);
            }
        }
        
        return nutrientesUnicos;
    }

    private String extraerNumero(String texto) {
        try {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
            java.util.regex.Matcher matcher = pattern.matcher(texto);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Ignorar
        }
        return "0";
    }

    private List<String> generarObservacionesBuenas(List<Nutriente> desglose) {
        List<String> buenas = new ArrayList<>();
        for (Nutriente n : desglose) {
            if (n.getNombre().equals("Azúcares") && "0".equals(n.getValor())) {
                buenas.add("Cero azúcares - Excelente para dietas low-carb");
            }
            if (n.getNombre().equals("Proteínas") && Integer.parseInt(n.getValor()) >= 10) {
                buenas.add("Alto contenido proteico (" + n.getValor() + "g)");
            }
        }
        if (buenas.isEmpty()) buenas.add("Análisis local completado");
        return buenas;
    }

    private List<String> generarObservacionesMalas(List<Nutriente> desglose) {
        List<String> malas = new ArrayList<>();
        for (Nutriente n : desglose) {
            if (n.getNombre().equals("Sodio") && Integer.parseInt(n.getValor()) > 400) {
                malas.add("Alto en sodio (" + n.getValor() + "mg)");
            }
        }
        if (malas.isEmpty()) malas.add("Configura API key de Gemini para análisis con IA");
        return malas;
    }

    private String generarConclusionLocal(List<Nutriente> desglose) {
        return "Análisis nutricional completado. Para análisis más preciso con IA, configura tu API key de Gemini.";
    }
}