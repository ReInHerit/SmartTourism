# -*- coding: utf-8 -*-
import cv2
import numpy as np
import tensorflow as tf
from tensorflow.keras.models import Model
from tensorflow.keras.models import load_model
from tensorflow.keras.applications import VGG16
from tensorflow.keras.applications import VGG19
from tensorflow.keras.applications import MobileNetV2


import pathlib




class Extractor:
    def __init__(self, dsc_type, vector_size=32):
        self.size = vector_size
        self.type = dsc_type
        self.preprocess = None
        self.extractor = self.__create_descriptor()

    def __create_descriptor(self):
        if self.type == 'MobileNetV3_Large_100':
            self.preprocess = tf.keras.applications.mobilenet_v2.preprocess_input
            interpreter = tf.lite.Interpreter(model_path="models/lite-model_imagenet_mobilenet_v3_large_100_224_classification_5_default_1.tflite")
            self.input_details = interpreter.get_input_details()
            self.output_details = interpreter.get_output_details()
            return interpreter
        elif self.type == 'MobileNetV3_Large_075':
            self.preprocess = tf.keras.applications.mobilenet_v2.preprocess_input
            interpreter = tf.lite.Interpreter(model_path="models/lite-model_imagenet_mobilenet_v3_large_075_224_classification_5_default_1.tflite")
            self.input_details = interpreter.get_input_details()
            self.output_details = interpreter.get_output_details()
            return interpreter
        elif self.type == 'MobileNetV3_Small_100':
            self.preprocess = tf.keras.applications.mobilenet_v2.preprocess_input
            interpreter = tf.lite.Interpreter(model_path="models/lite-model_imagenet_mobilenet_v3_small_100_224_classification_5_default_1.tflite")
            self.input_details = interpreter.get_input_details()
            self.output_details = interpreter.get_output_details()
            return interpreter
        elif self.type == 'autoencoder':
            return load_model('./outputs/encoder.h5')
        else:
            self.preprocess = tf.keras.applications.mobilenet_v2.preprocess_input
            interpreter = tf.lite.Interpreter(model_path="models/"+self.type)
            self.input_details = interpreter.get_input_details()
            self.output_details = interpreter.get_output_details()
            return interpreter

    def descript(self, image):
        # Dinding image keypoints
        kps = self.extractor.detect(image)
        if not kps:
            return None
        # Getting first 32 of them.
        # Sorting them based on keypoint response value(bigger is better)
        kps = sorted(kps, key=lambda x: -x.response)[:self.size]
        # computing descriptors vector
        kps, dsc = self.extractor.compute(image, kps)
        # Flatten all of them in one big vector - our feature vector
        dsc = dsc.flatten()
        # Making descriptor of same size
        # Descriptor vector size is 64
        needed_size = (self.size * 64)
        if dsc.size < needed_size:
            # if we have less the 32 descriptors then just adding zeros at the
            # end of our feature vector
            dsc = np.concatenate([dsc, np.zeros(needed_size - dsc.size)])
        return np.array(dsc)

    def compress(self, image):
        if self.preprocess:
            image = self.preprocess(image)
        image = np.expand_dims(image, axis=0)

        # Allocate tensors
        self.extractor.allocate_tensors()

        # Create input tensor out of raw features
        self.extractor.set_tensor(self.input_details[0]['index'], image)

        # Run inference
        self.extractor.invoke()

        output = self.extractor.get_tensor(self.output_details[0]['index'])
        output = output[0,:]

        return output

    def extract(self, image):
        if self.type in ['ORB', 'AKAZE', 'SURF']:
            return self.descript(image)
        else:
            return self.compress(image)

