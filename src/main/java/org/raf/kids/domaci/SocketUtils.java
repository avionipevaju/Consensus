package org.raf.kids.domaci;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketUtils {

	public static String readLine(Socket s) {
		try {
			BufferedReader reader = new BufferedReader(
									new InputStreamReader(
									s.getInputStream()));
			
			String line = reader.readLine();
			
			return line;
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}
}
