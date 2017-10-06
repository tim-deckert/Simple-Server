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
    /*
     * processRequest takes a connection socket as a parameter,
     * and processes the HTML request. It does not return anything, but
     * it does print out a response to the HTML request
     *
    */
    
    private static void processRequest(Socket connectionSocket) throws Exception
    {
        //Creates a string to match for the 301 error case
        String case301Error = "index3.html";
        
        //This creates a structure that can read the input as bytes, converts the bytes to characters,
        //and can be read line-by-line.
        //It is taken from the professor's slides
        BufferedReader inFromClient = 
            new BufferedReader( new
            InputStreamReader(connectionSocket.getInputStream()));
        
        //Reads a single line from the input
        String line = inFromClient.readLine();
        
        //Splits the line by spaces, giving an array of words
        String[] words= line.split(" ");
        
        System.out.println(words[1]);
        
        //In HTTP GET requests, the file name still has the '/' character in front of it
        //This removes the leading '/' and give the string for the file name
        words[1] = words[1].substring(1);
        
        //Creates a structure that can write strings to bytes and output the bytes to the socket
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        
        //Creates a file structure using the filename provided by the HTTP request
        File htmlFile = new File(words[1]);
        
        try {
            //do 200
            //This tries to open the file provided by the HTTP request, with a structure
            //that can read it line-by-line
            BufferedReader htmlReader = new BufferedReader (new FileReader(htmlFile));
            
            //If it successfully finds, and open the file, it creates the appropriate header
            //for a 200 message
            outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
            outToClient.writeBytes("Date: "+new Date().toString()+"\r\n");
            outToClient.writeBytes("Content-Length: "+htmlFile.length()+"\r\n");
            outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
            
            //After the header it sends the contents of the file, which should be HTML code
            while ((line = htmlReader.readLine()) != null)
            {
                outToClient.writeBytes(line);
            }
        }
        //If the file is not found, then we knoe we are in one of two cases
        catch (FileNotFoundException e) {
            //Either the page no longer exist, and we need to redirect using a 301
            //We know it is a 301 if the name of the file that was sent is matches 
            //the one we specified fo the 301 case
            if (words[1].compareToIgnoreCase(case301Error) == 0)
            {
                //do 301
                //Sets a new file with the filename of the page we want to redirect to
                File case301File = new File("index.html");
                //creates a structure that can read the file line-by-line
                BufferedReader case301Reader = new BufferedReader (new FileReader(case301File));
                
                //Creates the correct HTTP header for a 301 request
                outToClient.writeBytes("HTTP/1.1 301 Redirect");
                outToClient.writeBytes("Date: "+new Date().toString()+"\r\n");
                outToClient.writeBytes("Content-Length: "+case301File.length()+"\r\n");
                outToClient.writeBytes("Content-Type: text/html\r\n");
                //The Location field is needed to redirect the old name to the new one
                outToClient.writeBytes("Location: http://127.0.0.1:6789/index.html\r\n\r\n");
                
            //After the header it sends the contents of the file, which should be HTML code
                while ((line = case301Reader.readLine()) != null)
                {
                    outToClient.writeBytes(line);
                }
            }
            //Or there is no file by that name on the system, so it needs to 404
            else
            {
                //do 404
                //Creates the header for the 404 case
                outToClient.writeBytes("HTTP/1.1 404 Not Found");
                outToClient.writeBytes("Date: "+new Date().toString()+"\r\n");
                outToClient.writeBytes("Content-Type: text/html\r\n\r\n");
                //Sends an indication to the user that they have hit a 404 code, using HTML
                outToClient.writeBytes("<!DOCTYPE html> <html> <h1> Error 404 </h1> <p> PAGE NOT FOUND </p> </html>");
            }
        }
    }
    
    public static void main (String args[]) throws Exception
    {
        /*
         * I utilized the code from the professor's slides heavily
         * This creates a new server welcome socket on port 6789
        */
        ServerSocket welcomeSocket = new ServerSocket(6789);
        
        
        //The server runs, and waits for requests until it is killed externally 
        while(true)
        {
            //Creates a new socket that accepts data coming in on the socket
            //specified by welcomeSocket
            Socket connectionSocket = welcomeSocket.accept();
            
            //Creates a thread on the fly without needing to 
            //implement or extend the Thread class
            //A new thread will be spawned every time data comes in on the socket
            Thread myThread = new Thread()
            {
                //Overrides the Thread class' run method
                @Override
                public void run()
                {
                    //This should process any requests that come in on the port
                    //If there is no data, or any problem occurs it will print the stack trace
                    try {
                        processRequest(connectionSocket);
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            
            };
            
            //Executes the run command of the thread, to actually execute the HTTP request
            myThread.start();
            
        }
    }
}