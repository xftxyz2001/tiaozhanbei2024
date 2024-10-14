import torch
import torch.nn as nn
from torch.utils.data import Dataset, ConcatDataset, DataLoader

import argparse
import os
from tqdm import tqdm, trange

from model_1demon import TTpretrainModel
from data import   trim_collate, VQAData ,trim_collate_no
import sys
from torch.optim import lr_scheduler
import torch.multiprocessing as mp

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--epoch",type=int,default=1,help="epoch of training")
    parser.add_argument("--lr",type=float,default=4e-5,help="learning rate during training")
    parser.add_argument("--cuda",type=bool,default=True,help="whether use gpu")
    parser.add_argument("--gpu",type=int,default=1,help="whether use gpu")
#     parser.add_argument("--load",type=str,default='../mmirtt/pretrain/output/epoch2004.pth')
    parser.add_argument("--load",type=str,default='./output/epoch1615.pth')
    # Only for Trilinear Transformer
    parser.add_argument("--num_layers",type=int,default=6, help="number of layers in Trilinear Transformer")
    parser.add_argument("--ff_dim",type=int,default=3072, help="dims of Position Wise Feed Forward layer in Trilinear Transformer")
    parser.add_argument("--dropout",type=float,default=0.1, help="dropout rate in Trilinear Transformer")
    
    # Initial
    parser.add_argument("--batch_size",type=int,default=1,help="batch size during training")
    parser.add_argument("--qst_len",type=int,default=12,help="max length of question")
    parser.add_argument("--ans_len",type=int,default=6,help="max length of answer")
    parser.add_argument("--seed",type=int,default=2450,help="seed of random")
    parser.add_argument("--step_length",type=int,default=10,help="seed of random")
    parser.add_argument("--output", type=str, default='./output/', help="dir of output")
    parser.add_argument("--dy_step_size",type=int,default=800,help="动态学习率变化步长")
    parser.add_argument("--dy_step_gamma", type=int, default=0.9, help="动态学习率变化倍数")
    return parser.parse_args()


if __name__ == '__main__':
    # torch.multiprocessing.set_start_method('spawn')
    config=parse_args()
    max_length = [config.qst_len, config.ans_len]
    vqa2_dset = VQAData(maxlen=max_length)
    device = torch.device('cuda:0')

    # device = torch.device("cpu")
    if config.seed is not None:
        # random.seed(config.seed)
        torch.manual_seed(config.seed)  # 为CPU设置种子用于生成随机数，以使得结果是确定的   　　
        torch.cuda.manual_seed(config.seed)  # 为当前GPU设置随机种子；
        torch.backends.cudnn.deterministic = True

    model = TTpretrainModel(num_layers=config.num_layers, ff_dim=config.ff_dim, dropout=config.dropout,v_dim=768,hid_vim=1024)
#     少一个模型加载
    ckpt = torch.load(config.load)

    model.load_state_dict(ckpt)
    
    
    model.to(device)
    optimizer=torch.optim.Adadelta(model.parameters(), lr=1e-3, rho=0.9, eps=1e-06, weight_decay=0)
#     optimizer=torch.optim.SGD(model.parameters(),lr=config.lr,weight_decay=5e-06)
    
    """
    动态学习率
    参数1，指定使用的优化器
    参数2，mode，可选择‘min’（min表示当监控量停止下降的时候，学习率将减小）或者‘max’（max表示当监控量停止上升的时候，学习率将减小）
    参数3，factor，代表学习率每次降低多少
    参数4，patience，容忍网路的性能不提升的次数，高于这个次数就降低学习率
    参数5，min_lr，学习率的下限
    """
    scheduler = lr_scheduler.ReduceLROnPlateau(optimizer, mode='min', factor=config.dy_step_gamma, patience=10, min_lr=config.lr/10)

    #
    model.train()
    trainloader = DataLoader(vqa2_dset, 4, num_workers=0, collate_fn=trim_collate_no,shuffle=True)
    total_loss=0
    epoch=1616
    turn=1055
    de=0
    # for index,batch in tqdm(trainloader):
    # for index,(question,question_emb,question_re_emb , feats,multiple_choices, answer_list,answer_emb,answer_choice) in tqdm(enumerate(trainloader), total=len(trainloader), leave = True):
    loop = tqdm(enumerate(trainloader), total=len(trainloader))
    max=0
    for batch_idx,batch in loop:
        optimizer.zero_grad()#优化器进行清零，防止上次计算结果对这次计算产生影响
        


        # print(question,question_emb,question_re_emb)
        question, question_emb, text, text_emb, feats, multiple_choices, multiple_choices_emb, answer=batch['question'],batch['question_emb'],batch['text'],batch['text_emb'],batch['feats'],batch['multiple_choices'],batch['multiple_choices_emb'],batch['answer']
#         print('modal pretrain.py',modal)
        
        loss=model(question_emb, multiple_choices_emb , text_emb,feats,multiple_choices,answer,0.4)
        # print("loss结束一次")
#         print("loss",loss[0])
        loss[0].backward()
        scheduler.step(loss[0])#更新学习率
        optimizer.step()#更新网络中指定的需要优化的参数
        
#         max=0;
#         # print(loss[1].size(),max,answer_choice,)
#         for i in range(64):
#             if loss[1][i]>loss[1][max]:
#                 max=i;
        
#         print(max,answer_choice)
        
        total_loss += loss[0].item()

#         print(loss[1])
#         print("Download progress: {}%: ".format((ex*100)/10),"loss",loss[0])
        if batch_idx>=turn:break
    print("total_loss",total_loss/(3000-de))
    torch.save(model.state_dict(), os.path.join(config.output, 'epoch%d.pth' % epoch))
 
