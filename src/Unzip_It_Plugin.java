import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

public class Unzip_It_Plugin implements PlugIn{
	/** Version **/
	String version= "v1.0.0";
	
	/** Date **/
	String date= "230717";
	
	/** Input folder **/
	String zipFile="";
	
	/** Output folder **/
	String out="";
	
	boolean remove=Prefs.get("unzipIt_remove.boolean", false);
	
	/** The files paths from the input folder **/
	List<String> filesPaths = new ArrayList<String>();
	

	@Override
	public void run(String arg) {
		if(GUI()) {
			unzipFolder(zipFile, out);
			if(remove) {
				try {
					Files.delete(Paths.get(zipFile));
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
		GenericDialog gd= new GenericDialog("Unzip it !");
		gd.addFileField("Zip_file", zipFile);
		gd.addDirectoryField("Output_folder", out);
		gd.addCheckbox("Remove_zip_file", remove);
		
		gd.addMessage("");
		gd.addMessage("<html><small><i><p>Unzip it !, "+version+"/"+date+"</p>Infos/bug report: <a href=\"mailto:fabrice.cordelieres@gmail.com\">fabrice.cordelieres@gmail.com</a></i></small></html>");
		
		gd.showDialog();
		
		zipFile=gd.getNextString();
		out=gd.getNextString();
		
		//Checks for the final separator
		out=out.endsWith(File.separator)?out:out+File.separator;
		
		remove=gd.getNextBoolean();
		
		if(gd.wasOKed()) {
			if(!zipFile.toLowerCase().endsWith(".zip")) {
				IJ.error("The file should\nbe a zip file !");
				return false;
			}
			if(!(new File(zipFile).exists() && new File(zipFile).isFile())) {
				IJ.error("Warning", "The path to the zip file\n does not exist or points at a directory !");
				return false;
			}
			if(!(new File(out).exists() && new File(out).isDirectory())) {
				IJ.error("Warning", "The output folders\nshould be an existing directory !");
				return false;
			}
			Prefs.set("unzipIt_remove.boolean", remove);
			return true;
		}
		
		return false;
	}
	
	
	//Code adapted from https://www.digitalocean.com/community/tutorials/java-unzip-file-example
	
	/**
     * This method is aimed at unzipping a zip file to a folder
     * @param zipFile name of the zip file (should include the zip extension), as a String
     * @param out name of output folder, as a String
     */
    public void unzipFolder(String zipFile, String out) {
    	 FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(out+fileName);
                
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                	fos.write(buffer, 0, len);
                }
                fos.close();
                
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }
}
