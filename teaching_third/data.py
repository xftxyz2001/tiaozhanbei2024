import torch
from torch.nn import functional as F
from torch.utils.data import Dataset, ConcatDataset, DataLoader
import matplotlib.image as mpig
import random
import os
import json
import csv
import sys
# import nltk
import base64
import time
import psutil
from PIL import Image
from torch._six import string_classes
import collections
import _pickle as cPickle
import numpy as np
import warnings
warnings.filterwarnings("ignore")
import h5py
from transformers import BertPreTrainedModel, BertConfig, BertModel, BertTokenizer
from torchvision import transforms as trans
from torchvision.models.resnet import resnet101, Bottleneck
import re
import pandas as pd

def trim_collate(batch):
    "Puts each data field into a tensor with outer dimension batch size"
    _use_shared_memory = True
    error_msg = "batch must contain tensors, numbers, dicts or lists; found {}"
    elem_type = type(batch[0])
    print(batch[0])
    if torch.is_tensor(batch[0]):
        out = None
        if 1 < batch[0].dim(): # image features
            max_num_boxes = max([x.size(0) for x in batch])
            if _use_shared_memory:
                # If we're in a background process, concatenate directly into a
                # shared memory tensor to avoid an extra copy
                numel = len(batch) * max_num_boxes * batch[0].size(-1)
                storage = batch[0].storage()._new_shared(numel)
                out = batch[0].new(storage)
            # warning: F.pad returns Variable!
            return torch.stack([F.pad(x, (0,0,0,max_num_boxes-x.size(0))).data for x in batch], 0, out=out)
        else:
            if _use_shared_memory:
                # If we're in a background process, concatenate directly into a
                # shared memory tensor to avoid an extra copy
                numel = sum([x.numel() for x in batch])
                storage = batch[0].storage()._new_shared(numel)
                out = batch[0].new(storage)
            return torch.stack(batch, 0, out=out)
    elif elem_type.__module__ == 'numpy' and elem_type.__name__ != 'str_' \
            and elem_type.__name__ != 'string_':
        elem = batch[0]
        if elem_type.__name__ == 'ndarray':
            # array of string classes and object
            if re.search('[SaUO]', elem.dtype.str) is not None:
                raise TypeError(error_msg.format(elem.dtype))

            return torch.stack([torch.from_numpy(b) for b in batch], 0)
        if elem.shape == ():  # scalars
            py_type = float if elem.dtype.name.startswith('float') else int
            return numpy_type_map[elem.dtype.name](list(map(py_type, batch)))
    elif isinstance(batch[0], int):
        return torch.LongTensor(batch)
    elif isinstance(batch[0], float):
        return torch.DoubleTensor(batch)
    elif isinstance(batch[0], string_classes):
        return batch
    elif isinstance(batch[0], collections.Mapping):
        return {key: default_collate([d[key] for d in batch]) for key in batch[0]}
    elif isinstance(batch[0], collections.Sequence):
        transposed = zip(*batch)
        return [trim_collate(samples) for samples in transposed]

    raise TypeError((error_msg.format(type(batch[0]))))
def trim_collate_no(batch_dic):
    res = {}
    res['question'] = batch_dic[0][0]
    res['question_emb'] = batch_dic[0][1]
    res['text'] = batch_dic[0][2]
    res['text_emb']=batch_dic[0][3]
    res['feats'] = batch_dic[0][4]

    res['multiple_choices'] = batch_dic[0][5]

    res['multiple_choices_emb'] = batch_dic[0][6]
    res['answer'] = batch_dic[0][7]
   
    return res

# ------------------------------
# masked language
# ------------------------------
tokenizer = BertTokenizer.from_pretrained("../mmirtt/bert/")

def random_word(tokens):
    """
    Masking some random tokens for Language Model task with probabilities as in the original BERT paper.
    :param tokens: list of str, tokenized sentence.
    :return: (list of str, list of int), masked tokens and related labels for LM prediction
    """
    output_label = []

    for i, token in enumerate(tokens):
        prob = random.random()
        # mask token with probability
        ratio = 0.15
        if prob < ratio:
            prob /= ratio

            # 80% randomly change token to mask token
            if prob < 0.8:
                tokens[i] = "[MASK]"

            # 10% randomly change token to random token
            elif prob < 0.9:
                tokens[i] = random.choice(list(tokenizer.vocab.items()))[0]

            # -> rest 10% randomly keep current token

            # append current token to output (we will predict these later)
            try:
                output_label.append(tokenizer.vocab[token])
            except KeyError:
                # For unknown words (should not occur with BPE vocab)
                output_label.append(tokenizer.vocab["[UNK]"])
        else:
            # no masking token (will be ignored by loss function later)
            output_label.append(-1)

    return tokens, output_label
def textmask(feature,maskQue,numb):
    temp=torch.zeros(768)
    for i in range(len(maskQue)):
        if maskQue[i] ==1:
            for x in range(numb):
                if(i*num<768):
                    temp[i*num]=1
    feature=feature.sum(feature.mm(feature,temp))

def textmaskencode(text, max_seq_length):
    tokens = tokenizer.tokenize(text.strip())

    # Account for [CLS] and [SEP] with "- 2"
    if len(tokens) > max_seq_length - 2:
        tokens = tokens[:(max_seq_length - 2)]

    # Ge random words
    masked_tokens, masked_label = random_word(tokens)

    # concatenate lm labels and account for CLS, SEP, SEP
    masked_tokens = ['[CLS]'] + masked_tokens + ['[SEP]']
    seq_len = len(masked_tokens)
    if len(masked_tokens) < max_seq_length:
        masked_tokens += ['[PAD]' for _ in range(max_seq_length - len(masked_tokens))] #Padding sentences

    input_ids = tokenizer.convert_tokens_to_ids(masked_tokens)

    # Mask & Segment Word
    lm_label_ids = ([-1] + masked_label + [-1])
    input_mask = [0] * seq_len

    # Zero-pad up to the sequence length.
    while len(input_ids) < max_seq_length:
        input_ids.append(0)
    while len(input_mask) < max_seq_length:
        input_mask.append(1)
    while len(lm_label_ids) < max_seq_length:
        lm_label_ids.append(-1)

    return torch.tensor(input_ids), torch.tensor(input_mask), torch.tensor(lm_label_ids)

def maskQ(leng,answer):
    temp=torch.zeros(leng)
    temp[answer]=1;
    return temp;
    
# ------------------------------
# ------- v7w dataset ----------
# ------------------------------
def _create_entry(img, question, answer, label, ans_gt, ans_mc):
    if None!=answer:
        answer.pop('image_id')
        answer.pop('question_id')
    entry = {
        'question_id' : question['question_id'],
        'image_id'    : question['image_id'],
        'image'       : img,
        'question'    : question['question'],
        'answer'      : answer,
        'label'      : label,
        'ans_gt'      : ans_gt,
        'ans_mc'      : ans_mc}
    return entry


def _load_dataset(dataroot, name, img_id2val, label2ans, ans_candidates):
    """Load entries

    img_id2val: dict {img_id -> val} val can be used to retrieve image or features
    dataroot: root path of dataset
    name: 'train', 'val', 'test'
    """
    question_path = os.path.join(
        dataroot, 'v7w_%s_questions.json' % name)
    questions = sorted(json.load(open(question_path))['questions'],
                       key=lambda x: x['question_id'])
    entries = []
    for question in questions:
        img_id = question['image_id']
        ans_mc = ans_candidates[str(question['question_id'])]['mc']
        ans_gt = ans_candidates[str(question['question_id'])]['ans_gt']
        label = ans_candidates[str(question['question_id'])]['label']

        entries.append(_create_entry(img_id2val[img_id], question, None, label, ans_gt, ans_mc))

    return entries




# ------------------------------
# ------ vqa2.0 dataset --------
# ------------------------------
# extract region feature
csv.field_size_limit(sys.maxsize)
FIELDNAMES = ["img_id", "img_h", "img_w", "objects_id", "objects_conf",
              "attrs_id", "attrs_conf", "num_boxes", "boxes", "features"]

def load_obj_tsv(fname):
    """Load object features from tsv file.

    :param fname: The path to the tsv file.
    :param topk: Only load features for top K images (lines) in the tsv file.
        Will load all the features if topk is either -1 or None.
    :return: A list of image object features where each feature is a dict.
        See FILENAMES above for the keys in the feature dict.
    """
    data = {}
    start_time = time.time()
    print("Start to load Faster-RCNN detected objects from %s" % fname)
    with open(fname) as f:
        reader = csv.DictReader(f, FIELDNAMES, delimiter="\t")
        for i, item in enumerate(reader):            
            boxes = int(item['num_boxes'])
            feat = np.frombuffer(base64.b64decode(item['features']), dtype=np.float32)
            feat = feat.reshape((boxes, -1))
            id = item['img_id']
            data[id] = feat
    elapsed_time = time.time() - start_time
    print("Loaded %d images in file %s in %d seconds." % (len(data), fname, elapsed_time))
    return data


class VQAData(Dataset):

    def __init__(self,maxlen):
        self.maxlen = maxlen


        # self.quesTestList=json.load(open('./data/ManyModalQAData/official_aaai_split_train_data.json','r'))
        pd.set_option('display.notebook_repr_html', False)
        # 读取xls（绝对路径）
        self.data = pd.read_excel(io="./data_random1.xlsx")
        # self.answerList=json.load(open('./data/answer_list_train.json','r'))
        # images
        # self.device=torch.device("cpu")
        self.device=torch.device('cuda:0')
        self.tokenizer = BertTokenizer.from_pretrained('../mmirtt/bert/')
        self.model = BertModel.from_pretrained('../mmirtt/bert/').to(self.device)
        self.img_path='./data/ManyModalImages/'

        self.quesdata = []  # question information

    def __len__(self):
        # print(len(self.quesTestList))
        return self.data.shape[0]
    def get_Img(self,img_Id):

        fname="./lmg"+str(img_Id)+".jpg"
        if os.path.exists(fname):

            image_info=Image.open(fname).convert('RGB')
            image_transform=trans.Compose([
                trans.Resize(256),
                trans.CenterCrop(768),
                trans.ToTensor(),
                trans.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
            ])
            image_info=image_transform(image_info)
            feats=image_info.unsqueeze(0)
        else:
#             print('img_Id data.py333',img_Id)
            feats=torch.zeros((1,3,768,768))
            feats[0][0][0]=-1
        # img_mpig = mpig.imread(fname)/255
        # feats= torch.from_numpy(img_mpig).float()
#         print("data.py336",feats.shape,type(feats))
        return feats;

    def bert_encode(self, str,size_len):
        strs = str.split("，")
        emb = []
        temp_emb=[]
        out_emb=torch.zeros(size_len)
        for i in range(len(strs)):
            temp = str[i]
            temp_emb.append(self.model(**self.tokenizer(temp, return_tensors='pt').to(self.device)).pooler_output.to(torch.device("cpu"))[0])

        for i in range(size_len):
            temp=0;

            for n in range(len(strs)):
                temp=temp+temp_emb[n][i]
            temp=temp/len(strs);
            # print("data.py 348",temp)
            out_emb[i]=temp
        return out_emb.unsqueeze(0)
    def __getitem__(self, index):
        
        if index==0:index=1;
#         if self.data.loc[index]['Passage_three']=='无':
#             index=66;

#         print(index)
        # self.quesTestList[index];
        text=self.data.loc[index]['Text']
        img_id=self.data.loc[index]['Img_id']

        question="请小朋友仔细观察这幅图片，结合自己的生活经历，描述一下图片中人物会想什么？感受如何？以及故事后续内容？，请注意使用优秀的字词、短语会更棒呦"
        score=self.data.loc[index]['Score']
        appraise=self.data.loc[index]['Appraise']
        appr=self.data.loc[index]['Appr']
        multiple_choices=self.data.loc[index]['Passage']
        passage_one=self.data.loc[index]['Passage_three']

        # img_id = np.array(self.quesTestList[index]['id'])
        # print('img_id data.py360',img_id)
#         datum = self.quesTestList[index]
        r4 = "\\[]{}.!@#$%^&*()<>?,./;'|~`+_=-"
        cleanr = re.compile('<.*?>')

        feats=np.array(self.get_Img(img_id))\

        # question=datum['question']
        appr=appr.split(" ")
        question_emb=self.bert_encode(question,768)
        
        answer= answer=torch.zeros(1).to(torch.device("cpu"));
        answer[0]=self.data.loc[index]['Score_three'];

        
        # text=datum['text']
        text_emb =self.bert_encode(passage_one,768)
#         answer=self.data.loc[index]['Appr']
#         quest_id = datum['id']
        # print("data.py 368",len(self.answerList))
        # print('data.py 369',self.answerList.get(str(index)))
        # print('data.py 369', self.answerList.get("120"))
#         ma = maskQ(768, answer);
        # multiple_choices=self.answerList.get(str(index))["choice_list"]

        multiple_choices_emb = self.model(**self.tokenizer(multiple_choices, return_tensors='pt').to(self.device)).pooler_output.to(torch.device("cpu"))
#         text_emb=textmask(text_emb,ma,10);
#         quest_id=datum['id']

#         modal=datum['q_type']

        return question,question_emb,text,text_emb,feats,multiple_choices,multiple_choices_emb,answer

class VQAData_predict(Dataset):

   
    def __init__(self,maxlen):
        self.maxlen = maxlen


        # self.quesTestList=json.load(open('./data/ManyModalQAData/official_aaai_split_train_data.json','r'))
        pd.set_option('display.notebook_repr_html', False)
        # 读取xls（绝对路径）
        self.data = pd.read_excel(io="./data_predict_random1.xlsx")
        # self.answerList=json.load(open('./data/answer_list_train.json','r'))
        # images
        # self.device=torch.device("cpu")
        self.device=torch.device('cuda:0')
        self.tokenizer = BertTokenizer.from_pretrained('../mmirtt/bert/')
        self.model = BertModel.from_pretrained('../mmirtt/bert/').to(self.device)
        self.img_path='./data/ManyModalImages/'

        self.quesdata = []  # question information

    def __len__(self):
        # print(len(self.quesTestList))
        return self.data.shape[0]
    def get_Img(self,img_Id):

        fname="./lmg"+str(int(img_Id))+".jpg"
        if os.path.exists(fname):

            image_info=Image.open(fname).convert('RGB')
            image_transform=trans.Compose([
                trans.Resize(256),
                trans.CenterCrop(768),
                trans.ToTensor(),
                trans.Normalize([0.485,0.456,0.406],[0.229,0.224,0.225])
            ])
            image_info=image_transform(image_info)
            feats=image_info.unsqueeze(0)
        else:
#             print('img_Id data.py333',img_Id)
            feats=torch.zeros((1,3,768,768))
            feats[0][0][0]=-1
        # img_mpig = mpig.imread(fname)/255
        # feats= torch.from_numpy(img_mpig).float()
#         print("data.py336",feats.shape,type(feats))
        return feats;

    def bert_encode(self, str,size_len):
        strs = str.split("，")
        emb = []
        temp_emb=[]
        out_emb=torch.zeros(size_len)
        for i in range(len(strs)):
            temp = str[i]
            temp_emb.append(self.model(**self.tokenizer(temp, return_tensors='pt').to(self.device)).pooler_output.to(torch.device("cpu"))[0])

        for i in range(size_len):
            temp=0;

            for n in range(len(strs)):
                temp=temp+temp_emb[n][i]
            temp=temp/len(strs);
            # print("data.py 348",temp)
            out_emb[i]=temp
        return out_emb.unsqueeze(0)
    def __getitem__(self, index):
        if index==0:index=1;
#         if self.data.loc[index]['Passage_three']=='无':
#             index=random.randint(1,250);
#         if self.data.loc[index]['Passage_three']=='无':
#             index=66;
#         if self.data.loc[index]['Passage_three']=='无':
#             index=random.randint(1,250);
#         if self.data.loc[index]['Passage_three']=='无':
#             index=random.randint(1,250);
#         print(index)
        # self.quesTestList[index];
#         text=self.data.loc[index]['Passage']
        img_id=self.data.loc[index]['Img_id']

        question="请小朋友仔细观察这幅图片，结合自己的生活经历，描述一下图片中人物会想什么？感受如何？以及故事后续内容？，请注意使用优秀的字词、短语会更棒呦"
        score=self.data.loc[index]['Score']
        appraise=self.data.loc[index]['Appraise']
        appr=self.data.loc[index]['Appr']
        multiple_choices=self.data.loc[index]['Passage']
        passage_one=self.data.loc[index]['Passage_three']

        # img_id = np.array(self.quesTestList[index]['id'])
        # print('img_id data.py360',img_id)
#         datum = self.quesTestList[index]
        r4 = "\\[]{}.!@#$%^&*()<>?,./;'|~`+_=-"
        cleanr = re.compile('<.*?>')
        
        feats=np.array(self.get_Img(img_id))\

        # question=datum['question']
        appr=appr.split(" ")
        question_emb=self.bert_encode(question,768)
        
        answer= answer=torch.zeros(1).to(torch.device("cpu"));
        answer[0]=self.data.loc[index]['Score_three'];

        text=passage_one
        # text=datum['text']
        text_emb =self.bert_encode(passage_one,768)
#         answer=self.data.loc[index]['Appr']
#         quest_id = datum['id']
        # print("data.py 368",len(self.answerList))
        # print('data.py 369',self.answerList.get(str(index)))
        # print('data.py 369', self.answerList.get("120"))
#         ma = maskQ(768, answer);
        # multiple_choices=self.answerList.get(str(index))["choice_list"]

        multiple_choices_emb = self.model(**self.tokenizer(multiple_choices, return_tensors='pt').to(self.device)).pooler_output.to(torch.device("cpu"))
#         text_emb=textmask(text_emb,ma,10);
#         quest_id=datum['id']

#         modal=datum['q_type']

        return question,question_emb,text,text_emb,feats,multiple_choices,multiple_choices_emb,answer