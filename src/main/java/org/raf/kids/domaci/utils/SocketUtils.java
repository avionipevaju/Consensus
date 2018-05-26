package org.raf.kids.domaci.utils;

import org.raf.kids.domaci.vo.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class SocketUtils {

	private static Logger logger = LoggerFactory.getLogger(SocketUtils.class);

	public static void writeMessage(Socket s, Message message) throws IOException {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(s.getOutputStream());
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
			objectOutputStream.close();
    }

    public static Message readMessage(Socket s) throws IOException, ClassNotFoundException {
            ObjectInputStream objectInputStream =  new ObjectInputStream(s.getInputStream());
            Message message = (Message) objectInputStream.readObject();
            objectInputStream.close();
            return message;
    }

	public static String readLine(Socket s) throws IOException {
			BufferedReader reader = new BufferedReader(
									new InputStreamReader(
									s.getInputStream()));
			
			String line = reader.readLine();
			
			return line;
	}
	
	public static void writeLine(Socket s, String line) throws IOException {
			BufferedWriter writer = new BufferedWriter(
									new OutputStreamWriter(
									s.getOutputStream()));

			writer.write(line);
			writer.write("\n");
			writer.flush();

	}
}
