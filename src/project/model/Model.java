package project.model;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Class model of the MVC from the Albert Image Downloader.
 * 
 * @author Alberto Casas Ortiz.
 */
public class Model {
	/** Admitted formats of images. */
	public static String imageFormatList[] = {"BMP", "GIF", "IMG", "JPEG", "JPG", "PNG", "TIFF"};
	/** URL of the origin web. */
	private String URL;
	/** Array containing selected image formats. */
	private ArrayList<String> imageFormats;
	/** Minimum width of images. */
	private int minWidth;
	/** Minimum height of images. */
	private int minHeight;
	/** Progress of the download. */
	private int progress;
	
	/**
	 * Constructor for class Image Saver.
	 */
	public Model() {
		this.URL = "";
		this.imageFormats = new ArrayList<String>();
		this.minWidth = 100;
		this.minHeight = 100;
		this.progress = 0;
	}

	/**
	 * Constructor for class Image Saver.
	 * @param URL URL of the page containing images.
	 * @param imageFormats Image formats to be downloaded.
	 * @param minWidth Minimum width of the images.
	 * @param minHeight Minimum height of the images.
	 */
	public void setValues(String URL, ArrayList<String> imageFormats, int minWidth, int minHeight) {
		this.URL = URL;
		this.imageFormats = imageFormats;
		this.minWidth = minWidth;
		this.minHeight = minHeight;
	}
	
	/**
	 * Save images from an URL with determined formats and minimum size.
	 * 
	 * @throws IOException
	 */
	public void saveImages() throws IOException, IllegalArgumentException {
		//Reset progress.
		this.progress = 0;
		
		//Get HTML from URL.
		Document doc = connectAndGetHTML(this.URL);
		
		//Get URLs of images in HTML code.
		ArrayList<URL> imagesURLs = selectImagesFromDocument(doc);

		//Create file which will contain the images.
		File file = new File("images//");
		file.mkdirs();
		
		//Download images.
		RenderedImage image = null;
		for (int i = 0; i < imagesURLs.size(); i++) {
			URL url = imagesURLs.get(i);
			//Read image.
			image = ImageIO.read(url);
			//Get name of the image.
			String[] splitted = url.getPath().split("/");
			String imageName = splitted[splitted.length-1];
			//Get format of the image.
			splitted = imageName.split("\\.");
			String imageFormat = splitted[splitted.length-1];
			//Save image if the image satisfy conditions.
			if(this.saveConditions(image)){
				//Create name of the image.
				file = new File("images//" + imageName);
				file.createNewFile();
				//Save image.
				ImageIO.write(image, imageFormat, file);
				//Update progress.
				this.progress = (100*i)/imagesURLs.size();
			}
		}
		this.progress = 100;
	}

	public int getProgress(){
		return this.progress;
	}
	
	/**
	 * Connect to URL and get HTML in a document.
	 * @return Document with HTML.
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public static Document connectAndGetHTML(String URL) throws IOException, IllegalArgumentException{
		return Jsoup.connect(URL).userAgent("Google").get();
	}
	
	/**
	 * Select all URLs of images an save it in an array.
	 * @param doc Document with HTML.
	 * @return ArrayList with URLs.
	 * @throws MalformedURLException 
	 */
	public ArrayList<URL> selectImagesFromDocument(Document doc) throws MalformedURLException{
		ArrayList<URL> urls = new ArrayList<URL>();
		Elements images = new Elements();
		
		//Get images from all image formats.
		for(int i = 0; i < this.imageFormats.size(); i++){
			if(Arrays.asList(Model.imageFormatList).contains(this.imageFormats.get(i).toUpperCase()))
				images.addAll(doc.select("img[src$=." + this.imageFormats.get(i) + "]"));
		}
		
		//Get URLs of the images.
		for(int i = 0; i < images.size(); i++){
			urls.add(new URL(images.get(i).absUrl("src")));
		}
		
		return urls;
	}
	
	/**
	 * Conditions to save the image.
	 * @param image Image to be tested.
	 * @return Return true if the image satisfy the condition.
	 */
	private boolean saveConditions(RenderedImage image){
		boolean save = false;
		if(image.getWidth() >= this.minWidth && image.getHeight() >= this.minHeight){
			save = true;
		}
		return save;
	}
}
