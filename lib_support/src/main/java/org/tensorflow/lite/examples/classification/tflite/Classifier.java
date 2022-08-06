/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package org.tensorflow.lite.examples.classification.tflite;

import static java.lang.Math.min;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.Rot90Op;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

/** A classifier specialized to label images using TensorFlow Lite. */
public abstract class Classifier {
  public static final String TAG = "ClassifierWithSupport";
  private static final int K_TOP_RESULT = 10;

  /** The model type used for classification. */
  public enum Model {
    FLOAT_MOBILENET,
    QUANTIZED_MOBILENET,
    FLOAT_EFFICIENTNET,
    QUANTIZED_EFFICIENTNET
  }

  /** The runtime device type used for executing classification. */
  public enum Device {
    CPU,
    NNAPI,
    GPU
  }

  /** Number of results to show in the UI. */
  private static final int MAX_RESULTS = 3;

  /** The loaded TensorFlow Lite model. */

  /** Image size along the x axis. */
  private final int imageSizeX;

  /** Image size along the y axis. */
  private final int imageSizeY;

  /** Optional GPU delegate for accleration. */
  private GpuDelegate gpuDelegate = null;

  /** Optional NNAPI delegate for accleration. */
  private NnApiDelegate nnApiDelegate = null;

  /** An instance of the driver class to run model inference with Tensorflow Lite. */
  protected Interpreter tflite;

  /** Options for configuring the Interpreter. */
  private final Interpreter.Options tfliteOptions = new Interpreter.Options();

  /** Labels corresponding to the output of the vision model. */
  //private final List<String> labels;

  /** Input image TensorBuffer. */
  private TensorImage inputImageBuffer;

  /** Output probability TensorBuffer. */
  private final TensorBuffer outputProbabilityBuffer;

  /** Processer to apply post processing of the output probability. */
  private final TensorProcessor probabilityProcessor;

  private final Retrievor retrievor;

  private Context context;

  /**
   * Creates a classifier with the provided configuration.
   *
   * @param activity The current Activity.
   * @param model The model to use for classification.
   * @param device The device to use for classification.
   * @param numThreads The number of threads to use for classification.
   * @return A classifier with the desired configuration.
   */
  public static Classifier create(Activity activity, Model model, Device device, int numThreads)
      throws IOException {

    if (model == Model.QUANTIZED_MOBILENET) {
      return new ClassifierQuantizedMobileNet(activity, device, numThreads);
    } else if (model == Model.FLOAT_MOBILENET) {
      return new ClassifierFloatMobileNet(activity, device, numThreads);
    } else if (model == Model.FLOAT_EFFICIENTNET) {
      return new ClassifierFloatEfficientNet(activity, device, numThreads);
    } else if (model == Model.QUANTIZED_EFFICIENTNET) {
      return new ClassifierQuantizedEfficientNet(activity, device, numThreads);
    } else {
      throw new UnsupportedOperationException();
    }
  }

  /** An immutable result returned by a Classifier describing what was recognized. */
  public static class Recognition {
    /**
     * A unique identifier for what has been recognized. Specific to the class, not the instance of
     * the object.
     */
    private final String id;

    /** Display name for the recognition. */
    private final String title;

    /**
     * A sortable score for how good the recognition is relative to others. Higher should be better.
     */
    private Double confidence;

    /** Optional location within the source image for the location of the recognized object. */
    private RectF location;

    public Recognition(
            final String id, final String title, Double confidence, final RectF location) {
      this.id = id;
      this.title = title;
      this.confidence = confidence;
      this.location = location;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }

    public Double getConfidence() {
      return confidence;
    }

    public void setConfidence(Double confidence) {
      this.confidence=confidence;
    }

    public RectF getLocation() {
      return new RectF(location);
    }

    public void setLocation(RectF location) {
      this.location = location;
    }

    @NonNull
    @Override
    public String toString() {
      String resultString = "";
      if (id != null) {
        resultString += "[" + id + "] ";
      }

      if (title != null) {
        resultString += title + " ";
      }

      if (confidence != null) {
        resultString += String.format("(%.1f%%) ", confidence * 100.0f);
      }

      if (location != null) {
        resultString += location + " ";
      }

      return resultString.trim();
    }
  }

  /** Initializes a {@code Classifier}. */
  protected Classifier(Activity activity, Device device, int numThreads) throws IOException {

    retrievor = new Retrievor(activity);
    this.context = activity.getApplicationContext();

    MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(activity, getModelPath());
    switch (device) {
      case NNAPI:
        nnApiDelegate = new NnApiDelegate();
        tfliteOptions.addDelegate(nnApiDelegate);
        break;
      case GPU:
        CompatibilityList compatList = new CompatibilityList();
        if(compatList.isDelegateSupportedOnThisDevice()){
          // if the device has a supported GPU, add the GPU delegate
          GpuDelegate.Options delegateOptions = compatList.getBestOptionsForThisDevice();
          GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
          tfliteOptions.addDelegate(gpuDelegate);
          Log.d(TAG, "GPU supported. GPU delegate created and added to options");
        } else {
          tfliteOptions.setUseXNNPACK(true);
          Log.d(TAG, "GPU not supported. Default to CPU.");
        }
        break;
      case CPU:
        tfliteOptions.setUseXNNPACK(true);
        Log.d(TAG, "CPU execution");
        break;
    }
    tfliteOptions.setNumThreads(numThreads);
    tflite = new Interpreter(tfliteModel, tfliteOptions);

    // Loads labels out from the label file.
    //labels = FileUtil.loadLabels(activity, getLabelPath());

    // Reads type and shape of input and output tensors, respectively.
    int imageTensorIndex = 0;
    int[] imageShape = tflite.getInputTensor(imageTensorIndex).shape(); // {1, height, width, 3}
    imageSizeY = imageShape[1];
    imageSizeX = imageShape[2];

    DataType imageDataType = tflite.getInputTensor(imageTensorIndex).dataType();
    int probabilityTensorIndex = 0;
    int[] probabilityShape =
        tflite.getOutputTensor(probabilityTensorIndex).shape(); // {1, NUM_CLASSES}
    DataType probabilityDataType = tflite.getOutputTensor(probabilityTensorIndex).dataType();

    // Creates the input tensor.
    inputImageBuffer = new TensorImage(imageDataType);


    // Creates the output tensor and its processor.
    outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);


    // Creates the post processor for the output probability.
    probabilityProcessor = new TensorProcessor.Builder().add(getPostprocessNormalizeOp()).build();

    Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
  }

  /** Runs inference and returns the classification results. */
  public List<Recognition> recognizeImage(final Bitmap bitmap, int sensorOrientation) {
    // Logs this method so that it can be analyzed with systrace.

    Trace.beginSection("recognizeImage");

    //Load image
    Trace.beginSection("loadImage");
    long startTimeForLoadImage = SystemClock.uptimeMillis();
    inputImageBuffer = loadImage(bitmap, sensorOrientation);
    long endTimeForLoadImage = SystemClock.uptimeMillis();
    Trace.endSection();



    Bitmap b =inputImageBuffer.getBitmap();

    int[] pixels = new int[b.getHeight()*b.getWidth()];

    b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());

    for (int i=0; i<b.getWidth(); i++){
      int p = pixels[i];

      int R = (p >> 16) & 0xff;
      int G = (p >> 8) & 0xff;
      int B = p & 0xff;

      Log.v("InputBuffer",R+" "+G+" "+B);

    }




    Log.v(TAG, "Timecost to load the image: " + (endTimeForLoadImage - startTimeForLoadImage));
    // Runs the inference call.
    Trace.beginSection("runInference");
    long startTimeForReference = SystemClock.uptimeMillis();
    //LITTLE ENDIAN ORDER
    tflite.run(inputImageBuffer.getBuffer(), outputProbabilityBuffer.getBuffer().rewind()); //RUN INFERENCE
    long endTimeForReference = SystemClock.uptimeMillis();
    Trace.endSection();
    Log.v(TAG, "Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

    //Get features
    float [] features = outputProbabilityBuffer.getFloatArray();
    //float[] features = probabilityProcessor.process(outputProbabilityBuffer).getFloatArray();

    //ArrayList<Element> result = retrievor.getNearest(features,K_TOP_RESULT);

    //Faiss Search
    Trace.beginSection("runFaissSearch");
    long startTimeForFaiss = SystemClock.uptimeMillis();
    ArrayList<Element> result = retrievor.faissSearch(features,K_TOP_RESULT);
    long endTimeForFaiss = SystemClock.uptimeMillis();
    Log.v(TAG, "Timecost to run Faiss Search: " + (endTimeForFaiss - startTimeForFaiss));
    Trace.endSection();

    // Gets top-k results.
    Trace.beginSection("runPostProcess");
    long startTimeForPostProcess = SystemClock.uptimeMillis();
    Map<String, Double> labeledDistance = createMap(result);
    List<Recognition> finalResult = getTopKProbability(labeledDistance);
    long endTimeForPostProcess = SystemClock.uptimeMillis();
    Log.v(TAG, "Timecost to run Post Process: " + (endTimeForPostProcess - startTimeForPostProcess));
    Trace.endSection();

    Trace.endSection(); //end recognize image section

    String app = "";
    for (Float f: features) {
      app+=f+" ";
    }

    //Log.v(TAG, "input" + Arrays.toString(inputImageBuffer.getBuffer().array()));
    Log.v(TAG,"features: "+app);
    Log.v(TAG,"result: "+result.toString());
    Log.v(TAG,"finalResult: "+finalResult.toString());

    return finalResult;
  }

  /** Closes the interpreter and model to release resources. */
  public void close() {
    if (tflite != null) {
      tflite.close();
      tflite = null;
    }
    if (gpuDelegate != null) {
      gpuDelegate.close();
      gpuDelegate = null;
    }
    if (nnApiDelegate != null) {
      nnApiDelegate.close();
      nnApiDelegate = null;
    }
  }

  /** Get the image size along the x axis. */
  public int getImageSizeX() {
    return imageSizeX;
  }

  /** Get the image size along the y axis. */
  public int getImageSizeY() {
    return imageSizeY;
  }

  /** Loads input image, and applies preprocessing. */
  private TensorImage loadImage(Bitmap bitmap, int sensorOrientation) {
    // Loads bitmap into a TensorImage.


    //Try with particular image
    InputStream in = null;
    BitmapFactory.Options options = null;
    try{
      in = context.getAssets().open("images/Palazzo_Vecchio_004.JPG");
      options = new BitmapFactory.Options();
      options.inPreferredConfig = Bitmap.Config.ARGB_8888;
      bitmap = BitmapFactory.decodeStream(in,null,options);

    }catch (Exception e){

    }

    //int[] pixels = new int[bitmap.getHeight()*bitmap.getWidth()];

    //bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

    /*
    for (int i=0; i<bitmap.getHeight()*bitmap.getWidth(); i++){
      int p = pixels[i];

      int R = (p >> 16) & 0xff;
      int G = (p >> 8) & 0xff;
      int B = p & 0xff;

      //Log.v(TAG, i+": "+R+" "+G+" "+B);
    }*/


    inputImageBuffer.load(bitmap); //image in ARGB_8888


    // Creates processor for the TensorImage.
    int cropSize = min(bitmap.getWidth(), bitmap.getHeight());
    int numRotation = sensorOrientation / 90;


    ImageProcessor imageProcessor =
        new ImageProcessor.Builder()
            .add(new ResizeWithCropOrPadOp(cropSize, cropSize)) //fa diventare immagine quadrata senza perdita risoluzione
            // To get the same inference results as lib_task_api, which is built on top of the Task
            // Library, use ResizeMethod.BILINEAR. ERA (ResizeMethod.NEAREST_NEIGHBOR)
            .add(new ResizeOp(imageSizeX, imageSizeY, ResizeMethod.NEAREST_NEIGHBOR))
            //.add(new ResizeOp(224,224,ResizeMethod.BILINEAR))
            //.add(new Rot90Op(numRotation)) //numRotation instead of 0
            //.add(getPreprocessNormalizeOp())
            .build();
    return imageProcessor.process(inputImageBuffer);
  }


  private static Map<String, Double> createMap(List<Element> results) {

    Map<String, Double> labeledProbability = new TreeMap<String, Double>();

    int size = min(K_TOP_RESULT,results.size());

    for (int i = 0; i < size; i++){
      Element e = results.get(i);
      String color = e.getColor();
      String style = e.getStyle();
      double distance = e.getDistance();

      String newKey = color+" "+style;

      if (labeledProbability.containsKey(newKey)){
        double value = labeledProbability.get(newKey);
        double newValue = (value*2+distance)/3; //average with more importance on first positions
        labeledProbability.put(newKey,newValue);

      }else {
        labeledProbability.put(newKey, distance);
      }
    }

    return labeledProbability;
  }

  /** Gets the top-k results. */
  private static List<Recognition> getTopKProbability(Map<String, Double> labelProb) {
    // Find the best classifications.
    PriorityQueue<Recognition> pq =
        new PriorityQueue<>(
            MAX_RESULTS,
            new Comparator<Recognition>() {
              @Override
              public int compare(Recognition lhs, Recognition rhs) {
                // Intentionally re-(from me)reversed to put high confidence at the head of the queue.
                return Double.compare(lhs.getConfidence(),rhs.getConfidence());
              }
            });

    for (Map.Entry<String, Double> entry : labelProb.entrySet()) {
      pq.add(new Recognition("" + entry.getKey(), entry.getKey(), entry.getValue(), null));
    }

    final ArrayList<Recognition> recognitions = new ArrayList<>();
    int recognitionsSize = min(pq.size(), MAX_RESULTS);
    for (int i = 0; i < recognitionsSize; ++i) {
      recognitions.add(pq.poll());
    }

    return recognitions;
  }

  /** Gets the name of the model file stored in Assets. */
  protected abstract String getModelPath();

  /** Gets the name of the label file stored in Assets. */
  protected abstract String getLabelPath();

  /** Gets the TensorOperator to nomalize the input image in preprocessing. */
  protected abstract TensorOperator getPreprocessNormalizeOp();

  /**
   * Gets the TensorOperator to dequantize the output probability in post processing.
   *
   * <p>For quantized model, we need de-quantize the prediction with NormalizeOp (as they are all
   * essentially linear transformation). For float model, de-quantize is not required. But to
   * uniform the API, de-quantize is added to float model too. Mean and std are set to 0.0f and
   * 1.0f, respectively.
   */
  protected abstract TensorOperator getPostprocessNormalizeOp();
}
