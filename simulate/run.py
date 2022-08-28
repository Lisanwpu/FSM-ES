import os
os.environ['HDF5_DISABLE_VERSION_CHECK'] = '2'
import cv2
import numpy as np
np.set_printoptions(threshold=10000000)
from tensorflow.keras import backend as K
import matplotlib.pyplot as plt
from keras.models import Sequential, load_model
from keras.preprocessing.image import load_img, img_to_array
##from tensorflow.keras.utils import load_img, img_to_array
from scipy import *
import sys
import scipy
import scipy.stats
from scipy import stats
from keras.preprocessing import image
from rectangle import draw
os.environ["TF_CPP_MIN_LOG_LEVEL"]='2'

model = load_model('C:\\Users\\Administrator\\Desktop\\Project\\Project\\simulate\\taptap-logo-autoencoder-model.h5')

img_width, img_height, channels = 128,128,3
input_shape = (img_width, img_height, channels)

# draw将txt转换成了一个按照类型进行颜色标注的图形，是原GUI的原型
# save_path = r"C:\Users\LQY\Desktop\autoencoderfeturepaper\GUISimilarity\autoencoder\save"
# dir_path = r"C:\Users\LQY\Desktop\autoencoderfeturepaper\GUISimilarity\autoencoder\save0"
# txt_path = r"C:\Users\LQY\Desktop\autoencoderfeturepaper\GUISimilarity\autoencoder\txt\111.txt"
save_path = sys.argv[3]
dir_path = sys.argv[2]
txt_path = sys.argv[1]
draw(dir_path, txt_path)


# 加载数据
dir1 = save_path
file = ["%s/%s" % (dir1, y) for y in os.listdir(dir1)]
length = len(file)+1
arr = np.empty((length, img_width, img_height, channels), dtype=np.float32)
for j, imgfile in enumerate(file):
    img = load_img(imgfile)
    y = img_to_array(img).reshape(img_width, img_height, channels)
    y = y.astype('float32') / 255.
    arr[j] = y

img_path = dir_path+"\\"+txt_path.split("\\")[-1].split(".")[0]+".jpg"
img = cv2.imread(img_path)
img = cv2.resize(img, (128, 128))
img = cv2.imwrite(img_path, img)
img = load_img(img_path)
y = img_to_array(img).reshape(img_width, img_height, channels)
y = y.astype('float32') / 255.
arr[length-1] = y

X = arr

# p.set_printoptions()用于控制Python中小数的显示精度
# 总是显示所有的数组元素值
np.set_printoptions(threshold=10000000000)
get_encoded = K.function([model.layers[0].input], [model.layers[7].output])
P = np.round(get_encoded([X])[0],2)
P = P.reshape(length, 512)
# print('特征值')
# print(P)
img_decoded = model.predict(X)
im_list=1
for m in range(im_list):
   plt.subplot(2,im_list,m+1)
   plt.imshow(X[m+12].reshape(128,128,3))
   plt.subplot(2,im_list,m+1+im_list)
   plt.imshow(img_decoded[m+12].reshape(128,128,3))
   plt.axis('off')


# js
Matrix = np.zeros(length)
s = 0
min = 1
max = 0
min_flag = 0
# t=0

m = length-1
for n in range(0, length-1):
    js = 0
    for i, j in zip(P[m,:], P[n,:]):
        i=i/sum(P[m])
        j=j/sum(P[n])
        if i == 0:
            i=i+0.0000001
        if j == 0:
            j = j + 0.00000001
        js += 0.5*i * np.log(i / (0.5 * i + 0.5 * j))
    # for j, i in zip(P[m,:], P[n,:]):
        js += 0.5*j * np.log(j / (0.5 * i + 0.5 * j))

    s=s+1
    # print(m,n,js)
    Matrix[n] = js
    if (js < min):
        min = js
        min_flag = n
    if (js > max):
        max = js

# print(min, '\n')

# 数据归一化处理
files = os.listdir(save_path)
filename = files[min_flag].split("\\")[-1].split(".")[0]
# for i in range(length-1):
    # print(1-Matrix[i]/max)
print(filename, 1-Matrix[min_flag]/max)
# 注意：必须加这一句！！
sys.stdout.flush()