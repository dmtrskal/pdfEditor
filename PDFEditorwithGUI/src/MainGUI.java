import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.pdmodel.PDDocument;


public class MainGUI {
	private JFrame mainFrame;
	private JTextArea editorTextArea;
	private JLabel statusLabel;
	private JLabel eventLabel;
	
	private String outputName;
	private String fileName;
	private String selected;
	private String info;
	private Map<String, String> fileList = new HashMap<String, String>();
	private JPanel filesListPanel;
	private DefaultListModel model = new DefaultListModel();
	private JList listbox;
	

	private Mlab mlab = new Mlab();
	private PDFTool tool = new PDFTool();
	private OnlineContent oc = new OnlineContent();
	
	public MainGUI() {
		prepareGUI();
	}

	private JMenuBar CreateMenuBar() {
		
		// Create the menu bar.
		JMenuBar menuBar = new JMenuBar();

		// Build the File menu.
		JMenu menuFile = new JMenu("File");
		
		
		// a group of JMenuItems under File
		
		JMenuItem fileNew = new JMenuItem("New");
		fileNew.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
				// Define fileName where the new file will be saved
				JFileChooser fileChooser = new JFileChooser();
		    	fileChooser = new JFileChooser(System.getenv("user.home"));
		    	fileChooser.setSelectedFile(new File(".mlab"));
		    	int result = fileChooser.showSaveDialog(new JFrame());
		    	if (result == JFileChooser.APPROVE_OPTION) {
		    		File selectedFile = fileChooser.getSelectedFile();
		    		fileName = selectedFile.getAbsolutePath();
		    		
		    		String checkMlab = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
					if (checkMlab.equals("mlab")) {
						try {
							boolean exist = mlab.newMlab(fileName);
							mlab.openMlab(fileName);
							if (!exist) {
								// save an empty file to existing
								mlab.saveMlab(fileName, "");
							}
							// display an empty string
							fileList.put(fileName, "");
							
							//update model
							if (!model.contains(fileName)) {
								model.addElement(fileName);
							}		
							// already contains the last file opened
							listbox.setSelectedValue(fileName, true);
							try {
								// update status label
								statusLabel.setText(mlab.statusMlab(fileName));
								eventLabel.setText("File created");
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							editorTextArea.setText(fileList.get(fileName));
							editorTextArea.setCaretPosition(0);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else {
						// Wrong file type(not .mlab)
				    	String message = "Can't create " + fileName ; 
				    	JOptionPane.showMessageDialog(new JFrame(),
				    		    message,
				    		    "Error",
				    		    JOptionPane.ERROR_MESSAGE);
					}
		    	}
		    }
		});
		menuFile.add(fileNew);
		
		
		JMenuItem fileOpen = new JMenuItem("Open");
		fileOpen.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {	
		    	
		    	// Define fileNames to open
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setMultiSelectionEnabled(true);		//enable multiple selection of files
				fileChooser.setFileFilter(new FileNameExtensionFilter("Mlab Files", "mlab"));
				int result = fileChooser.showOpenDialog(new JFrame());
				
				if (result == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();
					String content = null;
					
					for (int f = 0; f < files.length; f++){
						fileName = files[f].getAbsolutePath();
						
						String checkMlab = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
						if (checkMlab.equals("mlab")) {
							try {
								content = mlab.openMlab(fileName);
								//fileList.put(fileName.substring(fileName.lastIndexOf(System.getProperty("file.separator"))+1, fileName.length()), content);
								if (content != null) {
									fileList.put(fileName, content);
								}
								else {
									// File already open
							    	String message = fileName + "  is already open " ; 
							    	JOptionPane.showMessageDialog(new JFrame(),
							    		    message,
							    		    "Error",
							    		    JOptionPane.ERROR_MESSAGE);
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						else {
							// Wrong file type
					    	String message = "Can't open " + fileName ; 
					    	JOptionPane.showMessageDialog(new JFrame(),
					    		    message,
					    		    "Error",
					    		    JOptionPane.ERROR_MESSAGE);
						}
					}
					
					// Add to list files that doesn't already contain 
					String name = null;
					for(String key : fileList.keySet()) {
						if (!model.contains(key)) {
							//name = key.substring(key.lastIndexOf(System.getProperty("file.separator"))+1, key.length());
							//update model
							model.addElement(key);
							name = key;
							//System.out.println(name);
						}
					}
					if (name != null){
						// show content of last file opened
						listbox.setSelectedValue(name, true);
						try {
							// update status label
							statusLabel.setText(mlab.statusMlab(name));
							eventLabel.setText("File opened");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						editorTextArea.setText(fileList.get(name));
						editorTextArea.setCaretPosition(0);
					}
				}
		    }
		});
		menuFile.add(fileOpen);
		
		
		JMenuItem fileSave = new JMenuItem("Save");
		fileSave.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {	
		    	System.out.println("Save : " + selected);
		    	
		    	mlab.saveMlab(selected, editorTextArea.getText());
		    	
		    	// show new content
		    	fileList.put(selected, editorTextArea.getText());
				listbox.setSelectedValue(selected, true);
				try {
					// update status label
					statusLabel.setText(mlab.statusMlab(selected));
					eventLabel.setText("File saved");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				editorTextArea.setText(fileList.get(selected));
				editorTextArea.setCaretPosition(0);
		    }
		});
		menuFile.add(fileSave);
		
		
		JMenuItem fileSaveAs = new JMenuItem("Save As");
		fileSaveAs.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {	
		    	// Define fileName where the new file will be saved
				JFileChooser fileChooser = new JFileChooser();
		    	fileChooser = new JFileChooser(System.getenv("user.home"));
		    	fileChooser.setSelectedFile(new File(".mlab"));
		    	fileChooser.setFileFilter(new FileNameExtensionFilter("Mlab Files", "mlab"));
		    	int result = fileChooser.showSaveDialog(new JFrame());
		    	if (result == JFileChooser.APPROVE_OPTION) {
		    		File selectedFile = fileChooser.getSelectedFile();
		    		outputName = selectedFile.getAbsolutePath();
		    	
		    		String directory = outputName.substring(0, outputName.lastIndexOf("/"));
		    		String child = outputName.substring(outputName.lastIndexOf("/")+1, outputName.length());
		    		File f = new File(directory, child);
		    		if(f.exists()){
		    			// file exist in current directory
		    		    
		    			// confirm in order to replace content of existing file
		    			String message = outputName + " already exists.\n Do you want to replace it?";
						int confirmed = JOptionPane.showConfirmDialog(null, 
								message, "Confirm Save As",
						        JOptionPane.YES_NO_OPTION);
			
						    if (confirmed == JOptionPane.YES_OPTION) {
						    	// Save to already existing file and open it
				    			
				    			if (fileList.containsKey(outputName)) {
				    				// File is already open, so just save it
				    				mlab.saveMlab(outputName, editorTextArea.getText());
				    				
				    				// show content of new file saved
				    				fileList.put(outputName, editorTextArea.getText());
									listbox.setSelectedValue(outputName, true);
									try {
										// update status label
										statusLabel.setText(mlab.statusMlab(outputName));
										eventLabel.setText("File saved as " + outputName);
									} catch (IOException e1) {
										e1.printStackTrace();
									}
									editorTextArea.setText(fileList.get(outputName));
									editorTextArea.setCaretPosition(0);
				    				
				    			}
				    			else{
				    				// Existing file is not open
				    				// Save to existing file and display content
				    				mlab.saveAsMlab(outputName, editorTextArea.getText());
				    				
				    				String content;
									try {
										content = mlab.openMlab(outputName);
										
										fileList.put(outputName, content);
										
										//update model
										if (!model.contains(outputName)) {
											model.addElement(outputName);
										}		
										// already contains the last file opened
										fileList.put(outputName, editorTextArea.getText());
										listbox.setSelectedValue(outputName, true);
										try {
											// update status label
											statusLabel.setText(mlab.statusMlab(outputName));
											eventLabel.setText("File saved as " + outputName);
										} catch (IOException e1) {
											e1.printStackTrace();
										}
										editorTextArea.setText(fileList.get(outputName));
										editorTextArea.setCaretPosition(0);
									} catch (IOException e1) {
										e1.printStackTrace();
									}
				    			}
				    			
						    }
		    		} 
		    		else {
		    			// file does not exist in current directory
		    			
		    			// Save as new file and open it
		    			mlab.saveAsMlab(outputName, editorTextArea.getText());
		    			
		    			String checkMlab = outputName.substring(outputName.lastIndexOf(".") + 1, outputName.length());
		    			if (checkMlab.equals("mlab")) {
							try {
								String content = mlab.openMlab(outputName);
								
								fileList.put(outputName, content);
								
								//update model
								if (!model.contains(outputName)) {
									model.addElement(outputName);
								}		
								// already contains the last file opened
								fileList.put(outputName, editorTextArea.getText());
								listbox.setSelectedValue(outputName, true);
								try {
									// update status label
									statusLabel.setText(mlab.statusMlab(outputName));
									eventLabel.setText("File saved as " + outputName);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								editorTextArea.setText(fileList.get(outputName));
								editorTextArea.setCaretPosition(0);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						else {
							// Wrong file type(not .mlab)
							// New file is created, but not supperted from application
					    	String message = "Can't open " + outputName ; 
					    	JOptionPane.showMessageDialog(new JFrame(),
					    		    message,
					    		    "Wrong type format",
					    		    JOptionPane.ERROR_MESSAGE);
						}
		    		}
		    	}
		    }
		});
		menuFile.add(fileSaveAs);

		
		JMenuItem fileClose = new JMenuItem("Close");
		fileClose.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
		    {
				Map<String, BufferedReader> tempMap = new HashMap<String, BufferedReader>();
				tempMap = mlab.getNameBr();
				
				/*for (String key : tempMap.keySet()) {
					System.err.println(key + " needs to close!!!");
				}*/
				// remove selected item from list
				fileList.remove(selected);
				model.removeElement(selected);
				try {
					// update status label
					statusLabel.setText(mlab.statusMlab(selected));
					eventLabel.setText("File closed");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				editorTextArea.setText("");
				
				mlab.closeMlab(tempMap.get(selected));
				tempMap.remove(selected);
				mlab.setNameBr(tempMap);
		    }
		   
		});
		menuFile.add(fileClose);
		
		
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
		    {
				int confirmed = JOptionPane.showConfirmDialog(null, 
				        "Are you sure you want to exit the program?", "Exit Program Message Box",
				        JOptionPane.YES_NO_OPTION);
	
				    if (confirmed == JOptionPane.YES_OPTION) {
				    	Map<String, BufferedReader> tempMap = new HashMap<String, BufferedReader>();
						tempMap = mlab.getNameBr();
						// close all open files
						for (String key : tempMap.keySet()) {
							mlab.closeMlab(tempMap.get(key));
						}
						// exit
				    	System.exit(0);
				    }
		    }
		   
		});
		menuFile.add(fileExit);
		
		
		/////////////////////////////////////////////////////////////////////////////////////////
		// Build the Edit menu.
		JMenu menuEdit = new JMenu("Edit");
		
		
		// Create PDF based on text displayed at editor text area by selected file
		JMenuItem editCreatePDF = new JMenuItem("Create PDF");
		editCreatePDF.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
				// suggested name
		        String suggName = selected.substring(0, selected.lastIndexOf(".") + 1) + "pdf";
		        
		        // Define outputName where the merged file will be saved
				JFileChooser fileChooser = new JFileChooser();
            	fileChooser = new JFileChooser(System.getenv("user.home"));
            	fileChooser.setSelectedFile(new File(suggName));
            	fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
            	int result = fileChooser.showSaveDialog(new JFrame());
            	if (result == JFileChooser.APPROVE_OPTION) {
            		File selectedFile = fileChooser.getSelectedFile();
				    outputName = selectedFile.getAbsolutePath();
            		
            		String directory = outputName.substring(0, outputName.lastIndexOf("/"));
    	    		String child = outputName.substring(outputName.lastIndexOf("/")+1, outputName.length());
    	    		File f = new File(directory, child);
    	    		if(f.exists()){
    	    			// file exist in current directory
    	    		    
    	    			// confirm in order to replace content of existing file
    	    			String message = outputName + " already exists.\n Do you want to replace it?";
    					int confirmed = JOptionPane.showConfirmDialog(null, 
    							message, "Confirm Save As",
    					        JOptionPane.YES_NO_OPTION);
    		
    					    if (confirmed == JOptionPane.YES_OPTION) {
    					    	// Replace existing pdf
    					    	PDFromMlab(selected, outputName);
    					    }
    	    		}
    	    		else {
    	    			// Create new pdf file
    	    			PDFromMlab(selected, outputName);
    	    		}
            	}
		    }
		});
		menuEdit.add(editCreatePDF);
		
		
		// Create a mlab based on information return from http://www.omdbapi.com/
		JMenuItem editOnlineContent = new JMenuItem("Online Content");
		editOnlineContent.addActionListener(new ActionListener() 
		{
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Execute when editOnlineContent is pressed
		    	
		    	// Define movie/series name
            	JTextField movieName = new JTextField();
            	Object[] message = {
            			"Movie/Series:", movieName
                };
            	int option = JOptionPane.showConfirmDialog(null, message, "Give the name of movie/series", JOptionPane.OK_CANCEL_OPTION);
            	
            	if (option == JOptionPane.OK_OPTION) {
            		
            		
					try {
						info = oc.getOnlineContent(movieName.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					if (oc.getTitle() != null) {
						// Movie/series found
						
						// Define outputName where the mlab will be saved
						JFileChooser fileChooser2 = new JFileChooser();
		            	fileChooser2 = new JFileChooser(System.getenv("user.home"));
		            	fileChooser2.setSelectedFile(new File("onlineContent.mlab"));
		            	int result2 =fileChooser2.showSaveDialog(new JFrame());
		            	if (result2 == JFileChooser.APPROVE_OPTION) {
		            		File selectedFile2 = fileChooser2.getSelectedFile();
						    outputName = selectedFile2.getAbsolutePath();
		            	}
						try {
							oc.createMlab(outputName);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
	            		String content = null;
	            		
	            		try {
							content = mlab.openMlab(outputName);
							fileList.put(outputName, content);
							
							//update model
							model.addElement(outputName);
							// already contains the last file opened
							listbox.setSelectedValue(outputName, true);
							try {
								// update status label
								statusLabel.setText(mlab.statusMlab(outputName));
								eventLabel.setText("File created");
							} catch (IOException e1) {
								e1.printStackTrace();
							}
							editorTextArea.setText(fileList.get(outputName));
							editorTextArea.setCaretPosition(0);
							
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					else {
						// Movie/series not found
				    	JOptionPane.showMessageDialog(new JFrame(),
				    			movieName.getText() + " : " + info,
				    		    "Error",
				    		    JOptionPane.ERROR_MESSAGE);
					}
            	}
		    }
		});
		menuEdit.add(editOnlineContent);
		
		
		// Create a PDF based on information return from http://www.omdbapi.com/
		JMenuItem editOnlinePdf = new JMenuItem("Online PDF");
		editOnlinePdf.addActionListener(new ActionListener() 
		{
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Execute when editOnlinePdf is pressed
		    	
		    	// Define movie/series name
            	JTextField movieName = new JTextField();
            	Object[] message = {
            			"Movie/Series:", movieName
                };
            	int option = JOptionPane.showConfirmDialog(null, message, "Give the name of movie/series", JOptionPane.OK_CANCEL_OPTION);
		    	
            	
            	if (option == JOptionPane.OK_OPTION) {
            		
            		
					try {
						info = oc.getOnlineContent(movieName.getText());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
            		
					if (oc.getTitle() != null) {
						// Movie/series found
						
						// Define outputName where the mlab will be saved
						JFileChooser fileChooser2 = new JFileChooser();
		            	fileChooser2 = new JFileChooser(System.getenv("user.home"));
		            	fileChooser2.setSelectedFile(new File("onlineContent.pdf"));
		            	int result2 =fileChooser2.showSaveDialog(new JFrame());
		            	if (result2 == JFileChooser.APPROVE_OPTION) {
		            		File selectedFile2 = fileChooser2.getSelectedFile();
						    outputName = selectedFile2.getAbsolutePath();
		            	}
						
		            	// create temporary file
				        File tmp = null;
						try {
							tmp = File.createTempFile("tmp", ".mlab", new File(outputName.substring(0, outputName.lastIndexOf("/"))));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						finally{
	            			// delete file when the virtual machine terminate
					        tmp.deleteOnExit();
	            		}
						
	            		fileName = tmp.getAbsolutePath();
	            		try {
							oc.createMlab(fileName);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
	            		try {
							oc.createPDF(fileName, outputName);
						} catch (IOException e2) {
							e2.printStackTrace();
						}/*
	            		finally{
	            			// another way to delete tmp file
	            			// delete file since pdf is made
					        tmp.delete();
							
	            		}*/
					}
					else {
						// Movie/series not found
				    	JOptionPane.showMessageDialog(new JFrame(),
				    			movieName.getText() + " : " + info,
				    		    "Error",
				    		    JOptionPane.ERROR_MESSAGE);
					}
            	}
		    }
		});
		menuEdit.add(editOnlinePdf);
		
		
		// Merge PDFs
		JMenuItem editMergePdf = new JMenuItem("Merge PDFs");
		editMergePdf.addActionListener(new ActionListener() 
		{
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	String[] inputFileNames = null;
		    	//Execute when editMergePdf is pressed
		    	
		    	// Define fileNames to be merged
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.setMultiSelectionEnabled(true);		//enable multiple selection of files
				fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
				int result = fileChooser.showOpenDialog(new JFrame());
				
				if (result == JFileChooser.APPROVE_OPTION) {
					File[] files = fileChooser.getSelectedFiles();
					
					inputFileNames = new String[files.length];
					for (int f = 0; f < files.length; f++){
						fileName = files[f].getAbsolutePath();
						inputFileNames[f] = fileName;
					}
				}
				// Define outputName where the merged file will be saved
				JFileChooser fileChooser2 = new JFileChooser();
            	fileChooser2 = new JFileChooser(System.getenv("user.home"));
            	fileChooser2.setSelectedFile(new File("merged.pdf"));
            	int result2 = fileChooser2.showSaveDialog(new JFrame());
            	if (result2 == JFileChooser.APPROVE_OPTION) {
            		File selectedFile2 = fileChooser2.getSelectedFile();
				    outputName = selectedFile2.getAbsolutePath();
            	}
				if (inputFileNames != null) {	
					tool.mergePDF(outputName, inputFileNames);
				}
		    }
		});
		menuEdit.add(editMergePdf);
		
		
		// Extract all pages 
		JMenuItem editExtractAllPages = new JMenuItem("Extract All Pages");
		editExtractAllPages.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        //Execute when editExtractAllPages is pressed
		    	JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

				fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
				int result = fileChooser.showOpenDialog(new JFrame()); 
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    fileName = selectedFile.getAbsolutePath();
				    //System.out.println("Selected file: " + fileName);
					try {
						tool.splitPDF(fileName);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
		    }
		});
		menuEdit.add(editExtractAllPages);
		
		
		// Extract consecutive pages of PDF
		JMenuItem editExtractPages = new JMenuItem("Extract Pages");
		editExtractPages.addActionListener(new ActionListener() 
		{
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Execute when editExtractPages is pressed
		    	
		    	//Define input PDF fileName
		    	JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

				fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
				int result = fileChooser.showOpenDialog(new JFrame()); 
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    fileName = selectedFile.getAbsolutePath();
				}
				
				// Define outputName where the new file will be saved
				JFileChooser fileChooser2 = new JFileChooser();
            	fileChooser2 = new JFileChooser(System.getenv("user.home"));
            	fileChooser2.setSelectedFile(new File("new.pdf"));
            	int result2 =fileChooser2.showSaveDialog(new JFrame());
            	if (result2 == JFileChooser.APPROVE_OPTION) {
            		File selectedFile2 = fileChooser2.getSelectedFile();
				    outputName = selectedFile2.getAbsolutePath();
            	}
            	
            	// Define start page and end page
            	JTextField startPage = new JTextField();
            	JTextField endPage = new JTextField();
            	Object[] message = {
            	    "Start Page:", startPage,
            	    "End Page:", endPage
            	};

            	int option = JOptionPane.showConfirmDialog(null, message, "Range of consecutive pages", JOptionPane.OK_CANCEL_OPTION);
            	if (option == JOptionPane.OK_OPTION) {
            		int startpg = Integer.parseInt(startPage.getText());
            		int endpg = Integer.parseInt(endPage.getText());
            		
					try {
						tool.splitConsecutive(fileName, startpg, endpg, outputName);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
            	}
					
		    }
		});
		menuEdit.add(editExtractPages);
		
		
		// Split a PDF file to 2 parts according the page given
		JMenuItem editSplitPdf = new JMenuItem("Split PDF");
		editSplitPdf.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	//Execute when editSplitPdf is pressed
		    	
		    	//Define input PDF fileName
		    	JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

				fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
				int result = fileChooser.showOpenDialog(new JFrame()); 
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    fileName = selectedFile.getAbsolutePath();
				}
            	
            	// Define split page
            	JTextField splitpage = new JTextField();
            	Object[] message = {
            			"Split Page", splitpage
                };

            	int option = JOptionPane.showConfirmDialog(null, message, "Page that defines the split", JOptionPane.OK_CANCEL_OPTION);
            	if (option == JOptionPane.OK_OPTION) {
            		int splitpg = Integer.parseInt(splitpage.getText());
            		
					try {
						tool.splitTwoParts(fileName, splitpg);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
            	}
		    }
		});
		menuEdit.add(editSplitPdf);
		
		
		/////////////////////////////////////////////////////////////////////////////////////////
		// Build the Help menu
		JMenu menuHelp = new JMenu("Help");
		
		JMenuItem helpAbout = new JMenuItem("About");
		helpAbout.addActionListener(new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        //Execute when About is pressed
		    	String message = "Author : Kalogeropoulos Dimitrios\n" + 
		    					"Contact : dmtrs_kal@hotmail.com" ;
		    	JOptionPane.showMessageDialog(new JFrame(),
		    		    message,
		    		    "MediaLab PDF Editor",
		    		    JOptionPane.INFORMATION_MESSAGE);
		    }
		});
		menuHelp.add(helpAbout);
		
		// add components to menuBar
		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		menuBar.add(menuHelp);
		
		return menuBar;
	}
	
	// Main method
	public static void main(String[] args) {
		MainGUI swingContainer = new MainGUI();
		swingContainer.showJPanel();
	}

	// Set mainFrame
	private void prepareGUI() {
		mainFrame = new JFrame("MediaLab PDF Editor");
		mainFrame.setSize(1000, 600);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		mainFrame.setVisible(true);
	}

	
	private JPanel CreateToolBar() {
		JPanel buttonsPanel = new JPanel();
		
		// Heading1
		JButton heading1Button = new JButton("Heading1");
		heading1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Heading1 fontType:\n");
			}
		});
		buttonsPanel.add(heading1Button);

		// Heading2
		JButton heading2Button = new JButton("Heading2");
		heading2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Heading2 fontType:\n");
			}
		});
		buttonsPanel.add(heading2Button);

		// Heading3
		JButton heading3Button = new JButton("Heading3");
		heading3Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Heading3 fontType:\n");
			}
		});
		buttonsPanel.add(heading3Button);

		// Heading4
		JButton heading4Button = new JButton("Heading4");
		heading4Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Heading4 fontType:\n");
			}
		});
		buttonsPanel.add(heading4Button);

		// Paragraph
		JButton paragraphButton = new JButton("Paragraph");
		paragraphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Paragraph fontSize: fontType: fontStyle: fontColor:\n");
			}
		});
		buttonsPanel.add(paragraphButton);

		// Format
		JButton formatButton = new JButton("Format");
		formatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Format fontSize: fontType: fontStyle: fontColor:\n");
			}
		});
		buttonsPanel.add(formatButton);

		// New Line
		JButton newLineButton = new JButton("NewLine");
		newLineButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;NewLine\n");
			}
		});
		buttonsPanel.add(newLineButton);
		
		// Image
		JButton imageButton = new JButton("Image");
		imageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;Image scale:\n");
			}
		});
		buttonsPanel.add(imageButton);
		
		// Unordered List
		JButton unOrderedListButton = new JButton("Unordered List");
		unOrderedListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;UnorderedList fontSize: fontType: fontStyle: fontColor:\n");
			}
		});
		buttonsPanel.add(unOrderedListButton);
		
		// Ordered List
		JButton orderedListButton = new JButton("Ordered List");
		orderedListButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorTextArea.append("\n&;OrderedList fontSize: fontType: fontStyle: fontColor:\n");
			}
		});
		buttonsPanel.add(orderedListButton);
		
		//buttonsPanel.setFont(new Font("Arial", Font.PLAIN, 6));
		//buttonsPanel.setPreferredSize(new Dimension(30, 20));
		buttonsPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		return buttonsPanel;
	}

	
	private JPanel CreateListPanel() {
		filesListPanel = new JPanel();
		filesListPanel.setLayout(new BorderLayout());
		filesListPanel.setBackground(Color.WHITE);
		// Create a new listbox control
		listbox = new JList<String>(model);
		listbox.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent lsEvent) {
				  if(!(lsEvent.getValueIsAdjusting())){
					 // item selected at mouseUp event 
				     JList list = (JList) lsEvent.getSource();
				     Object selection = list.getSelectedValue(); // if not using generics
				     if (selection != null) {
				    	selected = selection.toString();
				    	try {
				    		// update status label
							statusLabel.setText(mlab.statusMlab(selected));
							eventLabel.setText("");
						} catch (IOException e) {
							e.printStackTrace();
						}
				    	editorTextArea.setText(fileList.get(selected));
					    editorTextArea.setCaretPosition(0);
				     }
				  }
				}
		});
		JScrollPane scroll = new JScrollPane(listbox);
		scroll.setPreferredSize(new Dimension(120, 100));
		scroll.setHorizontalScrollBar(new JScrollBar(JScrollBar.HORIZONTAL));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		filesListPanel.add(scroll,BorderLayout.CENTER);
		filesListPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		return filesListPanel;
	}

	
	private JPanel CreateEditorPanel() {
		JPanel editorPanel = new JPanel(new BorderLayout());
		editorPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		editorTextArea = new JTextArea();
		JScrollPane scroll = new JScrollPane(editorTextArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		editorPanel.add(scroll, BorderLayout.CENTER);
		return editorPanel;
	}

	
	private JPanel CreateLabelPanel() {
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.setBorder(BorderFactory.createRaisedBevelBorder());
		statusLabel = new JLabel("File: - Size:");
		eventLabel = new JLabel("");
		labelPanel.add(statusLabel,BorderLayout.WEST);
		labelPanel.add(eventLabel,BorderLayout.EAST);
		return labelPanel;
	}

	
	private void showJPanel() {
		mainFrame.setJMenuBar(CreateMenuBar());
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(CreateToolBar(), BorderLayout.NORTH);
		mainPanel.add(CreateListPanel(), BorderLayout.WEST);
		mainPanel.add(CreateEditorPanel(), BorderLayout.CENTER);
		mainPanel.add(CreateLabelPanel(), BorderLayout.SOUTH);
		mainFrame.add(mainPanel);
		mainFrame.setVisible(true);
	}
	
	/**
	 * Create a PDF from mlab file and save it
	 * @param source	the selected mlab file at list
	 * @param dest		the name of pdf file which will be created
	 */
	private void PDFromMlab(String source, String dest) {
		MlabToPDF app = new MlabToPDF();
        PDDocument doc = new PDDocument();
        FileReader fr = null;
		try {
			fr = new FileReader(source);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try
        {
        	app.createPDFFromMlab( doc, fr );
        	doc.save( dest );

        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
        finally
        {
        	try {
				fr.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            try {
				doc.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        }
	}
	

}
