from flask import Flask
from flask import request

###################################################   MODEL CODE
import os
import gc
import sys
import json
import glob
import random
from pathlib import Path

import cv2
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

import itertools
from tqdm import tqdm

from mrcnn.config import Config
from mrcnn import utils
import mrcnn.model as modellib
from mrcnn import visualize
from mrcnn.model import log


#VARIABLES
NUM_CATS = 46
IMAGE_SIZE = 512
ROOT_DIR="Mask_RCNN"
model_path = 'models/mask_rcnn_fashion_0007.h5'

def resize_image(image_path):
    img = cv2.imread(image_path)
    img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img = cv2.resize(img, (IMAGE_SIZE, IMAGE_SIZE), interpolation=cv2.INTER_AREA)  
    return img

class FashionConfig(Config):
    NAME = "fashion"
    NUM_CLASSES = NUM_CATS + 1 # +1 for the background class
    
    GPU_COUNT = 1
    IMAGES_PER_GPU = 4 # a memory error occurs when IMAGES_PER_GPU is too high
    
    BACKBONE = 'resnet50'
    
    IMAGE_MIN_DIM = IMAGE_SIZE
    IMAGE_MAX_DIM = IMAGE_SIZE    
    IMAGE_RESIZE_MODE = 'none'
    
    RPN_ANCHOR_SCALES = (16, 32, 64, 128, 256)
    #DETECTION_NMS_THRESHOLD = 0.0
    
    # STEPS_PER_EPOCH should be the number of instances 
    # divided by (GPU_COUNT*IMAGES_PER_GPU), and so should VALIDATION_STEPS;
    # however, due to the time limit, I set them so that this kernel can be run in 9 hours
    STEPS_PER_EPOCH = 1000
    VALIDATION_STEPS = 200

class InferenceConfig(FashionConfig):
    GPU_COUNT = 1
    IMAGES_PER_GPU = 1

inference_config = InferenceConfig()
model = modellib.MaskRCNN(mode="inference",
                          config=inference_config,
                          model_dir=ROOT_DIR
)

model.load_weights('/Users/shankar99/Documents/SSN/projects/fashionMaskRCNN/mask_rcnn_fashion_0007.h5',by_name=True)

image_path="/Users/shankar99/Documents/SSN/projects/fashionMaskRCNN/images/004e9e21cd1aca568a8ffc77a54638ce.jpg"
image_id="004e9e21cd1aca568a8ffc77a54638ce.jpg"
result = model.detect([resize_image(image_path)])
r = result[0]
print(r)

label_path='/Users/shankar99/Documents/SSN/projects/fashionMaskRCNN/label_descriptions.json'

with open(label_path) as f:
    label_descriptions = json.load(f)

label_names = [x['name'] for x in label_descriptions['categories']]

print(label_names[r['class_ids'][1]])

output = label_names[r['class_ids'][1]]

##################################################### FLASK API


app = Flask(__name__)

@app.route('/',methods=['GET','POST'])

def handle_request():
	return output

app.run(host="0.0.0.0",port=5000,debug=True)