// src/components/ImageUpload.jsx
import React, { useRef, useState } from "react";

const ImageUpload = ({ onImageUpload, loading }) => {
  const fileInputRef = useRef(null);
  const [preview, setPreview] = useState(null);
  const [error, setError] = useState(null);

  const handleFileSelect = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validar tipo de archivo
    const validTypes = ["image/jpeg", "image/jpg", "image/png", "image/webp"];
    if (!validTypes.includes(file.type)) {
      setError("Por favor selecciona un archivo de imagen válido (JPG, PNG o JPEG)");
      return;
    }

    // Validar tamaño (max 10MB)
    if (file.size > 10 * 1024 * 1024) {
      setError("El tamaño de la imagen debe ser menor a 10MB");
      return;
    }

    setError(null);
    
    // Crear preview
    const previewUrl = URL.createObjectURL(file);
    setPreview(previewUrl);
    
    // Enviar al componente padre
    onImageUpload(file);
  };

  const triggerFileInput = () => {
    fileInputRef.current?.click();
  };

  const removeImage = () => {
    setPreview(null);
    setError(null);
    // Limpiar el input file
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className="image-upload">
      <input
        type="file"
        ref={fileInputRef}
        accept=".jpg,.jpeg,.png,.webp"
        onChange={handleFileSelect}
        style={{ display: "none" }}
        disabled={loading}
      />

      {error && (
        <div className="upload-error">
          <p>❌ {error}</p>
          <button
            onClick={() => setError(null)}
            className="upload-button secondary-button"
          >
            OK
          </button>
        </div>
      )}

      {!preview ? (
        <div className="upload-placeholder">
          <h3>Subir Etiqueta Nutricional</h3>
          <p>Selecciona una imagen de la etiqueta nutricional para analizar su contenido</p>
          
          <button
            onClick={triggerFileInput}
            className="upload-button primary-button"
            disabled={loading}
          >
            {loading ? "Procesando..." : "Seleccionar Imagen"}
          </button>

          <div className="upload-info">
            <p><strong>Formatos soportados:</strong> JPG, PNG, JPEG</p>
            <p><strong>Tamaño máximo:</strong> 10MB</p>
          </div>
        </div>
      ) : (
        <div className="image-preview">
          <h3>Vista Previa de la Imagen</h3>
          <img
            src={preview}
            alt="Vista previa de la etiqueta nutricional"
            className="preview-image"
          />
          <div className="preview-controls">
            <button
              onClick={triggerFileInput}
              className="upload-button secondary-button"
              disabled={loading}
            >
              Seleccionar Otra Imagen
            </button>
            <button
              onClick={removeImage}
              className="upload-button secondary-button"
              disabled={loading}
            >
              🗑 Eliminar Imagen
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ImageUpload;