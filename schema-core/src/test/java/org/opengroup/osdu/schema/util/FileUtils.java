package org.opengroup.osdu.schema.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

public class FileUtils {

    public String read(String filePath) throws IOException {

		InputStream inStream = this.getClass().getResourceAsStream(filePath);
		BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
        StringBuilder stringBuilder = new StringBuilder();
        
        String eachLine = "";
        while((eachLine = br.readLine()) != null){
        	stringBuilder.append(eachLine);
        }
        
    	return stringBuilder.toString();
    }
    
    public String read(File file) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        
        String eachLine = "";
        while((eachLine = br.readLine()) != null){
        	stringBuilder.append(eachLine);
        }
        
    	return stringBuilder.toString();
    }
    
    public static boolean isNullOrEmpty( final Collection< ? > c ) {
        return c == null || c.isEmpty();
    }
}
