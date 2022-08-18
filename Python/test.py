# -*- coding: utf-8 -*-
import cv2
import pickle
import argparse
import progressbar
import numpy as np
from scripts import Extractor
from scripts import Retrievor
from preprocessors import AspectAwarePreprocessor
from preprocessors import ImageToArrayPreprocessor
import time

import tensorflow as tf

from PIL import Image

#np.set_printoptions(threshold=np.inf)


# initialize process
aap = AspectAwarePreprocessor(224, 224)
iap = ImageToArrayPreprocessor()


image = cv2.imread('dataFirenze\Palazzo_Vecchio\Palazzo_Vecchio_003.JPG')
#image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

#img = Image.open('dataFirenze\Palazzo_Vecchio\Palazzo_Vecchio_004.JPG')
#image2 = np.array(img)

image = aap.preprocess(image)

image = iap.preprocess(image)

extractor = Extractor("MobileNet") #return model

retrievor = Retrievor('features/RGB-features.pck')

features = extractor.extract(image)

#print("features: ", features)

print("QUICKSORT SEARCH: ")

start = time.time()
distance = retrievor.search(features, depth=5, distance='euclidean')
end = time.time()

print(distance)

print("Time: ", end - start)


print("FAISS SEARCH: ")

start = time.time()
distance = retrievor.searchFAISS(features, depth=5, distance='euclidean')
end = time.time()

print(distance)

print("Time: ", end - start)

print("FAISS SEARCH 2: ")

start = time.time()
distance = retrievor.searchFAISS2(features, depth=5, distance='euclidean')
end = time.time()

print(distance)

print("Time: ", end - start)

