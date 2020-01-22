import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStreamReader;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocument;

public class Main {

	public static void main(String[] args) throws IOException, COSVisitorException {
		 /*Using Mlab class*/
		// Use "\\" instead of "\"
		//String filename = "C:\\Users\\Dimitris\\Desktop\\dimitris.mlab";
		//String destname = "C:\\Users\\Dimitris\\Desktop\\test-output.mlab";
		
		//Mlab example = new Mlab();
		//example.newMlab(filename);
		
		//String input = readFromStdin();
		
		//example.saveMlab(filename, input.toString());
		//example.saveAsMlab(destname, input.toString());
		//String str = example.openMlab(filename);		// need to close buffered reader of opened file
		//System.out.println(str);
		
		/*for (String key : example.getNameBr().keySet()) {
			System.err.println(key + " needs to close!!!");
		}*/
		//example.closeMlab(example.getNameBr().get(filename));
		//example.deleteMlab(filename);
		/*for (String key : example.getNameBr().keySet()) {
			System.err.println(key + " needs to close!!!");
		}*/
		//example.statusMlab(filename);

		
		/*Using PDFTool class*/
		//String filename = "C:\\Users\\Dimitris\\Desktop\\new.pdf"; // Windows example
		String filename = "/home/dimitris/Desktop/new.pdf"; // Linux example
		
		PDFTool example = new PDFTool();
		example.createEmptyPDF(filename);
		//example.addPage(filename);
		
		//String[] inputfileNames = {"C:\\Users\\Dimitris\\Desktop\\example.pdf", "C:\\Users\\Dimitris\\Desktop\\test.pdf"};
		//example.mergePDF("C:\\Users\\Dimitris\\Desktop\\merged.pdf", inputfileNames);
		//example.splitPDF("C:\\Users\\Dimitris\\Desktop\\test.pdf");
		//example.splitConsecutive("C:\\Users\\Dimitris\\Desktop\\test.pdf", 2, 8, "C:\\Users\\Dimitris\\Desktop\\Newsplit.pdf");
		//example.splitTwoParts("C:\\Users\\Dimitris\\Desktop\\test.pdf", 3);
		
		/*Using MlabToPDF class*/
		/* 
        //String fileName = "C:\\Users\\Dimitris\\Desktop\\example.mlab";
		String fileName = "/home/dimitris/workspace/MediaLabPDFEditor/example/example.mlab";
		//String fileName = "C:\\Users\\Dimitris\\Desktop\\Texnologia Polumeswn - Project\\example.mlab";
        //String outputName =  "C:\\Users\\Dimitris\\Desktop\\output.pdf";
        String outputName = "/home/dimitris/workspace/MediaLabPDFEditor/example/output.pdf";
    	MlabToPDF app = new MlabToPDF();
        PDDocument doc = new PDDocument();
        FileReader fr = new FileReader(fileName);
        try
        {
        	app.createPDFFromMlab( doc, fr );
        	doc.save( outputName );

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
	    */
		
		/*Using Online Content class*/
		//String fileName = "C:\\Users\\Dimitris\\Desktop\\omdbcontent.mlab"; 
		//String destfileName = "C:\\Users\\Dimitris\\Desktop\\omdbcontent.pdf";
		//Getting movie name from user 
		/*System.out.println("Give the name of movie of series:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
		String movieName = br.readLine();
		
		OnlineContent oc = new OnlineContent();
		String info = oc.getOnlineContent(movieName);
		System.out.println(info);
		
		oc.createMlab(fileName);
		oc.createPDF(fileName, destfileName);*/
	}
	
	/**
	 * Read from stdin
	 * @return		string given by user 
	 * */
	public static String readFromStdin() throws IOException{
		BufferedReader br = 
                new BufferedReader(new InputStreamReader(System.in));
		
		StringBuilder content = new StringBuilder();
		
		content.append("");
		String input;	
		
		while((input = br.readLine())!=null) {
			content.append(input);
			content.append("\n");
		}
		return content.toString();
	} 
}
