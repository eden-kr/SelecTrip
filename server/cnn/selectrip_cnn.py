# -*- coding: utf-8 -*-
"""
Created on Thu Jul 30 15:17:39 2020

@author: 82109
"""
import numpy
from PIL import Image
import matplotlib.pyplot as plt
import numpy as np

def ImageToName(file):
    image_w = 128
    image_h = 128
    X = []
    
    img = Image.open(file)
    img = img.convert("RGB")
    img = img.resize((image_w, image_h))
    data = np.asarray(img)      #이미지 배열 재생성
    X.append(data)
    X = np.array(X)

    #모델 불러오기
    from keras.models import load_model
    model = load_model("C:/selectripModel/train/model/multi_img_classification.model")
    model.summary()
    prediction = model.predict(X)
    np.set_printoptions(formatter={'float': lambda x: "{0:0.3f}".format(x)})

    print('Using tensorflow..')
    cnt = 0

    for i in prediction:
        pre_ans = i.argmax()  # 예측 레이블
        if pre_ans == 0: pre_ans_str = "스위스"
        elif pre_ans == 1: pre_ans_str = "파리"
        else: pre_ans_str = "인식 불가"

        if i[0] >= 0.7 : 
            print(pre_ans_str+"(을)를 추천합니다.")
            return pre_ans_str

        if i[1] >= 0.7: 
            print(pre_ans_str+"(을)를 추천합니다.")
            return pre_ans_str
  
        cnt += 1