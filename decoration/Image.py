import argparse
import cv2
import numpy as np




refPt = []    # initialize the list of reference points and boolean indicating
cropping = False     # whether cropping is being performed or not
image = cv2.imread("image.jpg") #read the image

def click_and_crop(event, x, y, flags, param): #cropping function
    # grab references to the global variables
    global refPt, cropping
    # if the left mouse button was clicked, record the starting
    # (x, y) coordinates and indicate that cropping is being
    # performed
    if event == cv2.EVENT_LBUTTONDOWN:
        refPt = [(x, y)]
        cropping = True
    # check to see if the left mouse button was released
    elif event == cv2.EVENT_LBUTTONUP:
        # record the ending (x, y) coordinates and indicate that
        # the cropping operation is finished
        refPt.append((x, y))
        cropping = False
        # draw a rectangle around the region of interest
#        cv2.rectangle(image, refPt[0], refPt[1], (0, 255, 0), 2)
#        cv2.imshow("image", image)

# construct the argument parser and parse the arguments

clone = image.copy()
cv2.namedWindow("image")
cv2.setMouseCallback("image", click_and_crop)
# keep looping until the 'q' key is pressed
while True:
    # display the image and wait for a keypress
    cv2.imshow("image", image)
    key = cv2.waitKey(1) & 0xFF
    # if the 'r' key is pressed, reset the cropping region
    if key == ord("r"):
       image = clone.copy()
    # if the 'c' key is pressed, break from the loop
    elif key == ord("c"):
       break
# if there are two reference points, then crop the region of interest
# from teh image and display it
roi = clone[refPt[0][1]:refPt[1][1], refPt[0][0]:refPt[1][0]] #the cropped image

red_image = cv2.imread("red_color.jpg") #image of the desired decoration to be added

resized = cv2.resize(red_image,(roi.shape[1],roi.shape[0])) #make the size of red_image equal to roi size
# the first two coordinates (top corner)
y_offset = refPt[0][1]
x_offset = refPt[0][0]
image[y_offset:y_offset+resized.shape[0], x_offset:x_offset+resized.shape[1]] = resized #put the dicoration in the original image

cv2.imshow("image", image) #show the final image
cv2.waitKey()

# close all open windows
cv2.destroyAllWindows()
