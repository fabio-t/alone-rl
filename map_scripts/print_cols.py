#!/usr/bin/python

from __future__ import print_function

from PIL import Image
import numpy as np
from math import sqrt

im = Image.open("map.png")
pix = im.load()

w,h = im.size

print(im.size)

all_cols = []

for x in range(w):
	for y in range(h):
		all_cols.append(pix[x,y])

unique_cols = set(all_cols)

print(unique_cols)

print("unique cols:", len(unique_cols))

w = int(sqrt(len(unique_cols)))
h = w + 1

im2 = Image.new("RGB", (w,h))
im2.putdata(sorted((unique_cols)))
im2.save("cols.png")
