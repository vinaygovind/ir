/**
 * 
 */
package ir.invertedindex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * @author vgovind
 *
 */
class Index {
	
	// The inverted index data structure
	Map<String, HashSet<Integer>> index;
	// Map storing document to document id mappings
	Map<Integer, String> documentIdMap;
	
	String[] booleanOperators  = {"AND", "and", "OR", "or", "NOT", "not"};
	
	String[] stopWords  = { "and", "or", "not", "a", "the", "an"};
	
	Index() {
		index = new HashMap<String, HashSet<Integer>>();
		documentIdMap = new HashMap<Integer, String>();
	}

	public void buildIndex(String[] files) throws Exception {
		int i = 0;
		for (String fileName : files) {
			File file = new File(fileName);
			FileInputStream fis = new FileInputStream(file.getAbsolutePath());
			XWPFDocument document = new XWPFDocument(fis);
			List<XWPFParagraph> paragraphs = document.getParagraphs();
			documentIdMap.put(i,fileName);
			for (XWPFParagraph para : paragraphs) {
				String[] words = para.getText().split("\\W+");
				for (String word : words) {
					word = word.toLowerCase();
					if (!Arrays.asList(stopWords).contains(word)) {
						if (!index.containsKey(word))
							index.put(word, new HashSet<Integer>());
						index.get(word).add(i);
						
					}
				}
			}
			i++;
			document.close();
			fis.close();
		}
		writeIndex();
	}
	
	private void writeIndex() throws IOException {
		PrintWriter out = new PrintWriter("Index.txt");
        out.println(this.toString());
        out.close();
    }
	
	private Set<Integer> getDocumentSet(String word) {
		return index.get(word.toLowerCase()) == null ? new HashSet<Integer>() : index.get(word.toLowerCase()) ;
	}

	public void find(String phrase) {
		String[] words = phrase.split("\\W+");
		Set<Integer> result = this.getDocumentSet(words[0]);
		int startIndex = 1;
		if (("NOT").equals(words[0]) || ("not").equals(words[0])) {
			result.clear();
			result.addAll(documentIdMap.keySet());
			result.removeAll(this.getDocumentSet(words[1]));
			startIndex = 2;
		}
		int currentIndexToCheck = 0;
		boolean noCondition = false;
		for (int currentIndex = startIndex; currentIndex < words.length; currentIndex= currentIndexToCheck + 1) {
			noCondition = false;
			currentIndexToCheck = currentIndex + 1;
			if (("NOT").equals(words[currentIndex + 1]) || ("not").equals(words[currentIndex + 1])) {
				currentIndexToCheck ++;
				noCondition = true;
			}
			Set<Integer> intermediateResult = this.getDocumentSet(words[currentIndexToCheck]);
			
			if (noCondition) {
				Set<Integer> noResult = new HashSet<Integer>();
				noResult.addAll(documentIdMap.keySet());
				noResult.removeAll(intermediateResult);
				intermediateResult = new HashSet<Integer>(noResult);
			}
			if (("AND").equals(words[currentIndex] ) || ("and").equals(words[currentIndex])) {
				result.retainAll(intermediateResult);
			} else if (("OR").equals(words[currentIndex]) || ("or").equals(words[currentIndex])) {
				result.addAll(intermediateResult);
			}
		}
		
		if (result.size() == 0) {
			System.out.println("Not found");
			return;
		}
		System.out.println("Found in: ");
		for (int num : result) {
			System.out.println("\t" + documentIdMap.get(num));
		}
	}
	
	@Override
	public String toString() {
		StringBuffer indexList = new StringBuffer();
		for (Map.Entry<String, HashSet<Integer>> entry : index.entrySet()) {
			if (entry.getKey().length() > 0) {
				StringBuffer buffer = new StringBuffer(entry.getKey()).append(" -> " );
				Iterator<Integer> it = entry.getValue().iterator();
				while(it.hasNext()) {
					buffer.append(it.next().toString()).append(" , ");				
				}
				buffer.deleteCharAt(buffer.lastIndexOf(","));
				buffer.append(System.getProperty("line.separator"));
				indexList.append(buffer);
			}
		}
		return indexList.toString();
	}
}

public class InvertedIndex {

	public static void main(String args[]) throws Exception {
		if (args.length == 0) {
			System.out.println("Please provide the document set to index....Example:- java -jar invertedindex-0.0.1-SNAPSHOT.jar \"C:\\Users\\vgovind\\Documents\\IR_Assignment\\Doc1.docx\" \"C:\\Users\\vgovind\\Documents\\IR_Assignment\\Doc2.docx\"\r\n" + 
					"");
		} else {
			Index index = new Index();
			index.buildIndex(args);
			System.out.println("Search phrase: ");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String phrase = in.readLine();
			index.find(phrase);
		}
	}
}