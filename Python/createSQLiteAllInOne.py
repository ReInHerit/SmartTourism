
import pickle as pkl
import pandas as pd
import xml.etree.cElementTree as ET

import sqlite3

import numpy as np
np.set_printoptions(threshold=np.inf)

with open("./features/MobileNetV3_Small_100_features_v2.pck", "rb") as f:
    object = pkl.load(f)
    
df = pd.DataFrame(object)

con = sqlite3.connect("MN3Small100_v2.sqlite")
cur = con.cursor()

sql_create_table = """ CREATE TABLE IF NOT EXISTS AllInOne (style, color, value) """

cur.execute(sql_create_table)

l = len(object)
k = 1

for matrix,c,s in object:
    # Insert a row of data
    #print(s,c)
    print((k/l)*100)
    k = k+1

    val = str(matrix)
    #print(val)

    sql = ''' INSERT INTO AllInOne (style,color,value)
              VALUES(?,?,?) '''
    new = cur.execute(sql, (s,c,val))

    # Save (commit) the changes
    con.commit()

# We can also close the connection if we are done with it.
# Just be sure any changes have been committed or they will be lost.
con.close()