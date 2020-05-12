
def nothing(x):
    print(x)


refPt = []


def click_and_crop(event, x, y, flags, param):
    global refPt
    if event == cv2.EVENT_LBUTTONDOWN:
        refPt = [(x, y)]
    elif event == cv2.EVENT_LBUTTONUP:
        refPt.append((x, y))



def Furniture_Analysis(Furniture_Path):
    Furniture_original_image = cv2.imread(Furniture_Path)
    Furniture_image_copy = Furniture_original_image.copy()

    gray = cv2.cvtColor(Furniture_image_copy, cv2.COLOR_BGR2GRAY)
    (_, black_And_White_Image) = cv2.threshold(gray, 200, 255, cv2.THRESH_BINARY)
    cany = cv2.Canny(black_And_White_Image, 20, 170)

    contours, hirachey = cv2.findContours(cany, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)

    for contour in contours:
        cv2.drawContours(Furniture_image_copy, contour, cv2.FILLED, (0, 0, 0), 3)

    gray_image = cv2.cvtColor(Furniture_image_copy, cv2.COLOR_BGR2GRAY)
    (thresh, blackAndWhiteImage) = cv2.threshold(gray_image, 200, 255, cv2.THRESH_BINARY)
    not_blackAndWhiteImage = cv2.bitwise_not(blackAndWhiteImage)
    Furniture_with_black_background = cv2.bitwise_and(Furniture_original_image, Furniture_original_image,
                                                      mask=not_blackAndWhiteImage)

    cv2.imwrite("Furniture_with_black_background.png", Furniture_with_black_background)
    cv2.imwrite("Furniture_mask.png", blackAndWhiteImage)


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

    Furniture_with_black_background = cv2.imread("Furniture_with_black_background.png")
    Furniture_mask = cv2.imread("Furniture_mask.png")

    resized = cv2.resize(Furniture_mask, (roi.shape[1], roi.shape[0]))

    Out_Image = cv2.bitwise_and(resized, roi)

    Furniture_with_black_background = cv2.resize(Furniture_with_black_background, (roi.shape[1], roi.shape[0]))

    Out1_Image = cv2.bitwise_or(Furniture_with_black_background, Out_Image)

    image[y_offset:y_offset + resized.shape[0], x_offset:x_offset + resized.shape[1]] = Out1_Image

    cv2.imshow("image", image)
    cv2.waitKey()
