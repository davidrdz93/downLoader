package downLoader;

public interface DownloaderService 
{
	/**
	 * salva su filesystem ricorsivamente file
	 * 
	 * @param url Stringa con la url dalla quale effettuare la ricerca
	 * */
	void download(String url);
}
