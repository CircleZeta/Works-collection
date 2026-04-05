import torch
import matplotlib.pyplot as plt
import numpy as np
from PIL import Image
import os
import pickle
import glob

from models.mil_attention import MILAttention
from utils.io import load_checkpoint, load_pickle

def visualize_attention(bag_path, mil_model, device, output_dir):
    bag_data = load_pickle(bag_path)
    
    patch_embeddings = bag_data['embeddings']
    positions = bag_data['positions']
    patch_size = bag_data['patch_size']
    wsi_dimensions = bag_data['wsi_dimensions']
    slide_id = bag_data['slide_id']
    
    patch_embeddings = torch.tensor(patch_embeddings, dtype=torch.float32).to(device)
    
    with torch.no_grad():
        prediction, attention_scores, _ = mil_model(patch_embeddings)
    
    attention_scores = attention_scores.cpu().numpy()
    prediction = prediction.cpu().numpy()[0]
    
    print(f'Slide {slide_id} prediction: {prediction:.4f}')
    print(f'Attention scores range: {attention_scores.min():.4f} to {attention_scores.max():.4f}')
    
    # Visualize top attention patches with dummy images
    plt.figure(figsize=(12, 8))
    
    top_k = min(25, len(patch_embeddings))
    top_indices = np.argsort(attention_scores)[-top_k:][::-1]
    
    cols = min(5, top_k)
    rows = (top_k + cols - 1) // cols
    
    for i, idx in enumerate(top_indices):
        plt.subplot(rows, cols, i + 1)
        # Create a dummy patch with color based on attention score
        dummy_patch = np.zeros((100, 100, 3))
        dummy_patch[:, :, 0] = attention_scores[idx]  # Red channel
        dummy_patch[:, :, 1] = 0.5  # Green channel
        dummy_patch[:, :, 2] = 1.0 - attention_scores[idx]  # Blue channel
        plt.imshow(dummy_patch)
        plt.title(f'Attn: {attention_scores[idx]:.3f}')
        plt.axis('off')
    
    plt.tight_layout()
    output_path = os.path.join(output_dir, f'{slide_id}_top_attention.png')
    plt.savefig(output_path, dpi=300, bbox_inches='tight')
    plt.close()
    
    return attention_scores, bag_data

def create_heatmap(attention_scores, bag_data, output_path):
    positions = bag_data['positions']
    patch_size = bag_data['patch_size']
    wsi_dimensions = bag_data['wsi_dimensions']
    
    # Scale down for faster processing
    scale_factor = 0.1
    scaled_wsi_dimensions = (int(wsi_dimensions[0] * scale_factor), int(wsi_dimensions[1] * scale_factor))
    scaled_patch_size = int(patch_size * scale_factor)
    
    heatmap = np.zeros((scaled_wsi_dimensions[1], scaled_wsi_dimensions[0]))
    
    for (x, y), score in zip(positions, attention_scores):
        scaled_x = int(x * scale_factor)
        scaled_y = int(y * scale_factor)
        if scaled_y + scaled_patch_size <= heatmap.shape[0] and scaled_x + scaled_patch_size <= heatmap.shape[1]:
            heatmap[scaled_y:scaled_y+scaled_patch_size, scaled_x:scaled_x+scaled_patch_size] += score
    
    plt.figure(figsize=(12, 10))
    plt.imshow(heatmap, cmap='jet')
    plt.colorbar(label='Attention Score', fraction=0.046, pad=0.04)
    plt.title(f'Attention Heatmap - Slide {bag_data["slide_id"]}')
    plt.axis('off')
    
    plt.savefig(output_path, dpi=300, bbox_inches='tight')
    plt.close()

def create_simple_overlay(heatmap, bag_data, output_path):
    # Create a simple overlay visualization without requiring actual WSI
    plt.figure(figsize=(12, 10))
    
    # Create a dummy WSI background
    background = np.ones((heatmap.shape[0], heatmap.shape[1], 3)) * 0.8
    
    # Normalize heatmap for overlay
    normalized_heatmap = (heatmap - heatmap.min()) / (heatmap.max() - heatmap.min())
    
    # Apply colormap to heatmap
    cmap = plt.get_cmap('jet')
    heatmap_colored = cmap(normalized_heatmap)[:, :, :3]
    
    # Overlay heatmap on background
    overlay = 0.7 * background + 0.3 * heatmap_colored
    
    plt.imshow(overlay)
    plt.colorbar(label='Attention Score', fraction=0.046, pad=0.04)
    plt.title(f'Attention Overlay - Slide {bag_data["slide_id"]}')
    plt.axis('off')
    
    plt.savefig(output_path, dpi=300, bbox_inches='tight')
    plt.close()

def main():
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    
    mil_model = MILAttention(input_dim=1024, hidden_dim=128)
    
    # Load checkpoint if it exists, otherwise use random weights for testing
    checkpoint_path = 'checkpoints/best_model.pth'
    if os.path.exists(checkpoint_path):
        checkpoint = load_checkpoint(checkpoint_path, device)
        mil_model.load_state_dict(checkpoint['model_state_dict'])
    else:
        print('Warning: No checkpoint found, using random weights')
    
    mil_model = mil_model.to(device)
    mil_model.eval()
    
    # Find any sample bag
    bag_paths = glob.glob('data/camelyon17_processed/*_bag.pkl')
    if not bag_paths:
        print('No bag files found!')
        return
    
    bag_path = bag_paths[0]
    output_dir = 'visualizations'
    os.makedirs(output_dir, exist_ok=True)
    
    print('Visualizing attention...')
    attention_scores, bag_data = visualize_attention(bag_path, mil_model, device, output_dir)
    
    print('Creating heatmap...')
    heatmap_path = os.path.join(output_dir, f'{bag_data["slide_id"]}_heatmap.png')
    create_heatmap(attention_scores, bag_data, heatmap_path)
    
    print('Creating simple overlay...')
    # Create heatmap again for overlay
    positions = bag_data['positions']
    patch_size = bag_data['patch_size']
    wsi_dimensions = bag_data['wsi_dimensions']
    scale_factor = 0.1
    scaled_wsi_dimensions = (int(wsi_dimensions[0] * scale_factor), int(wsi_dimensions[1] * scale_factor))
    scaled_patch_size = int(patch_size * scale_factor)
    
    heatmap = np.zeros((scaled_wsi_dimensions[1], scaled_wsi_dimensions[0]))
    for (x, y), score in zip(positions, attention_scores):
        scaled_x = int(x * scale_factor)
        scaled_y = int(y * scale_factor)
        if scaled_y + scaled_patch_size <= heatmap.shape[0] and scaled_x + scaled_patch_size <= heatmap.shape[1]:
            heatmap[scaled_y:scaled_y+scaled_patch_size, scaled_x:scaled_x+scaled_patch_size] += score
    
    overlay_path = os.path.join(output_dir, f'{bag_data["slide_id"]}_overlay.png')
    create_simple_overlay(heatmap, bag_data, overlay_path)
    
    print(f'Visualization completed. Results saved to {output_dir}')
    print(f'Slide ID: {bag_data["slide_id"]}')
    print(f'Attention scores: {attention_scores[:5]}...')

if __name__ == '__main__':
    main()
