import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashMap;
import java.util.Map;

public class Mlab {
	
	// Map filename to respective buffered reader
	private Map<String, BufferedReader> nameBr = new HashMap<String, BufferedReader>();
	
	// Getter
	public Map<String, BufferedReader> getNameBr() {
	      return nameBr;
	}
	
	// Setter
	public void setNameBr(Map<String, BufferedReader> namebr) {
		this.nameBr = namebr;
	}
	
	/**
	 *  Create a new mlab file 
	 * @param filename 		path of file which will be created
	 * @return 				if file exists return false, else true
	 * */
	public boolean newMlab(String filename){
		try {
   		      File file = new File(filename);
   		      
		      if (file.createNewFile()){
		        System.out.println(filename + " is created!");
		        return true;
		      }else{
		        System.out.println(filename + " already exists.");
		        return false;
		      }
		      
	    	} catch (IOException e) {
		      e.printStackTrace();
	    	}
			// file not created
			return false;
	}
	
	/**
	 * Open a mlab file and read its content
	 * @param filename 		path of file which will open
	 * @return				string with content of fileName
	 * */
	public String openMlab(String fileName) throws IOException{
		if (!nameBr.containsKey(fileName)) {
			// file is not already open
		    BufferedReader br = new BufferedReader(new FileReader(fileName));
		    nameBr.put(fileName, br);
		    
		    StringBuilder sb = new StringBuilder();
		        
	        String line;
	        while ((line = br.readLine()) != null) {
	            sb.append(line);
	            sb.append("\n");
	        }
	        return sb.toString();
		}
		else {
			System.err.println(fileName + " is already open!");
			return null;
		}
	}
	
	
	/**
	 * Append to mlab file and Save it at current path
	 * @param filename 		path of file which is open
	 * @param text	 		content to be saved 
	 * */
	public void saveMlab(String filename, String text){
		try{
			PrintWriter out = new PrintWriter(filename);
			
			out.println(text);
			out.close();
		}catch(IOException io){
			io.printStackTrace();
		}	
	}
	
	
	/**
	 * Save as new file. If the name of new file already exists, then replace it 
	 * @param destFileName		new path where the file will be saved
	 * @param text	 			content to be saved 
	 * */
	public void saveAsMlab(String destFileName, String text){
		File file = new File(destFileName);
		if (!file.exists()) {
			// destFileName does not exit, so create new file
			newMlab(destFileName);
		}
		saveMlab(destFileName, text);
	}
	
	/**
	 * Close file 
	 * @param br 		Buffered Reader opened by method openMlab
	 * */
	public void closeMlab(BufferedReader br) {
		try 
        {
            if (br != null)
            {
            	br.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
	
	/**
	 * Delete a file 
	 * @param filename 		path of file to delete
	 * */
	public void deleteMlab(String filename){
		try{	
    		File file = new File(filename);
        	
    		if (nameBr.containsKey(filename)) {
    			// First close Buffered Reader mapped to fileName and delete entry from map
    			closeMlab(nameBr.get(filename));
    			nameBr.remove(filename);
    		}
    		
    		if(file.delete()){
    			System.out.println(file.getName() + " is deleted!");
    		}else{
    			System.err.println("Delete operation failed.");
    		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	
	/**
	 * Provide(print at stdout) status information about a mlab file 
	 * @param filename 			path of file which is open
	 * @return 					information with name and size of file
	 * */
	public String statusMlab(String filename) throws IOException{
		File file = new File(filename);
		
		Path path = Paths.get(filename);
		BasicFileAttributes attributes = null;
		
		attributes = Files.readAttributes(path, BasicFileAttributes.class);
		UserPrincipal owner = Files.getOwner(path);
		
		System.out.println("Size: " + file.length() + " bytes");
		System.out.println("CreationTime: " + attributes.creationTime());
		System.out.println("LastModifiedTime: " + attributes.lastModifiedTime());
		System.out.println("Owner: " + owner.getName());
		
		String stringResult = "File: " + filename + " - " + "Size: " + ((double) ((double) file.length()/1000)) + " Kb";
		return stringResult;
	}
	
}
	