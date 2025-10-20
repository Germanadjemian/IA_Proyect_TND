import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

export const analyzeNutritionLabel = async (imageFile) => {
  const formData = new FormData();
  formData.append('imagen', imageFile);
  
  try {
    console.log('Enviando imagen a la API...');
    
    const response = await axios.post(`${API_BASE_URL}/ocr/procesar`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      timeout: 30000,
    });

    console.log('Respuesta del backend:', response.data);
    
    return response.data;
    
  } catch (error) {
    console.error('API Error:', error);
    
    if (error.response) {
      throw new Error(`Error del servidor: ${error.response.data.error || error.response.status}`);
    } else if (error.request) {
      throw new Error('No se pudo conectar con el servidor. Verifica que la API esté ejecutándose.');
    } else {
      throw new Error('Error al procesar la imagen: ' + error.message);
    }
  }
};