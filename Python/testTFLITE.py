import numpy as np
from preprocessors.imagetoarraypreprocessor import ImageToArrayPreprocessor
import tensorflow as tf
import time

import cv2
from scripts import Extractor
from scripts import Retrievor
from preprocessors import AspectAwarePreprocessor

augum = True
np.set_printoptions(threshold=np.inf)

def recognize_image(image):
    # Location of tflite model file (int8 quantized)
    model_path = "models/lite-model_imagenet_mobilenet_v3_large_100_224_classification_5_default_1.tflite"  

    # Load TFLite model and allocate tensors.
    interpreter = tf.lite.Interpreter(model_path=model_path)

    # Get input and output tensors.
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # Allocate tensors
    interpreter.allocate_tensors()

    #Preprocess mobilenet
    preprocess = tf.keras.applications.mobilenet_v2.preprocess_input
    image = preprocess(image)

    # Create input tensor out of raw features
    interpreter.set_tensor(input_details[0]['index'], image)

    # Run inference
    start = time.time()
    interpreter.invoke()
    end = time.time()
    print("Inference Time: ", end - start)

    output = interpreter.get_tensor(output_details[0]['index'])
    return output[0,:]



image = cv2.imread('dataFirenze\Palazzo_Vecchio\Palazzo_Vecchio_004.JPG')
image = cv2.cvtColor(image, cv2.COLOR_BGR2RGB)

# initialize process
aap = AspectAwarePreprocessor(224, 224)
itp = ImageToArrayPreprocessor()

# Preprocess image
imageNormal,imageZoom1,imageZoom2 = aap.preprocessAugumentation(image,0.5,0.3)

imageNormal = itp.preprocess(imageNormal)
imageZoom1 = itp.preprocess(imageZoom1)
imageZoom2 = itp.preprocess(imageZoom2)

imageNormal = np.expand_dims(imageNormal, axis=0)
imageZoom1 = np.expand_dims(imageZoom1, axis=0)
imageZoom2 = np.expand_dims(imageZoom2, axis=0)

out = recognize_image(imageNormal)
out1 = recognize_image(imageZoom1)
out2 = recognize_image(imageZoom2)

retrievor = Retrievor('features/MobileNetV3_Large_100_features.pck')

print("FAISS SEARCH: ")
distance = retrievor.searchFAISS(out, depth=3, distance='euclidean')
print("Normal: ")
print(distance)

distance1 = retrievor.searchFAISS(out1, depth=3, distance='euclidean')
print("Zoom 0.5: ")
print(distance1)

distance2 = retrievor.searchFAISS(out2, depth=3, distance='euclidean')
print("Zoom 0.3: ")
print(distance2)



