package search;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class Driver {

	public static void main(String[] args) {

		String docsFile = "docs.txt";
        String noiseWords = "noisewords.txt";

        LittleSearchEngine searchEngine = new LittleSearchEngine();

        try {
			searchEngine.makeIndex(docsFile, noiseWords);
			for(String key: searchEngine.keywordsIndex.keySet()) {
				System.out.print("Key: " + key + "\t");
				for(int i = 0; i < searchEngine.keywordsIndex.get(key).size(); i++) {
					System.out.print(searchEngine.keywordsIndex.get(key).get(i) + "  ");
				}
				System.out.println();
				
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
        System.out.println(searchEngine.top5search("in","round"));
	}
}

	//Key: round	(AliceCh1.txt,3)  (WowCh1.txt,2)  (AliceCh2.txt,2)  
	//Key: small	(AliceCh1.txt,3)  (AliceCh2.txt,3)  (WowCh1.txt,2)  


	// AliceCh1.txt , AliceCh2.txt, WowCh1.txt


	


