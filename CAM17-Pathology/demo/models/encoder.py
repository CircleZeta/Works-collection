import torch
import torch.nn as nn
import torchvision.models as models

class Encoder(nn.Module):
    def __init__(self, model_name='resnet50', pretrained=True, embedding_dim=1024):
        super(Encoder, self).__init__()
        
        if model_name == 'resnet50':
            self.base_model = models.resnet50(pretrained=pretrained)
            self.base_model = nn.Sequential(*list(self.base_model.children())[:-1])
            self.fc = nn.Linear(2048, embedding_dim)
        elif model_name == 'resnet34':
            self.base_model = models.resnet34(pretrained=pretrained)
            self.base_model = nn.Sequential(*list(self.base_model.children())[:-1])
            self.fc = nn.Linear(512, embedding_dim)
        else:
            raise ValueError(f"Unsupported model name: {model_name}")
        
        self.dropout = nn.Dropout(0.5)
        self.relu = nn.ReLU()
    
    def forward(self, x):
        batch_size, channels, height, width = x.size()
        
        features = self.base_model(x)
        features = features.view(features.size(0), -1)
        features = self.fc(features)
        features = self.relu(features)
        features = self.dropout(features)
        
        return features
    
    def get_feature_dim(self):
        return self.fc.out_features
