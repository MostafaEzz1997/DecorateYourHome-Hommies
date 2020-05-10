from PyQt5.QtWidgets import *
from PyQt5.QtGui import *
from PyQt5 import QtGui
import sys
import cv2
import numpy as np

#
#
#
#
#######################        Stitching Code      #######################
def Stitch(x):
    images = []
    dim = (1024, 768)
    for i in x:
        n = cv2.imread(i, cv2.IMREAD_COLOR)
        c = cv2.resize(n, dim, interpolation=cv2.INTER_AREA)
        images.append(c)
    stitcher = cv2.Stitcher.create()
    ret, pano = stitcher.stitch(images)
    if ret == cv2.STITCHER_OK:
        return pano
    else:
        return ("Error")
#
#
#
#
##################      end of stitching function       ##############



#
#
#
#
###############     Start of coloring code    ######################
#
#
#
#

def nothing(x):
    print(x)


refPt = []
cropping = False


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
    cv2.createTrackbar('B', "image", 0, 255, nothing)
    cv2.createTrackbar('G', "image", 0, 255, nothing)
    cv2.createTrackbar('R', "image", 0, 255, nothing)

    clone = image.copy()
    cv2.namedWindow("image")
    cv2.setMouseCallback("image", click_and_crop)

    while True:
        cv2.imshow("image", image)
        key = cv2.waitKey(1) & 0xFF
        if key == ord("c"):
            break

    roi = clone[refPt[0][1]:refPt[1][1], refPt[0][0]:refPt[1][0]]

    img = np.zeros((roi.shape[0], roi.shape[1], 3), np.uint8)

    y_offset = refPt[0][1]
    x_offset = refPt[0][0]

    while True:
        cv2.imshow("name", img)
        k = cv2.waitKey(1) & 0xFF
        if k == 27:
            break

        b = cv2.getTrackbarPos("B", "image")
        g = cv2.getTrackbarPos("G", "image")
        r = cv2.getTrackbarPos("R", "image")
        img[:] = [b, g, r]
        image[y_offset:y_offset + roi.shape[0], x_offset:x_offset + roi.shape[1]] = img

    cv2.imshow("image", image)
    cv2.waitKey()


#
#
#
#
###############     end of coloring code    ######################






#
#
#
#
#############     Start of decoration code      #############
#
#
#
#

def Decoration(image, decore_image):
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
    roi = clone[refPt[0][1]:refPt[1][1], refPt[0][0]:refPt[1][0]]

    Decoration_image = cv2.imread(decore_image)

    resized = cv2.resize(Decoration_image, (roi.shape[1], roi.shape[0]))

    y_offset = refPt[0][1]
    x_offset = refPt[0][0]
    image[y_offset:y_offset + resized.shape[0], x_offset:x_offset + resized.shape[1]] = resized

    cv2.imshow("image", image)
    cv2.waitKey()

#
#
#
#
####################      end of decoration code      ####################
#
#
#
#
#
#
#
#
####################      Start of Adding Furniture Code      ####################
#
#
#
#

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

def Adding_Furniture_with_original_size(image, Furniture_image_path):
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
    Furniture_with_black_background = cv2.imread("Furniture_with_black_background.png")
    Furniture_mask = cv2.imread("Furniture_mask.png")

    y_center = refPt[0][1]
    x_center = refPt[0][0]

    y, x, _ = Furniture_with_black_background.shape

    top_x = int(x_center - (x / 2))
    top_y = int(y_center - (y / 2))

    cutted_image = image[top_y:top_y+y, top_x:top_x+x]

    Out_Image = cv2.bitwise_and(cutted_image,Furniture_mask)
    Final_Out = cv2.bitwise_or(Furniture_with_black_background, Out_Image)

    image[top_y:top_y+y, top_x:top_x+x] = Final_Out

    cv2.imshow("image", image)
    cv2.waitKey()


#
#
#
#
#
############       end of Adding Furniture code      ###############
#
#
#
#

#
#
#
#
####################       Start of GUI Code       ####################
#
#
#
#
class FileDialog(QFileDialog):
    def __init__(self, *args, **kwargs):
        super(FileDialog, self).__init__(*args, **kwargs)
        self.setOption(QFileDialog.DontUseNativeDialog, True)
        self.setFileMode(QFileDialog.ExistingFiles)

    def accept(self):
        super(FileDialog, self).accept()



class Window(QMainWindow):
    def __init__(self):
        super().__init__()
        self.title = "Hommies"
        self.top = 100
        self.left = 200
        self.width = 600
        self.hight = 500

        self.InitWindow()

    def InitWindow(self):
        self.flag = 0
        self.setWindowTitle(self.title)
        self.setGeometry(self.top,self.left,self.width,self.hight)

        self.label = QLabel(self)
        self.label.setPixmap(QPixmap('image.jpg'))
        self.label.setGeometry(200,20,200,150)

        self.button = QPushButton('Color Your Room', self)
        self.button.setFont(QtGui.QFont("Sanserif", 15))
        self.button.clicked.connect(self.ColoringWindow)
        self.button.resize(300, 40)
        self.button.move(100, 200)

        self.button2 = QPushButton('Put Some Furniture', self)
        self.button2.setFont(QtGui.QFont("Sanserif", 15))
        self.button2.clicked.connect(self.FurnitureWindow)
        self.button2.resize(300, 40)
        self.button2.move(220, 300)
        self.show()

    def ColoringWindow(self):
        mydialog = QDialog(self)
        mydialog.setWindowTitle(self.title)
        mydialog.setGeometry(self.top, self.left, self.width, self.hight)

        mydialog.Choose_RImages_button = QPushButton('Choose Your Room Images', mydialog)
        mydialog.Choose_RImages_button.setFont(QtGui.QFont("Sanserif", 15))
        mydialog.Choose_RImages_button.clicked.connect(self.Select_Your_Room_Images)
        mydialog.Choose_RImages_button.resize(300, 40)
        mydialog.Choose_RImages_button.move(100, 100)

        mydialog.Choose_DImages_button = QPushButton('Choose Your Decoration Image', mydialog)
        mydialog.Choose_DImages_button.setFont(QtGui.QFont("Sanserif", 15))
        mydialog.Choose_DImages_button.clicked.connect(self.Choose_DImages)
        mydialog.Choose_DImages_button.resize(300, 40)
        mydialog.Choose_DImages_button.move(100, 200)

        mydialog.Choose_CImages_button = QPushButton('Choose Your Colors', mydialog)
        mydialog.Choose_CImages_button.setFont(QtGui.QFont("Sanserif", 15))
        mydialog.Choose_CImages_button.clicked.connect(self.Choose_CImages)
        mydialog.Choose_CImages_button.resize(300, 40)
        mydialog.Choose_CImages_button.move(100, 300)

        self.hide()
        mydialog.show()


    def FurnitureWindow(self):
        mydialog2 = QDialog(self)
        mydialog2.setWindowTitle(self.title)
        mydialog2.setGeometry(self.top, self.left, self.width, self.hight)

        mydialog2.Choose_RImages_button = QPushButton('Choose Your Room Images', mydialog2)
        mydialog2.Choose_RImages_button.setFont(QtGui.QFont("Sanserif", 15))
        mydialog2.Choose_RImages_button.clicked.connect(self.Select_Your_Room_Images)
        mydialog2.Choose_RImages_button.resize(500, 40)
        mydialog2.Choose_RImages_button.move(75, 100)

        mydialog2.Choose_FImages_button_ORI = QPushButton('Choose Your Furniture Image with its original size', mydialog2)
        mydialog2.Choose_FImages_button_ORI.setFont(QtGui.QFont("Sanserif", 15))
        mydialog2.Choose_FImages_button_ORI.clicked.connect(self.Choose_FImages)
        mydialog2.Choose_FImages_button_ORI.resize(500, 40)
        mydialog2.Choose_FImages_button_ORI.move(75, 200)

        mydialog2.Choose_FImages_button_USI = QPushButton('Choose Your Furniture Image Dynamically sized', mydialog2)
        mydialog2.Choose_FImages_button_USI.setFont(QtGui.QFont("Sanserif", 15))
        mydialog2.Choose_FImages_button_USI.clicked.connect(self.Choose_FImages_USI)
        mydialog2.Choose_FImages_button_USI.resize(500, 40)
        mydialog2.Choose_FImages_button_USI.move(75, 300)


        self.hide()
        mydialog2.show()


    def Select_Your_Room_Images(self):
        dialog = FileDialog()
        self.flag = 1
        if dialog.exec_() == QDialog.Accepted:
            print(dialog.selectedFiles())
            self.show()
            self.Selected_Images = dialog.selectedFiles()
            self.Stitched_Image = Stitch(self.Selected_Images)


    ############     select multiple images and store its paths, then pass them to stitching function      #############


    def Choose_DImages(self):
        if self.flag == 1:
            fname = QFileDialog.getOpenFileName(self, 'Open File', 'c\\', 'Image files (*.jpg *.png)')
            Image_Path_For_DImage = fname[0]
            Decoration(self.Stitched_Image,Image_Path_For_DImage)
            self.show()
        else:
            self.show()
            QMessageBox.about(self,"Error Message","Please Choose Your Room Images First ^_^")
        #######      Call Inserting_Image code and pass var out image to it     ########


    def Choose_CImages(self):
        if self.flag == 1:
            coloring(self.Stitched_Image)
        else:
            self.show()
            QMessageBox.about(self,"Error Message","Please Choose Your Room Images First ^_^")

        #######      Call Coloring_Image code and pass var out image to it     ########


    def Choose_FImages(self):
        if self.flag == 1:
            fname = QFileDialog.getOpenFileName(self, 'Open File', 'c\\', 'Image files (*.jpg *.png)')
            Image_Path_For_FImages = fname[0]
            Adding_Furniture_with_original_size(self.Stitched_Image,Image_Path_For_FImages)
            self.show()

        else:
            self.show()
            QMessageBox.about(self,"Error Message","Please Choose Your Room Images First ^_^")


        #######      Call Adding_Furniture_To_Image code and pass var out image to it     ########


    def Choose_FImages_USI(self):
        if self.flag == 1:
            fname = QFileDialog.getOpenFileName(self, 'Open File', 'c\\', 'Image files (*.jpg *.png)')
            Image_Path_For_FImages = fname[0]
            Adding_Furniture_Size(self.Stitched_Image,Image_Path_For_FImages)
            self.show()

        else:
            self.show()
            QMessageBox.about(self,"Error Message","Please Choose Your Room Images First ^_^")

if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = Window()
    # ex = Widget()
    sys.exit(app.exec_())