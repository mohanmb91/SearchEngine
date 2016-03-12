package cs3.calstatela.edu.SearchEngine.Crawl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cs3.calstatela.edu.SearchEngine.Model.ImageModel;

public class FetchImage {


	public List<ImageModel> fetchImageDetails(String url) {
		
		Document doc = null;
		try {
			doc = Jsoup.connect(url).ignoreContentType(true).validateTLSCertificates(false).ignoreHttpErrors(true).timeout(10*100000).get();
		} catch (IOException e) {
			e.printStackTrace();
		}  
		Elements images = doc.select("img[src]");  
		
		List<ImageModel> imageMetaDetails = new ArrayList<ImageModel>();
		int limit = 0;
		System.out.println("URL***************");
		for (Element image : images) {  
			if (limit < 10) {
				
				imageMetaDetails.add(new ImageModel(image.attr("src"), image.attr("height"), image.attr("width"), image.attr("alt")));
			
				limit = limit + 1;
		    } else {
		        break;
		    }

		}
		return imageMetaDetails;
	}

}

