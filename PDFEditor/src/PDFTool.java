import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFMergerUtility;
import org.apache.pdfbox.util.Splitter;

public class PDFTool {
	
	/** 
	 * Create an empty PDF
	 * @param outputName	path of PDF file which will be created
	 * */
	public void createEmptyPDF(String outputName) {
		PDDocument doc = new PDDocument();
        doc.addPage(new PDPage());
        try{
            doc.save(outputName);
            doc.close();
        } catch (IOException | COSVisitorException io){
            System.err.println(io.getMessage());
        }
    }
    
    /**
     * Add page to an existing PDF
	 * @param pdfName	path of PDF file
	 * */
    public void addPage(String pdfName) throws IOException {    
    	PDDocument doc = PDDocument.load(pdfName);
        doc.addPage(new PDPage());
        try {
			doc.save(pdfName);
			doc.close();
		} catch (COSVisitorException | IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Merge two or more PDF files
     * @param outputName	path of output PDF merged file
     * @param args			path of PDFf files which will be merged(dynamic -- variable number of arguments )
     * */
    public void mergePDF(String outputName, String[] args){
    	if (args.length < 2) {
    		System.err.println("At method mergePDF: Number of PDF files to be merged must be more than 2...");
    	}
    	else {
			PDFMergerUtility merger = new PDFMergerUtility();
			List<String> filesToMerge = new ArrayList<String>();
			for (String arg : args) {
				filesToMerge.add(arg);
			}
			for (String temp : filesToMerge) {
				merger.addSource(temp);    
			}
			merger.setDestinationFileName(outputName);
			try {
				merger.mergeDocuments();
			} catch (COSVisitorException | IOException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    
    /**
     * Split a PDF file to one PDF file per page
     * @param pdfName	path of PDF file	
     * */
    public void splitPDF(String pdfName) throws IOException {
    	PDDocument document = PDDocument.load(pdfName);
    	String directory = pdfName.substring(0, pdfName.lastIndexOf(System.getProperty("file.separator"))+1);
    	
    	Splitter aSplitter = new Splitter();
        
        aSplitter.setStartPage(1);
        //aSplitter.setSplitAtPage()	//default is 1
        List<PDDocument> aParts = aSplitter.split(document);
        //System.out.println("List size = " + aParts.size());		//USED FOR DEBUG
        Iterator<PDDocument> iterator = aParts.listIterator();
    	int i = 1;
    	
   		while(iterator.hasNext()){
   			PDDocument pd = iterator.next();
   			try{
   				// Saving each page with its assumed page no.
   				pd.save(directory + "Page " + i++ + ".pdf");
   			} catch (COSVisitorException anException){
   				// Something went wrong with a PDF object
   				System.err.println("Something went wrong with page " + (i-1) + "\n Here is the error message" + anException);                
 			}
    		finally {
    			pd.close();
    		}
    	}
   		document.close();
    }
    
    /** 
     * Split a PDF file to consecutive pages according to input
     * Input is checked, otherwise error messages are printed
     * @param pdfName		path of PDF file	
     * @param sStartPage	starting page of new PDF file
     * @param sEndPage		ending page of new PDF file
     * @param outputName	path of output PDF file
     * */
    public void splitConsecutive(String pdfName, int sStartPage, int sEndPage, String outputName) throws IOException {
    	PDDocument document = PDDocument.load(pdfName);
    	Splitter aSplitter = new Splitter();
        int aNumberOfPages = document.getNumberOfPages();
        
        boolean aStartEndPageSet = false;
        if ((sStartPage > 0) && (sStartPage <= aNumberOfPages)) {
            aSplitter.setStartPage(sStartPage);
            aStartEndPageSet = true;
        }
        else{
        	// wrong StartPage
        	aStartEndPageSet = false;
        	System.err.println("At method splitConsecutive: Wrong StartPage parameter...");
        }
        
        if ((sEndPage > 0) && (sEndPage >= sStartPage) && (sEndPage <= aNumberOfPages)) {
            aSplitter.setEndPage(sEndPage);
            aStartEndPageSet = true;
            aSplitter.setSplitAtPage(sEndPage);		//maximum possible size of PDF
        }
        else{
        	// wrong EndPage
        	aStartEndPageSet = false;
        	System.err.println("At method splitConsecutive: Wrong EndPage parameter...");
        }
        
        if (aStartEndPageSet) {
        	List<PDDocument> aParts = aSplitter.split(document);
        	//System.out.println("List size = " + aParts.size());		//USED FOR DEBUG
        	// Because of command "aSplitter.setSplitAtPage(sEndPage);" we have only one element at list returned by split method
        	PDDocument newpdf = aParts.get(0);
        	try {
				newpdf.save(outputName);
				newpdf.close();
			} catch (COSVisitorException e) {
				e.printStackTrace();
			}
        	newpdf.close();
        }
        document.close();
    }
 
    /**
     * Split a PDF file to 2 parts according the page given
     * Input is checked, otherwise error messages are printed
     * @param pdfName		path of PDF file	
     * @param split			page which is the last page of part_1 of the 2 new files
     * 						The second PDF file(part_2) contains the rest pages of the input PDF file
     * */
    public void splitTwoParts(String pdfName, int split) throws IOException {
    	PDDocument document = PDDocument.load(pdfName);
    	String directory = pdfName.substring(0, pdfName.lastIndexOf(System.getProperty("file.separator"))+1);

    	int aNumberOfPages = document.getNumberOfPages();
        if ((split > 0) && (split <= aNumberOfPages)) {
        	// acceptable split
        	this.splitConsecutive(pdfName, 1, split, directory + "Part_1.pdf");
        	this.splitConsecutive(pdfName, split+1, aNumberOfPages, directory + "Part_2.pdf");
        }
        else{
        	System.err.println("At method splitTwoParts: Wrong split parameter...");
        }
        document.close();
    }
    

}
