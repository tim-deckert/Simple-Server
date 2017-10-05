/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Timmy
 */
import java.io.*;
import java.net.*;
import java.util.Date;

public class Server {
    
    private static void processRequest(Socket connectionSocket) throws Exception
    {
        String case301Error = "index3.html";
        BufferedReader inFromClient = 
            new BufferedReader( new
            InputStreamReader(connectionSocket.getInputStream()));
        
        String line = inFromClient.readLine();
        
        String[] words= line.split(" "); 
        words[1] = words[1].substring(1);
        
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        
        File htmlFile = new File(words[1]);
        
        try {
            //do 200
            
            BufferedReader htmlReader = new BufferedReader (new FileReader(htmlFile));
            
            outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
            outToClient.writeBytes("Date: "+new Date().toString()+"\r\n");
            System.out.println("Date: "+new Date().toString()+"\r\n");
            outToClient.writeBytes("Content-Length: "+htmlFile.length()+"\r\n");
            System.out.println("Content-Length: "+htmlFile.length()+"\r\n");
            outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
            
            while ((line = htmlReader.readLine()) != null)
            {
                outToClient.writeBytes(line);
                System.out.println(line);
            }
        }
        catch (FileNotFoundException e) {
            System.out.println(words[1]);
            if (words[1].compareToIgnoreCase(case301Error) == 0)
            {
                //do 301
                File case301File = new File("index.html");
                BufferedReader case301Reader = new BufferedReader (new FileReader(case301File));
                
                outToClient.writeBytes("HTTP/1.1 301 Redirect");
                outToClient.writeBytes("Date: "+new Date().toString()+"\r\n");
                outToClient.writeBytes("Content-Length: "+case301File.length()+"\r\n");
                outToClient.writeBytes("Content-Type: text/html\r\n");
                outToClient.writeBytes("Location: http://127.0.0.1:6789/index.html\r\n\r\n");
                                
                while ((line = case301Reader.readLine()) != null)
                {
                    outToClient.writeBytes(line);
                }
            }
            else
            {
                //do 404
                System.out.println("We in case 404");
                outToClient.writeBytes("HTTP/1.1 404 Not Found");
                outToClient.writeBytes("Date: "+new Date().toString()+"\r\n");
                outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
                outToClient.writeBytes("<!DOCTYPE html> <html> <h1> Error 404 </h1> <p> PAGE NOT FOUND </p> </html>");
            }
        }
    }
    
    public static void main (String args[]) throws Exception
    {
        ServerSocket welcomeSocket = new ServerSocket(6789);
        
        while(true)
        {
            Socket connectionSocket = welcomeSocket.accept();
            
            Thread myThread = new Thread()
            {
                @Override
                public void run()
                {
                    try {
                        processRequest(connectionSocket);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            
            };
            myThread.start();
            
        }
    }
    
}
