import java.io.BufferedReader;
//import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.*;
  
public class OnlineContent {
	
	private String title;
	private String year;
	private String genre;
	private Float imdbRating;
	private String plot;

	// constructor
	public OnlineContent(){      
	}
	
	// constructor
	public OnlineContent(String title, String year, String genre, Float imdbRating, String plot){
		this.title = title;
	    this.year = year;
	    this.genre = genre;
	    this.imdbRating = imdbRating;
	    this.plot = plot;
	}
	    
	public String getTitle() {
	      return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getYear() {
	      return year;
	}
	
	public void setYear(String year) {
		this.year = year;
	}
	
	public String getGenre() {
	      return genre;
	}
	
	public void setGenre(String genre) {
		this.genre = genre;
	}
	
	public Float getImdbRating() {
	      return imdbRating;
	}
	public void setImdbRating(Float imdbRating) {
		this.imdbRating = imdbRating;
	}
	
	public String getPlot() {
	      return plot;
	}
	
	public void setPlot(String plot) {
		this.plot = plot;
	}
	
	@Override
	public String toString() {
		return "Content from OMDB : \n Title= " + title + "\n Year= " + year + "\n " +
	            "Genre= " + genre + "\n imdbRating= " + imdbRating + "\n Plot= " + plot ;
	}
	
	
	/**
     * Get online content from http://www.omdbapi.com/
     * @param movieName 		The name of movie or series given by user
     * @return 					The information requested as string(or error message) 
     */
	public String getOnlineContent(String movieName) throws IOException{  
     
		URL url = null;  
		String apiUrl="http://www.omdbapi.com/";  
		String dataUrl = null;  
		String retData = null;
		String response = null;
		BufferedReader in = null;
		
		try{  
		   //Check if user has given nothing or blank as input  
		   if(movieName == null || movieName.equals("")){  
			    response = "Empty input given. Try again..."; 
		   }  
		   else {
			   //Remove unwanted space from movieName by trimming it  
			   movieName = movieName.trim();  
		     
			   //Replacing white spaces with + sign as white spaces are not allowed in OMDB api  
			   movieName = movieName.replace(" ","+");  
		     
			   //Forming a complete url ready to send 
			   //r : data type to return(we want json)
			   //plot : return short or full plot(we want full plot)
			   dataUrl = apiUrl + "?t=" + movieName + "&plot=full";  
		        
			   url = new URL(dataUrl);     
			   in = new BufferedReader(new InputStreamReader(url.openStream()));
			   
			   //Reading data from url  
			   while((retData = in.readLine()) != null){ 
				   JSONObject obj = new JSONObject(retData);
				   
				   response = obj.getString("Response");
				   
				   if (response.equals("False")){
					   //wrong name given
					   response = obj.getString("Error");
				   }
				   else {
					   title = obj.getString("Title");
					   year = obj.getString("Year");
					   genre = obj.getString("Genre");
					   imdbRating = (float) obj.getDouble("imdbRating");
					   plot = obj.getString("Plot");
					   
					   response = this.toString();
				   }
			   }
		   }
	  }    
	  catch(Exception e){  
		  System.out.println(e);  
	  }  
	  finally{
		  if(in != null){  
			  in.close();  
		  }
	  }
	  return response;       
	} 
	
	
	/**
     * Create mlab file based on online content from http://www.omdbapi.com/
     * If movie or series is not found then null is printed
     * @param fileName 			path of mlab file which will be created
	 * @throws IOException 
     */
	public void createMlab(String fileName) throws IOException {
		Mlab mlab = new Mlab();
		mlab.newMlab(fileName);
		
		PrintWriter pw = new PrintWriter(new FileWriter(fileName));
		 
		pw.println("&;Heading1 fontType:1");
		pw.println(getTitle());
		pw.println();
		
		pw.println("&;Heading3 fontType:3");
		pw.println("Genre: " + getGenre());
		pw.println();
		
		pw.println("&;Heading3 fontType:3");
		pw.println("Year: " + getYear());
		pw.println();
		
		pw.println("&;Heading3 fontType:3");
		pw.println("IMDbRating: " + getImdbRating());
		pw.println();
		
		pw.println("&;Paragraph fontSize:12 fontType:2 fontStyle:3 fontColor:2");
		pw.print(getPlot());
		
		pw.close();
		
	}
	
	/**
     * Create PDF based on mlab created from online content from http://www.omdbapi.com/
     * If movie or series is not found then null is printed
     * @param fileName 				path of mlab file which will be used
     * @param destfileName 			path of PDF file which will be created
	 * @throws IOException
     */
	public void createPDF(String fileName, String destfilename) throws IOException  {
		MlabToPDF app = new MlabToPDF();
        PDDocument doc = new PDDocument();
        FileReader fr = new FileReader(fileName);
        
        try
        {
        	app.createPDFFromMlab( doc, fr );
        	doc.save( destfilename );

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
        	fr.close();
            doc.close();
        }
		
	}
	
}