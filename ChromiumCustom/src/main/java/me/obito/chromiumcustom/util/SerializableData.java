package me.obito.chromiumcustom.util;

import java.io.*;

public enum SerializableData {
    Players("plugins/ChromiumCore/players"),
    Wars("plugins/ChromiumCore/wars"),
	Farming("plugins/ChromiumFinal/farming");
    
    private String fileName;

    SerializableData(String fileDir) {
        this.fileName = fileDir;
    }

    public String getFileName() {
        return this.fileName;
    }
    
    public Object readFromDatabase(String fileName) {
        Object obj = null;
        
        try {
        	File check = new File(this.fileName);
        	if(!check.exists()) {
        		check.mkdirs();
        	}
        	check = new File(this.fileName+"/"+fileName);
        	if(!check.exists()) {
        		check.createNewFile();
        		return null;
        	}
            FileInputStream file = new FileInputStream(this.fileName+"/"+fileName);
            ObjectInputStream in = new ObjectInputStream(file); 

            obj = in.readObject(); 
            
            in.close(); 
            file.close();
            
            return obj;
        }
  
        catch (IOException ex) {
            return obj;
        }
  
        catch (ClassNotFoundException ex) { 
            return obj;
        }
    }
    
    public Object readFromDatabase() {
        Object obj = null;
        
        try {
        	File check = new File(this.fileName);
        	if(!check.exists()) {
        		check.mkdirs();
        		check.createNewFile();
        		return null;
        	}
            FileInputStream file = new FileInputStream(this.fileName);
            ObjectInputStream in = new ObjectInputStream(file); 

            obj = in.readObject(); 
            
            in.close(); 
            file.close();
            
            return obj;
        }
  
        catch (IOException ex) {
            return obj;
        }
  
        catch (ClassNotFoundException ex) { 
            return obj;
        }
    }

    public boolean writeToDatabase(Object obj, String fileName) {
        try { 
        	File check = new File(this.fileName);
        	if(!check.exists()) {
        		check.mkdirs();
        	}
            FileOutputStream file = new FileOutputStream(this.fileName+"/"+fileName); 
            ObjectOutputStream out = new ObjectOutputStream(file);
            
            out.writeObject(obj);
            out.close(); 
            file.close();
            return true;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean writeToDatabase(Object obj) {
        try { 
            FileOutputStream file = new FileOutputStream(this.fileName); 
            ObjectOutputStream out = new ObjectOutputStream(file);
            
            out.writeObject(obj);
            out.close(); 
            file.close();
            return true;
        } catch (IOException e){
            e.printStackTrace();
            return false;
        }
    }

	public void setDir(String dataFolder) {
		this.fileName = dataFolder;
	}

}
