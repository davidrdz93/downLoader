package downLoader;

import java.util.Scanner;

public class Downloader 
{

	public static void main(String[] args) 
	{
		
		String sourceUrl;
		String destination;
		String sequenzaTags;
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Source url:");	
		sourceUrl = scanner.nextLine();
		
		System.out.println("Cartella di destinazione:");
		destination = scanner.nextLine();
		
		System.out.println("Sequenza tags (es tr td a): ");
		sequenzaTags = scanner.nextLine();
		
		DownloaderService downloaderService = new DownloaderServiceImp(sourceUrl, 
				                                                       destination + '/', 
				                                                       sequenzaTags);
		downloaderService.download(sourceUrl);
		
		scanner.close();
		
	}
}
