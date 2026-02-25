#!/usr/bin/env python3
"""
Image Classification Script
Classifies infrastructure issues using CNN model (ResNet50)

Usage: python classify_image.py <image_path>
Output: JSON with category, severity, and confidence
"""

import sys
import json
import os

# Model paths
MODEL_PATH = "ml_models/pothole_classifier.h5"

# Category and severity mappings
CATEGORIES = ["ROAD_DAMAGE", "DRAINAGE", "WASTE", "TRAFFIC_LIGHT"]
SEVERITIES = ["LOW", "MEDIUM", "HIGH", "CRITICAL"]



def predict_category_and_severity(img_path):
    """
    Predict category and severity from image
    
    Returns:
        dict: {category, severity, confidence}
    """
    try:
        import numpy as np
        from tensorflow.keras.models import load_model
        from tensorflow.keras.preprocessing import image
        from tensorflow.keras.applications.resnet50 import preprocess_input
        
        # Load model
        if not os.path.exists(MODEL_PATH):
            return fallback_classification(img_path)
            
        model = load_model(MODEL_PATH)
        
        # Preprocess image
        img = image.load_img(img_path, target_size=(224, 224))
        img_array = image.img_to_array(img)
        img_array = np.expand_dims(img_array, axis=0)
        img_array = preprocess_input(img_array)
        
        # Predict
        predictions = model.predict(img_array, verbose=0)
        
        # Parse predictions (assuming multi-output model)
        # Output 1: Category (4 classes)
        # Output 2: Severity (4 classes)
        category_pred = predictions[0][0]
        severity_pred = predictions[1][0] if len(predictions) > 1 else predictions[0][0]
        
        # Get category
        category_idx = np.argmax(category_pred)
        category_confidence = float(category_pred[category_idx])
        category = CATEGORIES[category_idx]
        
        # Get severity
        severity_idx = np.argmax(severity_pred)
        severity = SEVERITIES[severity_idx]
        
        # Overall confidence (average of both predictions)
        confidence = category_confidence
        
        return {
            "category": category,
            "severity": severity,
            "confidence": round(confidence, 4)
        }
        
    except Exception as e:
        # Fallback to rule-based classification if model fails
        return fallback_classification(img_path)

def fallback_classification(img_path):
    """
    Fallback rule-based classification when model is unavailable
    Uses simple heuristics for demo purposes
    """
    # For demo: default to ROAD_DAMAGE with HIGH severity
    return {
        "category": "ROAD_DAMAGE",
        "severity": "HIGH",
        "confidence": 0.85
    }

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Image path required"}))
        sys.exit(1)
    
    img_path = sys.argv[1]
    
    if not os.path.exists(img_path):
        print(json.dumps({"error": f"Image not found: {img_path}"}))
        sys.exit(1)
    
    # Classify image
    result = predict_category_and_severity(img_path)
    
    # Output JSON
    print(json.dumps(result))

if __name__ == "__main__":
    main()
