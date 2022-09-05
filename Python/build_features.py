# -*- coding: utf-8 -*-
from contextlib import nullcontext
import cv2
import csv
import pickle
import progressbar
import numpy as np
import pandas as pd
from scripts import Extractor
from preprocessors import AspectAwarePreprocessor
from preprocessors import ImageToArrayPreprocessor

from datetime import datetime

def printIMG(image):
    # show the image, provide window name first
    cv2.imshow('image window', image)
    # add wait key. window waits until user presses a key
    cv2.waitKey(0)
    # and finally destroy/close all open windows
    cv2.destroyAllWindows()

# data
data = pd.read_csv('./outputs/train.csv')
# initialize process
iap = ImageToArrayPreprocessor()
aap = AspectAwarePreprocessor(224, 224)

# loop over types
#types = [
#   'AKAZE', 'ORB', 'SURF',
#    'VGG16', 'VGG19', 'MobileNet',
#    'MobileNet'
#    'autoencoder'
#]

types = [
    'MobileNetV3_Large_100',
    'MobileNetV3_Large_075',
    'MobileNetV3_Small_100',

]


# loop over images
for dType in types:
    print('[INFO]: Working with {} ...'.format(dType))
    extractor = Extractor(dType)
    db = []
    widgets = [
        "Extract features: ", progressbar.Percentage(), " ",
        progressbar.Bar(), " ", progressbar.ETA()
    ]
    pbar = progressbar.ProgressBar(maxval=len(data), widgets=widgets).start()

    for index, row in data.iterrows():
        # preprocessing
        print(row.path)
        image = cv2.imread(row.path)
        image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

        h = image.shape[0]
        w = image.shape[1]

        #printIMG(image)

        #AUGUMENTATION 3X3

        imgheight=h - (h%3)
        imgwidth=w - (w%3)

        y1 = 0
        M = imgheight//3
        N = imgwidth//3

        tiles = list()

        for y in range(0,imgheight,M):
            for x in range(0, imgwidth, N):
                y1 = y + M
                x1 = x + N
                tiles.append(image[y:y+M,x:x+N])


        #PREPROCESS IMG

        image,image1,image2 = aap.preprocessAugumentation(image,0.5,0.3)
        if dType in ['VGG16', 'VGG19', 'MobileNet', 'autoencoder','MobileNetV3_Large_100','MobileNetV3_Large_075','MobileNetV3_Small_100']:
            image = iap.preprocess(image)
            image1 = iap.preprocess(image1)
            image2 = iap.preprocess(image2)


        features = extractor.extract(image)
        features1 = extractor.extract(image1)
        features2 = extractor.extract(image2)


        #SAVING
        if isinstance(features, np.ndarray):
            db.append([features, row.color, row.type])
        if isinstance(features1, np.ndarray):
            db.append([features1, row.color, row.type])
        if isinstance(features2, np.ndarray):
            db.append([features2, row.color, row.type])
        
        #PREPROCESS TILES

        for t in tiles:
            t = aap.preprocess(t)
            #printIMG(t)
            if dType in ['VGG16', 'VGG19', 'MobileNet', 'autoencoder','MobileNetV3_Large_100','MobileNetV3_Large_075','MobileNetV3_Small_100',]:
                t = iap.preprocess(t)
            featuresTile = extractor.extract(t)

            #SAVING
            if isinstance(featuresTile, np.ndarray):
                db.append([featuresTile, row.color, row.type])
            

        pbar.update(index)
    pbar.finish()

    #SAVING TO FILE

    now = datetime.now() # current date and time
    date_time = now.strftime("%m%d%Y-%H%M%S")

    with open('./features/' + dType + '_features_v2.pck', 'wb') as fp:
        pickle.dump(db, fp)

    print('Extraction finish. DB saved.')

