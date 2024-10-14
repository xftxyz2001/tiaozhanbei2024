from sentence_transformers import SentenceTransformer, util
import pandas as pd
model = SentenceTransformer("all-MiniLM-L6-v2")

# Two lists of sentences
sentences1 = [
    
]

# sentences2 = [
#     "冬天，2",
#     "下了一场大雪，几个小朋友来公园一起玩，2",
#     "一场暴雪过后，大地像铺上了棉花。3",
#     "冬天悄悄的来了。一夜之间，我们的城市穿上了雪白的棉衣。大人们冷得都不想出。小孩子们却兴奋的不得了，他们穿上厚重的棉袄，约好了一起到公园玩。4",
#     "我和我的朋友们在们千万，草地绿油油的像一片柔软的地毯，天蓝蓝的，几只小鸟在翠绿的树上欢快的唱歌，小花有五颜六色的，云朵白白的一大片3",
#     "春天来了！小刚、乐乐、钉钉，一起去春天的田野里放风筝。4",
#     "冬天来了，星期天早上，我透过窗户看见门外下了一场大学，树上的叶子都掉光了，连房子的房顶上都下满了雪白雪白的雪，我就叫上我的好朋友去玩，他们都穿上了棉袄，棉鞋，4",
#     "快乐的冬天！冬天来了，到处白茫茫的一片，寒风呼呼的挂着。树呀！房子呀！都看不清了，这时小朋友们在外面玩，5",
#     "冬天来了！外面下起了鹅毛大雪，外面都是白茫茫的一片，小朋友从家里跑出来在一起玩。5",
#     "星期六早上，我和小明小安，拿着我们的风筝去公园里放风筝2",
#     "春天的美好，春天来了3",
#     "春天来了，3",
#     "快乐的春天，一天我和好朋友小明、小军，准备到家门口放风筝4",
#     "春天到了！小鸟飞来飞去，小丽和小明一起去田野里放风筝3",
#     "美好的春天，有一天他们几个约定明天下午3点到大草原里玩，时间这么快就到了，他们坐汽车一会就到了，他们三个先吃完饭，跑进大草原，他们活蹦乱跳5",
#     "小王和小型他们约好一起去植树1",
#     "有一天，有了个小朋友出去放风筝2",
#     "春天的美好，春天到了，外面美丽极了，万物复苏，柳绿花红、莺歌燕舞、到处都是生机勃勃。5"
#     "三个小朋友在植树！1",
#     "植树节到了，2",
#     "一天，三个小朋友去森林去植树3",
#     "奶奶生病了，一天孙女去看望生病了的奶奶2",
#     "植树，春天来了，有一天小明，小丽、小芙去植树了1",
#     "植树，星期天，小明、小红和笑笑去草地上种树4",
#     "植树，三月十二日植树节到了，5",
#     "春天来了，植树节那天，三个小朋友约好去公园植树。5",
#     "奶奶生病了1",
#     "一天，小女孩去看望她住在病房里地奶奶2",
#     "奶奶生病了，一天中午，小丽去看他地奶奶3",
#     "奶生病了，一天，小丽地奶奶生病了，小丽到医院看望奶奶、奶奶坐在床上4",
#     "奶奶生病了，一天有一位小女孩去看望她地奶奶，5",
# ]
sentences2 = [

]
data = pd.read_excel(io="./data_predict_random1.xlsx")
data1=pd.read_excel(io="./data_random1.xlsx")
correct=0;
for n in range(1,169):
#         print(data1.loc[n]['Passage_three'],data1.loc[n]['Score_three'])
        sentences2.append(data1.loc[n]['Passage'] + str(int(data1.loc[n]['Score'])))
for i in range(1,data.shape[0]):
#     print(i)
    for n in range(1,len(sentences2)):
        sentences1.append(data.loc[i]['Passage'])
#     print(sentences1)
    embeddings1 = model.encode(sentences1, convert_to_tensor=True)
    embeddings2 = model.encode(sentences2, convert_to_tensor=True)

        # Compute cosine-similarities
    cosine_scores = util.cos_sim(embeddings1, embeddings2)
    max=0
    for n in range(len(sentences1)):
        if cosine_scores[n][n]>cosine_scores[max][max]:
            max=n;
    print(max)
    if int(sentences2[max][len(sentences2[max])-1:len(sentences2[max])])==data.loc[i]['Score']:
        correct=correct+1
    sentences1=[]
print(correct/data.shape[0]);

    
    
    
# # Compute embedding for both lists
# embeddings1 = model.encode(sentences1, convert_to_tensor=True)
# embeddings2 = model.encode(sentences2, convert_to_tensor=True)

# # Compute cosine-similarities
# cosine_scores = util.cos_sim(embeddings1, embeddings2)

# # Output the pairs with their score
# for i in range(len(sentences1)):
#     print("{} \t\t {} \t\t Score: {:.4f}".format(
#         sentences1[i], sentences2[i], cosine_scores[i][i]
#     ))