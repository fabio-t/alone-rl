#!/usr/bin/python

from PIL import Image

with open("elevation.data", "rb") as f:
    bytes = f.read()

img = Image.new('RGB', (2048,2048), "black") # create a new black image
pixels = img.load() # create the pixel map

for i in range(img.size[0]):    # for every pixel:
    for j in range(img.size[1]):
        byte_i = 2048*i + j
        hv = ord(bytes[byte_i]) / 255.0
        if hv < 0.01:
            c = (0, 0, 255) # deep water
        elif hv < 0.05:
            c = (0, 128, 255) # water
        elif hv < 0.08:
            c = (255, 153, 51) # sand
        elif hv < 0.1:
            c = (0, 204, 102) # ground
        elif hv < 0.4:
            c = (0, 255, 0) # grass
        elif hv < 0.7:
            c = (0, 153, 0) # hill grass
        elif hv < 0.8:
            c = (102, 102, 0) # high hill
        elif hv < 0.9:
            c = (102, 51, 0) # mountain
        else:
            c = (192, 192, 192) # high mountain

        pixels[i,j] = c

img.show()

