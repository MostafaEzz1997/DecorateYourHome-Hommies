
def nothing(x):
    print(x)

#this pointer which holds the cropped points
refPt = []

# callback function for mouse event
def click_and_crop(event, x, y, flags, param):
    global refPt
    # if user pressed left mouse, then store its starting points (x,y)
    if event == cv2.EVENT_LBUTTONDOWN:
        refPt = [(x, y)]
    # on release store last (x,y) in refpt
    elif event == cv2.EVENT_LBUTTONUP:
        refPt.append((x, y))

# this function perform main analysis on furniture image
# it takes one argument which is path to furniture image
# it returns nothing but saving the results (two output images) in (Furniture_with_black_background.png, Furniture_mask.png)

def Furniture_Analysis(Furniture_Path):
    # read the furniture image and store it then make a copy of it
    Furniture_original_image = cv2.imread(Furniture_Path)
    Furniture_image_copy = Furniture_original_image.copy()
    
    # convert it into gray scale image, then make it black and white
    # the idea here is that I want to make a mask from the input (furniture image), to just add it to the room image.
    # So I converted it to gray scale image, then convert it into black and white image to make it easier for canny to detect
    # its edges and to get the best results.
    gray = cv2.cvtColor(Furniture_image_copy, cv2.COLOR_BGR2GRAY)
    (_, black_And_White_Image) = cv2.threshold(gray, 200, 255, cv2.THRESH_BINARY)
    cany = cv2.Canny(black_And_White_Image, 20, 170)
    
    # After detecting edges I've used contours to fill the area between the edges (to make a mask)
    # So I've detected contours, then draw it.
    contours, hirachey = cv2.findContours(cany, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

    for contour in contours:
        cv2.drawContours(Furniture_image_copy, contour, cv2.FILLED, (0, 0, 0), 3)
    
    # here I wanted to have a mask (black shape with a white background) and an image with the same size as the mask but
    # (orginal shape with black background)
    gray_image = cv2.cvtColor(Furniture_image_copy, cv2.COLOR_BGR2GRAY)
    (thresh, blackAndWhiteImage) = cv2.threshold(gray_image, 200, 255, cv2.THRESH_BINARY)
    not_blackAndWhiteImage = cv2.bitwise_not(blackAndWhiteImage)
    Furniture_with_black_background = cv2.bitwise_and(Furniture_original_image, Furniture_original_image,
                                                      mask=not_blackAndWhiteImage)
    # here I'm just storing the results
    cv2.imwrite("Furniture_with_black_background.png", Furniture_with_black_background)
    cv2.imwrite("Furniture_mask.png", blackAndWhiteImage)

# this function simply adds the furniture image to the original image in the area (which user specified)
# it takes two arguments => room image, Furniture image, then passes Furniture image to (Furniture_Analysis function)
# it returns nothing but showing the output result to the user.
def Adding_Furniture_Size(image, Furniture_image_path):
    Furniture_Analysis(Furniture_image_path)
    
    
    cv2.namedWindow("image")
    clone = image.copy()
    cv2.namedWindow("image")
    cv2.setMouseCallback("image", click_and_crop)

    while True:
        cv2.imshow("image", image)
        key = cv2.waitKey(1) & 0xFF
        if key == ord("c"):
            break

    roi = clone[refPt[0][1]:refPt[1][1], refPt[0][0]:refPt[1][0]]

    y_offset = refPt[0][1]
    x_offset = refPt[0][0]
    
    # this part reads the output images (which was created by the previous function)
    Furniture_with_black_background = cv2.imread("Furniture_with_black_background.png")
    Furniture_mask = cv2.imread("Furniture_mask.png")
    
    # resizing the mask to the cropped region
    resized = cv2.resize(Furniture_mask, (roi.shape[1], roi.shape[0]))
    
    # performing bitwise and between our resized masked image and (roi) which is cropped region from the user.
    # which results black object represents (furniture) and original room background
    Out_Image = cv2.bitwise_and(resized, roi)
    
    # This part resizes (Furniture_with_black_background) to make it just like the cropped region
    # then performing bitwise or, which results real object (furniture object) with original room background
    Furniture_with_black_background = cv2.resize(Furniture_with_black_background, (roi.shape[1], roi.shape[0]))
    
    Out1_Image = cv2.bitwise_or(Furniture_with_black_background, Out_Image)
    
    # this part will just add the image to the region which user specified, then showing it
    image[y_offset:y_offset + resized.shape[0], x_offset:x_offset + resized.shape[1]] = Out1_Image

    cv2.imshow("image", image)
    cv2.waitKey()
