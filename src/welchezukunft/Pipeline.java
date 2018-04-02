package welchezukunft;

import edu.stanford.nlp.international.Language;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class Pipeline{

	static Properties props;
    static StanfordCoreNLP stanford;
    static TregexPattern npPattern;
	
	Pipeline(){
		props = new Properties();
    	props.put("annotators","tokenize, ssplit, pos, lemma, ner, parse, dcoref");
    	stanford = new StanfordCoreNLP(props);
    	npPattern = TregexPattern.compile("@NP");
	}

    public void doSentenceTest(String textInput) {
	    String text = textInput;

	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);
	    // run all Annotators on this text
	    stanford.annotate(document);
	
	    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
	    for (CoreMap sentence : sentences) {
	
	        Tree sentenceTree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
	        TregexMatcher matcher = npPattern.matcher(sentenceTree);
	
	        while (matcher.find()) {
	            //this tree should contain "The fitness room" 
	            Tree nounPhraseTree = matcher.getMatch();
	            //Question : how do I find that "dirty" has a relationship to the nounPhraseTree
        }
        // Output dependency tree
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs = gsf.newGrammaticalStructure(sentenceTree);
        Collection<TypedDependency> tdl = gs.typedDependenciesCollapsed();
        //System.out.println("typedDependencies: "+ gs.typedDependenciesCollapsed()); 

        //sort out only compounts and nouns
        List<wordRel> positions = new ArrayList<wordRel>();
        for (Iterator<TypedDependency> it = tdl.iterator(); it.hasNext();) { 
            TypedDependency s = it.next(); 
            if(s.reln().toString() == "det" || s.reln().toString() == "compound" || s.reln().toString() == "amod" ) {
            	//System.out.println(s);
            	//System.out.println("String: "+ s.toString() + " / IW: " + s.dep().index() + " / gov " + s.gov().index());
            	///System.out.println("store: " + s.gov().index() + s.dep().index() + s.gov().originalText() + s.dep().originalText());
            	positions.add(new wordRel(s.gov().index(),s.dep().index(),s.gov().originalText(),s.dep().originalText()));
            }
        }
        //recombine lists
        for(int i = 0; i < positions.size(); i++) {
        	wordRel w = positions.get(i);
        	boolean isnext = false;
        	int length = 1;
        	Map<Integer,String> wordparts = new HashMap<Integer,String>();
        	wordparts.put(w.currPos, w.currWord);
        	wordparts.put(w.relPos, w.relWord);
        	if(i < positions.size()-1) {
	        	while(isnext == false) {
	        		isnext = true;
	        		if(w.currPos == positions.get(i+1).currPos) {  
	        		  wordparts.put(positions.get(i+1).relPos,positions.get(i+1).relWord);
	        		  isnext = false;	
	        		  length++;
	        		  i++;
	        		  if(i>=positions.size()-1) {
	        			  isnext = true;
	        			  break;
	        		  }
	        		}
	        	}
        	}
        	Map<Integer,String> wordpartsSorted = new TreeMap<Integer,String>(wordparts);
       		List<String> result2 = new ArrayList(wordpartsSorted.values());
       		String output = result2.stream().map( n -> n.toString() ).collect( Collectors.joining( " " ) );
        	System.out.println(i + ": " + output );	
        	}      	
        }      
        System.out.println("-------------------");
}

    
    class wordRel {
    	
    	int currPos;
    	int relPos;
    	String currWord;
    	String relWord;
    	
    	public wordRel(int curr, int rel, String currWord, String relWord){
    		this.currPos = curr;
    		this.relPos = rel;
    		this.currWord = currWord;
    		this.relWord = relWord;
    	}
    	
    	
    }
}