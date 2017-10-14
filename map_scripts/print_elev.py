#!/usr/bin/python

from __future__ import print_function

from PIL import Image
import numpy as np
from math import sqrt

vals = []

with open("elevation.data", "rb") as f:
	bytes = f.read()

print(set([ord(b) for b in bytes]))

