// src/App.jsx - SE MANTIENE IGUAL
import React, { useState } from 'react';
import ImageUpload from './components/ImageUpload';
import NutritionResults from './components/NutritionResults';
import { analyzeNutritionLabel } from './services/api';
import './App.css';

function App() {
  const [nutritionData, setNutritionData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleImageUpload = async (imageFile) => {
    setLoading(true);
    setError(null);
    setNutritionData(null);
    
    try {
      const data = await analyzeNutritionLabel(imageFile);
      setNutritionData(data);
    } catch (err) {
      setError(err.message || 'Error al analizar la etiqueta nutricional.');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Escáner de Etiquetas Nutricionales con IA</h1>
        <p>Sube una imagen de una etiqueta nutricional para analizar su contenido con Gemini AI</p>
      </header>
      
      <main className="app-content">
        <section className="left-panel">
          <ImageUpload 
            onImageUpload={handleImageUpload}
            loading={loading}
          />
        </section>
        
        <section className="right-panel">
          <NutritionResults 
            data={nutritionData}
            loading={loading}
            error={error}
          />
        </section>
      </main>
    </div>
  );
}

export default App;