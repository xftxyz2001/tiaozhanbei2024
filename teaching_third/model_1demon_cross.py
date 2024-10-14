import torch
import torch.nn as nn
from torch.nn import CrossEntropyLoss
import torchvision
from TTmodel_1demon import QstEncoder, TrilinearTransformer
# import TTmodel_1demon as TT
import TTmodel_ori as TT
# ------------------------------
# -------- model ---------------
# ------------------------------
class BertPredictionHeadTransform(nn.Module):
    def __init__(self):
        super(BertPredictionHeadTransform, self).__init__()
        self.dense = nn.Linear(768, 768)
        self.transform_act_fn = nn.ReLU()
        self.LayerNorm = nn.LayerNorm(768, eps=1e-12)

    def forward(self, hidden_states):
        hidden_states = self.dense(hidden_states)
        hidden_states = self.transform_act_fn(hidden_states)
        hidden_states = self.LayerNorm(hidden_states)
        return hidden_states


class BertLMPredictionHead(nn.Module):
    def __init__(self, bert_model_embedding_weights):
        super(BertLMPredictionHead, self).__init__()
        self.transform = BertPredictionHeadTransform()

        # The output weights are the same as the input embeddings, but there is
        # an output-only bias for each token.
        self.decoder = nn.Linear(bert_model_embedding_weights.size(1),
                                 bert_model_embedding_weights.size(0),
                                 bias=False)
        self.decoder.weight = bert_model_embedding_weights
        self.bias = nn.Parameter(torch.zeros(bert_model_embedding_weights.size(0)))

    def forward(self, hidden_states):
        hidden_states = self.transform(hidden_states)
        hidden_states = self.decoder(hidden_states) + self.bias
        return hidden_states


class TTpretrainModel(nn.Module):

    def __init__(self, num_layers=2, ff_dim=3072, dropout=0.1,v_dim=768,hid_vim=4096):

        super().__init__()
        self.qst_encoder = QstEncoder()

        # self.transformer = TrilinearTransformer(num_layers=num_layers, dim=768, num_heads=12, ff_dim=ff_dim, dropout=dropout)
        self.cls = BertLMPredictionHead(self.qst_encoder.bert.embeddings.word_embeddings.weight)

        self.loss_fct = CrossEntropyLoss(ignore_index=-1)
#         self.resnet50_model = models.resnet50(pretrained=True)

        self.image_encoder = torchvision.models.resnet101(pretrained=True)
        self.image_encoder.fc = nn.Linear(2048, v_dim)
        self.qst_encoder = TT.QstEncoder()
        self.v_linear = nn.Linear(v_dim, v_dim)
        self.text_linear = nn.Linear(hid_vim, v_dim)
        self.ans_linear = nn.Linear(hid_vim,v_dim)
        self.sigmoid=nn.Sigmoid()
        # self.transformerinput = TrilinearTransformer(num_layers=2, dim=768, num_heads=6, ff_dim=1024, dropout=0.1)
#         self.transformer=TT.TrilinearUnineTransformer(num_layers=96, dim=v_dim, num_heads=4, ff_dim=1024, dropout=0)
        self.transformer=TT.TrilinearUnineTransformer(num_layers=16, dim=v_dim, num_heads=4, ff_dim=1024, dropout=0)
        # self.transformer=TrilinearTransformer(num_layers=4, dim=768, num_heads=16, ff_dim=3096, dropout=0.1)
        self.logit_fc = nn.Sequential(
            nn.Linear(v_dim, v_dim * 2),
            nn.ReLU(inplace=False),
            nn.Dropout(0.1),
            nn.LayerNorm(v_dim * 2, eps=1e-12),
            nn.Linear(v_dim * 2, 4102),
            nn.ReLU(inplace=False),
        )
        self.lebal_fc = nn.Sequential(
            nn.Linear(v_dim, v_dim * 2),
            nn.LeakyReLU(inplace=False),
            nn.Dropout(0.0),
#             nn.LayerNorm(v_dim * 2, eps=1e-12),
            nn.Linear(v_dim * 2, 1),
#             nn.Sigmoid(),
        )
        self.VQ1 = nn.Sequential(
            nn.Linear(v_dim, v_dim * 2),
            nn.LeakyReLU(inplace=False),
            nn.Dropout(0.0),
            nn.LayerNorm(v_dim * 2, eps=1e-12),
            nn.Linear(v_dim * 2, 2),

        )
        self.VQ2= nn.Sequential(
            nn.Linear(v_dim, v_dim * 2),
            nn.LeakyReLU(inplace=False),
            nn.Dropout(0.05),
            nn.LayerNorm(v_dim * 2, eps=1e-12),
            nn.Linear(v_dim * 2, 1),
            
        )
        self.VQ3= nn.Sequential(
            nn.Linear(v_dim, v_dim * 2),
            nn.LeakyReLU(inplace=False),
            nn.Dropout(0),
            nn.LayerNorm(v_dim * 2, eps=1e-12),
            nn.Linear(v_dim * 2, 64),
#             nn.Tanh(),
#             nn.Sigmoid(),
           
        )


        # self.init_weights()
        # for m in self.modules():
        #     # if isinstance(m, nn.Conv2d):
        #     #     n = m.kernel_size[0] * m.kernel_size[1] * m.out_channels
        #     #     m.weight.data.normal_(0, math.sqrt(2. / n))
        #     #     if m.bias is not None:
        #     #         m.bias.data.zero_()
        #     if isinstance(m, nn.BatchNorm2d):
        #         nn.init.xavier_uniform_(m.weight.data.unsqueeze(0))
        #         m.bias.data.zero_()
        #     # elif isinstance(m, nn.ReLU):
        #     #     torch.nn.init.kaiming_normal(m.weight,a=math.sqrt(5))
        #     #     m.bias.data.zero_(m.weight,a=math.sqrt(5))
        #     elif isinstance(m, nn.Softmax):
        #         nn.init.xavier_uniform_(m.weight.data.unsqueeze(0))
        #         if m.bias is not None:
        #             m.bias.data.zero_()

    def forward(self, input_ids, ans_ids, scribe_emb,img_data,multiple_choices,answer,per):
        label=torch.zeros(1).to(torch.device('cuda:0'))
        label[0]=answer/5;
#         if answer>=3:
#             label[0]=1;
#         if answer<3:
#             label[0]=0;
#         label[int(answer)]=1
        label_ch=torch.zeros(2).to(torch.device('cuda:0'))
        modal='text'
        if modal=='text':
            label_ch[0]=1
        else:
            label_ch[1]=1
        # img_data = img_data.unsqueeze(2)
        # ans_ids=ans_ids.unsqueeze(2)
        # v_mask = self.make_mask(img_data.view(3,3,-1,512))
        # q_mask = self.make_mask(input_ids)
        # a_mask= self.make_mask(ans_ids)
        # print("img_feature,qst_feature,ans_feature,scribe_feature", img_data.size(), input_ids.size(), ans_ids.size(),
        #       scribe_emb.size())
        input_ids=input_ids.to(torch.device('cuda:0'))
        ans_ids=ans_ids.to(torch.device('cuda:0'))
        
       
        img_data1=torch.from_numpy(img_data).to(torch.device('cuda:0'))
#         img_data1= img_data1.transpose(0,2)
        
       
        img_data1=self.image_encoder(img_data1).unsqueeze(0)
#         print(img_data1.size())
        scribe_emb=scribe_emb.to(torch.device('cuda:0'))
        qst_feature = self.make_mask(input_ids).to(torch.device('cuda:0'))
        ans_feature = self.make_mask(ans_ids).to(torch.device('cuda:0'))
#         temp=torch.zeros(64).to(torch.device('cuda:0'))
#         temp[answer_choice]=1
        img_data1=img_data1.reshape(-1,4)[0:128*768,:].reshape(-1,768).to(torch.device('cuda:0'));
        img_feature = self.v_linear(img_data1).to(torch.device('cuda:0'))
        scribe_feature =self.make_mask(scribe_emb).to(torch.device('cuda:0'))
        answer=answer.to(torch.device('cuda:0'))
        # print("v_mask,q_mask,a_mask",v_mask.size(),q_mask.size(),a_mask.size())
        # img_feature=img_feature.unsqueeze(1)

        # print("img_feature,qst_feature,ans_feature,img_feature",type(img_feature),type(qst_feature),type(ans_feature),type(img_feature))
        # print("input_ids,ans_ids,img_data,img_feature", input_ids.size(), ans_ids.size(), type(img_data1))
        # print("img_data1,input_ids,ans_ids,scribe_emb", img_data1.device, input_ids.device,ans_ids.device,scribe_emb.device)
        # print("img_feature,qst_feature,ans_feature,scribe_feature", img_feature.device, qst_feature.device,ans_feature.device, scribe_feature.device)
        
        
#         img_data1=img_data1.expand(512,img_data1.size(1))
#         input_ids = input_ids.expand(512, input_ids.size(1))
#         ans_ids = ans_ids.expand(512, ans_ids.size(1))
#         scribe_emb = scribe_emb.expand(512, scribe_emb.size(1))


#         print("img_data1,input_ids,ans_ids,scribe_emb",img_data1.size(),input_ids.size(),ans_ids.size(),scribe_emb.size())
#         print("model_1 179.py",qst_feature[0].size())
#         img_data1[0][answer_choice]=img_data1[0][answer_choice]+1000;
        img_feature, qst_feature, ans_feature,scribe_feature = self.transformer(img_data1, input_ids, ans_ids, scribe_emb, img_feature, qst_feature, ans_feature,scribe_feature)
        # img_feature, qst_feature, ans_feature = self.transformer(img_data1, input_ids, ans_ids,img_feature, qst_feature,ans_feature)
#         print("img_feature,qst_feature,ans_feature,scribe_feature"
#                                      ,img_data1.size(),input_ids.size(),ans_ids.size(),scribe_emb.size())                                      
        q_prediction_scores = self.VQ1(qst_feature)[0]
        temp1=per+(1-per)*(q_prediction_scores[0]/(q_prediction_scores[0]+q_prediction_scores[1]))
        temp2=per-(1-per)*(q_prediction_scores[1]/(q_prediction_scores[0]+q_prediction_scores[1]))
        scribe_feature=scribe_feature*temp1
        img_feature=scribe_feature*temp2
#         q_prediction_scores=nn.Sigmoid(self,q_prediction_scores)
        
        a_prediction_scores = self.lebal_fc(img_feature+ans_feature+qst_feature+scribe_feature)[0]
    

        # print(temp,temp.size(),answer_choice)
#         print("a_prediction_scores",a_prediction_scores.size(),answer.size())
        los_func=nn.CrossEntropyLoss()
        los_func_mse=nn.MSELoss()
        label_loss=los_func(q_prediction_scores,label_ch)
#         print(a_prediction_scores,answer)
        ans_loss=los_func_mse(a_prediction_scores,label)
        # print(nn.Softmax(a_prediction_scores))
        criterion = nn.CosineEmbeddingLoss()
        # print("img_feature,scribe_feature",img_feature.size(),scribe_emb.size())
        Loss = nn.MSELoss()
        a_loss=Loss(img_feature,scribe_feature)
#         print(img_feature,scribe_feature)
        b_loss=Loss(img_feature,ans_feature)
#         a_prediction_scores=a_prediction_scores+temp
        result=[]
#         result.append(a_loss+b_loss+ans_loss)
        
        # q_masked_lm_loss = self.loss_fct(
        #         q_prediction_scores.view(-1, self.qst_encoder.bert.embeddings.word_embeddings.weight.size(0)),
        #         q_lm_label_ids.view(-1)
        #     )
        # a_masked_lm_loss = self.loss_fct(
        #         a_prediction_scores.view(-1, self.qst_encoder.bert.embeddings.word_embeddings.weight.size(0)),
        #         a_lm_label_ids.view(-1)
        #     )
#         print(a_prediction_scores,label)
#         if a_prediction_scores>=0.5:
#             a_prediction_scores=3;
#         if a_prediction_scores<0.5:
#              a_prediction_scores=2;
        a_prediction_scores=a_prediction_scores*5;
        result.append(ans_loss)
        result.append(ans_loss+label_loss)
        result.append(ans_loss+a_loss)
        result.append(ans_loss+label_loss+b_loss)
        result.append(ans_loss+label_loss+a_loss+b_loss)
        result.append(a_prediction_scores)
        result.append(img_feature)
        result.append(scribe_feature)
#         result.append(max_c)
        return result
        # return (q_masked_lm_loss+a_masked_lm_loss)

    def make_mask(self, feature):
        return (torch.sum(
            torch.abs(feature),
            dim=-1
        ) == 0) #.unsqueeze(1).unsqueeze(2)


# ------------------------------
# -------- model ---------------
# ------------------------------

class BCEFocalLoss(torch.nn.Module):

    def __init__(self, gamma=2, alpha=0.6, reduction='elementwise_mean'):
        super().__init__()
        self.gamma = gamma
        self.alpha = alpha
        self.reduction = reduction

    def forward(self, _input, target):
        pt = torch.sigmoid(_input)
        # pt = _input
        alpha = self.alpha
        loss = - alpha * (1 - pt) ** self.gamma * target * torch.log(pt) - \
               (1 - alpha) * pt ** self.gamma * (1 - target) * torch.log(1 - pt)
        if self.reduction == 'elementwise_mean':
            loss = torch.mean(loss)
        elif self.reduction == 'sum':
            loss = torch.sum(loss)
        return loss


class TriTransModel(nn.Module):

    def __init__(self, num_layers=2, ff_dim=3072, dropout=0.1):
        super().__init__()
        self.qst_encoder = QstEncoder()
        self.transformer = TrilinearTransformer(num_layers=num_layers, dim=768, num_heads=12, ff_dim=ff_dim,
                                                dropout=dropout)

        self.v_linear = nn.Linear(2048, 768)
        self.logit_fc = nn.Sequential(
            nn.Linear(768, 768 * 2),
            nn.ReLU(inplace=False),
            nn.Dropout(0.2),
            nn.LayerNorm(768 * 2, eps=1e-12),
            nn.Linear(768 * 2, 2)
        )

        self.loss_fct = BCEFocalLoss(gamma=2, alpha=0.6, reduction='sum')

    def forward(self, input_ids, ans_ids, labels=None, img_data=None):
        # Make mask
        q_mask = self.make_mask(input_ids.unsqueeze(2))
        a_mask = self.make_mask(ans_ids.unsqueeze(2))
        v_mask = self.make_mask(img_data)

        qst_feature = self.qst_encoder(input_ids)
        ans_feature = self.qst_encoder(ans_ids)
        img_feature = self.v_linear(img_data)

        img_feature, qst_feature, ans_feature = self.transformer(img_feature, qst_feature, ans_feature, v_mask, q_mask,
                                                                 a_mask)

        combined_feature = ans_feature.sum(1)
        logits = self.logit_fc(combined_feature)

        outputs = (logits,)

        if labels is not None:
            loss = self.loss_fct(logits.view(-1, 2), labels.view(-1, 2))

            outputs = (loss,) + outputs

        return outputs

    def make_mask(self, feature):
        return (torch.sum(
            torch.abs(feature),
            dim=-1
        ) == 0)  # .unsqueeze(1).unsqueeze(2)