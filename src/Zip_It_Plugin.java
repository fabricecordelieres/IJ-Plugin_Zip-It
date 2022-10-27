import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Zip_It_Plugin implements PlugIn{
	/** Version **/
	String version= "v1.0.0";
	
	/** Date **/
	String date= "221027";
	
	/** Input folder **/
	String in="";
	
	/** Output folder **/
	String out="";
	
	/** Zip file name **/
	String filename=Prefs.get("zipIt_file.string", "myZipFile.zip");
	
	/** True to delete the input folder **/
	boolean remove=Prefs.get("zipIt_remove.boolean", false);
	
	/** The files paths from the input folder **/
	List<String> filesPaths = new ArrayList<String>();
	

	@Override
	public void run(String arg) {
		if(GUI()) {
			zipFolder(in, out+filename);
			if(remove) {
				try {
					removeInputFolder(in);
				} catch (IOException e) {
			        e.printStackTrace();
			    }
			}
		}
		
	}
	
	/**
	 * Displays the graphical user interface
	 * @return true if the GUI has been Oked and parameters went through the check, false otherwise
	 */
	public boolean GUI() {
		GenericDialog gd= new GenericDialog("Zip it !");
		gd.addDirectoryField("Input_folder", in);
		gd.addDirectoryField("Output_folder", out);
		gd.addStringField("Zip_filename", filename);
		gd.addCheckbox("Remove_input_folder", remove);
		
		gd.addMessage("");
		gd.addMessage("<html><small><i><p>Zip it !, "+version+"/"+date+"</p>Infos/bug report: <a href=\"mailto:fabrice.cordelieres@gmail.com\">fabrice.cordelieres@gmail.com</a></i></small></html>");
		
		gd.showDialog();
		
		in=gd.getNextString();
		out=gd.getNextString();
		
		//Checks for the final separator
		in=in.endsWith(File.separator)?in:in+File.separator;
		out=out.endsWith(File.separator)?out:out+File.separator;
		
		filename=gd.getNextString();
		
		//Check for the zip extension
		filename=filename.toLowerCase().endsWith(".zip")?filename:filename+".zip";
		
		remove=gd.getNextBoolean();
		
		if(gd.wasOKed()) {
			if(in.equals(out)) {
				IJ.error("Warning", "The input and output folders\nshould be different !");
				return false;
			}
			if(!(new File(in).exists() && new File(in).isDirectory() && new File(out).exists() && new File(out).isDirectory())) {
				IJ.error("Warning", "The input and output folders\nshould be existing directories !");
				return false;
			}
			Prefs.set("zipIt_file.string", filename);
			Prefs.set("zipIt_remove.boolean", remove);
			return true;
		}
		
		return false;
	}
	
	
	//Code adapted from https://www.digitalocean.com/community/tutorials/java-zip-file-folder-example
	
	/**
     * This method is aimed at zipping a folder
     * @param in input folder to zip, as a String
     * @param zipFileName name of the zip file (should include the zip extension), as a String
     */
    public void zipFolder(String in, String zipFileName) {
        try {
        	File dir= new File(in);
            recursivelyBuildFileList(in);
            
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for(String filePath : filesPaths){
                ZipEntry ze = new ZipEntry(filePath.substring(dir.getAbsolutePath().length()+1, filePath.length())); //Uses relative path to build entries
                zos.putNextEntry(ze);
                //read the file and write to ZipOutputStream
                FileInputStream fis = new FileInputStream(filePath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * This method is aimed at recursively building a files list
     * @param in the input folder, as a string
     * @throws IOException
     */
    public void recursivelyBuildFileList(String in) throws IOException {
        File[] files = new File(in).listFiles();
        for(File file : files){
            if(file.isFile()) filesPaths.add(file.getAbsolutePath());
            else recursivelyBuildFileList(file.getAbsolutePath());
        }
    }
    
    //Code adapted from https://softwarecave.org/2018/03/24/delete-directory-with-contents-in-java/#:~:text=Removing%20empty%20directory%20in%20Java,will%20refuse%20to%20remove%20it.
    
    /**
     * This method recursively deletes all files and folder from the input folder
     * @param in input folder, as a String
     * @throws IOException
     */
    public void removeInputFolder(String in) throws IOException {
    	Path path=Paths.get(in); 
    	Files.walk(path)
		    .sorted(Comparator.reverseOrder())
		    .map(Path::toFile)
		    .forEach(File::delete);
    }
}
