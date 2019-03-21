package downLoader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

public class DownloaderServiceImp implements DownloaderService
{
	
	private String sourceUrl;
	private String destination;
	private String sequenzaTags;
	private ArrayList<String> completePath = new ArrayList<>();
	
	DownloaderServiceImp(String sourceUrl, String destination, String sequenzaTags) 
	{
		this.sourceUrl = sourceUrl;
		this.destination = destination;
		this.completePath.add(destination);
		this.sequenzaTags = sequenzaTags;
	}
	
	/**
	 * Stampa su stdout uno stream 
	 * 
	 * @author David Rodriguez
	 * @param std Buffer da inviare su stdout
	 * @throws IOException Se ci sono errori nella lettura del buffer
	 * */
	private void writeStdout(BufferedReader std) throws IOException
	{
		String line = null;
		while ((line = std.readLine()) != null)
			System.out.println(line);
	}
	
	/**
	 * Concatena tutti gli elementi di completePath e crea una stringa
	 * 
	 * @author David Rodriguez
	 * @return Stringa della concatenazione degli elementi di completePath
	 * */
	private String getFileDirectory() 
	{
		StringBuilder path = new StringBuilder();
		this.completePath.stream().forEach(dir -> path.append(dir));
		return path.toString();
	}
	
	/**
	 * Salva il file a cui fa riferimento url nel filesystem
	 * utilizza streams di uscita
	 * 
	 * @author David Rodriguez
	 * @param url Stringa con la url che punta al file
	 * @param filePath Directory nella quale salvare il file
	 * @param fileName Nome da dare il file
	 * */
	private void fetchSaveFile(String url, String filePath, String fileName) 
	{
		try (BufferedInputStream inputStream = new BufferedInputStream(new URL(url).openStream()))
		{
			final String filePathName = filePath + fileName;
			File file = new File(filePathName);
			if (file.exists())
			{
				System.out.println(String.format("Il file %s esiste, non lo scarico", filePathName));
				return;
			}
			new File(filePath).mkdirs();
			
			System.out.printf("Scarico file da url %s\n", url);
			FileOutputStream fileOutput = new FileOutputStream(filePathName);
			float totalBytes = 0;
			int bufferSize = 1024, bytesRead;
			byte buffer[] = new byte[bufferSize]; // 1 kb a ciclo
			
			while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1)
			{
				fileOutput.write(buffer, 0, bytesRead);
				totalBytes += bytesRead;
			}
			
			System.out.printf("Scritti %f kb\n", totalBytes/1024);
			System.out.printf("Creato file %s\n", filePathName);
			Stream.generate(() -> '*').limit(20).forEach(ch -> System.out.print(ch));
			System.out.print('\n');
			fileOutput.close();
		}
			
		catch (IOException ioe) 
		{
			ioe.printStackTrace();
		}
		
	}
	
	/**
	 * Salva il file a cui fa riferimento url nel filesystem
	 * utilizza il programma curl
	 * 
	 * @author David Rodrig
	 * @param url Stringa con la url che punta al file
	 * @param filePath Directory nella quale salvare il file
	 * @param fileName Nome da dare il file
	 * */
	private void execCurl(String url, String filePath, String fileName)
	{		
		String command = String.format("curl -o %s%s %s", filePath, fileName, url);
		try {
			
			File file = new File(String.format("%s%s", filePath, fileName));
			if (file.exists())
			{
				System.out.println(String.format("Il file %s%s esiste, non lo scarico", filePath, fileName));
				return;
			}
			new File(filePath).mkdirs();
			
			Process process = Runtime.getRuntime().exec(command);
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
			System.out.println(command);
			this.writeStdout(stdOut);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Cerca i tags 'a' contenuti nella stringa url  
	 * 
	 * @author David Rodrig 
	 * @param url Url nella quale cercare i tags
	 * @return aTags tutti i tag trovati
	 * */
	private Elements getTags(String url) throws IOException
	{
		Document doc = Jsoup.connect(url).get();
		Elements aTags = doc.select(this.sequenzaTags);
		return aTags;
	}
	
	/**
	 * Implementazione, contiene la logica per scaricare e trovare i file nuovi
	 * 
     * @author David Rodrig
	 * */
	public void download(String url) 
	{
		try
		{
			Elements elements = this.getTags(url);
			for (Element element: elements)
			{
				String absUrl = element.absUrl("href");
				String relativeUrl = element.attr("href");
				
				if (absUrl.endsWith("/"))
				{
					if (element.text().equals("Parent Directory"))
						continue;
							
					this.completePath.add(relativeUrl);
					this.download(absUrl);
				}
				
				else
					this.fetchSaveFile(absUrl, this.getFileDirectory(), relativeUrl);

			}
			this.completePath.remove(this.completePath.size() - 1);
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
}
