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
					//check if cloud was already used
					Optional<wordcloud> targetWC = timeline.clouds.stream().filter(wordcloud -> wordcloud.id == keyword.index).findFirst();
					int posinArray = 0;
					//if cloud is already present
					if(targetWC.isPresent()) {
						//get position in cloud-array to access right cloud
						posinArray = timeline.clouds.indexOf(targetWC.get());
						//create badge
						timeline.clouds.get(posinArray).createBadge(keyword.word, keyword.seconds,keyword.sentence_id,true);
					}
					else if(targetWC.isPresent()==false) {
						//create new cloud
						wordcloud target = new wordcloud(WordCloudTimeline.timeLine,keyword.index);
						timeline.clouds.add(target);
						posinArray = timeline.clouds.size()-1;
						//create badge
						timeline.clouds.get(posinArray).createBadge(keyword.word, keyword.seconds,keyword.sentence_id,true);
						
					}
					//wait
					Thread.sleep(8000/keywords.size());
				}
				
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
