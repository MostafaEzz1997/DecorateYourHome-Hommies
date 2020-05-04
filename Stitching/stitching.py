import cv2
import numpy as np

dim = (1024, 768)
left = cv2.imread('1.jpg', cv2.IMREAD_COLOR)
# ReSize to (1024,768)
left = cv2.resize(left, dim, interpolation=cv2.INTER_AREA)

medium = cv2.imread('2.jpg', cv2.IMREAD_COLOR)
# ReSize to (1024,768)
medium = cv2.resize(medium, dim, interpolation=cv2.INTER_AREA)

right = cv2.imread('3.jpg', cv2.IMREAD_COLOR)
# ReSize to (1024,768)
right = cv2.resize(right, dim, interpolation=cv2.INTER_AREA)

max_right = cv2.imread('4.jpg', cv2.IMREAD_COLOR)
# ReSize to (1024,768)
max_right = cv2.resize(max_right, dim, interpolation=cv2.INTER_AREA)


images = []
images.append(left)
images.append(medium)
images.append(right)
images.append(max_right)
stitcher = cv2.Stitcher.create()
ret, pano = stitcher.stitch(images)

if ret == cv2.STITCHER_OK:
    cv2.imshow('Panorama', pano)
    cv2.imwrite('output.jpg', pano)
    cv2.waitKey()
    cv2.destroyAllWindows()
else:
    print("ERROR")
