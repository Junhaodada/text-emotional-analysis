#coding: utf-8
import pandas as pd
import jieba
import time

f = open(r'C:\Users\HP\Desktop\NLP\Sentiment-Analysis\1.情绪分类及特征提取(大连理工词典)\庆余年220.csv', encoding='utf8')
weibo_df = pd.read_csv(f)
# print(weibo_df.head())


df = pd.read_excel(
    r"C:\Users\HP\Desktop\NLP\Sentiment-Analysis\1.情绪分类及特征提取(大连理工词典)\大连理工大学中文情感词汇本体NAU.xlsx")
# print(df.head())


Happy = []
Good = []
Surprise = []
Anger = []
Sad = []
Fear = []
Disgust = []

for idx, row in df.iterrows():
    if row['情感分类'] in ['PA', 'PE']:
        Happy.append(row['词语'])
    if row['情感分类'] in ['PD', 'PH', 'PG', 'PB', 'PK']:
        Good.append(row['词语'])
    if row['情感分类'] in ['PC']:
        Surprise.append(row['词语'])
    if row['情感分类'] in ['NB', 'NJ', 'NH', 'PF']:
        Sad.append(row['词语'])
    if row['情感分类'] in ['NI', 'NC', 'NG']:
        Fear.append(row['词语'])
    if row['情感分类'] in ['NE', 'ND', 'NN', 'NK', 'NL']:
        Disgust.append(row['词语'])
    if row['情感分类'] in ['NAU']:  # 修改: 原NA算出来没结果
        Anger.append(row['词语'])


# print(Happy)

#正负计算不是很准 自己可以制定规则  
Positive = Happy + Good + Surprise
Negative = Anger + Sad + Fear + Disgust
print('情绪词语列表整理完成')  
print(Anger)


#---------------------------------------中文分词---------------------------------

#添加自定义词典和停用词
#jieba.load_userdict("user_dict.txt")
stop_list = pd.read_csv('stop_words.txt',
                        engine='python',
                        encoding='utf-8',
                        delimiter="\n",
                        names=['t'])
