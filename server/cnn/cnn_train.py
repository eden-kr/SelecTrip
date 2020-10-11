import numpy as np
import pandas as pd
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import Flatten, Dropout
from keras.layers.convolutional import Conv2D
from keras.layers.convolutional import MaxPooling2D
from keras.preprocessing.image import ImageDataGenerator
import glob
from PIL import Image
import os
from sklearn.model_selection import train_test_split
from keras.callbacks import EarlyStopping, ModelCheckpoint
import matplotlib.pyplot as plt
import keras.backend.tensorflow_backend as K
import tensorflow as tf
from tensorflow.python.client import device_lib


#train 파일 생성
np.random.seed(7)

caltech_dir = "C:/selectrip"
categories = ["스위스","파리"]
nb_classes = len(categories)

#이미지 크기 조정
image_w = 128
image_h = 128

#픽셀 조정
pixels = image_h * image_w * 3

X = []
y = []

#이미지 파일 순회
for idx, cat in enumerate(categories):
    label = [0 for i in range(nb_classes)]
    label[idx] = 1

    image_dir = caltech_dir + "/" + cat
    files = os.listdir(image_dir)
    for i in range(len(files)):
        files[i]=image_dir+'/'+ files[i]

    print(cat, "파일 길이 :", len(files))
    for f in files:
        img = Image.open(f)
        img = img.convert("RGB")
        img = img.resize((image_w, image_h))
        data = np.asarray(img)
        X.append(data)
        y.append(label)

X = np.array(X)
y = np.array(y)

X_train, X_test, y_train, y_test = train_test_split(X,y, shuffle=True, test_size=0.3)
xy = (X_train, X_test, y_train, y_test )
np.save("C:/selectripModel/train/train", xy )
print("ok",len(y))

#GPU 메모리 설정
config = tf.compat.v1.ConfigProto()
config.gpu_options.allow_growth = True
session = tf.compat.v1.Session(config=config)   #더 많은 메모리 필요 시 필요한 GPU메모리 영역 확장


# train파일에서 훈련, 검증 나누기
X_train, X_test, y_train, y_test = np.load('C:/selectripModel/train/train.npy')        #케라스 데이터 불러오기
print(X_train.shape)
print(X_train.shape[0])

#일반화
X_train = X_train.astype(float) / 255
X_test = X_test.astype(float) / 255

#모델 구성
#print(device_lib.list_local_devices())      #keras + tf에서 사용 가능한 하드웨어 지원 확인
with K.tf_ops.device('/device:CPU:0'):
    model = Sequential()
    model.add(Conv2D(32, (3, 3), padding="same", input_shape=X_train.shape[1:], activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Conv2D(64, (3, 3), padding="same", activation='relu'))
    model.add(MaxPooling2D(pool_size=(2, 2)))
    model.add(Dropout(0.25))

    model.add(Flatten())
    model.add(Dense(256, activation='relu'))
    model.add(Dropout(0.5))
    model.add(Dense(nb_classes, activation='softmax'))
    model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
    
    #모델 생성 후 저장
    model_dir = 'C:/selectripModel/train/model'

    if not os.path.exists(model_dir):
        os.mkdir(model_dir)
        
    #얼리스타핑
    model_path = model_dir + '/multi_img_classification.model'
    checkpoint = ModelCheckpoint(filepath=model_path, monitor='val_loss', verbose=1, save_best_only=True)
    #validation accuracy가 1%이상 증가하지 않을 경우 성능 증가가 없다고 가정
    early_stopping = EarlyStopping(monitor='val_loss',mode = 'min', verbose= 1, patience=50)
model.summary()

#모델훈련
history = model.fit(X_train, y_train, batch_size=7, epochs=100, validation_data=(X_test, y_test),
                    callbacks=[checkpoint, early_stopping])

print("accuracy : %.4f" % (model.evaluate(X_test, y_test)[1]))

# plt!
y_vloss = history.history['val_loss']
y_loss = history.history['loss']
x_len = np.arange(len(y_loss))

#시각화
plt.plot(x_len, y_vloss, marker='.', c='red', label='val_set_loss')
plt.plot(x_len, y_loss, marker='.', c='blue', label='train_set_oss')
plt.legend()
plt.xlabel('epochs')
plt.ylabel('loss')
plt.grid()
plt.show()