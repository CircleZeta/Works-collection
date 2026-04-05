import os
import argparse
import pickle
import numpy as np
from PIL import Image
import torch
from torchvision import transforms

from preprocessing.wsi_reader import WSIImageReader
from preprocessing.patch_extractor import PatchExtractor
from models.encoder import Encoder
from utils.io import save_pickle, create_directory

def get_wsi_files(data_dir):
    wsi_files = []
    for root, dirs, files in os.walk(data_dir):
        for file in files:
            if file.endswith('.tif'):
                wsi_files.append(os.path.join(root, file))
    return wsi_files

def get_slide_label(wsi_path, label_file):
    with open(label_file, 'r') as f:
        labels = f.readlines()
    
    slide_id = os.path.basename(wsi_path).replace('.tif', '')
    for line in labels:
        if slide_id in line:
            return int(line.strip().split(',')[1])
    return -1

def process_wsi(wsi_path, label, encoder, patch_extractor, transform, output_dir, device, target_mpp=0.25):
    print(f'Processing {wsi_path}')
    
    reader = WSIImageReader(wsi_path)
    reader.open()
    
    try:
        actual_mpp = reader.get_mpp()
        if actual_mpp is None:
            print(f'Warning: MPP not found for {wsi_path}, using default level 0')
            target_level = 0
        else:
            scale_factor = actual_mpp / target_mpp
            
            for level in range(len(reader.slide.level_downsamples)):
                downsample = reader.slide.level_downsamples[level]
                if abs(downsample - scale_factor) < 0.1:
                    target_level = level
                    break
            else:
                target_level = 0
        
        print(f'Using level {target_level} for 20X magnification')
        
        wsi_dimensions = reader.get_dimensions(level=target_level)
        print(f'WSI dimensions at level {target_level}: {wsi_dimensions}')
        
        region = reader.read_region((0, 0), target_level, wsi_dimensions)
        region = region.convert('RGB')
        
        patches, positions = patch_extractor.extract_patches(region, level=target_level)
        print(f'Extracted {len(patches)} patches')
        
        filtered_patches, filtered_positions = patch_extractor.filter_background_patches(patches, positions)
        print(f'Filtered to {len(filtered_patches)} patches after background removal')
        
        if len(filtered_patches) == 0:
            print(f'Warning: No patches left after filtering for {wsi_path}')
            return None
        
        patch_embeddings = []
        for i, patch in enumerate(filtered_patches):
            if i % 100 == 0:
                print(f'Processing patch {i+1}/{len(filtered_patches)}')
            
            transformed_patch = transform(patch)
            transformed_patch = transformed_patch.unsqueeze(0).to(device)
            
            with torch.no_grad():
                embedding = encoder(transformed_patch)
            
            patch_embeddings.append(embedding.squeeze().cpu().numpy())
        
        patch_embeddings = np.array(patch_embeddings)
        
        slide_id = os.path.basename(wsi_path).replace('.tif', '')
        
        bag_data = {
            'slide_id': slide_id,
            'wsi_path': wsi_path,
            'label': label,
            'embeddings': patch_embeddings,
            'positions': filtered_positions,
            'patch_size': patch_extractor.patch_size,
            'level': target_level,
            'wsi_dimensions': wsi_dimensions
        }
        
        bag_path = os.path.join(output_dir, f'{slide_id}_bag.pkl')
        save_pickle(bag_data, bag_path)
        
        return bag_data
        
    finally:
        reader.close()

def main():
    parser = argparse.ArgumentParser(description='Prepare CAMELYON17 dataset for MIL training')
    parser.add_argument('--data_dir', type=str, required=True, help='Path to CAMELYON17 dataset directory')
    parser.add_argument('--label_file', type=str, required=True, help='Path to slide-level label file')
    parser.add_argument('--output_dir', type=str, default='data/camelyon17_processed', help='Output directory for processed bags')
    parser.add_argument('--num_slides', type=int, default=50, help='Number of slides to process (subset)')
    parser.add_argument('--patch_size', type=int, default=256, help='Patch size in pixels')
    parser.add_argument('--stride', type=int, default=256, help='Stride between patches')
    parser.add_argument('--model_name', type=str, default='resnet50', help='Encoder model name')
    
    args = parser.parse_args()
    
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    
    create_directory(args.output_dir)
    
    encoder = Encoder(model_name=args.model_name, pretrained=True, embedding_dim=1024)
    encoder = encoder.to(device)
    encoder.eval()
    
    patch_extractor = PatchExtractor(patch_size=args.patch_size, stride=args.stride)
    
    transform = transforms.Compose([
        transforms.Resize((224, 224)),
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
    ])
    
    wsi_files = get_wsi_files(args.data_dir)
    print(f'Found {len(wsi_files)} WSI files')
    
    selected_files = wsi_files[:args.num_slides]
    print(f'Selected {len(selected_files)} slides for processing')
    
    bags = []
    for wsi_path in selected_files:
        label = get_slide_label(wsi_path, args.label_file)
        if label == -1:
            print(f'Skipping {wsi_path} - label not found')
            continue
        
        bag_data = process_wsi(wsi_path, label, encoder, patch_extractor, transform, args.output_dir, device)
        if bag_data:
            bags.append(bag_data)
    
    print(f'Processed {len(bags)} slides successfully')
    
    split = {
        'train': bags[:int(0.7 * len(bags))],
        'val': bags[int(0.7 * len(bags)):int(0.9 * len(bags))],
        'test': bags[int(0.9 * len(bags)):]
    }
    
    for split_name, split_bags in split.items():
        split_data = []
        for bag in split_bags:
            split_data.append({
                'bag_path': os.path.join(args.output_dir, f'{bag["slide_id"]}_bag.pkl'),
                'label': bag['label'],
                'slide_id': bag['slide_id']
            })
        
        split_path = os.path.join(args.output_dir, f'{split_name}_split.pkl')
        save_pickle(split_data, split_path)
        print(f'Saved {split_name} split with {len(split_data)} bags')

if __name__ == '__main__':
    main()
