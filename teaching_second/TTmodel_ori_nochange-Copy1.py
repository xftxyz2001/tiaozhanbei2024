import torch
from torch import nn
from torch.nn import functional as F

import math
import numpy as np
from transformers import BertPreTrainedModel, BertConfig, BertModel


def gelu(x):
    """Implementation of the gelu activation function.
        For information: OpenAI GPT's gelu is slightly different (and gives slightly different results):
        0.5 * x * (1 + torch.tanh(math.sqrt(2 / math.pi) * (x + 0.044715 * torch.pow(x, 3))))
        Also see https://arxiv.org/abs/1606.08415
    """
    return x * 0.5 * (1.0 + torch.erf(x / math.sqrt(2.0)))


class GeLU(nn.Module):
    """Implementation of the gelu activation function.
        For information: OpenAI GPT's gelu is slightly different (and gives slightly different results):
        0.5 * x * (1 + torch.tanh(math.sqrt(2 / math.pi) * (x + 0.044715 * torch.pow(x, 3))))
        Also see https://arxiv.org/abs/1606.08415
    """

    def __init__(self):
        super().__init__()

    def forward(self, x):
        return gelu(x)


def split_last(x, shape):
    "split the last dimension to given shape"
    shape = list(shape)
    assert shape.count(-1) <= 1
    if -1 in shape:
        shape[shape.index(-1)] = int(x.size(-1) / -np.prod(shape))
    return x.view(*x.size()[:-1], *shape)


def merge_last(x, n_dims):
    "merge the last n_dims to a dimension"
    s = x.size()
    assert n_dims > 1 and n_dims < len(s)
    return x.reshape(*s[:-n_dims], -1)


# ------------------------------
#  MSA(multiheadselfattention)
# ------------------------------

class MultiHeadedSelfAttention(nn.Module):
    """Multi-Headed Dot Product Attention"""

    def __init__(self, dim, num_heads, dropout):
        super().__init__()
        self.proj_q = nn.Linear(dim, dim)
        self.proj_k = nn.Linear(dim, dim)
        self.proj_v = nn.Linear(dim, dim)
        self.drop = nn.Dropout(dropout)
        self.n_heads = num_heads
        self.scores = None  # for visualization

    def forward(self, x, mask):
        """
        x, q(query), k(key), v(value) : (B(batch_size), S(seq_len), D(dim))
        mask : (B(batch_size) x S(seq_len))
        * split D(dim) into (H(n_heads), W(width of head)) ; D = H * W
        """
        # (B, S, D) -proj-> (B, S, D) -split-> (B, S, H, W) -trans-> (B, H, S, W)
        q, k, v = self.proj_q(x), self.proj_k(x), self.proj_v(x)
        q, k, v = (split_last(x, (self.n_heads, -1)).transpose(1, 2) for x in [q, k, v])
        # (B, H, S, W) @ (B, H, W, S) -> (B, H, S, S) -softmax-> (B, H, S, S)
        scores = q @ k.transpose(-2, -1) / np.sqrt(k.size(-1))
        # if mask is not None:
        #     mask = mask[:, None, None, :].float()
        #     # scores -= 10000.0 * (1.0 - mask)
        #     scores -= 10000.0 * mask
        scores = self.drop(F.softmax(scores, dim=-1))
        # (B, H, S, S) @ (B, H, S, W) -> (B, H, S, W) -trans-> (B, S, H, W)
        h = (scores @ v).transpose(1, 2).contiguous()
        # -merge-> (B, S, D)
        h = merge_last(h, 2)
        self.scores = scores
        return h


class PositionWiseFeedForward1(nn.Module):
    """FeedForward Neural Networks for each position"""

    def __init__(self, dim, ff_dim):
        super().__init__()
        self.fc1 = nn.Linear(dim, ff_dim)
        self.fc2 = nn.Linear(ff_dim, dim)

    def forward(self, x):
        # (B, S, D) -> (B, S, D_ff) -> (B, S, D)
        return self.fc2(F.gelu(self.fc1(x)))


class Block(nn.Module):
    """Transformer Block"""

    def __init__(self, dim, num_heads, ff_dim, dropout):
        super().__init__()
        self.attn = MultiHeadedSelfAttention(dim, num_heads, dropout)
        self.proj = nn.Linear(dim, dim)
        self.norm1 = nn.LayerNorm(dim, eps=1e-6)
        self.pwff = PositionWiseFeedForward1(dim, ff_dim)
        self.norm2 = nn.LayerNorm(dim, eps=1e-6)
        self.drop = nn.Dropout(dropout)

    def forward(self, x, mask):
        h = self.drop(self.proj(self.attn(self.norm1(x), mask)))
        x = x + h
        h = self.drop(self.pwff(self.norm2(x)))
        x = x + h
        return x


# ---------------------------------
#  MTA(multiheadtrilinearattention)
# ---------------------------------
class TrilinearUnineTransformer(nn.Module):
    """Transformer with Self-Attentive Blocks"""

    def __init__(self, num_layers, dim, num_heads, ff_dim, dropout):
        super().__init__()
        self.TriTransblocksforimage = nn.ModuleList([
            TriTransBlock(dim, num_heads, ff_dim, dropout) for _ in range(int(num_layers / 2))])
        self.Transblocksforimage = nn.ModuleList([
            Block(dim, num_heads, ff_dim, dropout) for _ in range(int(num_layers / 2))])
        self.MultiHeadedTrilinearAttentionForimage = MultiHeadedALtrlinearAttention(dim, num_heads, dropout)

        self.TriTransblocksforText = nn.ModuleList([
            TriTransBlock(dim, num_heads, ff_dim, dropout) for _ in range(int(num_layers / 2))])
        self.TransblocksforText = nn.ModuleList([
            Block(dim, num_heads, ff_dim, dropout) for _ in range(int(num_layers / 2))])
        self.MultiHeadedTrilinearAttentionForText = MultiHeadedALtrlinearAttention(dim, num_heads, dropout)
        self.out = nn.Linear(dim, dim)
        self.bat = nn.BatchNorm1d(dim)

    def forward(self, v, q, a, k, v_mask=None, q_mask=None, a_mask=None, k_mask=None):
       

        # print(v_i.size(),v.size(),v_t.size())
        v_ii =  v 
        q_ii =  q 
        a_ii =  a 
        k_ii =  k 

        for TTblock, Tblock in zip(self.TriTransblocksforimage, self.Transblocksforimage):
            v_ii_, q_ii_, a_ii_ = TTblock(v_ii, q_ii, a_ii, v_mask, q_mask, a_mask)
            v_ii = Tblock(v_ii_, mask=v_mask)
            q_ii = Tblock(q_ii_, mask=q_mask)
            a_ii = Tblock(a_ii_, mask=a_mask)
        v_tt = v_ii
        q_tt =  q_ii
        a_tt =  a_ii
        
        q_out = q_tt
        a_out = a_tt
        k_out = k_ii
        v_out = v_tt

        return v_out, q_out, a_out, k_out
class MultiHeadedALtrlinearAttention(nn.Module):
    """Multi-Headed Dot Product Attention"""

    def __init__(self, dim, num_heads, dropout):
        super().__init__()
        self.proj_v = nn.Linear(dim, dim)
        self.proj_q = nn.Linear(dim, dim)
        self.proj_a = nn.Linear(dim, dim)
        self.proj_k = nn.Linear(dim, dim)
        self.proj_v_out = nn.Linear(dim, dim)
        self.proj_q_out = nn.Linear(dim, dim)
        self.proj_a_out = nn.Linear(dim, dim)
        self.proj_k_out = nn.Linear(dim, dim)
        self.drop = nn.Dropout(dropout)
        self.n_heads = num_heads
        self.scores = None  # for visualization

    def forward(self, v, q, a, k, v_mask, q_mask, a_mask, k_mask):
        """
        x, q(query), k(key), v(value) : (B(batch_size), S(seq_len), D(dim))
        * split D(dim) into (H(n_heads), W(width of head)) ; D = H * W
        """
        # (B, S, D) -proj-> (B, S, D) -split-> (B, S, H, W) -trans-> (B, H, S, W)
        if (k.size(0)!=a.size(0)) and (k.size(0)==1):
            k=k.expand(a.size(0),k.size(1))
        if (k.size(0)!=a.size(0)) and (a.size(0)==1):
            a=a.expand(k.size(0),a.size(1))

        num_batch = 1
        num_v = int(v.size(1)/8)
        num_q = int(q.size(1)/8)
        num_a = int(a.size(1)/8)
        num_k = int(k.size(1)/8)
        # print("处理前v.size(),q.size(),a.size(),k.size()",v.size(),q.size(),a.size(),k.size())

        v, q, a, k = self.proj_v(v), self.proj_q(q), self.proj_a(a), self.proj_k(k)
        v_out, q_out, a_out, k_out = self.proj_v_out(v), self.proj_q_out(q), self.proj_a_out(a), self.proj_k_out(k)
        # (B, S, D) -split-> (B, H, S, W)
        v, q, a, k = (split_last(x, (self.n_heads, -1)).transpose(1, 2) for x in [v, q, a, k])
        v_out, q_out, a_out, k_out = (split_last(x, (self.n_heads, -1)).transpose(1, 2) for x in
                                      [v_out, q_out, a_out, k_out])
        # print("处理后v.size(),q.size(),a.size(),k.size()", v.size(), q.size(), a.size(), k.size())
        # (B, H, V, W) * (B, H, Q, W) * (B, H, A, W) -softmax-> (B, H, V, Q, A)
        # 3.15 update / np.sqrt(a.size(-1))
        v = v.unsqueeze(0)
        q = q.unsqueeze(0)
        a = a.unsqueeze(0)
        k = k.unsqueeze(0)
        att = torch.einsum('bhvw,bhqw,bhaw -> bhvqa', [v, q, a]) / np.sqrt(a.size(-1))
        att1 = torch.einsum('bhvw,bhqw,bhkw -> bhvqk', [v, q, k]) / np.sqrt(a.size(-1))
        # mask
        # print(v_mask.size())
        # if v_mask is not None:
        #     v_mask = v_mask[:, None, :, None, None].float()
        #     att -= 10000.0 * v_mask
        # if q_mask is not None:
        #     q_mask = q_mask[:, None, None, :, None].float()
        #     att -= 10000.0 * q_mask
        # if a_mask is not None:
        #     a_mask = a_mask[:, None, None, None, :].float()
        #     att -= 10000.0 * a_mask
        # print("att1.size()",att1.size(),v_out.size(),q_out.size())
        # print("att.size()", att.size(), v_out.size(), q_out.size())

        att = self.drop(F.softmax(att.view(-1, self.n_heads, num_v * num_q * num_a), -1))
        att = att.view(-1, self.n_heads, num_v, num_q, num_a)
        # print("处理后",att1.size(),v_out.size())
        # print("处理后att.size()", att.size(), v_out.size(), q_out.size())
        v_out=v_out.unsqueeze(0)
        q_out=q_out.unsqueeze(0)
        a_out=a_out.unsqueeze(0)
        k_out=k_out.unsqueeze(0)
        # k_out=k_out.reshape(k_out.size(0),k_out.size(1),768,-1)
        # (B, H, V, Q, A) * (B, H, S, W) -> (B, V, H, W)
        v_out = torch.einsum('bhvqa,bhvw -> bvhw', [att1, v_out])
        q_out = torch.einsum('bhvqa,bhqw -> bqhw', [att1, q_out])
        a_out = torch.einsum('bhvqa,bhaw -> bahw', [att1, a_out])

        # （B, S, H, W) -merge-> (B, S, D)
        # print("vqak",v_out.size(),q_out.size(),a_out.size(),k_out.size())
        v_out = merge_last(v_out, 2)
        q_out = merge_last(q_out, 2)
        a_out = merge_last(a_out, 2)
        k_out = merge_last(k_out, 2)
        self.scores = att
        # print("vqak", v_out.size(), q_out.size(), a_out.size(), k_out.size())
        return v_out, q_out, a_out, k_out

class MultiHeadedTrilinearAttention(nn.Module):
    """Multi-Headed Dot Product Attention"""

    def __init__(self, dim, num_heads, dropout):
        super().__init__()
        self.proj_v = nn.Linear(dim, dim)
        self.proj_q = nn.Linear(dim, dim)
        self.proj_a = nn.Linear(dim, dim)
        self.proj_v_out = nn.Linear(dim, dim)
        self.proj_q_out = nn.Linear(dim, dim)
        self.proj_a_out = nn.Linear(dim, dim)
        self.drop = nn.Dropout(dropout)
        self.n_heads = num_heads
        self.scores = None  # for visualization

    def forward(self, v, q, a, v_mask, q_mask, a_mask):
        """
        x, q(query), k(key), v(value) : (B(batch_size), S(seq_len), D(dim))
        * split D(dim) into (H(n_heads), W(width of head)) ; D = H * W
        """
        # (B, S, D) -proj-> (B, S, D) -split-> (B, S, H, W) -trans-> (B, H, S, W)
        num_batch = v.size(0)
        num_v = int(v.size(1) / 8)
        num_q = int(q.size(1) / 8)
        num_a = int(a.size(1) / 8)

        v, q, a = self.proj_v(v), self.proj_q(q), self.proj_a(a)
        v_out, q_out, a_out = self.proj_v_out(v), self.proj_q_out(q), self.proj_a_out(a)
        # (B, S, D) -split-> (B, H, S, W)
        v, q, a = (split_last(x, (self.n_heads, -1)).transpose(1, 2) for x in [v, q, a])
        v_out, q_out, a_out = (split_last(x, (self.n_heads, -1)).transpose(1, 2) for x in [v_out, q_out, a_out])
        v = v.unsqueeze(0)
        q = q.unsqueeze(0)
        a = a.unsqueeze(0)
        # (B, H, V, W) * (B, H, Q, W) * (B, H, A, W) -softmax-> (B, H, V, Q, A)
        # 3.15 update / np.sqrt(a.size(-1))
        att = torch.einsum('bhvw,bhqw,bhaw -> bhvqa', [v, q, a]) / np.sqrt(a.size(-1))
        # mask
        # if v_mask is not None:
        #     v_mask = v_mask[:, None, :, None, None].float()
        #     att -= 10000.0 * v_mask
        # if q_mask is not None:
        #     q_mask = q_mask[:, None, None, :, None].float()
        #     att -= 10000.0 * q_mask
        # if a_mask is not None:
        #     a_mask = a_mask[:, None, None, None, :].float()
        #     att -= 10000.0 * a_mask

        att = self.drop(F.softmax(att.view(-1, self.n_heads, num_v * num_q * num_a), -1))
        att = att.view(-1, self.n_heads, num_v, num_q, num_a)
        v_out = v_out.unsqueeze(0).reshape(att.size(0),att.size(1),att.size(2),-1)
        q_out = q_out.unsqueeze(0).reshape(att.size(0),att.size(1),att.size(2),-1)
        a_out = a_out.unsqueeze(0).reshape(att.size(0),att.size(1),att.size(2),-1)
        # (B, H, V, Q, A) * (B, H, S, W) -> (B, V, H, W)
        # print(att.size(), v_out.size())
        v_out = torch.einsum('bhvqa,bhvw -> bvhw', [att, v_out])
        q_out = torch.einsum('bhvqa,bhqw -> bqhw', [att, q_out])
        a_out = torch.einsum('bhvqa,bhaw -> bahw', [att, a_out])
        # （B, S, H, W) -merge-> (B, S, D)
        v_out = merge_last(v_out, 2).reshape(-1,768)
        q_out = merge_last(q_out, 2).reshape(-1,768)
        a_out = merge_last(a_out, 2).reshape(-1,768)
        # print(v_out.size())
        self.scores = att

        return v_out, q_out, a_out


class PositionWiseFeedForward(nn.Module):
    """FeedForward Neural Networks for each position"""

    def __init__(self, dim, ff_dim):
        super().__init__()
        self.fc1 = nn.Linear(dim, ff_dim)
        self.fc2 = nn.Linear(ff_dim, dim)

    def forward(self, v, q, a):
        # (B, S, D) -> (B, S, D_ff) -> (B, S, D)
        v = self.fc2(F.gelu(self.fc1(v)))
        q = self.fc2(F.gelu(self.fc1(q)))
        a = self.fc2(F.gelu(self.fc1(a)))
        return v, q, a


class TriTransBlock(nn.Module):
    """Transformer Block"""

    def __init__(self, dim, num_heads, ff_dim, dropout):
        super().__init__()
        self.attn = MultiHeadedTrilinearAttention(dim, num_heads, dropout)
        self.proj = nn.Linear(dim, dim)
        self.norm1 = nn.LayerNorm(dim, eps=1e-6)
        self.pwff = PositionWiseFeedForward(dim, ff_dim)
        self.norm2 = nn.LayerNorm(dim, eps=1e-6)
        self.drop = nn.Dropout(dropout)

    def forward(self, v, q, a, v_mask, q_mask, a_mask):
        v_h, q_h, a_h = self.attn(self.norm1(v), self.norm1(q), self.norm1(a), v_mask, q_mask, a_mask)
        v_h, q_h, a_h = self.drop(self.proj(v_h)), self.drop(self.proj(q_h)), self.drop(self.proj(a_h))
        v = v + v_h
        q = q + q_h
        a = a + a_h
        v_h, q_h, a_h = self.pwff(self.norm2(v), self.norm2(q), self.norm2(a))
        v_h, q_h, a_h = self.drop(v_h), self.drop(q_h), self.drop(a_h)
        v = v + v_h
        q = q + q_h
        a = a + a_h
        return v, q, a


# ---------------------------------
# trilinear transformer
# ---------------------------------

class TrilinearTransformer(nn.Module):
    """Transformer with Self-Attentive Blocks"""

    def __init__(self, num_layers, dim, num_heads, ff_dim, dropout):
        super().__init__()
        self.TriTransblocks = nn.ModuleList([
            TriTransBlock(dim, num_heads, ff_dim, dropout) for _ in range(num_layers)])
        self.Transblocks = nn.ModuleList([
            Block(dim, num_heads, ff_dim, dropout) for _ in range(num_layers)])

    def forward(self, v, q, a, v_mask=None, q_mask=None, a_mask=None):
        for TTblock, Tblock in zip(self.TriTransblocks, self.Transblocks):
            v, q, a = TTblock(v, q, a, v_mask, q_mask, a_mask)
            v = Tblock(v, mask=v_mask)
            q = Tblock(q, mask=q_mask)
            a = Tblock(a, mask=a_mask)
        return v, q, a


class QstEncoder(nn.Module):

    def __init__(self):
        super(QstEncoder, self).__init__()
        self.bert = BertModel.from_pretrained('../mmirtt/bert', output_hidden_states=True)

    def forward(self, input_ids, token_type_ids=None, attention_mask=None):
        qst_vec = self.bert(input_ids, token_type_ids, attention_mask)
        qst_feature = qst_vec[2]
        qst_feature = qst_feature[-1]

        return qst_feature