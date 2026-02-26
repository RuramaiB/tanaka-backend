#!/usr/bin/env python3
"""
Setup ML Models and Directories
Creates the required directory structure for ML models and provides instructions.
"""

import os
import sys

def setup():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    
    models_dir = os.path.join(project_root, "ml_models")
    
    print(f"--- ML Model Setup ---")
    
    # 1. Create ml_models directory
    if not os.path.exists(models_dir):
        print(f"Creating directory: {models_dir}")
        os.makedirs(models_dir)
    else:
        print(f"Directory already exists: {models_dir}")
        
    # 2. Check for pothole_classifier.h5
    model_path = os.path.join(models_dir, "pothole_classifier.h5")
    if not os.path.exists(model_path):
        print(f"\n[!] WARNING: Model file not found: {model_path}")
        print(f"To use the AI classification feature, you need to:")
        print(f"1. Download or train a pothole classification model (Keras/TensorFlow .h5 format).")
        print(f"2. Place it at: {model_path}")
        print(f"\nTip: You can find relevant datasets on Kaggle:")
        print(f"https://www.kaggle.com/datasets/farzadnekouei/pothole-segmentation-for-road-damage-assessment")
    else:
        print(f"\n[OK] Model file found: {model_path}")

    # 3. Verify requirements
    requirements_path = os.path.join(script_dir, "requirements.txt")
    if os.path.exists(requirements_path):
        print(f"\nDon't forget to install dependencies:")
        print(f"pip install -r ml_scripts/requirements.txt")

    print(f"\nSetup check complete.")

if __name__ == "__main__":
    setup()
