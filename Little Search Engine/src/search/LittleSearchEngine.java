package search;

import java.io.*;
import java.util.*;

/**
 * This class encapsulates an occurrence of a keyword in a document. It stores the
 * document name, and the frequency of occurrence in that document. Occurrences are
 * associated with keywords in an index hash table.
 * 
 * @author Sesh Venugopal
 * 
 */
class Occurrence {
	/**
	 * Document in which a keyword occurs.
	 */
	String document;
	
	/**
	 * The frequency (number of times) the keyword occurs in the above document.
	 */
	int frequency;
	
	/**
	 * Initializes this occurrence with the given document,frequency pair.
	 * 
	 * @param doc Document name
	 * @param freq Frequency
	 */
	public Occurrence(String doc, int freq) {
		document = doc;
		frequency = freq;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "(" + document + "," + frequency + ")";
	}
}

/**
 * This class builds an index of keywords. Each keyword maps to a set of documents in
 * which it occurs, with frequency of occurrence in each document. Once the index is built,
 * the documents can searched on for keywords.
 *
 */
public class LittleSearchEngine {
	
	/**
	 * This is a hash table of all keywords. The key is the actual keyword, and the associated value is
	 * an array list of all occurrences of the keyword in documents. The array list is maintained in descending
	 * order of occurrence frequencies.
	 */
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	/**
	 * The hash table of all noise words - mapping is from word to itself.
	 */
	HashMap<String,String> noiseWords;
	
	/**
	 * Creates the keyWordsIndex and noiseWords hash tables.
	 */
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashMap<String,String>(100,2.0f);
	}
	
	/**
	 * This method indexes all keywords found in all the input documents. When this
	 * method is done, the keywordsIndex hash table will be filled with all keywords,
	 * each of which is associated with an array list of Occurrence objects, arranged
	 * in decreasing frequencies of occurrence.
	 * 
	 * @param docsFile Name of file that has a list of all the document file names, one name per line
	 * @param noiseWordsFile Name of file that has a list of noise words, one noise word per line
	 * @throws FileNotFoundException If there is a problem locating any of the input files on disk
	 */
	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.put(word,word);
		}
		
		// index all keywords
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeyWords(docFile);
			mergeKeyWords(kws);
		}
		
	}

	/**
	 * Scans a document, and loads all keywords found into a hash table of keyword occurrences
	 * in the document. Uses the getKeyWord method to separate keywords from other words.
	 * 
	 * @param docFile Name of the document file to be scanned and loaded
	 * @return Hash table of keywords in the given document, each associated with an Occurrence object
	 * @throws FileNotFoundException If the document file is not found on disk
	 */
	public HashMap<String,Occurrence> loadKeyWords(String docFile) 
	throws FileNotFoundException {
		
		HashMap<String,Occurrence> current = new HashMap<String,Occurrence>();
		Scanner sc = new Scanner(new File(docFile));
		while (sc.hasNext()) {
			String word = sc.next();
			word = getKeyWord(word);
			if(word==null) {
				continue;
			}

			else if(!current.containsKey(word)) {
				current.put(word,new Occurrence(docFile,1));
			}
			else {
				current.get(word).frequency++;
			}
		}
		
		return current;
	}
	
	/**
	 * Merges the keywords for a single document into the master keywordsIndex
	 * hash table. For each keyword, its Occurrence in the current document
	 * must be inserted in the correct place (according to descending order of
	 * frequency) in the same keyword's Occurrence list in the master hash table. 
	 * This is done by calling the insertLastOccurrence method.
	 * 
	 * @param kws Keywords hash table for a document
	 */
	public void mergeKeyWords(HashMap<String,Occurrence> kws) {
		 
		for(String x:kws.keySet()) {
			if(keywordsIndex.containsKey(x)) {
				keywordsIndex.get(x).add(kws.get(x));
				insertLastOccurrence(keywordsIndex.get(x));
			}
			else {
				ArrayList<Occurrence> temp = new ArrayList<Occurrence>();
				temp.add(kws.get(x));
				keywordsIndex.put(x,temp);
			}
			
		}
	}
	
	/**
	 * Given a word, returns it as a keyword if it passes the keyword test,
	 * otherwise returns null. A keyword is any word that, after being stripped of any
	 * TRAILING punctuation, consists only of alphabetic letters, and is not
	 * a noise word. All words are treated in a case-INsensitive manner.
	 * 
	 * Punctuation characters are the following: '.', ',', '?', ':', ';' and '!'
	 * 
	 * @param word Candidate word
	 * @return Keyword (word without trailing punctuation, LOWER CASE)
	 */
	public String getKeyWord(String word) {
		String newWord = ""; 
		int startIndex=0;
		int endIndex=0;
		for(int i=0;i<word.length();i++) {
			char temp = word.charAt(i);
			if(Character.isLetter(temp)) {
				startIndex = i;
				break;
			}
			else if(!Character.isLetter(temp)) {
				return null;
			}
		}
		
		for(int i=word.length()-1;i>=0;i--) {
			char temp = word.charAt(i);
			if(!Character.isLetter(temp)) {
				continue;
			}
			else if(Character.isLetter(temp)) {
				endIndex= i;
				break;
			}
		}
		
		for(int i=startIndex;i<=endIndex;i++) {
			char ch = word.charAt(i);
			newWord+=ch;
			
			 if(ch=='.'||ch==','||ch=='?'||ch==':'||ch==';'||ch=='!' ||ch=='-') {
				return null;
			}

		}
		if(noiseWords.containsKey(newWord)) {
			return null;
		}
		
		return newWord.toLowerCase();
	}
	
	/**
	 * Inserts the last occurrence in the parameter list in the correct position in the
	 * same list, based on ordering occurrences on descending frequencies. The elements
	 * 0..n-2 in the list are already in the correct order. Insertion of the last element
	 * (the one at index n-1) is done by first finding the correct spot using binary search, 
	 * then inserting at that spot.
	 * 
	 * @param occs List of Occurrences
	 * @return Sequence of mid point indexes in the input list checked by the binary search process,
	 *         null if the size of the input list is 1. This returned array list is only used to test
	 *         your code - it is not used elsewhere in the program.
	 */
	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {
		
		if(occs.size()==1) {
			return null;
		}
		ArrayList<Integer> track = new ArrayList<Integer>();
		int left=0;
		int right = occs.size()-2;
		while(!(left>right)) {
			int mid = (left+right)/2;
			track.add(mid);
			if(occs.get(mid).frequency>occs.get(occs.size()-1).frequency){
				left = mid+1;
			}
			else if((occs.get(mid).frequency<occs.get(occs.size()-1).frequency)) {
				right = mid-1;
			}
			else {
				left=mid+1;
			}
		}
		Occurrence temp = occs.get(occs.size()-1);
		occs.add(left,temp);
		occs.remove(occs.size()-1);
		return track;
	
	}
	
	/**
	 * Search result for "kw1 or kw2". A document is in the result set if kw1 or kw2 occurs in that
	 * document. Result set is arranged in descending order of occurrence frequencies. (Note that a
	 * matching document will only appear once in the result.) Ties in frequency values are broken
	 * in favor of the first keyword. (That is, if kw1 is in doc1 with frequency f1, and kw2 is in doc2
	 * also with the same frequency f1, then doc1 will appear before doc2 in the result. 
	 * The result set is limited to 5 entries. If there are no matching documents, the result is null.
	 * 
	 * @param kw1 First keyword
	 * @param kw1 Second keyword
	 * @return List of NAMES of documents in which either kw1 or kw2 occurs, arranged in descending order of
	 *         frequencies. The result size is limited to 5 documents. If there are no matching documents,
	 *         the result is null.
	 */
	public ArrayList<String> top5search(String kw1, String kw2) {
		ArrayList<String> current = new ArrayList<String>();
		ArrayList<Occurrence> objects = new ArrayList<Occurrence>();
		for(int i=0;i<keywordsIndex.get(kw1).size();i++) {
			String temp = keywordsIndex.get(kw1).get(i).document;
			Occurrence obj1 = keywordsIndex.get(kw1).get(i);
			for(int j=0;j<keywordsIndex.get(kw2).size();j++) {
				String temp2 = keywordsIndex.get(kw2).get(j).document;
				Occurrence obj2 = keywordsIndex.get(kw2).get(j);
				if(temp.equals(temp2)){
					objects.add(obj1);
					objects.add(obj2);
				}
				else {
					continue;
				}
				
			}
		}
		sorting(objects,kw1,kw2);
		for(int i=0; i<objects.size();i++) {
			current.add(objects.get(i).document);
		}
		for(int i=0;i<current.size();i++) {
			int counter =0;
			String temp = current.get(i);
			 for(int j=0;j<current.size();j++) {
				 String temp2 = current.get(j);
				 if(temp.equals(temp2)) {
					 counter++;
					 if(counter>1){
						current.remove(temp2);		
					 }
				 }
				 
			 }
		
		}
		if(current.size()==0) {
			return null;
		}
		if(current.size()>5) {
			while(current.size()>5) {
				current.remove(current.size()-1);
			}
		}
		
		return current;
	}
	
	private void sorting(ArrayList<Occurrence> current, String kw1, String kw2) {
		for(int i=0;i<current.size();i++) {
			for(int j=0;j<current.size();j++) {
				if(current.get(i).frequency<current.get(j).frequency &&i<j) {
					Occurrence temp = current.get(j);
					current.set(j, current.get(i));
					current.set(i, temp);
				}
				else if(current.get(i).frequency>=current.get(j).frequency){
					continue;
				}
			}
			
		}
	}
}
