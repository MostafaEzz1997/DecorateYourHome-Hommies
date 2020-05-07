import cv2
import numpy as np

dim = (1024, 768) # new dimenstions

left = cv2.imread('left0.jpg', cv2.IMREAD_COLOR) #read first image

left = cv2.resize(left, dim, interpolation=cv2.INTER_AREA) # ReSize to (1024,768)

right = cv2.imread('right0.jpg', cv2.IMREAD_COLOR)#read second image

right = cv2.resize(right, dim, interpolation=cv2.INTER_AREA)# ReSize to (1024,768)

images = []      #list of images

images.append(left)    #add the first image to the list

images.append(right)   #add the second image to the list

stitcher = cv2.Stitcher.create()
ret, pano = stitcher.stitch(images) #return two values first value panorama of the two images and 1 or pano is empty and 0
#if ret =1  show pano 
if ret == cv2.STITCHER_OK:
    cv2.imshow('Panorama', pano)
    cv2.waitKey()
    cv2.destroyAllWindows()
else:
    print("ERROR")
