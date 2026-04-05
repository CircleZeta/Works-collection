import torch
import torch.nn as nn

class MILAttention(nn.Module):
    def __init__(self, input_dim, hidden_dim=128):
        super(MILAttention, self).__init__()
        
        self.attention = nn.Sequential(
            nn.Linear(input_dim, hidden_dim),
            nn.Tanh(),
            nn.Linear(hidden_dim, 1)
        )
        
        self.classifier = nn.Sequential(
            nn.Linear(input_dim, 1),
            nn.Sigmoid()
        )
    
    def forward(self, bag):
        bag_size = bag.size(0)
        
        attention_scores = self.attention(bag)
        attention_scores = torch.softmax(attention_scores.view(-1), dim=0)
        
        bag_features = torch.sum(bag * attention_scores.view(-1, 1), dim=0)
        
        prediction = self.classifier(bag_features)
        
        return prediction, attention_scores, bag_features
    
    def get_attention_scores(self, bag):
        attention_scores = self.attention(bag)
        attention_scores = torch.softmax(attention_scores.view(-1), dim=0)
        
        return attention_scores
    
    def get_bag_representation(self, bag):
        attention_scores = self.get_attention_scores(bag)
        bag_features = torch.sum(bag * attention_scores.view(-1, 1), dim=0)
        
        return bag_features
