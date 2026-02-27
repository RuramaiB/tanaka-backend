#!/usr/bin/env python3
"""
YOLOv8 Training Script for Pothole Segmentation
Downloads dataset from Kaggle and fine-tunes YOLOv8-seg.
"""

import os
import sys
import yaml
from ultralytics import YOLO

def train():
    # 1. Setup paths
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    models_dir = os.path.join(project_root, "ml_models")
    data_dir = os.path.join(project_root, "datasets", "potholes")
    
    if not os.path.exists(models_dir):
        os.makedirs(models_dir)
        
    # 2. Download dataset if not exists
    # Requires KAGGLE_USERNAME and KAGGLE_KEY environment variables
    dataset_id = "farzadnekouei/pothole-image-segmentation-dataset"
    if not os.path.exists(data_dir):
        print(f"--- Downloading dataset: {dataset_id} ---")
        try:
            import kaggle
            kaggle.api.authenticate()
            kaggle.api.dataset_download_files(dataset_id, path=data_dir, unzip=True)
            print("[OK] Dataset downloaded.\n")
        except ImportError:
            print("[ERROR] Kaggle library not found. Please install via requirements.txt")
            return
        except Exception as e:
            print(f"[ERROR] Failed to download dataset: {e}")
            print("[TIP] Ensure KAGGLE_USERNAME and KAGGLE_KEY are set.")
            return

    # 3. Load YOLOv8-seg model (nano version for speed)
    print("--- Loading YOLOv8-seg ---")
    model = YOLO("yolov8n-seg.pt")

    # 4. Train model
    # Note: data.yaml path needs to be absolute for YOLO
    yaml_path = os.path.join(data_dir, "data.yaml")
    
    print(f"--- Starting Training with {yaml_path} ---")
    results = model.train(
        data=yaml_path,
        epochs=50, # Reduced for demo, original used 150
        imgsz=640,
        batch=16,
        patience=10,
        device='cpu', # Force CPU if no GPU
        project="pothole_training",
        name="seg_run"
    )

    # 5. Move best weights to models_dir
    best_weights = os.path.join("pothole_training", "seg_run", "weights", "best.pt")
    target_path = os.path.join(models_dir, "pothole_seg_best.pt")
    
    if os.path.exists(best_weights):
        import shutil
        shutil.copy(best_weights, target_path)
        print(f"[OK] Training complete. Model saved to {target_path}")
    else:
        print("[ERROR] best.pt not found after training.")

if __name__ == "__main__":
    train()
