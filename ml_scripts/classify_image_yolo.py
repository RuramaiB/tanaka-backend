#!/usr/bin/env python3
"""
YOLOv8 Image Classification/Segmentation Script
Replaces the legacy ResNet50 classifier.

Usage: python classify_image_yolo.py <image_path>
"""

import sys
import json
import os
import traceback

# Calculate absolute paths
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_PATH = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "ml_models", "pothole_seg_best.pt"))
FALLBACK_MODEL = os.path.abspath(os.path.join(SCRIPT_DIR, "..", "ml_models", "yolov8n-seg.pt"))

def predict(img_path):
    try:
        from ultralytics import YOLO
        
        # Determine which model to use
        active_model = MODEL_PATH if os.path.exists(MODEL_PATH) else FALLBACK_MODEL
        
        if not os.path.exists(active_model):
            # If even the base nano model is missing, try to download it via YOLO class
            model = YOLO("yolov8n-seg.pt") 
        else:
            model = YOLO(active_model)

        # Run inference
        results = model(img_path, verbose=False)
        
        if not results:
            return {"category": "ROAD_DAMAGE", "severity": "MEDIUM", "confidence": 0.0}

        # Take first result
        res = results[0]
        
        # YOLOv8-seg logic
        # If potholes detected, categorize as ROAD_DAMAGE
        if len(res.boxes) > 0:
            conf = float(res.boxes.conf[0])
            # Determine severity based on area or count (simplified)
            num_potholes = len(res.boxes)
            severity = "HIGH" if num_potholes > 3 else "MEDIUM"
            if num_potholes > 5: severity = "CRITICAL"
            
            return {
                "category": "ROAD_DAMAGE",
                "severity": severity,
                "confidence": round(conf, 4),
                "detections": num_potholes,
                "mode": "yolov8"
            }
        else:
            # No potholes detected - might be another issue or clear road
            return {
                "category": "WASTE", # Placeholder for other categories
                "severity": "LOW",
                "confidence": 0.1,
                "mode": "yolov8"
            }

    except Exception as e:
        return {
            "category": "ROAD_DAMAGE",
            "severity": "HIGH",
            "confidence": 0.5,
            "error": str(e),
            "mode": "fallback"
        }

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "No image path provided"}))
        sys.exit(1)
        
    img_path = sys.argv[1]
    result = predict(img_path)
    print(json.dumps(result))

if __name__ == "__main__":
    main()
