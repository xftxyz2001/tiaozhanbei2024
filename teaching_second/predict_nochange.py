import torch

from torch.utils.data import DataLoader
from data import VQAData, VQAData_predict, trim_collate_no
import numpy as np
import argparse
import matplotlib.animation as animation
import seaborn as sns
from model_1demon_nochange import TTpretrainModel
import sys
import matplotlib.pyplot as plt
from tqdm import tqdm, trange
from sklearn.decomposition import PCA
import pandas as pd


def update(num):
    ax.clear()
    ax.scatter(x, y, z)
    ax.view_init(elev=num, azim=num)


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--epoch", type=int, default=1, help="epoch of training")
    parser.add_argument("--lr", type=float, default=3e-5, help="learning rate during training")
    parser.add_argument("--cuda", type=bool, default=True, help="whether use gpu")
    parser.add_argument("--gpu", type=int, default=1, help="whether use gpu")
    parser.add_argument("--load", type=str, default='./output/epoch20.pth')

    # Only for Trilinear Transformer
    parser.add_argument("--num_layers", type=int, default=6, help="number of layers in Trilinear Transformer")
    parser.add_argument("--ff_dim", type=int, default=3072,
                        help="dims of Position Wise Feed Forward layer in Trilinear Transformer")
    parser.add_argument("--dropout", type=float, default=0.1, help="dropout rate in Trilinear Transformer")

    # Initial
    parser.add_argument("--batch_size", type=int, default=1, help="batch size during training")
    parser.add_argument("--qst_len", type=int, default=12, help="max length of question")
    parser.add_argument("--ans_len", type=int, default=6, help="max length of answer")
    parser.add_argument("--seed", type=int, default=2450, help="seed of random")

    parser.add_argument("--output", type=str, default='./output/', help="dir of output")
    return parser.parse_args()


if __name__ == '__main__':

    config = parse_args()
    max_length = [config.qst_len, config.ans_len]
    vqa2_dset = VQAData_predict(maxlen=max_length)
    device = torch.device('cuda:0')

    # device = torch.device("cpu")
    if config.seed is not None:
        # random.seed(config.seed)
        torch.manual_seed(config.seed)  # 为CPU设置种子用于生成随机数，以使得结果是确定的   　　
        torch.cuda.manual_seed(config.seed)  # 为当前GPU设置随机种子；
        torch.backends.cudnn.deterministic = True

    model = TTpretrainModel(num_layers=config.num_layers, ff_dim=config.ff_dim, dropout=config.dropout, v_dim=768,
                            hid_vim=1024)
    # 少一个模型加载
    ckpt = torch.load(config.load)
    model.load_state_dict(ckpt)
    model.to(device)
    #
    model.train()
    trainloader = DataLoader(vqa2_dset, 1, num_workers=0, collate_fn=trim_collate_no, shuffle=True)
    total_loss = 0
    epoch = 10
    turn = 258
    correct = 0;
    max = 0;
    de = 0;
    loop = tqdm(enumerate(trainloader), total=len(trainloader))
    score_cor = 0;

    result_img = [];
    result_sc=[]
    result_sentence = [];
    result_scs = [];
    curr = 0;

    correct_count = 0
    correct_correct = 0
    correct_predict = 0;
    false_count = 0;
    false_false = 0
    false_predict = 0;
    for batch_idx, batch in loop:
        # print(batch);
        question, question_emb, text, text_emb, feats, multiple_choices, multiple_choices_emb, answer = batch[
                                                                                                            'question'], \
                                                                                                        batch[
                                                                                                            'question_emb'], \
                                                                                                        batch['text'], \
                                                                                                        batch[
                                                                                                            'text_emb'], \
                                                                                                        batch['feats'], \
                                                                                                        batch[
                                                                                                            'multiple_choices'], \
                                                                                                        batch[
                                                                                                            'multiple_choices_emb'], \
                                                                                                        batch['answer']
        #         print('modal pretrain.py',modal)
        loss = model(question_emb, multiple_choices_emb, text_emb, feats, multiple_choices, answer, 0.5)
        if answer==3
            conti=conti+1
        
        #         print(loss[6])
        sum = 0;
        sum_c = 0;
        # print(loss[1].size(),max,answer_choice,)
        
        temp_c=torch.zeros(1);
        temp_c[0]=loss[5]
        if temp_c - answer < 0.5 and temp_c - answer >-0.5:
            correct = correct + 1;
        if temp_c >= 3:
            correct_predict = correct_predict + 1
        if answer >= 3:
            correct_count = correct_count + 1
            if temp_c >= 3:
                correct_correct = correct_correct + 1
        if temp_c < 3:
            false_predict = false_predict + 1
        if answer < 3:
            false_count = false_count + 1
            if temp_c < 3:
                false_false = false_false + 1
        sum = sum + temp_c;
        sum_c = sum_c + answer
#         print(temp_c,answer)
#         print(sum,sum_c)
        #         print("sum_c",sum_c)
        if sum - sum_c < 1 and sum - sum_c >-1:
            score_cor = score_cor + 1;
#         curr = 0;
#         if batch_idx >= 0:
#             temp = np.zeros(768);
#             temp1=np.zeros(768);
#             temp_scs = np.zeros(5);
#             for i in range(1):
#                 temp_scs[i] = answer
#             for i in range(768):
#                 temp[i] = loss[6][0][curr];
#                 temp1[i]=loss[7][0][curr];
#                 curr = curr + 1;

#             result_sc.append(sum_c)
#             result_scs.append(temp_scs)
#             result_img.append(temp)
#             result_sentence.append(temp1)

#         #         print(loss[5],answer)
        if batch_idx >= turn: break

    #     for i in range(20):
    #         plt.gcf().clear()
    #         ax = sns.heatmap(result_img[i]);
    #         figure = ax.get_figure()
    #         figure.savefig('sns_heatmap_%d.jpg'%i)


#     pca = PCA(n_components=3)
#     X_pca = pca.fit_transform(result_img)
#     y_pic_sence = np.zeros(len(X_pca))

    # 可视化降维后的数据

#     y = np.zeros(len(X_pca))
#     for i in range(len(X_pca)):
#         #         if result_sc[i]>=14:
#         #             y[i]=1;
#         if result_scs[i][0] <= 2:
#             y[i] = 1;
#         if result_scs[i][1] <= 2:
#             y[i] = 2;
#         if result_scs[i][2] <= 2:
#             y[i] = 3;
#         if result_scs[i][3] <= 2:
#             y[i] = 4;
#         if result_scs[i][4] <= 2:
#             y[i] = 5;

#     for i in range(192):
#         if X_pca[i][0] > 20 or X_pca[i][0] < -10:
#             X_pca[i][0] = 0;
#         fig = plt.figure()
#         ax = fig.add_subplot(111, projection='3d')
#         ax.scatter(X_pca[:, 0], X_pca[:, 1],X_pca[:, 2],c=y, cmap='coolwarm')
#         ax.view_init(elev=20, azim=30)
#         plt.savefig('3dplot.png', dpi=300)

    print("小项F1", (correct * 100) / ( (turn + 1)), "%")
    print("评分F1", (score_cor * 100) / (turn + 1), "%")
    print("小评分正正概率", (correct_correct * 100) / (correct_count), "%")
    print("小评分误误概率", (false_false * 100) / (false_count), "%")
    print("计数总计为正的,预测总计为正的，为正的其中预测为正的", correct_count, correct_predict, correct_correct)
    print("计数总计为负的,预测总计为负的，为负的其中预测为负的", false_count, false_predict, false_false)