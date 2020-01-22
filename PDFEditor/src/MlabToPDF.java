import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;


public class MlabToPDF {
	
	final int margin = 35;		// margin from borders of page
	final int distance = 5;		// distance after block of text
	private int fontTypeId = 0;
	private int fontStyleId = 0;
	private int fontColorId = 0;
	private int fontSizeNum = 0;
	private int fontSize = 0;
	private float scaleValue = 1;
	private PDFont font = null;
	private Color color = Color.BLACK;	//default color of text
	private float height = 0;			//height of each lines - based on font
    
    private int fontTypeLen = "fontType:".length();
	private int fontStyleLen = "fontStyle:".length();
	private int fontSizeLen = "fontSize:".length();
	private int fontColorLen = "fontColor:".length();
    
	private float offsetY = -1;		//offset from down side
    private float offsetX = -1;		//offset from left side
    PDPage page = new PDPage();
    private float maxStringLength = page.getMediaBox().getWidth() - 2*margin;	//maximum size of string at each line
    
    /**
	 * Create a PDF file based on formatting commands and text/images that a .mlab file contains
	 * @param doc 				PDDocument referred to PDF file to be made 
	 * @param text 				FileReader referred to path of mlab file
	 * */
	public void createPDFFromMlab(PDDocument doc, Reader text) throws IOException
	{
		 try
	        {
	            BufferedReader data = new BufferedReader( text );
	            String nextLine = null;
	            PDPageContentStream contentStream = null;
	            float tmpOffsetX = 0;				//offset from start of line
	            
	            String restFirstWord = null;		//current formatting command
	            String prevRestFirstWord = null;	//previous formatting command
	            
	            boolean textIsEmpty = true;				//special case of creating a PDF document from an empty string.
	            boolean checkImage = false;				//check if Image formatting command is given by user
	            boolean checkNewLine = false;			//check if NewLine formatting command is given by user
	            boolean checkUnorderedList = false;		//check if UnorderedList formatting command is given by user
	            boolean checkOrderedList = false;		//check if OrderedList formatting command is given by user
	            int listIndex = 0;						//index for elements in ordered list
	            boolean checkFormat = false;			//check if Format formatting command is given by user
	            boolean checkParagraph = false;			//check if Paragraph formatting command is given by user
	            boolean printedUntilEof = false;		//check if we have printed at PDf until end of line
	            
	            // nextLineToDraw contains string which will be printed each time
	            StringBuffer nextLineToDraw = new StringBuffer();
	            while( (nextLine = data.readLine()) != null )
	            {	
	            	// !! IMPORTANT !!
	            	//nextLine refers to line read from mlab file
	            	//nextLineToDraw refers to line which will be printed at PDF
	            	if (nextLine.length() > 0) {
		                // The input text is nonEmpty. New pages will be created and added
		                // to the PDF document as they are needed, depending on the length of
		                // the text.
		                textIsEmpty = false;
		                String[] lineWords = nextLine.trim().split( " " );
		                
		                //find first 2 letters of each line in order to check later
		            	String firstLetters = lineWords[0].substring(0, 2);
		            	
		            	if (firstLetters.equals("&;")) {
		            		
		            		// the following if-statement is true if there is no empty line between 2 formatting commands
		            		// so print existing content at nextLineToDraw
		    	            if (nextLineToDraw.toString().length() > 0) {
		    	            	if (contentStream == null) {
		    	            		// We have crossed the end-of-page boundary and need to extend the
			                        // document by another page.
		            				ReturnPDValues rv = addPDFPage(doc, contentStream);
			                    	contentStream = rv.contentStream;
			                    	page = rv.page;
			                    }
			                    if( contentStream == null )
			                    {
			                        throw new IOException( "Error:Expected non-null content stream." );
			                    }
			                    //move to next line
		    	                contentStream.moveTextPositionByAmount( 0, -height);
		    	                offsetY -= height;
		    	                contentStream.setFont( font, fontSize );
		    	                contentStream.setNonStrokingColor(color);
		    	                contentStream.drawString( nextLineToDraw.toString() );
		    	                printedUntilEof = false;
		    	                //System.out.println("-5 offsetY = "  + offsetY);
		    	                //System.out.println("TYPWSA STO PDF-5 : " +  nextLineToDraw.toString() );
		    	                
		    	                nextLineToDraw = new StringBuffer();
		    	            }
		            		
		            		prevRestFirstWord = restFirstWord;
		            		if ((prevRestFirstWord != null) && (prevRestFirstWord.equals("UnorderedList"))) {
		            			//End of UnorderedList formatting command, so return the offsetX back to margin
		            			contentStream.moveTextPositionByAmount( -4*distance, 0);
		            			checkUnorderedList = false;
		            		}
		            		if ((prevRestFirstWord != null) && (prevRestFirstWord.equals("OrderedList"))) {
		            			//End of OrderedList formatting command, so return the offsetX back to margin
		            			contentStream.moveTextPositionByAmount( -4*distance, 0);
		            			checkOrderedList = false;
		            		}
		            		
		            		restFirstWord = lineWords[0].substring(lineWords[0].lastIndexOf(";")+1);
		            		
		            		//System.out.println("PREVIOUS ENTOLH FORMAT:" + prevRestFirstWord);
		            		//System.out.println("VRHKA ENTOLH FORMAT:" + restFirstWord);
		            		if (restFirstWord.equals("Heading1")) {
		            			//Heading 1
		            			checkParagraph = false;
		            			fontTypeId = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontType:")+ fontTypeLen));
		            			setHeading1(fontTypeId);
		            		}
		            		else if (restFirstWord.equals("Heading2")) {
		            			//Heading 2
		            			checkParagraph = false;
		            			fontTypeId = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontType:")+ fontTypeLen));
		            			setHeading2(fontTypeId);
		            		}
		            		else if (restFirstWord.equals("Heading3")) {
		            			//Heading 3
		            			checkParagraph = false;
		            			fontTypeId = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontType:")+ fontTypeLen));
		            			setHeading3(fontTypeId);
		            		}
		            		else if (restFirstWord.equals("Heading4")) {
		            			//Heading 4
		            			checkParagraph = false;
		            			fontTypeId = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontType:")+ fontTypeLen));
		            			setHeading4(fontTypeId);
		            		}
		            		else if (restFirstWord.equals("NewLine")) {
		            			//New Line
		            			checkParagraph = false;
		            			setNewLine();
		            			checkNewLine =  true;
		            		}
		            		else if (restFirstWord.equals("Image")) {
		            			//Image
		            			checkParagraph = false;
		            			checkImage = true;
		            			scaleValue = Float.parseFloat(lineWords[1].substring(lineWords[1].indexOf("scale:")+ "scale:".length()));

		            		}
		            		else if (restFirstWord.equals("Paragraph")) {
		            			//Paragraph
		            			checkParagraph = true;
		            			//System.out.println("MPHKA STO &;PARAGRAPH");
		            			//System.out.println(Arrays.toString(lineWords));
		            			fontSizeNum = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontSize:")+ fontSizeLen));
		            			fontTypeId = Integer.parseInt(lineWords[2].substring(lineWords[2].indexOf("fontType:")+ fontTypeLen));
		            			fontStyleId = Integer.parseInt(lineWords[3].substring(lineWords[3].indexOf("fontStyle:")+ fontStyleLen));
		            			fontColorId = Integer.parseInt(lineWords[4].substring(lineWords[4].indexOf("fontColor:")+ fontColorLen));
		            			setParagraph(fontSizeNum, fontTypeId, fontStyleId, fontColorId);
		            		}
		            		else if (restFirstWord.equals("Format")) {
		            			//Format
		            			if (checkParagraph) {
		            				// remove distinct distance which Paragraph added 
		            				contentStream.moveTextPositionByAmount( 0, distance);
			                    	offsetY += distance;
			            			checkFormat = true;
			            			fontSizeNum = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontSize:")+ fontSizeLen));
			            			fontTypeId = Integer.parseInt(lineWords[2].substring(lineWords[2].indexOf("fontType:")+ fontTypeLen));
			            			fontStyleId = Integer.parseInt(lineWords[3].substring(lineWords[3].indexOf("fontStyle:")+ fontStyleLen));
			            			fontColorId = Integer.parseInt(lineWords[4].substring(lineWords[4].indexOf("fontColor:")+ fontColorLen));
			            			setParagraph(fontSizeNum, fontTypeId, fontStyleId, fontColorId);
		            			}
		            			else {
		            				System.err.println("Format must be after Paragraph...");
		            				System.exit(-1);
		            			}
		            		}
		            		else if (restFirstWord.equals("UnorderedList")) {
		            			//Unordered List
		            			checkParagraph = false;
		            			checkUnorderedList = true;
		            			if (contentStream == null) {
		            				ReturnPDValues rv = addPDFPage(doc, contentStream);
			                    	contentStream = rv.contentStream;
			                    	page = rv.page;
			                    }
			                    if( contentStream == null )
			                    {
			                        throw new IOException( "Error:Expected non-null content stream." );
			                    }
		            			fontSizeNum = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontSize:")+ fontSizeLen));
		            			fontTypeId = Integer.parseInt(lineWords[2].substring(lineWords[2].indexOf("fontType:")+ fontTypeLen));
		            			fontStyleId = Integer.parseInt(lineWords[3].substring(lineWords[3].indexOf("fontStyle:")+ fontStyleLen));
		            			fontColorId = Integer.parseInt(lineWords[4].substring(lineWords[4].indexOf("fontColor:")+ fontColorLen));
		            			contentStream = setList(contentStream, fontSizeNum, fontTypeId, fontStyleId, fontColorId);
		            		}
		            		else if (restFirstWord.equals("OrderedList")) {
		            			//Ordered List
		            			checkParagraph = false;
		            			checkOrderedList = true;
		            			if (contentStream == null) {
		            				ReturnPDValues rv = addPDFPage(doc, contentStream);
			                    	contentStream = rv.contentStream;
			                    	page = rv.page;
			                    }
			                    if( contentStream == null )
			                    {
			                        throw new IOException( "Error:Expected non-null content stream." );
			                    }
		            			fontSizeNum = Integer.parseInt(lineWords[1].substring(lineWords[1].indexOf("fontSize:")+ fontSizeLen));
		            			fontTypeId = Integer.parseInt(lineWords[2].substring(lineWords[2].indexOf("fontType:")+ fontTypeLen));
		            			fontStyleId = Integer.parseInt(lineWords[3].substring(lineWords[3].indexOf("fontStyle:")+ fontStyleLen));
		            			fontColorId = Integer.parseInt(lineWords[4].substring(lineWords[4].indexOf("fontColor:")+ fontColorLen));
		            			contentStream = setList(contentStream, fontSizeNum, fontTypeId, fontStyleId, fontColorId);
		            		}
		            		else{
		            			//Label of formatting command not supported
		            			System.err.println("Wrong formatting command!\n" + restFirstWord + " is not supported...");
		            			System.exit(-1);
		            		}
		            		
			            	
		            		height = font.getFontDescriptor().getFontBoundingBox().getHeight()/1000;
	        	            //calculate font height and increase by 22 percent.
	        	            height = height*fontSize*1.22f;
		            	}
		            	else {
			            		//Line doesn't start with "&;" , so print text based on last formatting command
			            		//System.out.println("Line doesn't start with &; ");
				                int lineIndex = 0;
				                while( lineIndex < lineWords.length )
				                {
				                	if (checkImage) {
				                		//formatting command is Image
				                		String imageName = lineWords[0];
				                		
				                		contentStream = setImage(imageName, doc, contentStream);
				                		
				                        lineIndex++;
				                	}
				                	else {
				                		//After formatting command there is text
					                    float lengthIfUsingNextWord = 0;
					                    do
					                    {
					                    	if (checkUnorderedList) {
					                    		if (nextLineToDraw.toString().length() > 0) {
					                    			//move to next line
					            	                contentStream.moveTextPositionByAmount( 0, -height);
					            	                offsetY -= height;
					            	                contentStream.setFont( font, fontSize );
					            	                contentStream.setNonStrokingColor(color);
					            	                contentStream.drawString( nextLineToDraw.toString() );
					            	                printedUntilEof = false;
					            	                //System.out.println("-4 offsetY = "  + offsetY);
					            	                //System.out.println("TYPWSA STO PDF-4 : " +  nextLineToDraw.toString() );
					            	            }
							                    nextLineToDraw = new StringBuffer();
							                    
							                    // each element of unordered list should start with "-"
					                    		nextLineToDraw.append( "- " );
					                    	}
					                    	else if (checkOrderedList) {
					                    		if (nextLineToDraw.toString().length() > 0) {
					                    			//move to next line
					            	                contentStream.moveTextPositionByAmount( 0, -height);
					            	                offsetY -= height;
					            	                contentStream.setFont( font, fontSize );
					            	                contentStream.setNonStrokingColor(color);
					            	                contentStream.drawString( nextLineToDraw.toString() );
					            	                printedUntilEof = false;
					            	                //System.out.println("-44 offsetY = "  + offsetY);
					            	                //System.out.println("TYPWSA STO PDF-44 : " +  nextLineToDraw.toString() );
					            	            }
							                    nextLineToDraw = new StringBuffer();
							                    
							                    // each element of ordered list should start with "X.", where X is a increasing number
					                    		nextLineToDraw.append(++listIndex + ". ");
					                    	}
					                    	//append next word
					                        nextLineToDraw.append( lineWords[lineIndex] );
					                        nextLineToDraw.append( " " );
					                        lineIndex++;
					                        if( lineIndex < lineWords.length )
					                        {
					                            String lineWithNextWord = nextLineToDraw.toString() + lineWords[lineIndex];
					                            if (checkFormat) {
					                            	//tmpOffsetX is needed because we may continue at the end of the previous printed nextLineToDraw 
					                            	lengthIfUsingNextWord = tmpOffsetX + (font.getStringWidth( lineWithNextWord )/1000) * fontSize;
					                            }
					                            else {
					                            	lengthIfUsingNextWord = (font.getStringWidth( lineWithNextWord )/1000) * fontSize;
					                            }
					                        }
					                    }
					                    while( lineIndex < lineWords.length &&
					                           lengthIfUsingNextWord < maxStringLength );
					                    //check if line read from mlab is longer than PDF line 
					                    if (lengthIfUsingNextWord >= maxStringLength) {
						                    if (checkFormat) {
						                    	if (printedUntilEof) {
						                    		//System.out.println("-1.1 offsetX = "  + offsetX);
								                    //System.out.println("-1.1 tmpOffsetX = "  + tmpOffsetX);
						                    		//System.out.println("lengthIfUsingNextWord = " + lengthIfUsingNextWord);
						                    		//move to next line
						                    		contentStream.moveTextPositionByAmount( 0, -height);
						                    		offsetY -= height;
						                    	}
						                    	else {
						                    		// last printed was not until Eof ,so move tmpOffsetX accordingly
						                    		contentStream.moveTextPositionByAmount( tmpOffsetX, 0);
						                    		offsetX += tmpOffsetX;
						                    	}
						                    
						                    }
						                    else {
						                    	if( offsetY - height < margin )
							                    {
							                        // We have crossed the end-of-page boundary and need to extend the
							                        // document by another page.
							                    	ReturnPDValues rv = addPDFPage(doc, contentStream);
							                    	contentStream = rv.contentStream;
							                    	page = rv.page;
							                    }
							                    if( contentStream == null )
							                    {
							                        throw new IOException( "Error:Expected non-null content stream." );
							                    }
						                    	//move to next line
						                    	contentStream.moveTextPositionByAmount( 0, -height);
						                    	offsetY -= height;
						                    	
						                    }
						                    //System.out.println("-1 offsetX = "  + offsetX);
						                    //System.out.println("-1 tmpOffsetX = "  + tmpOffsetX);
						                    contentStream.setFont( font, fontSize );
						                    contentStream.setNonStrokingColor(color);
						                    contentStream.drawString( nextLineToDraw.toString() );
						                    //System.out.println("TYPWSA STO PDF-1 : " +  nextLineToDraw.toString() );
						                    //print until eof
						                    printedUntilEof = true;
						                    
						                    
				                    		// if checkFormat, then move back until previous printed, in order to start from margin
						                    if (checkFormat) {
						                    	contentStream.moveTextPositionByAmount( -tmpOffsetX, 0);
						                    	offsetX = margin;
						                    	tmpOffsetX = 0;
						                    	
						                    }
						                    //System.out.println("-11 offsetX = "  + offsetX);
						                    
						                    nextLineToDraw = new StringBuffer();
					                    }
				                	}
				                }
		            	}	
	            	}
	            	else {
	            		//empty line read from mlab, so skip it and print content from previous line read(if exist)
	            		//System.out.println("line skipped");
	            		if (checkImage) {
	            			checkImage = false;
	            			continue;
	            		}
	            		else if (checkNewLine) {
	            			checkNewLine = false;
	            			height = font.getFontDescriptor().getFontBoundingBox().getHeight()/1000;
	        	            //calculate font height based on last font
	        	            height = height*fontSize;
	        	            
	        	            if( offsetY - height < margin )
		                    {
		                        // We have crossed the end-of-page boundary and need to extend the
		                        // document by another page.
	        	            	ReturnPDValues rv = addPDFPage(doc, contentStream);
		                    	contentStream = rv.contentStream;
		                    	page = rv.page;
		                    }
		                    if( contentStream == null )
		                    {
		                        throw new IOException( "Error:Expected non-null content stream." );
		                    }
		                    //move to next line
	        	            contentStream.moveTextPositionByAmount( 0, -height);
		                    offsetY -= height;
	            			continue;
	            		}
	            		
	            		//formatting command is not Image nor NewLine
	            		if (nextLineToDraw.toString().length() > 0) {
		            		if( offsetY - height < margin )
		                    {
		                        // We have crossed the end-of-page boundary and need to extend the
		                        // document by another page.
		            			ReturnPDValues rv = addPDFPage(doc, contentStream);
		                    	contentStream = rv.contentStream;
		                    	page = rv.page;
		                    }
		                    if( contentStream == null )
		                    {
		                        throw new IOException( "Error:Expected non-null content stream." );
		                    }
		                    
		                    
		                    if (checkFormat) {
		                    	//end of Format command
			            		checkFormat = false;
		                    }
		            		//move to next line
		            		contentStream.moveTextPositionByAmount( 0, -height);
		            		offsetY -= height;
		                    contentStream.setFont( font, fontSize );
		                    contentStream.setNonStrokingColor(color);
		                    contentStream.drawString( nextLineToDraw.toString() );
		                    
		                    //We have not printed until the end of line, so keep at tmpOffsetX the length(calculated with font) of
		                    //the string printed
		                    tmpOffsetX = (font.getStringWidth( nextLineToDraw.toString() )/1000) * fontSize;
		                    printedUntilEof = false;
		                    //System.out.println("TYPWSA STO PDF-2 : " +  nextLineToDraw.toString() );
		                    //System.out.println("-2 offsetX = "  + offsetX);
		                    //System.out.println("-2 offsetY = "  + offsetY);
		                    
		                    if (offsetX > (margin +  maxStringLength)){
		                    	//reached at end of line, so move back
		                    	contentStream.moveTextPositionByAmount( -tmpOffsetX, 0);
		                    	offsetX -= tmpOffsetX;
		                    }
		                    //System.out.println("-22 offsetX = "  + offsetX);
		                    
	                    	//end of block of text, so add a distinct distance 
	                    	contentStream.moveTextPositionByAmount( 0, -distance);
	                    	offsetY -= distance;
		                    //}
		                    
		                    nextLineToDraw = new StringBuffer();
		            	}
	            	}
	            }
	            // end of mlab file
	            // print last line if exists
	            if (nextLineToDraw.toString().length() > 0) {
	            	if (checkFormat) {
	            		contentStream.moveTextPositionByAmount( -tmpOffsetX, -height);
	            		offsetX = margin;
	            		checkFormat = false;
	            	}
	            	else {
	            		contentStream.moveTextPositionByAmount( 0, -height);
	            		offsetY -= height;
	            	}
	                contentStream.setFont( font, fontSize );
	                contentStream.setNonStrokingColor(color);
	                contentStream.drawString( nextLineToDraw.toString() );
	                //System.out.println("-3offsetY = "  + offsetY);
	                //System.out.println("TYPWSA STO PDF-3 : " +  nextLineToDraw.toString() );
	                printedUntilEof = false;
	                
	            }
	            
	            
	            // If the input text was the empty string, then the above while loop will have short-circuited
	            // and we will not have added any PDPages to the document.
	            // So in order to make the resultant PDF document readable by Adobe Reader etc, we'll add an empty page.
	            if (textIsEmpty)
	            {
	                doc.addPage(page);
	            }
	            if( contentStream != null )
	            {
	                contentStream.endText();
	                contentStream.close();
	            }
	        }
	        catch( IOException io )
	        {
	            if( doc != null )
	            {
	                doc.close();
	            }
	            throw io;
	        }
	}

	
    /**
     * Set fontSize of text 
     * @param size  	The fontSize given by user(or default)
     * @return 			The fontSize to set.
     */
    private int setFontSize(int size){
    	int aSize = 0;
    	if (size > 0){
    		aSize = size;
    	}
    	else {
    		System.err.println("Wrong fontSize. FontSize must be greater than zero.");
    		System.exit(-1);
    	}
    	return aSize;
    }
    
    
    /**
     * Set fontColor of text 
     * @param ColorId  		The fontColorId given by user
     * @return 				The fontColor to set.
     */
    private Color setFontColor(int ColorId){
    	Color aColor = null;
    	if (ColorId == 1){
    		aColor = Color.BLACK;
    	}
    	else if (ColorId == 2){
    		aColor = Color.BLUE;
    	}
    	else if (ColorId == 3){
    		aColor = Color.RED; 
    	}
    	else if (ColorId == 4){
    		aColor = Color.YELLOW; 
    	}
    	else {
    		System.err.println("Wrong fontColorId.");
    		System.exit(-1);
    	}
    	return aColor;
    }
    
    
    /**
     * Set font and fontStyle of text 
     * @param fontId  	The font given by user(or default)
     * @param StyleId  	The fontStyle given by user(or default)
     * @return 			The font and fontStyle to set.
     */
    private PDFont setFontAndFontStyle(int fontId, int StyleId) {
    	PDFont aFont = null;
    	if (fontId == 1){
    		if (StyleId == 1){
        		aFont = PDType1Font.TIMES_ROMAN;
        	}
        	else if (StyleId == 2){
        		aFont = PDType1Font.TIMES_BOLD;
        	}
        	else if (StyleId == 3){
        		aFont = PDType1Font.TIMES_ITALIC;
        	}
        	else if (StyleId == 4){
        		aFont = PDType1Font.TIMES_BOLD_ITALIC;
        	}
        	else {
        		System.err.println("Wrong fontStyleId");
        		System.exit(-1);
        	}
    	}
    	else if  (fontId == 2){
    		if (StyleId == 1){
        		aFont = PDType1Font.HELVETICA;
        	}
        	else if (StyleId == 2){
        		aFont = PDType1Font.HELVETICA_BOLD;
        	}
        	else if (StyleId == 3){
        		aFont = PDType1Font.HELVETICA_OBLIQUE;
        	}
        	else if (StyleId == 4){
        		aFont = PDType1Font.HELVETICA_BOLD_OBLIQUE;
        	}
        	else {
        		System.err.println("Wrong fontStyleId");
        		System.exit(-1);
        	}
    	}
    	else if (fontId == 3){
    		if (StyleId == 1){
        		aFont = PDType1Font.COURIER;
        	}
        	else if (StyleId == 2){
        		aFont = PDType1Font.COURIER_BOLD;
        	}
        	else if (StyleId == 3){
        		aFont = PDType1Font.COURIER_OBLIQUE;
        	}
        	else if (StyleId == 4){
        		aFont = PDType1Font.COURIER_BOLD_OBLIQUE;
        	}
        	else {
        		System.err.println("Wrong fontStyleId");
        		System.exit(-1);
        	}
    	}
    	else {
    		System.err.println("Wrong fontTypeId");
    		System.exit(-1);
    	}
    	return aFont;
    }
    
    
    /**
     * Add a PDF page and initialize values and objects used
     * @param doc  				The current PDF document
     * @param aContentStream  	The current contentStream of page
     * @return					Object with new ContentStream and page
     */
    private ReturnPDValues addPDFPage(PDDocument doc, PDPageContentStream aContentStream) throws IOException{
    	// We have crossed the end-of-page boundary and need to extend the
        // document by another page.
    	PDPage aPage = new PDPage();
        doc.addPage( aPage );
        if( aContentStream != null )
        {
            aContentStream.endText();
            aContentStream.close();
        }
        aContentStream = new PDPageContentStream(doc, aPage);
        aContentStream.beginText();
        this.offsetX = margin;
        this.offsetY = aPage.getMediaBox().getHeight() - margin;
        aContentStream.moveTextPositionByAmount(this.offsetX, this.offsetY );
        return (new ReturnPDValues(aContentStream, aPage));
    }
    
    
    //Formatting methods
    
    /**
     * Heading1 has fontSize 20, fontStyle Bold and font given by user 
     * @param fontId  The font to set.
     */
    private void setHeading1(int fontId)
    {
    	this.fontSize = 20;
    	this.font = setFontAndFontStyle(fontId, 2);
    	this.color = Color.BLACK;
    }
    
    /**
     * Heading2 has fontSize 18, fontStyle Bold and font given by user 
     * @param fontId  The font to set.
     */
    private void setHeading2(int fontId)
    {
    	this.fontSize = 18;
    	this.font = setFontAndFontStyle(fontId, 2);
    	this.color = Color.BLACK;    	
    }
    
    /**
     * Heading3 has fontSize 16, fontStyle Bold and font given by user 
     * @param fontId  The font to set.
     */
    private void setHeading3(int fontId)
    {
    	this.fontSize = 16;
    	this.font = setFontAndFontStyle(fontId, 2);
    	this.color = Color.BLACK;
    }
    
    /**
     * Heading4 has fontSize 14, fontStyle Bold and font given by user 
     * @param fontId  The font to set.
     */
    private void setHeading4(int fontId)
    {
    	this.fontSize = 14;
    	this.font = setFontAndFontStyle(fontId, 2);
    	this.color = Color.BLACK;
    }
    
    /**
     * NewLine adds a new line with height equal to Times Roman font and fontSize 12
     */
    private void setNewLine(){
    	this.fontSize = 12;
    	this.font = PDType1Font.TIMES_ROMAN;
    }
    
    /**
     * Paragraph consists of one or more sentences and starts at a new line
     * Font, fontSize, fontColor and fontStyle are given by the user
     * @param SizeNum  	The fontSize to set.
     * @param fontId  	The font to set.
     * @param StyleId  	The fontStyle to set.
     * @param ColorId  	The fontColor to set.
     */
    private void setParagraph(int SizeNum, int fontId, int StyleId, int ColorId){
    	this.fontSize = setFontSize(SizeNum);
    	this.font = setFontAndFontStyle(fontId, StyleId);
    	this.color = setFontColor(ColorId);
    }
    
    
    /**
     * Image is a file to be inserted on PDF with scaleValue as the scale of the initial image
     * @param imageName			The path to the image inserted on PDF
     * @param doc  				The current PDF document
     * @param ContentStream  	The current contentStream of page
     * @return					The refreshed contentStream of page
     * @throws IOException 	
     */
    private PDPageContentStream setImage(String imageName, PDDocument doc, PDPageContentStream contentStream) throws IOException{
    	File imageFile = new File(imageName);
		if (!imageFile.exists())
		{
		   throw new FileNotFoundException("Invalid path to image...");
		}
		//System.out.println("INSIDE &;Image : offsetX = "  + offsetX);
		//System.out.println("INSIDE &;Image : offsetY = "  + offsetY);
		
		BufferedImage tmp_image = ImageIO.read(imageFile);
        BufferedImage image = new BufferedImage(tmp_image.getWidth(), tmp_image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);        
        image.createGraphics().drawRenderedImage(tmp_image, null);

        PDXObjectImage ximage = new PDPixelMap(doc, image);
        
        //check if the dimensions(calculated with scale) of the image are beyond the limits of page
        //most possible to be out of limits because of width
        if ( (ximage.getWidth()*this.scaleValue > maxStringLength) || (ximage.getHeight()*this.scaleValue > (page.getMediaBox().getHeight() - 2*margin)) ) {
        	System.err.println("Wrong image width or height. Check scale value.");
    		System.exit(-1);
        }
        
        if( this.offsetY - this.height - ximage.getHeight()*this.scaleValue < margin )
        {
        	// We have crossed the end-of-page boundary and need to extend the
            // document by another page.
        	ReturnPDValues rv = addPDFPage(doc, contentStream);
        	contentStream = rv.contentStream;
        	this.page = rv.page;
        }
        if( contentStream == null )
        {
            throw new IOException( "Error:Expected non-null content stream." );
        }
        
        PDPageContentStream contentStreamImage = new PDPageContentStream(doc, this.page, true, true); //append and compress
        //coordinates of image
        float x = margin;
        float y = this.offsetY - ximage.getHeight()*this.scaleValue - this.height;
        contentStreamImage.drawXObject(ximage, x, y, ximage.getWidth()*this.scaleValue, ximage.getHeight()*this.scaleValue);
        contentStreamImage.close();
        
        this.offsetX = margin;
        contentStream.moveTextPositionByAmount( 0, -(ximage.getHeight()*this.scaleValue + this.height) );
        this.offsetY -= (ximage.getHeight()*this.scaleValue + this.height);
        //System.out.println("INSIDE &;Image : NEW offsetY = "  + this.offsetY);
        
        return contentStream;
    }
    
    /**
     * List consists of one or more elements, starts at a new line and has extra margin from left side
     * Two types of lists : - UnorderedList : Each element starts with "-"
     * 						- OrderedList : Elements are in ascending order
     * Font, fontSize, fontColor and fontStyle are given by the user
     * @param contentStream	The current contentStream
     * @param SizeNum  			The fontSize to set.
     * @param fontId  			The font to set.
     * @param StyleId  			The fontStyle to set.
     * @param ColorId  			The fontColor to set.
     * @return 					The updated contentStream
     * @throws IOException 
     */
    private PDPageContentStream setList(PDPageContentStream contentStream, int SizeNum, int fontId, int StyleId, int ColorId) throws IOException{
    	this.fontSize = setFontSize(SizeNum);
    	this.font = setFontAndFontStyle(fontId, StyleId);
    	this.color = setFontColor(ColorId);
        contentStream.moveTextPositionByAmount( 4*distance, 0);
        return contentStream;
    }
   
}
