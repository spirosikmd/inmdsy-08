import java.io.FileOutputStream;  
import java.io.ObjectInputStream;  
import java.io.ObjectOutputStream;  
import java.net.ServerSocket;  
import java.net.Socket;  
public class Server extends Thread {
	public static final int PORT = 3333;  
    public static final int BUFFER_SIZE = 100; 
    public void run() {  
        try {  
            ServerSocket serverSocket = new ServerSocket(PORT);  
  
            while (true) {  
                Socket s = serverSocket.accept();  
                saveFile(s);  
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
    private void saveFile(Socket socket) throws Exception {  
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());  
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());  
        FileOutputStream fos = null;  
        byte [] buffer = new byte[BUFFER_SIZE];  
        Object o = ois.readObject();  
        
        if (o instanceof String) {  
            fos = new FileOutputStream(o.toString());  
        } else {  
            throwException("Something is wrong");  
        } 
        Integer bytesRead = 0;  
        
        do {  
            o = ois.readObject();  
  
            if (!(o instanceof Integer)) {  
                throwException("Something is wrong");  
            }  
  
            bytesRead = (Integer)o;  
  
            o = ois.readObject();  
  
            if (!(o instanceof byte[])) {  
                throwException("Something is wrong");  
            }  
  
            buffer = (byte[])o;  
            fos.write(buffer, 0, bytesRead);  
        } while (bytesRead == BUFFER_SIZE);  
  
        fos.close();  
  
        ois.close();  
        oos.close();  
    }  
  
    public static void throwException(String message) throws Exception {  
        throw new Exception(message);  
    }  

	
	public static void main(String[] args) {
		new Server().start();

	}

}
