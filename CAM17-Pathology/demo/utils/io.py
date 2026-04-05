import torch
import os
import pickle
import numpy as np

def save_checkpoint(state, filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    torch.save(state, filename)
    print(f'Checkpoint saved to {filename}')

def load_checkpoint(filename, device):
    checkpoint = torch.load(filename, map_location=device)
    print(f'Checkpoint loaded from {filename}')
    return checkpoint

def save_pickle(obj, filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    with open(filename, 'wb') as f:
        pickle.dump(obj, f)
    print(f'Object saved to {filename}')

def load_pickle(filename):
    with open(filename, 'rb') as f:
        obj = pickle.load(f)
    print(f'Object loaded from {filename}')
    return obj

def save_bag(bag, bag_path):
    os.makedirs(os.path.dirname(bag_path), exist_ok=True)
    with open(bag_path, 'wb') as f:
        pickle.dump(bag, f)
    print(f'Bag saved to {bag_path}')

def load_bag(bag_path):
    with open(bag_path, 'rb') as f:
        bag = pickle.load(f)
    print(f'Bag loaded from {bag_path}')
    return bag

def save_attention_scores(attention_scores, positions, filename):
    data = {
        'attention_scores': attention_scores,
        'positions': positions
    }
    save_pickle(data, filename)

def load_attention_scores(filename):
    data = load_pickle(filename)
    return data['attention_scores'], data['positions']

def create_directory(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)
        print(f'Directory created: {directory}')
    return directory

def get_file_list(directory, extensions=None):
    file_list = []
    
    for root, dirs, files in os.walk(directory):
        for file in files:
            if extensions is None or any(file.endswith(ext) for ext in extensions):
                file_path = os.path.join(root, file)
                file_list.append(file_path)
    
    return sorted(file_list)
