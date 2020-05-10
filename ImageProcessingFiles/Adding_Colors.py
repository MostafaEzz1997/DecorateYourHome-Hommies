import cv2
import numpy as np

def nothing(x):
    print(x)

refPt = []
cropping = False

# function to get the user's click on the screen
def click_and_crop(event, x, y, flags, param):
    global refPt, cropping
    if event == cv2.EVENT_LBUTTONDOWN:
        refPt = [(x, y)]
        cropping = True
    elif event == cv2.EVENT_LBUTTONUP:
        refPt.append((x, y))
        cropping = False

def coloring(image):
	cv2.namedWindow("image")
	# create 3 bars for choosing the value of blue, green and red color
	cv2.createTrackbar('B',"image",0,255,nothing)
	cv2.createTrackbar('G',"image",0,255,nothing)
	cv2.createTrackbar('R',"image",0,255,nothing)
	
	
	clone = image.copy()
	cv2.namedWindow("image")
	# applying the click_and_crop function on the image 
	cv2.setMouseCallback("image", click_and_crop)
	
	while True:
		# displaying the image and wait for the user until finishes selecting
		cv2.imshow("image", image)
		key = cv2.waitKey(1) & 0xFF
		if key == ord("c"):
			break
	
	roi = clone[refPt[0][1]:refPt[1][1], refPt[0][0]:refPt[1][0]]
	
	img = np.zeros((roi.shape[0],roi.shape[1],3),np.uint8)
	
	y_offset = refPt[0][1]
	x_offset = refPt[0][0]
	
	
	while True:
		# wait for the user to choose the color using the bars
		cv2.imshow("name",img)
		k = cv2.waitKey(1) & 0xFF
		if k == 27:
			break
	
		b = cv2.getTrackbarPos("B","image")
		g = cv2.getTrackbarPos("G", "image")
		r = cv2.getTrackbarPos("R", "image")
		img[:] = [b,g,r]
		image[y_offset:y_offset+roi.shape[0], x_offset:x_offset+roi.shape[1]] = img
	
	cv2.imshow("image", image)
	cv2.waitKey()

# take the image and apply the coloring function on it
image = cv2.imread('G:/Try_Stitching/images/scottsdale/IMG_1786-2.jpg')
coloring(image)
