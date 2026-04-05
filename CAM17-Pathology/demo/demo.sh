#!/bin/bash

echo "=== CAMELYON17 MIL DEMO ==="
echo "1. Setting up environment..."
python setup_env.py

echo -e "\n2. Running complete demo..."
python run_demo.py

echo -e "\n3. Demo completed successfully!"
echo "Results are available in:"
echo "- checkpoints/best_model.pth (trained model)"
echo "- visualizations/ (attention visualizations)"
echo "- data/camelyon17_processed/ (processed data)"
