package welchezukunft;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

public class batchCreator implements Runnable {
	protected BlockingQueue<List<newKeyword>> blockingQueue;

	public batchCreator(BlockingQueue<List<newKeyword>> queue) {
		this.blockingQueue = queue;
	}

	public void run() {
		try {
			while (true) {
				List<newKeyword> keywords = blockingQueue.take();
				
				for (newKeyword keyword : keywords) {
					//check if word was already used
					Optional<wordcloud> targetWC = timeline.clouds.stream().filter(wordcloud -> wordcloud.id == keyword.index).findFirst();
					int posinArray = 0;
					if(targetWC.isPresent()) {
						//TODO get correct index
						posinArray = timeline.clouds.indexOf(targetWC);		
						timeline.clouds.get(posinArray).createBadge(keyword.word, keyword.seconds);
					}
					
					
					Thread.sleep(8000/keywords.size());
				}
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
