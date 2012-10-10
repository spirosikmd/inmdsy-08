import java.io.File;  
import java.io.FileInputStream;  
import java.io.ObjectInputStream;  
import java.io.ObjectOutputStream;  
import java.net.Socket;  
import java.util.Arrays; 
public class Client {
	public static void main(String[] args) throws Exception {
	String fileName = null;
	 try {  
         fileName = args[0];  
     } catch (Exception e) {  
         System.out.println("Pass file name as command line argument");  
     }  

     File file = new File(fileName);  
     Socket socket = new Socket("localhost", Server.PORT);  
     ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());  
     ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());  

     oos.writeObject(file.getName());  

     FileInputStream fis = new FileInputStream(file);  
     byte [] buffer = new byte[Server.BUFFER_SIZE];  
     Integer bytesRead = 0;  

     while ((bytesRead = fis.read(buffer)) > 0) {  
         oos.writeObject(bytesRead);  
         oos.writeObject(Arrays.copyOf(buffer, buffer.length));  
     }  

     oos.close();  
     ois.close();  
 }  
}    

	
