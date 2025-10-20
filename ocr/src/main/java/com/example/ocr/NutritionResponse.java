package com.example.ocr;

import java.util.List;

public class NutritionResponse {
    private String textoExtraido;
    private List<Nutriente> desglose;
    private List<String> observacionesBuenas;
    private List<String> observacionesMalas;
    private String conclusion;

    // Constructores
    public NutritionResponse() {}

    public NutritionResponse(String textoExtraido, List<Nutriente> desglose, 
                           List<String> observacionesBuenas, List<String> observacionesMalas, 
                           String conclusion) {
        this.textoExtraido = textoExtraido;
        this.desglose = desglose;
        this.observacionesBuenas = observacionesBuenas;
        this.observacionesMalas = observacionesMalas;
        this.conclusion = conclusion;
    }

    // Getters y Setters
    public String getTextoExtraido() { return textoExtraido; }
    public void setTextoExtraido(String textoExtraido) { this.textoExtraido = textoExtraido; }
    public List<Nutriente> getDesglose() { return desglose; }
    public void setDesglose(List<Nutriente> desglose) { this.desglose = desglose; }
    public List<String> getObservacionesBuenas() { return observacionesBuenas; }
    public void setObservacionesBuenas(List<String> observacionesBuenas) { this.observacionesBuenas = observacionesBuenas; }
    public List<String> getObservacionesMalas() { return observacionesMalas; }
    public void setObservacionesMalas(List<String> observacionesMalas) { this.observacionesMalas = observacionesMalas; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
}