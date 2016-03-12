package cs3.calstatela.edu.SearchEngine.Crawl;
import java.net.URL;
import com.norconex.commons.lang.url.URLNormalizer;

/*
 Reserving Semantics
The following normalizations are part of the RFC 3986 standard and should result in equivalent URLs (one that identifies the same resource):

Convert scheme and host to lower case
Convert escape sequence to upper case
Decode percent-encoded unreserved characters
Removing default ports
URL-Encode non-ASCII characters
Encode spaces to plus sign
 */

public class ProperURL {

	public String removeUrl(String url)
    {
		
		 URL normalizedURL = new URLNormalizer(url)
				 .lowerCaseSchemeHost()
		         .removeDefaultPort()
		         .removeDuplicateSlashes()
		         .removeDirectoryIndex()
		         .removeDirectoryIndex()
		         .removeFragment()
		         .replaceIPWithDomainName()
		         .removeDuplicateSlashes()
		         .removeTrailingQuestionMark()
		         .toURL();
		 //System.out.println(normalizedURL.toString());
		 return normalizedURL.toString();
    }

}
