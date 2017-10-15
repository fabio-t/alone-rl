#!/usr/bin/python

from PIL import Image

import numpy as np

with open("elevation.data", "rb") as f:
    bytes = f.read()

l_bytes = [ord(b) for b in bytes]
a_bytes = np.array(l_bytes)
m_bytes = a_bytes.reshape(2048, 2048)

def lowest_neighbour(grid, row, col):
    v = 256
    x_2 = -1
    y_2 = -1
    for x, y in (
            (row - 1, col), (row + 1, col), (row, col - 1),
            (row, col + 1), (row - 1, col - 1), (row - 1, col + 1),
            (row + 1, col - 1), (row + 1, col + 1)):
        if not (0 <= x < len(grid) and 0 <= y < len(grid[x])):
            # out of bounds
            continue
        if grid[x,y] < v:
            v = grid[x,y]
            x_2 = x
            y_2 = y
    return (x_2, y_2, v)

def lowest_neighbours(grid, row, col):
    v = 256
    for x, y in (
            (row - 1, col), (row + 1, col), (row, col - 1),
            (row, col + 1), (row - 1, col - 1), (row - 1, col + 1),
            (row + 1, col - 1), (row + 1, col + 1)):
        if not (0 <= x < len(grid) and 0 <= y < len(grid[x])):
            # out of bounds
            continue
        if grid[x,y] < v:
            v = grid[x,y]
            neighbours = [(x,y,v)]
        elif grid[x,y] == v:
            neighbours.append((x,y,v))
    return neighbours

# highest point
point = np.unravel_index(m_bytes.argmax(), m_bytes.shape)

# x = point[0]
# y = point[1]
# path = []
# while True:
#     path.append((x, y))
#     x2, y2, v = lowest_neighbour(m_bytes, x, y)
#     if (x2 == -1 and y2 == -1) or v < 12:
#         break
#     x = x2
#     y = y2

points = np.where(m_bytes > 150)
points = zip(points[0], points[1])
np.random.shuffle(points)

path = []
visited = set()

for point in list(points[0:25]):
    x = point[0]
    y = point[1]
    tovisit = set()
    visited.add((x,y))
    tovisit.add((x,y))
    while len(tovisit) > 0:
        tovisitnew = set()
        for x2,y2 in tovisit:
            path.append((x2,y2))
            neighbours = lowest_neighbours(m_bytes, x2, y2)
            # print(neighbours)
            # np.random.shuffle(neighbours)
            for x2,y2,v in neighbours[0:1]:
                if v < 12 or (x2,y2) in visited:
                    continue
                visited.add((x2,y2))
                tovisitnew.add((x2,y2))
        # print(tovisitnew)
        tovisit = tovisitnew

print(path)

for x,y in path:
    m_bytes[x,y] = 11


img = Image.new('RGB', (2048,2048), "black") # create a new black image
pixels = img.load()

for i in range(img.size[0]):
    for j in range(img.size[1]):
        hv = m_bytes[i,j]
        if hv < 3:
            c = (0, 0, 255) # deep water
        elif hv < 12:
            c = (0, 128, 255) # water
        elif hv < 20:
            c = (255, 153, 51) # sand
        elif hv < 25:
            c = (0, 204, 102) # ground
        elif hv < 100:
            c = (0, 255, 0) # grass
        elif hv < 180:
            c = (0, 153, 0) # hill grass
        elif hv < 205:
            c = (102, 102, 0) # high hill
        elif hv < 230:
            c = (102, 51, 0) # mountain
        else:
            c = (192, 192, 192) # high mountain

        pixels[i,j] = c

img.show()

