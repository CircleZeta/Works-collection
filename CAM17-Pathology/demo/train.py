import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader
from sklearn.metrics import roc_auc_score, accuracy_score
import numpy as np

from datasets.mil_dataset import MILDataset
from models.mil_attention import MILAttention
from utils.io import save_checkpoint, load_checkpoint

def train_epoch(model, dataloader, criterion, optimizer, device):
    model.train()
    
    epoch_loss = 0.0
    all_predictions = []
    all_labels = []
    
    for batch_idx, (bag_embeddings, labels) in enumerate(dataloader):
        # Since batch_size=1, we squeeze the batch dimension
        bag_embeddings = bag_embeddings.squeeze(0).to(device)
        labels = labels.to(device)
        
        optimizer.zero_grad()
        
        # Forward pass
        pred, _, _ = model(bag_embeddings)
        
        loss = criterion(pred, labels)
        loss.backward()
        optimizer.step()
        
        epoch_loss += loss.item()
        
        all_predictions.append(pred.cpu().detach().numpy()[0])
        all_labels.append(labels.cpu().numpy()[0])
        
        if batch_idx % 10 == 0:
            print(f'Batch {batch_idx}/{len(dataloader)}, Loss: {loss.item():.4f}')
    
    epoch_loss /= len(dataloader)
    
    all_predictions_np = np.array(all_predictions)
    all_labels_np = np.array(all_labels)
    
    predicted = (all_predictions_np > 0.5).astype(float)
    accuracy = accuracy_score(all_labels_np, predicted)
    
    if len(set(all_labels_np)) > 1:
        auc = roc_auc_score(all_labels_np, all_predictions_np)
    else:
        auc = 0.0
    
    return epoch_loss, accuracy, auc

def evaluate(model, dataloader, criterion, device):
    model.eval()
    
    epoch_loss = 0.0
    all_predictions = []
    all_labels = []
    
    with torch.no_grad():
        for batch_idx, (bag_embeddings, labels) in enumerate(dataloader):
            # Since batch_size=1, we squeeze the batch dimension
            bag_embeddings = bag_embeddings.squeeze(0).to(device)
            labels = labels.to(device)
            
            # Forward pass
            pred, _, _ = model(bag_embeddings)
            
            loss = criterion(pred, labels)
            epoch_loss += loss.item()
            
            all_predictions.append(pred.cpu().numpy()[0])
            all_labels.append(labels.cpu().numpy()[0])
    
    epoch_loss /= len(dataloader)
    
    all_predictions_np = np.array(all_predictions)
    all_labels_np = np.array(all_labels)
    
    predicted = (all_predictions_np > 0.5).astype(float)
    accuracy = accuracy_score(all_labels_np, predicted)
    
    if len(set(all_labels_np)) > 1:
        auc = roc_auc_score(all_labels_np, all_predictions_np)
    else:
        auc = 0.0
    
    return epoch_loss, accuracy, auc

def main():
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    
    data_dir = 'data/camelyon17_processed'
    
    train_dataset = MILDataset(data_dir=data_dir, split='train')
    train_dataloader = DataLoader(train_dataset, batch_size=1, shuffle=True, num_workers=0)
    
    val_dataset = MILDataset(data_dir=data_dir, split='val')
    val_dataloader = DataLoader(val_dataset, batch_size=1, shuffle=False, num_workers=0)
    
    mil_model = MILAttention(input_dim=1024, hidden_dim=128)
    mil_model = mil_model.to(device)
    
    criterion = nn.BCELoss()
    
    optimizer = optim.Adam(
        mil_model.parameters(),
        lr=1e-4, weight_decay=1e-5
    )
    
    scheduler = optim.lr_scheduler.ReduceLROnPlateau(optimizer, mode='min', factor=0.1, patience=3)
    
    num_epochs = 50
    best_val_auc = 0.0
    
    for epoch in range(num_epochs):
        print(f'Epoch {epoch+1}/{num_epochs}')
        print('-' * 20)
        
        train_loss, train_acc, train_auc = train_epoch(mil_model, train_dataloader, criterion, optimizer, device)
        val_loss, val_acc, val_auc = evaluate(mil_model, val_dataloader, criterion, device)
        
        scheduler.step(val_loss)
        
        print(f'Train Loss: {train_loss:.4f}, Train Acc: {train_acc:.4f}, Train AUC: {train_auc:.4f}')
        print(f'Val Loss: {val_loss:.4f}, Val Acc: {val_acc:.4f}, Val AUC: {val_auc:.4f}')
        print()
        
        if val_auc > best_val_auc:
            best_val_auc = val_auc
            save_checkpoint({
                'epoch': epoch,
                'model_state_dict': mil_model.state_dict(),
                'optimizer_state_dict': optimizer.state_dict(),
                'val_loss': val_loss,
                'val_auc': val_auc
            }, 'checkpoints/best_model.pth')
            print('Saved best model!')
    
    print('Training completed!')
    print(f'Best validation AUC: {best_val_auc:.4f}')

if __name__ == '__main__':
    main()
