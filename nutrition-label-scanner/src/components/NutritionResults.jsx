import React from "react";

const NutritionResults = ({ data, loading, error }) => {
  if (loading) {
    return (
      <div className="nutrition-results loading">
        <div className="loading-spinner"></div>
        <p>Analizando etiqueta nutricional con IA...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="nutrition-results error">
        <h3>Error</h3>
        <p>{error}</p>
      </div>
    );
  }

  if (!data) {
    return (
      <div className="nutrition-results placeholder">
        <h3>Análisis Nutricional con IA</h3>
        <p>
          Sube una foto de una etiqueta nutricional para ver el análisis detallado aquí.
        </p>
      </div>
    );
  }

  return (
    <div className="nutrition-results">
      <h3>Análisis Nutricional con IA</h3>

      <div className="results-content">
        {data.conclusion && (
          <div className="conclusion-section">
            <h4>Conclusión del Análisis</h4>
            <div className="conclusion-content">
              <p>{data.conclusion}</p>
            </div>
          </div>
        )}

        {data.desglose && data.desglose.length > 0 && (
          <div className="nutrition-details">
            <h4>Desglose Nutricional</h4>
            <div className="nutrients-grid">
              {data.desglose.map((nutriente, index) => (
                <div key={index} className="nutrient-item">
                  <span className="nutrient-name">{nutriente.nombre}</span>
                  <span className="nutrient-value">
                    {nutriente.valor} {nutriente.unidad}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {data.observacionesBuenas && data.observacionesBuenas.length > 0 && (
          <div className="positive-insights">
            <h4>✅ Aspectos Positivos</h4>
            <ul>
              {data.observacionesBuenas.map((observacion, index) => (
                <li key={index}>{observacion}</li>
              ))}
            </ul>
          </div>
        )}

        {data.observacionesMalas && data.observacionesMalas.length > 0 && (
          <div className="considerations">
            <h4>⚠️ Aspectos a Considerar</h4>
            <ul>
              {data.observacionesMalas.map((observacion, index) => (
                <li key={index}>{observacion}</li>
              ))}
            </ul>
          </div>
        )}

        {data.textoExtraido && (
          <div className="raw-text">
            <h4>📄 Texto Extraído por OCR</h4>
            <div className="raw-text-content">
              <pre>{data.textoExtraido}</pre>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default NutritionResults;