import org.opencv.core.Core;
import org.opencv.core.CvType;
//import org.opencv.core.Mat;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.MatVector;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_stitching.Stitcher;

public class stitch {
	public static void main(String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
         MatVector imgs = new MatVector();
         String result_name = "result.jpg";
         Mat pano = new Mat();
         Mat img1, img2, img3, img4;
         
         img1 = imread("D:\\4th CSE\\IP\\Stitching\\1.jpg");
         img2 = imread("D:\\4th CSE\\IP\\Stitching\\2.jpg");
         img3 = imread("D:\\4th CSE\\IP\\Stitching\\3.jpg");
         img4 = imread("D:\\4th CSE\\IP\\Stitching\\4.jpg");
         imgs.push_back(img1);
         imgs.push_back(img2);
         imgs.push_back(img3);
         imgs.push_back(img4);
         
         Stitcher stitcher = Stitcher.createDefault(true);
         int status = stitcher.stitch(imgs, pano); 
         
         if (status != Stitcher.OK) {
             System.out.println("Can't stitch images, error code = " + status);
         }
         else {
        	 System.out.println("OK");
        	 imwrite(result_name, pano);
         }
         
        
	}
}
