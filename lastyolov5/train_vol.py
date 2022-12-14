import xml.etree.ElementTree as ET
import pickle
import os
import shutil
from os import listdir, getcwd
from os.path import join
sets = ['train', 'trainval']
classes = ['CheckBox', 'Checkbox', 'Icon', 'Image', 'InputBox', 'RadioButton', 'Switch', 'Text', 'button']
def convert(size, box):
    dw = 1. / size[0]
    dh = 1. / size[1]
    x = (box[0] + box[1]) / 2.0
    y = (box[2] + box[3]) / 2.0
    w = box[1] - box[0]
    h = box[3] - box[2]
    x = x * dw
    w = w * dw
    y = y * dh
    h = h * dh
    return (x, y, w, h)
def convert_annotation(image_id):
    in_file = open('allXml/%s.xml' % (image_id),encoding='utf-8')
    out_file = open('allLabels/%s.txt' % (image_id), 'w',encoding='utf-8')
    tree = ET.parse(in_file)
    root = tree.getroot()
    size = root.find('size')
    w = int(size.find('width').text)
    h = int(size.find('height').text)
    for obj in root.iter('object'):
        difficult = obj.find('difficult').text
        cls = obj.find('name').text
        if cls not in classes or int(difficult) == 1:
            continue
        cls_id = classes.index(cls)
        xmlbox = obj.find('bndbox')
        b = (float(xmlbox.find('xmin').text), float(xmlbox.find('xmax').text), float(xmlbox.find('ymin').text),
             float(xmlbox.find('ymax').text))
        bb = convert((w, h), b)
        out_file.write(str(cls_id) + " " + " ".join([str(a) for a in bb]) + '\n')
wd = getcwd()
print(wd)
for image_set in sets:
    if not os.path.exists('allLabels/'):
        os.makedirs('allLabels/')
    image_ids = open('ImageSets/%s.txt' % (image_set),encoding='utf-8').read().strip().split()
    image_list_file = open('images_%s.txt' % (image_set), 'w',encoding='utf-8')
    labels_list_file=open('labels_%s.txt'%(image_set),'w',encoding='utf-8')
    for image_id in image_ids:
        image_list_file.write('%s.jpg\n' % (image_id))
        labels_list_file.write('%s.txt\n'%(image_id))
        convert_annotation(image_id)
    image_list_file.close()
    labels_list_file.close()


def copy_file(new_path,path_txt,search_path):#??????1???????????????????????????  ??????2???????????????????????????train,val?????????????????????txt??????  ??????3???????????????????????????
    if not os.path.exists(new_path):
        os.makedirs(new_path)
    with open(path_txt, 'r') as lines:
        filenames_to_copy = set(line.rstrip() for line in lines)
        # print('filenames_to_copy:',filenames_to_copy)
        # print(len(filenames_to_copy))
    for root, _, filenames in os.walk(search_path):
        # print('root',root)
        # print(_)
        # print(filenames)
        for filename in filenames:
            if filename in filenames_to_copy:
                shutil.copy(os.path.join(root, filename), new_path)

#????????????????????????????????????????????????????????????????????????yolo?????????????????????
copy_file('./images/train/','./images_train.txt','./all_image')
copy_file('./images/val/','./images_trainval.txt','./all_image')
copy_file('./labels/train/','./labels_train.txt','./all_labels')
copy_file('./labels/val/','./labels_trainval.txt','./all_labels')