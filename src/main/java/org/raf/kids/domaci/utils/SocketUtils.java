package org.raf.kids.domaci.utils;

import org.raf.kids.domaci.vo.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class SocketUtils {

	private static Logger logger = LoggerFactory.getLogger(SocketUtils.class);

	public static void writeMessage(Socket s, Message message) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
        } catch (IOException e) {
            logger.warn("Connection failure: {}", e.getMessage());
        }
    }

    public static Message readMessage(Socket s) {
        try {
            ObjectInputStream objectInputStream =  new ObjectInputStream(s.getInputStream());
            Message message = (Message) objectInputStream.readObject();
            return message;
        } catch (Exception e) {
            logger.warn("Connection failure: {}", e.getMessage());
            return null;
        }
    }

	public static String readLine(Socket s) {
		try {
			BufferedReader reader = new BufferedReader(
									new InputStreamReader(
									s.getInputStream()));
			
			String line = reader.readLine();
			
			return line;
		} catch (IOException e) {
			logger.warn("Connection failure: {}", e.getMessage());
		}
		
		return null;
	}
	
	public static void writeLine(Socket s, String line) {
		try {
			BufferedWriter writer = new BufferedWriter(
									new OutputStreamWriter(
									s.getOutputStream()));
			
			writer.write(line);
			writer.write("\n");
			writer.flush();
		} catch (IOException e) {
            logger.warn("Connection failure: {}", e.getMessage());
		}
	}
}
