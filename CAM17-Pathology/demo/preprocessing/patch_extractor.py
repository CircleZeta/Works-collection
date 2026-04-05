import numpy as np
from PIL import Image

class PatchExtractor:
    def __init__(self, patch_size=256, stride=256):
        self.patch_size = patch_size
        self.stride = stride
    
    def extract_patches(self, image, level=0):
        patches = []
        positions = []
        
        width, height = image.size
        
        for y in range(0, height - self.patch_size + 1, self.stride):
            for x in range(0, width - self.patch_size + 1, self.stride):
                patch = image.crop((x, y, x + self.patch_size, y + self.patch_size))
                patches.append(patch)
                positions.append((x, y))
        
        return patches, positions
    
    def filter_background_patches(self, patches, positions, threshold=0.95):
        filtered_patches = []
        filtered_positions = []
        
        for patch, pos in zip(patches, positions):
            np_patch = np.array(patch)
            
            if len(np_patch.shape) == 3 and np_patch.shape[2] == 4:
                np_patch = np_patch[:, :, :3]
            
            grayscale = np.mean(np_patch, axis=2)
            white_ratio = np.sum(grayscale > 200) / (self.patch_size * self.patch_size)
            
            if white_ratio < threshold:
                filtered_patches.append(patch)
                filtered_positions.append(pos)
        
        return filtered_patches, filtered_positions
    
    def preprocess_patch(self, patch):
        patch = patch.resize((self.patch_size, self.patch_size))
        np_patch = np.array(patch)
        
        if np_patch.ndim == 3 and np_patch.shape[2] == 4:
            np_patch = np_patch[:, :, :3]
        
        np_patch = np_patch / 255.0
        np_patch = np_patch.transpose(2, 0, 1)
        
        return np_patch
