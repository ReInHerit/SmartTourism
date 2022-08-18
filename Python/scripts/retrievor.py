# necessary packages
from cmath import log
import os
import pickle
import numpy as np
import faiss
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.metrics.pairwise import manhattan_distances
from sklearn.metrics.pairwise import euclidean_distances
import time


class Retrievor:
    def __init__(self, compressor):
        if not os.path.isfile(compressor):
            raise ValueError("File of features doesn't exist")
        self.__load_compressor(compressor)

    def __load_compressor(self, compressor):
        with open(compressor, 'rb') as fp:
            features = pickle.load(fp)
        styles = [f[2] for f in features]
        colors = [f[1] for f in features]
        matrix = [f[0] for f in features]
        self.matrix = np.array(matrix)
        self.colors = np.array(colors)
        self.styles = np.array(styles)


    def compute_distance(self, vector, distance='cosinus'):
        v = vector.reshape(1, -1)
        if distance == 'cosinus':
            return cosine_similarity(self.matrix, v)
        elif distance == 'manhattan':
            return manhattan_distances(self.matrix, v)
        elif distance == 'euclidean':
            return euclidean_distances(self.matrix, v)

    def search(self, wanted, distance='cosinus', depth=1):
        distances = self.compute_distance(wanted, distance).flatten()
        nearest_ids = np.argsort(distances)[:depth].tolist()

        n = distances.shape[0]
        #print("Calculated distances: ",n)
        #print("Worst case n^2: ", n*n)
        #print("Best case n log n: ", n*log(n))
        print("Expected comparisons: ", 1.4*n*log(n))

        return [
            self.colors[nearest_ids].tolist(),
            self.styles[nearest_ids].tolist(),
            distances[nearest_ids].tolist()
        ]

    def searchFAISS(self, wanted, distance='cosinus', depth=1):

        wanted = np.array([wanted])

        #FAISS

        d = self.matrix.shape[1]
        k = depth

       
        index = faiss.IndexFlatL2(d)  # the other index
        assert index.is_trained

        index.add(self.matrix)                  # add may be a bit slower as well

        start = time.time()
        D, I = index.search(wanted, k)     # actual search
        end = time.time()
        print("Searching Time: ", end - start)


        return [
            self.colors[I].tolist(),
            self.styles[I].tolist(),
            D
        ]

    def searchFAISS2(self, wanted, distance='cosinus', depth=1):

        wanted = np.array([wanted])

        #FAISS

        d = self.matrix.shape[1]
        k = depth
        nlist = 100
       
        quantizer = faiss.IndexFlatL2(d)  # the other index
        index = faiss.IndexIVFFlat(quantizer, d, nlist, faiss.METRIC_L2)

        assert not index.is_trained
        start = time.time()
        index.train(self.matrix)
        end = time.time()
        assert index.is_trained

        print("Training Time: ", end - start)

        index.add(self.matrix)                  # add may be a bit slower as well

        start = time.time()
        D, I = index.search(wanted, k)     # actual search
        end = time.time()
        print("Searching Time: ", end - start)

        return [
            self.colors[I].tolist(),
            self.styles[I].tolist(),
            D
        ]