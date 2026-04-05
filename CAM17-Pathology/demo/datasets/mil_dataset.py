import torch
from torch.utils.data import Dataset
import os
import pickle

class MILDataset(Dataset):
    def __init__(self, data_dir, split='train', transform=None):
        self.data_dir = data_dir
        self.split = split
        self.transform = transform
        
        self.samples = self._load_samples()
    
    def _load_samples(self):
        split_file = os.path.join(self.data_dir, f'{self.split}_split.pkl')
        
        with open(split_file, 'rb') as f:
            samples = pickle.load(f)
        
        return samples
    
    def __len__(self):
        return len(self.samples)
    
    def __getitem__(self, idx):
        sample = self.samples[idx]
        
        bag_path = sample['bag_path']
        label = sample['label']
        
        with open(bag_path, 'rb') as f:
            bag_data = pickle.load(f)
        
        embeddings = bag_data['embeddings']
        embeddings = torch.tensor(embeddings, dtype=torch.float32)
        
        label = torch.tensor(label, dtype=torch.float32)
        
        return embeddings, label
    
    def get_bag_info(self, idx):
        sample = self.samples[idx]
        bag_path = sample['bag_path']
        
        with open(bag_path, 'rb') as f:
            bag_data = pickle.load(f)
        
        return {
            'sample_info': sample,
            'bag_data': bag_data
        }
