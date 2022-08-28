import os
import os.path
import xml.etree.cElementTree as ET
import cv2
import numpy as np
import codecs

from PIL import Image
i=0
k=0
def draw(save_path, txt_path):
    """
    图片根据标注画框
    """
    data_all = np.loadtxt(txt_path)
    print(data_all)
    data_i = np.unique(data_all[:, 0])
    print(data_i)
    data_i = np.array(data_i)
    data_i = np.trunc(data_i)
    data_i = data_i.astype('int')
    print(len(data_i))
    for j in data_i:
        print('============',j)
        img = Image.new('RGB', (int(data_all[0,6]), int(data_all[0,7])), (255, 255, 255))
        # img.save("%d.jpg" % j)
        img = np.asarray(img)
        for i in range(len(data_all[:,0])):
            if(data_all[i,0]==j):
                x1 = int(data_all[i,1])
                x2 = int(data_all[i,3])
                y1 = int(data_all[i,2])
                y2 = int(data_all[i,4])
                if (data_all[i, 5] == 0.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (255, 0, 0), -1)
                if(data_all[i,5] == 1.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (0, 0, 255), -1)
                if(data_all[i,5] == 2.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (0, 255,0), -1)
                if(data_all[i,5] == 3.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (0,140,255), -1)
                if(data_all[i,5] == 4.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (0,255,255), -1)
                if(data_all[i,5] == 5.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (255,0,255), -1)
                if(data_all[i, 5] == 6.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (196,228,255),-1)
                if (data_all[i, 5] == 7.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (235,206,135), -1)
                if (data_all[i, 5] == 8.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (79,79,47), -1)
                if (data_all[i, 5] == 9.0):
                    cv2.rectangle(img, (x1, y1), (x2, y2), (0, 0, 0), -1)
# 'Icon','Text','Image','Semantic','Card','list','Button''RadioButton','Checkbox','Search','OnOffSwitch',
#255, 0, 255粉色100文本  BGR
#0, 0, 128蓝色 100单选框
#255, 0, 0红色 100图片
#0, 255, 0绿色
#255,255,0黄色
#0,0,0黑色
#0，255，255青色
# 字为绿色
        cv2.imwrite(os.path.join(save_path, "%s.jpg" %txt_path.split("\\")[-1].split(".")[0]), img)

# if __name__ == '__main__':
#     # image_path = r"D:\Program Files\feiq\Recv Files\python\autoencoder_related_data\cut_Img\all"
#     root_saved_path = r"C:\Users\LQY\Desktop\autoencoder feture_paper\rectangle\result"
#     draw(root_saved_path)
