import openslide
import numpy as np

class WSIImageReader:
    def __init__(self, wsi_path):
        self.wsi_path = wsi_path
        self.slide = None
    
    def open(self):
        self.slide = openslide.OpenSlide(self.wsi_path)
    
    def close(self):
        if self.slide:
            self.slide.close()
    
    def get_dimensions(self, level=0):
        return self.slide.level_dimensions[level]
    
    def read_region(self, location, level, size):
        return self.slide.read_region(location, level, size)
    
    def get_downsample(self, level=0):
        return self.slide.level_downsamples[level]
    
    def get_mpp(self):
        if 'openslide.mpp-x' in self.slide.properties:
            return float(self.slide.properties['openslide.mpp-x'])
        return None
