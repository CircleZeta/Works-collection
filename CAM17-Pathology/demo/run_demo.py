import os
import sys
import subprocess
import pickle
import numpy as np
import torch

from datasets.mil_dataset import MILDataset
from models.mil_attention import MILAttention
from utils.io import save_pickle

def run_command(cmd):
    print(f'\n=== Running: {cmd} ===')
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    print(result.stdout)
    if result.stderr:
        print(f'Stderr: {result.stderr}')
    if result.returncode != 0:
        print(f'Error: Command failed with exit code {result.returncode}')
        return False
    return True

def generate_sample_data():
    print('\n=== Generating sample data for testing ===')
    
    sample_dir = 'data/camelyon17_processed'
    os.makedirs(sample_dir, exist_ok=True)
    
    # Create sample bags
    bags = []
    for i in range(20):
        num_patches = np.random.randint(50, 200)
        embeddings = np.random.randn(num_patches, 1024).astype(np.float32)
        positions = [(j*256, k*256) for j, k in zip(range(num_patches), range(num_patches))]
        
        bag_data = {
            'slide_id': f'sample_{i}',
            'wsi_path': f'dummy_path_{i}.tif',
            'label': i % 2,  # Binary labels
            'embeddings': embeddings,
            'positions': positions,
            'patch_size': 256,
            'level': 0,
            'wsi_dimensions': (10000, 10000)
        }
        
        bag_path = os.path.join(sample_dir, f'sample_{i}_bag.pkl')
        save_pickle(bag_data, bag_path)
        bags.append(bag_data)
    
    # Create split files
    split = {
        'train': bags[:12],
        'val': bags[12:16],
        'test': bags[16:]
    }
    
    for split_name, split_bags in split.items():
        split_data = []
        for bag in split_bags:
            split_data.append({
                'bag_path': os.path.join(sample_dir, f'{bag["slide_id"]}_bag.pkl'),
                'label': bag['label'],
                'slide_id': bag['slide_id']
            })
        
        split_path = os.path.join(sample_dir, f'{split_name}_split.pkl')
        save_pickle(split_data, split_path)
    
    print(f'Generated {len(bags)} sample bags')
    return True

def setup_environment():
    print('=== Setting up environment ===')
    return run_command('python setup_env.py')

def prepare_data():
    print('=== Checking if data is already prepared ===')
    
    # Check if sample data already exists
    sample_dir = 'data/camelyon17_processed'
    if os.path.exists(os.path.join(sample_dir, 'train_split.pkl')):
        print('Sample data already exists, skipping data preparation')
        return True
    
    # Check if CAMELYON17 data is provided
    camelyon_data_dir = 'data/camelyon17'
    label_file = 'data/camelyon17/labels.csv'
    
    if os.path.exists(camelyon_data_dir) and os.path.exists(label_file):
        print('CAMELYON17 data found, starting preparation')
        return run_command('python prepare_camelyon17.py --data_dir data/camelyon17 --label_file data/camelyon17/labels.csv --output_dir data/camelyon17_processed --num_slides 10')
    else:
        print('CAMELYON17 data not found, generating sample data')
        return generate_sample_data()

def train_model():
    print('=== Training MIL model ===')
    return run_command('python train.py')

def visualize_attention():
    print('=== Visualizing attention ===')
    return run_command('python visualize_attention.py')

def main():
    print('=== CAMELYON17 MIL DEMO Runner ===')
    
    steps = [
        ('Setup Environment', setup_environment),
        ('Prepare Data', prepare_data),
        ('Train Model', train_model),
        ('Visualize Attention', visualize_attention)
    ]
    
    all_passed = True
    for step_name, step_func in steps:
        if not step_func():
            all_passed = False
            print(f'\nError: {step_name} failed')
            break
    
    if all_passed:
        print('\n=== DEMO completed successfully! ===')
        print('Results:')
        print('- Model weights: checkpoints/best_model.pth')
        print('- Visualizations: visualizations/ directory')
        print('- Training logs: printed to console')
    else:
        print('\n=== DEMO failed! ===')
        sys.exit(1)

if __name__ == '__main__':
    main()
