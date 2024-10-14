import pickle
import numpy as np
import json
import random

#
# train=json.load(open('F:/dataset/ManyModalQAData/official_aaai_split_train_data.json','r'))
# dev=json.load(open('F:/dataset/ManyModalQAData/official_aaai_split_dev_data.json','r'))
# print(len(train))
# print(len(dev))
# sum=0
# for i in range(len(dev)):
#     if((dev[i]['q_type']=='image')or(dev[i]['q_type']=='text')):
#         sum=sum+1
# # print(sum)
# answer_train={}
# text=""
# record=np.zeros(17)
# for i in range(len(dev)):
#     temp = {}
#     temp['id']=dev[i]['id']
#     num=random.randint(0,16)
#     text=''
#     # print(num)
#     for n in range(15):
#         if n==num:
#             text=text+dev[i]['answer']+" "
#         else:
#             text=text+dev[random.randint(0,1000)]['answer']+" "
#     temp['choice_list']=text
#     temp['choice']=num
#     record[num]=record[num]+1
#     # temp['answer_list']
#     answer_train[i]=temp
# #
# with open("./data/answer_list_dev.json", "w") as f:
#     json.dump(answer_train, f)
#     print("加载入文件完成...")



# train=json.load(open('F:/dataset/ManyModalQAData/official_aaai_split_train_data.json','r'))
# dev=json.load(open('F:/dataset/ManyModalQAData/official_aaai_split_dev_data.json','r'))
# print(len(train))
# print(len(dev))
# sum=0
# # for i in range(len(dev)):
# #     if((dev[i]['q_type']=='image')or(dev[i]['q_type']=='text')):
# #         sum=sum+1
# # print(sum)
# answer_train={}
# text=""
# record=np.zeros(17)
# for i in range(len(dev)):
#     temp = {}
#     temp['id']=dev[i]['id']
#     num=random.randint(0,16)
#     text=''
#     # print(num)
#     for n in range(15):
#         if n==num:
#             text=text+dev[i]['answer']+" "
#         else:
#             text=text+dev[random.randint(0,1000)]['answer']+" "
#     temp['choice_list']=text
#     temp['choice']=num
#     record[num]=record[num]+1
#     # temp['answer_list']
#     answer_train[i]=temp
# #
# with open("./data/answer_list_dev.json", "w") as f:
#     json.dump(answer_train, f)
#     print("加载入文件完成...")






import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.datasets import make_blobs
from sklearn.decomposition import PCA

X = np.random.randn(100, 768)

pca = PCA(n_components=2)
X_pca = pca.fit_transform(X)

# 可视化降维后的数据
plt.scatter(X_pca[:, 0], X_pca[:, 1], cmap='viridis')
plt.show()












